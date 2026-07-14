package com.example.attendance.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateEmployeeRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull @Pattern(regexp = "EMPLOYEE|MANAGER|ADMIN", message = "ロールはEMPLOYEE, MANAGER, ADMINのいずれかを指定してください") String role,
        @NotNull Long departmentId,
        @NotNull Long version
) {}
