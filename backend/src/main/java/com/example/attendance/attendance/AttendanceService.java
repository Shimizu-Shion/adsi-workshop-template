package com.example.attendance.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

public interface AttendanceService {

    AttendanceRecord clockIn(Long employeeId, LocalDateTime timestamp);

    AttendanceRecord clockOut(Long employeeId, LocalDateTime timestamp);

    AttendanceRecord updateRecord(Long recordId, UpdateAttendanceRequest request);

    List<AttendanceRecord> getRecordsByDate(Long employeeId, LocalDate date);

    MonthlyAttendanceResponse getMonthlyAttendance(Long employeeId, YearMonth yearMonth);

    int calculateDailyWorkMinutes(List<AttendanceRecord> records);

    int calculateOvertimeMinutes(int workMinutes);
}
