package com.example.attendance.employee.dto;

import com.example.attendance.employee.Employee;

public record EmployeeResponse(
        Long id,
        String employeeCode,
        String name,
        String email,
        String role,
        Long departmentId,
        String departmentName,
        boolean active,
        Long version
) {
    public static EmployeeResponse from(Employee entity) {
        return new EmployeeResponse(
                entity.getId(),
                entity.getEmployeeCode(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole().name(),
                entity.getDepartment() != null ? entity.getDepartment().getId() : null,
                entity.getDepartment() != null ? entity.getDepartment().getName() : null,
                entity.isActive(),
                entity.getVersion()
        );
    }
}
