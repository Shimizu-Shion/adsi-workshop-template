package com.example.attendance.attendance;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceRecordResponse(
    Long id,
    Long employeeId,
    LocalDate date,
    LocalDateTime clockIn,
    LocalDateTime clockOut,
    Long version
) {
    public static AttendanceRecordResponse from(AttendanceRecord record) {
        return new AttendanceRecordResponse(
            record.getId(),
            record.getEmployee().getId(),
            record.getDate(),
            record.getClockIn(),
            record.getClockOut(),
            record.getVersion()
        );
    }
}
