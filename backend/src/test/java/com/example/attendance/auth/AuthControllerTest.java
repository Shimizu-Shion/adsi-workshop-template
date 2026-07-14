package com.example.attendance.auth;

import com.example.attendance.employee.Department;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.employee.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        var dept = Department.builder().id(1L).name("開発部").build();
        var employee = Employee.builder()
                .employeeCode("TEST001")
                .name("テスト太郎")
                .email("test@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .department(dept)
                .active(true)
                .build();
        employeeRepository.save(employee);
    }

    @Test
    @DisplayName("ログイン成功: 正しい認証情報で200が返る")
    void login_validCredentials_returns200() throws Exception {
        var body = """
                {"employeeCode": "TEST001", "password": "password123"}
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employee.employeeCode").value("TEST001"))
                .andExpect(jsonPath("$.employee.name").value("テスト太郎"));
    }

    @Test
    @DisplayName("ログイン失敗: パスワード不一致で401")
    void login_wrongPassword_returns401() throws Exception {
        var body = """
                {"employeeCode": "TEST001", "password": "wrongpassword"}
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("ログイン失敗: 存在しないユーザーで401")
    void login_nonExistingUser_returns401() throws Exception {
        var body = """
                {"employeeCode": "NONEXIST", "password": "password123"}
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/auth/me: 未認証で401")
    void me_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("/auth/me: 認証済みで社員情報が返る")
    void me_authenticated_returnsEmployeeInfo() throws Exception {
        var loginBody = """
                {"employeeCode": "TEST001", "password": "password123"}
                """;

        var session = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn().getRequest().getSession();

        mockMvc.perform(get("/auth/me").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeCode").value("TEST001"));
    }

    @Test
    @DisplayName("ログアウト: セッション無効化")
    void logout_invalidatesSession() throws Exception {
        var loginBody = """
                {"employeeCode": "TEST001", "password": "password123"}
                """;

        var session = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andReturn().getRequest().getSession();

        mockMvc.perform(post("/auth/logout").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isNoContent());
    }
}
