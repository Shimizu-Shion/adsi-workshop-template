package com.example.attendance.employee.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateEmployeeRequest(
        @NotBlank @Size(max = 20) String employeeCode,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull @Pattern(regexp = "EMPLOYEE|MANAGER|ADMIN", message = "ロールはEMPLOYEE, MANAGER, ADMINのいずれかを指定してください") String role,
        @NotNull Long departmentId
) {}
