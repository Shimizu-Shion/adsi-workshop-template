package com.example.attendance.attendance;

import java.util.List;

public record MonthlyAttendanceResponse(
    Long employeeId,
    String employeeName,
    String yearMonth,
    List<DailyAttendanceSummary> records,
    int totalWorkMinutes,
    int totalOvertimeMinutes
) {}
