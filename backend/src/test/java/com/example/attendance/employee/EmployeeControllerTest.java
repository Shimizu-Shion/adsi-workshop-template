package com.example.attendance.employee;

import com.example.attendance.employee.dto.EmployeeResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    private final EmployeeResponse sampleResponse = new EmployeeResponse(
            1L, "EMP001", "田中太郎", "tanaka@example.com", "EMPLOYEE", 1L, "開発部", true, 0L);

    @Test
    @DisplayName("GET /employees: 未認証で401")
    void findAll_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /employees: 認証済みで200")
    @WithMockUser(roles = "EMPLOYEE")
    void findAll_authenticated_returns200() throws Exception {
        when(employeeService.findAll(null, null)).thenReturn(List.of(sampleResponse));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].employeeCode").value("EMP001"));
    }

    @Test
    @DisplayName("POST /employees: ADMINで201")
    @WithMockUser(roles = "ADMIN")
    void create_admin_returns201() throws Exception {
        when(employeeService.create(any())).thenReturn(sampleResponse);

        var body = """
                {
                    "employeeCode": "EMP001",
                    "name": "田中太郎",
                    "email": "tanaka@example.com",
                    "password": "password123",
                    "role": "EMPLOYEE",
                    "departmentId": 1
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.employeeCode").value("EMP001"));
    }

    @Test
    @DisplayName("POST /employees: EMPLOYEEで403")
    @WithMockUser(roles = "EMPLOYEE")
    void create_employee_returns403() throws Exception {
        var body = """
                {
                    "employeeCode": "EMP001",
                    "name": "田中太郎",
                    "email": "tanaka@example.com",
                    "password": "password123",
                    "role": "EMPLOYEE",
                    "departmentId": 1
                }
                """;

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /employees/{id}: ADMINで200")
    @WithMockUser(roles = "ADMIN")
    void update_admin_returns200() throws Exception {
        when(employeeService.update(eq(1L), any())).thenReturn(sampleResponse);

        var body = """
                {
                    "name": "田中太郎",
                    "email": "tanaka@example.com",
                    "role": "EMPLOYEE",
                    "departmentId": 1,
                    "version": 0
                }
                """;

        mockMvc.perform(put("/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /employees/{id}: ADMINで204")
    @WithMockUser(roles = "ADMIN")
    void deactivate_admin_returns204() throws Exception {
        doNothing().when(employeeService).deactivate(1L);

        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /employees/{id}: MANAGERで403")
    @WithMockUser(roles = "MANAGER")
    void deactivate_manager_returns403() throws Exception {
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isForbidden());
    }
}
