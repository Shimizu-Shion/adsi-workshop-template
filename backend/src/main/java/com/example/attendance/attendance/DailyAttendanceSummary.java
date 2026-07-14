package com.example.attendance.attendance;

import java.time.LocalDate;
import java.util.List;

public record DailyAttendanceSummary(
    LocalDate date,
    List<AttendanceRecordResponse> records,
    int workMinutes,
    int overtimeMinutes
) {}
