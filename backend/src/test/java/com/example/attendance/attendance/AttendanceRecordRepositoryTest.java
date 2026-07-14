package com.example.attendance.attendance;

import com.example.attendance.employee.Department;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AttendanceRecordRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AttendanceRecordRepository repository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        var department = new Department();
        department.setName("開発部");
        department = entityManager.persistAndFlush(department);

        employee = Employee.builder()
            .employeeCode("tanaka")
            .name("田中太郎")
            .email("tanaka@example.com")
            .passwordHash("$2a$10$hashedpassword")
            .role(Role.EMPLOYEE)
            .department(department)
            .active(true)
            .build();
        employee = entityManager.persistAndFlush(employee);
    }

    @Test
    @DisplayName("社員IDと日付でレコードを検索できる")
    void findByEmployeeIdAndDate_returnsRecords() {
        var record = AttendanceRecord.builder()
            .employee(employee)
            .date(LocalDate.of(2026, 7, 14))
            .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
            .clockOut(LocalDateTime.of(2026, 7, 14, 18, 0))
            .build();
        entityManager.persistAndFlush(record);

        var results = repository.findByEmployeeIdAndDate(employee.getId(), LocalDate.of(2026, 7, 14));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getClockIn()).isEqualTo(LocalDateTime.of(2026, 7, 14, 9, 0));
    }

    @Test
    @DisplayName("未退勤レコードを最新の出勤時刻順で取得できる")
    void findFirstByEmployeeIdAndDateAndClockOutIsNull_returnsLatest() {
        var record1 = AttendanceRecord.builder()
            .employee(employee)
            .date(LocalDate.of(2026, 7, 14))
            .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
            .clockOut(LocalDateTime.of(2026, 7, 14, 12, 0))
            .build();
        entityManager.persistAndFlush(record1);

        var record2 = AttendanceRecord.builder()
            .employee(employee)
            .date(LocalDate.of(2026, 7, 14))
            .clockIn(LocalDateTime.of(2026, 7, 14, 13, 0))
            .build();
        entityManager.persistAndFlush(record2);

        var result = repository.findFirstByEmployeeIdAndDateAndClockOutIsNullOrderByClockInDesc(
            employee.getId(), LocalDate.of(2026, 7, 14));

        assertThat(result).isPresent();
        assertThat(result.get().getClockIn()).isEqualTo(LocalDateTime.of(2026, 7, 14, 13, 0));
    }

    @Test
    @DisplayName("年月の範囲でレコードを検索できる")
    void findByEmployeeIdAndDateBetween_returnsRecordsInRange() {
        var record1 = AttendanceRecord.builder()
            .employee(employee)
            .date(LocalDate.of(2026, 7, 1))
            .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
            .clockOut(LocalDateTime.of(2026, 7, 1, 18, 0))
            .build();
        entityManager.persistAndFlush(record1);

        var record2 = AttendanceRecord.builder()
            .employee(employee)
            .date(LocalDate.of(2026, 7, 15))
            .clockIn(LocalDateTime.of(2026, 7, 15, 9, 0))
            .clockOut(LocalDateTime.of(2026, 7, 15, 18, 0))
            .build();
        entityManager.persistAndFlush(record2);

        var outOfRange = AttendanceRecord.builder()
            .employee(employee)
            .date(LocalDate.of(2026, 8, 1))
            .clockIn(LocalDateTime.of(2026, 8, 1, 9, 0))
            .clockOut(LocalDateTime.of(2026, 8, 1, 18, 0))
            .build();
        entityManager.persistAndFlush(outOfRange);

        var results = repository.findByEmployeeIdAndDateBetween(
            employee.getId(),
            LocalDate.of(2026, 7, 1),
            LocalDate.of(2026, 7, 31));

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getDate()).isEqualTo(LocalDate.of(2026, 7, 1));
        assertThat(results.get(1).getDate()).isEqualTo(LocalDate.of(2026, 7, 15));
    }
}
