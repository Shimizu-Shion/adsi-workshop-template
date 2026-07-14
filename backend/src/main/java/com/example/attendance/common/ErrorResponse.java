package com.example.attendance.common;

import java.util.List;

public record ErrorResponse(
    String message,
    String code,
    List<FieldError> errors
) {
    public record FieldError(String field, String message) {}

    public static ErrorResponse of(String message, String code) {
        return new ErrorResponse(message, code, List.of());
    }

    public static ErrorResponse withFieldErrors(String message, List<FieldError> errors) {
        return new ErrorResponse(message, "VALIDATION_ERROR", errors);
    }
}
