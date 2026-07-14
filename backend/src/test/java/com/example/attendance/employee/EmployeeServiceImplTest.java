package com.example.attendance.employee;

import com.example.attendance.employee.dto.CreateEmployeeRequest;
import com.example.attendance.employee.dto.UpdateEmployeeRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private EmployeeServiceImpl service;

    private Department dept;
    private Employee employee;

    @BeforeEach
    void setUp() {
        service = new EmployeeServiceImpl(employeeRepository, departmentRepository, passwordEncoder);

        dept = Department.builder().id(1L).name("開発部").build();
        employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .passwordHash("hashed")
                .role(Role.EMPLOYEE)
                .department(dept)
                .active(true)
                .version(0L)
                .build();
    }

    @Test
    @DisplayName("一覧取得: フィルタなし")
    void findAll_noFilter_returnsAll() {
        when(employeeRepository.findAll()).thenReturn(List.of(employee));

        var result = service.findAll(null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("田中太郎");
    }

    @Test
    @DisplayName("一覧取得: 部門フィルタ")
    void findAll_departmentFilter_returnsDepartmentEmployees() {
        when(employeeRepository.findByDepartmentId(1L)).thenReturn(List.of(employee));

        var result = service.findAll(1L, null);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("一覧取得: 有効フラグフィルタ")
    void findAll_activeFilter_returnsActiveOnly() {
        when(employeeRepository.findByActive(true)).thenReturn(List.of(employee));

        var result = service.findAll(null, true);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("詳細取得: 存在するID")
    void findById_existingId_returnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var result = service.findById(1L);

        assertThat(result.name()).isEqualTo("田中太郎");
        assertThat(result.departmentName()).isEqualTo("開発部");
    }

    @Test
    @DisplayName("詳細取得: 存在しないID → 例外")
    void findById_nonExistingId_throwsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("登録: 正常")
    void create_validRequest_returnsCreatedEmployee() {
        var request = new CreateEmployeeRequest(
                "EMP002", "佐藤花子", "sato@example.com", "password123", "EMPLOYEE", 1L);

        when(employeeRepository.findByEmployeeCode("EMP002")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("sato@example.com")).thenReturn(Optional.empty());
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(2L);
            return e;
        });

        var result = service.create(request);

        assertThat(result.employeeCode()).isEqualTo("EMP002");
        assertThat(result.name()).isEqualTo("佐藤花子");
    }

    @Test
    @DisplayName("登録: 重複メール → 例外")
    void create_duplicateEmail_throwsException() {
        var request = new CreateEmployeeRequest(
                "EMP003", "重複", "tanaka@example.com", "password123", "EMPLOYEE", 1L);

        when(employeeRepository.findByEmployeeCode("EMP003")).thenReturn(Optional.empty());
        when(employeeRepository.findByEmail("tanaka@example.com")).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("メールアドレスが既に使用されています");
    }

    @Test
    @DisplayName("登録: 重複社員コード → 例外")
    void create_duplicateCode_throwsException() {
        var request = new CreateEmployeeRequest(
                "EMP001", "重複", "new@example.com", "password123", "EMPLOYEE", 1L);

        when(employeeRepository.findByEmployeeCode("EMP001")).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("社員コードが既に使用されています");
    }

    @Test
    @DisplayName("更新: 正常")
    void update_validRequest_returnsUpdatedEmployee() {
        var request = new UpdateEmployeeRequest("田中次郎", "jiro@example.com", "MANAGER", 1L, 0L);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = service.update(1L, request);

        assertThat(result.name()).isEqualTo("田中次郎");
        assertThat(result.role()).isEqualTo("MANAGER");
    }

    @Test
    @DisplayName("無効化: 論理削除")
    void deactivate_existingEmployee_setsInactive() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        service.deactivate(1L);

        verify(employeeRepository).save(any(Employee.class));
        assertThat(employee.isActive()).isFalse();
    }
}
