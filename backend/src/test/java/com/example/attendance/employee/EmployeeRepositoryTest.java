package com.example.attendance.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private Department dept;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        departmentRepository.deleteAll();

        dept = departmentRepository.save(Department.builder().name("開発部").build());
    }

    @Test
    @DisplayName("社員コードで検索: 存在する場合")
    void findByEmployeeCode_existing_returnsEmployee() {
        var employee = Employee.builder()
                .employeeCode("EMP001")
                .name("田中太郎")
                .email("tanaka@example.com")
                .passwordHash("hashed")
                .role(Role.EMPLOYEE)
                .department(dept)
                .active(true)
                .build();
        employeeRepository.save(employee);

        var result = employeeRepository.findByEmployeeCode("EMP001");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("田中太郎");
    }

    @Test
    @DisplayName("社員コードで検索: 存在しない場合")
    void findByEmployeeCode_notExisting_returnsEmpty() {
        var result = employeeRepository.findByEmployeeCode("NONEXIST");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("部門IDで検索")
    void findByDepartmentId_returnsDepartmentEmployees() {
        var emp1 = Employee.builder()
                .employeeCode("EMP001").name("田中").email("t1@example.com")
                .passwordHash("h").role(Role.EMPLOYEE).department(dept).active(true).build();
        var otherDept = departmentRepository.save(Department.builder().name("営業部").build());
        var emp2 = Employee.builder()
                .employeeCode("EMP002").name("佐藤").email("t2@example.com")
                .passwordHash("h").role(Role.EMPLOYEE).department(otherDept).active(true).build();
        employeeRepository.saveAll(List.of(emp1, emp2));

        var result = employeeRepository.findByDepartmentId(dept.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("田中");
    }

    @Test
    @DisplayName("有効フラグでフィルタ")
    void findByActive_returnsOnlyActiveEmployees() {
        var active = Employee.builder()
                .employeeCode("EMP001").name("有効").email("a@example.com")
                .passwordHash("h").role(Role.EMPLOYEE).department(dept).active(true).build();
        var inactive = Employee.builder()
                .employeeCode("EMP002").name("無効").email("i@example.com")
                .passwordHash("h").role(Role.EMPLOYEE).department(dept).active(false).build();
        employeeRepository.saveAll(List.of(active, inactive));

        var result = employeeRepository.findByActive(true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("有効");
    }

    @Test
    @DisplayName("部門ID + 有効フラグの複合フィルタ")
    void findByDepartmentIdAndActive_returnsFilteredEmployees() {
        var emp = Employee.builder()
                .employeeCode("EMP001").name("田中").email("t@example.com")
                .passwordHash("h").role(Role.EMPLOYEE).department(dept).active(true).build();
        var inactiveEmp = Employee.builder()
                .employeeCode("EMP002").name("無効社員").email("i@example.com")
                .passwordHash("h").role(Role.EMPLOYEE).department(dept).active(false).build();
        employeeRepository.saveAll(List.of(emp, inactiveEmp));

        var result = employeeRepository.findByDepartmentIdAndActive(dept.getId(), true);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("田中");
    }
}
