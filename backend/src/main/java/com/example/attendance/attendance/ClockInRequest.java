package com.example.attendance.attendance;

import java.time.LocalDateTime;

public record ClockInRequest(
    LocalDateTime timestamp
) {}
