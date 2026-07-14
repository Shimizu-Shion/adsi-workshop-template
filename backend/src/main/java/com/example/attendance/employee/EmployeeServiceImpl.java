package com.example.attendance.employee;

import com.example.attendance.employee.dto.CreateEmployeeRequest;
import com.example.attendance.employee.dto.EmployeeResponse;
import com.example.attendance.employee.dto.UpdateEmployeeRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public List<EmployeeResponse> findAll(Long departmentId, Boolean active) {
        List<Employee> employees;
        if (departmentId != null && active != null) {
            employees = employeeRepository.findByDepartmentIdAndActive(departmentId, active);
        } else if (departmentId != null) {
            employees = employeeRepository.findByDepartmentId(departmentId);
        } else if (active != null) {
            employees = employeeRepository.findByActive(active);
        } else {
            employees = employeeRepository.findAll();
        }
        return employees.stream().map(EmployeeResponse::from).toList();
    }

    @Override
    public EmployeeResponse findById(Long id) {
        var employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("社員が見つかりません: id=" + id));
        return EmployeeResponse.from(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse create(CreateEmployeeRequest request) {
        if (employeeRepository.findByEmployeeCode(request.employeeCode()).isPresent()) {
            throw new IllegalArgumentException("社員コードが既に使用されています: " + request.employeeCode());
        }
        if (employeeRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("メールアドレスが既に使用されています: " + request.email());
        }

        var department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new EntityNotFoundException("部門が見つかりません: id=" + request.departmentId()));

        var employee = Employee.builder()
                .employeeCode(request.employeeCode())
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.valueOf(request.role()))
                .department(department)
                .active(true)
                .build();

        var saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, UpdateEmployeeRequest request) {
        var employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("社員が見つかりません: id=" + id));

        employee.setVersion(request.version());

        var department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new EntityNotFoundException("部門が見つかりません: id=" + request.departmentId()));

        employee.setName(request.name());
        employee.setEmail(request.email());
        employee.setRole(Role.valueOf(request.role()));
        employee.setDepartment(department);
        employee.setUpdatedAt(LocalDateTime.now());

        var saved = employeeRepository.save(employee);
        return EmployeeResponse.from(saved);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        var employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("社員が見つかりません: id=" + id));
        employee.setActive(false);
        employee.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(employee);
    }

    @Override
    public EmployeeResponse findByEmployeeCode(String employeeCode) {
        var employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new EntityNotFoundException("社員が見つかりません: code=" + employeeCode));
        return EmployeeResponse.from(employee);
    }
}
