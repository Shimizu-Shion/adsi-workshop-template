package com.example.attendance.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.employeeCode = :employeeCode")
    Optional<Employee> findByEmployeeCode(String employeeCode);

    Optional<Employee> findByEmail(String email);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.department.id = :departmentId")
    List<Employee> findByDepartmentId(Long departmentId);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.active = :active")
    List<Employee> findByActive(boolean active);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department WHERE e.department.id = :departmentId AND e.active = :active")
    List<Employee> findByDepartmentIdAndActive(Long departmentId, boolean active);

    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.department")
    List<Employee> findAllWithDepartment();
}
