package com.example.attendance.attendance;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record UpdateAttendanceRequest(
    @NotNull LocalDateTime clockIn,
    LocalDateTime clockOut,
    @NotNull Long version
) {}
