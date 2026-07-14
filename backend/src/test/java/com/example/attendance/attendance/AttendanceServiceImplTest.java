package com.example.attendance.attendance;

import com.example.attendance.employee.Department;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;
import com.example.attendance.employee.Role;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    private AttendanceRecordRepository repository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceServiceImpl service;

    private Employee employee;

    @BeforeEach
    void setUp() {
        var department = Department.builder().id(1L).name("開発部").build();
        employee = Employee.builder()
            .id(1L)
            .employeeCode("tanaka")
            .name("田中太郎")
            .email("tanaka@example.com")
            .passwordHash("hashed")
            .role(Role.EMPLOYEE)
            .department(department)
            .active(true)
            .version(0L)
            .build();
    }

    @Nested
    @DisplayName("出勤打刻")
    class ClockIn {

        @Test
        @DisplayName("正常に出勤レコードが作成される")
        void clockIn_createsNewRecord() {
            var timestamp = LocalDateTime.of(2026, 7, 14, 9, 0);
            var expected = AttendanceRecord.builder()
                .id(1L)
                .employee(employee)
                .date(timestamp.toLocalDate())
                .clockIn(timestamp)
                .version(0L)
                .build();

            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
            when(repository.save(any(AttendanceRecord.class))).thenReturn(expected);

            var result = service.clockIn(employee.getId(), timestamp);

            assertThat(result.getClockIn()).isEqualTo(timestamp);
            assertThat(result.getDate()).isEqualTo(LocalDate.of(2026, 7, 14));
            assertThat(result.getClockOut()).isNull();

            ArgumentCaptor<AttendanceRecord> captor = ArgumentCaptor.forClass(AttendanceRecord.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getClockIn()).isEqualTo(timestamp);
        }
    }

    @Nested
    @DisplayName("退勤打刻")
    class ClockOut {

        @Test
        @DisplayName("最新の未退勤レコードに退勤時刻が設定される")
        void clockOut_setsClockOutOnLatestRecord() {
            var clockInTime = LocalDateTime.of(2026, 7, 14, 9, 0);
            var clockOutTime = LocalDateTime.of(2026, 7, 14, 18, 0);
            var record = AttendanceRecord.builder()
                .id(1L)
                .employee(employee)
                .date(clockInTime.toLocalDate())
                .clockIn(clockInTime)
                .version(0L)
                .build();

            when(repository.findFirstByEmployeeIdAndDateAndClockOutIsNullOrderByClockInDesc(
                employee.getId(), clockOutTime.toLocalDate()))
                .thenReturn(Optional.of(record));
            when(repository.save(any(AttendanceRecord.class))).thenReturn(record);

            var result = service.clockOut(employee.getId(), clockOutTime);

            assertThat(result.getClockOut()).isEqualTo(clockOutTime);
        }

        @Test
        @DisplayName("未出勤の場合は例外が発生する")
        void clockOut_notClockedIn_throwsException() {
            var clockOutTime = LocalDateTime.of(2026, 7, 14, 18, 0);

            when(repository.findFirstByEmployeeIdAndDateAndClockOutIsNullOrderByClockInDesc(
                employee.getId(), clockOutTime.toLocalDate()))
                .thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.clockOut(employee.getId(), clockOutTime))
                .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("日次勤務時間計算")
    class DailyWorkMinutes {

        @Test
        @DisplayName("通常勤務（1区間）: 9:00-18:00 → 540分 - 60分休憩 = 480分")
        void calculateDailyWorkMinutes_singlePeriod() {
            var record = AttendanceRecord.builder()
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 18, 0))
                .build();

            int result = service.calculateDailyWorkMinutes(List.of(record));

            assertThat(result).isEqualTo(480);
        }

        @Test
        @DisplayName("中抜け（複数区間）: 9:00-12:00 + 13:00-18:00 → 480分 - 60分 = 420分")
        void calculateDailyWorkMinutes_multiplePeriods() {
            var record1 = AttendanceRecord.builder()
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 12, 0))
                .build();
            var record2 = AttendanceRecord.builder()
                .clockIn(LocalDateTime.of(2026, 7, 14, 13, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 18, 0))
                .build();

            int result = service.calculateDailyWorkMinutes(List.of(record1, record2));

            assertThat(result).isEqualTo(420);
        }

        @Test
        @DisplayName("未退勤の区間は計算対象外")
        void calculateDailyWorkMinutes_openPeriodExcluded() {
            var closedRecord = AttendanceRecord.builder()
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 12, 0))
                .build();
            var openRecord = AttendanceRecord.builder()
                .clockIn(LocalDateTime.of(2026, 7, 14, 13, 0))
                .clockOut(null)
                .build();

            int result = service.calculateDailyWorkMinutes(List.of(closedRecord, openRecord));

            // 180分 - 60分休憩 = 120分
            assertThat(result).isEqualTo(120);
        }

        @Test
        @DisplayName("合計が60分未満の場合、実働は0分（休憩控除で負にならない）")
        void calculateDailyWorkMinutes_lessThanBreak_returnsZero() {
            var record = AttendanceRecord.builder()
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 9, 30))
                .build();

            int result = service.calculateDailyWorkMinutes(List.of(record));

            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("残業判定")
    class OvertimeCalculation {

        @Test
        @DisplayName("所定435分超過分が残業: 480分 → 残業45分")
        void calculateOvertimeMinutes_exceeds435_returnsOvertime() {
            int result = service.calculateOvertimeMinutes(480);

            assertThat(result).isEqualTo(45);
        }

        @Test
        @DisplayName("所定435分以下は残業0")
        void calculateOvertimeMinutes_within435_returnsZero() {
            int result = service.calculateOvertimeMinutes(420);

            assertThat(result).isEqualTo(0);
        }

        @Test
        @DisplayName("ちょうど435分は残業0")
        void calculateOvertimeMinutes_exactly435_returnsZero() {
            int result = service.calculateOvertimeMinutes(435);

            assertThat(result).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("打刻修正")
    class UpdateRecord {

        @Test
        @DisplayName("時刻が正常に上書きされる")
        void updateRecord_updatesTimestamps() {
            var record = AttendanceRecord.builder()
                .id(1L)
                .employee(employee)
                .date(LocalDate.of(2026, 7, 14))
                .clockIn(LocalDateTime.of(2026, 7, 14, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 14, 18, 0))
                .version(0L)
                .build();

            var request = new UpdateAttendanceRequest(
                LocalDateTime.of(2026, 7, 14, 8, 30),
                LocalDateTime.of(2026, 7, 14, 17, 30),
                0L
            );

            when(repository.findById(1L)).thenReturn(Optional.of(record));
            when(repository.save(any(AttendanceRecord.class))).thenReturn(record);

            var result = service.updateRecord(1L, request);

            assertThat(result.getClockIn()).isEqualTo(LocalDateTime.of(2026, 7, 14, 8, 30));
            assertThat(result.getClockOut()).isEqualTo(LocalDateTime.of(2026, 7, 14, 17, 30));
        }

        @Test
        @DisplayName("存在しないレコードの場合は例外が発生する")
        void updateRecord_notFound_throwsException() {
            var request = new UpdateAttendanceRequest(
                LocalDateTime.of(2026, 7, 14, 8, 30),
                null,
                0L
            );

            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.updateRecord(999L, request))
                .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("月次集計")
    class MonthlyAttendance {

        @Test
        @DisplayName("日ごとのサマリーと月合計が正しく計算される")
        void getMonthlyAttendance_calculatesCorrectly() {
            var yearMonth = YearMonth.of(2026, 7);
            var day1Record = AttendanceRecord.builder()
                .id(1L)
                .employee(employee)
                .date(LocalDate.of(2026, 7, 1))
                .clockIn(LocalDateTime.of(2026, 7, 1, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 1, 18, 0))
                .version(0L)
                .build();
            var day2Record = AttendanceRecord.builder()
                .id(2L)
                .employee(employee)
                .date(LocalDate.of(2026, 7, 2))
                .clockIn(LocalDateTime.of(2026, 7, 2, 9, 0))
                .clockOut(LocalDateTime.of(2026, 7, 2, 19, 0))
                .version(0L)
                .build();

            when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));
            when(repository.findByEmployeeIdAndDateBetween(
                employee.getId(),
                yearMonth.atDay(1),
                yearMonth.atEndOfMonth()))
                .thenReturn(List.of(day1Record, day2Record));

            var result = service.getMonthlyAttendance(employee.getId(), yearMonth);

            assertThat(result.records()).hasSize(2);
            // Day1: 540min - 60min = 480min... wait: 9h = 540min - 60 = 480
            // Day2: 600min - 60min = 540min
            assertThat(result.totalWorkMinutes()).isEqualTo(480 + 540);
            // Day1 overtime: 480 - 435 = 45
            // Day2 overtime: 540 - 435 = 105
            assertThat(result.totalOvertimeMinutes()).isEqualTo(45 + 105);
        }
    }
}
