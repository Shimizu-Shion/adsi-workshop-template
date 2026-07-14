package com.example.attendance.employee;

import com.example.attendance.employee.dto.CreateEmployeeRequest;
import com.example.attendance.employee.dto.EmployeeResponse;
import com.example.attendance.employee.dto.UpdateEmployeeRequest;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponse> findAll(Long departmentId, Boolean active);

    EmployeeResponse findById(Long id);

    EmployeeResponse create(CreateEmployeeRequest request);

    EmployeeResponse update(Long id, UpdateEmployeeRequest request);

    void deactivate(Long id);

    EmployeeResponse findByEmployeeCode(String employeeCode);
}
