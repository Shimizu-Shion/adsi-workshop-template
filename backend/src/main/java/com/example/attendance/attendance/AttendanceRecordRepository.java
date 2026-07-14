package com.example.attendance.attendance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    Optional<AttendanceRecord> findFirstByEmployeeIdAndDateAndClockOutIsNullOrderByClockInDesc(
            Long employeeId, LocalDate date);

    @Query("SELECT a FROM AttendanceRecord a JOIN FETCH a.employee WHERE a.employee.id = :employeeId " +
           "AND a.date BETWEEN :startDate AND :endDate ORDER BY a.date, a.clockIn")
    List<AttendanceRecord> findByEmployeeIdAndDateBetween(
            @Param("employeeId") Long employeeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
