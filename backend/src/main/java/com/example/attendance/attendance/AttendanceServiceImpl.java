package com.example.attendance.attendance;

import com.example.attendance.employee.Employee;
import com.example.attendance.employee.EmployeeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final int BREAK_MINUTES = 60;
    private static final int STANDARD_WORK_MINUTES = 435;

    private final AttendanceRecordRepository repository;
    private final EmployeeRepository employeeRepository;

    public AttendanceServiceImpl(AttendanceRecordRepository repository,
                                 EmployeeRepository employeeRepository) {
        this.repository = repository;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public AttendanceRecord clockIn(Long employeeId, LocalDateTime timestamp) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> {
                log.warn("社員が見つかりません: employeeId={}", employeeId);
                return new EntityNotFoundException("指定された社員情報が見つかりません。");
            });

        AttendanceRecord record = AttendanceRecord.builder()
            .employee(employee)
            .date(timestamp.toLocalDate())
            .clockIn(timestamp)
            .build();

        return repository.save(record);
    }

    @Override
    public AttendanceRecord clockOut(Long employeeId, LocalDateTime timestamp) {
        LocalDate date = timestamp.toLocalDate();
        AttendanceRecord record = repository
            .findFirstByEmployeeIdAndDateAndClockOutIsNullOrderByClockInDesc(employeeId, date)
            .orElseThrow(() -> new IllegalArgumentException("出勤打刻がありません。先に出勤してください。"));

        record.setClockOut(timestamp);
        record.setUpdatedAt(LocalDateTime.now());
        return repository.save(record);
    }

    @Override
    public AttendanceRecord updateRecord(Long recordId, UpdateAttendanceRequest request) {
        AttendanceRecord record = repository.findById(recordId)
            .orElseThrow(() -> {
                log.warn("打刻レコードが見つかりません: recordId={}", recordId);
                return new EntityNotFoundException("指定された打刻レコードが見つかりません。");
            });

        if (!record.getVersion().equals(request.version())) {
            throw new org.springframework.orm.ObjectOptimisticLockingFailureException(
                AttendanceRecord.class.getName(), recordId);
        }

        record.setClockIn(request.clockIn());
        record.setClockOut(request.clockOut());
        record.setUpdatedAt(LocalDateTime.now());
        return repository.save(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getRecordsByDate(Long employeeId, LocalDate date) {
        return repository.findByEmployeeIdAndDate(employeeId, date);
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyAttendanceResponse getMonthlyAttendance(Long employeeId, YearMonth yearMonth) {
        Employee employee = employeeRepository.findById(employeeId)
            .orElseThrow(() -> {
                log.warn("社員が見つかりません: employeeId={}", employeeId);
                return new EntityNotFoundException("指定された社員情報が見つかりません。");
            });

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AttendanceRecord> records = repository.findByEmployeeIdAndDateBetween(
            employeeId, startDate, endDate);

        Map<LocalDate, List<AttendanceRecord>> recordsByDate = records.stream()
            .collect(Collectors.groupingBy(AttendanceRecord::getDate));

        List<DailyAttendanceSummary> dailySummaries = recordsByDate.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(entry -> {
                int workMinutes = calculateDailyWorkMinutes(entry.getValue());
                int overtimeMinutes = calculateOvertimeMinutes(workMinutes);
                List<AttendanceRecordResponse> responses = entry.getValue().stream()
                    .map(AttendanceRecordResponse::from)
                    .toList();
                return new DailyAttendanceSummary(entry.getKey(), responses, workMinutes, overtimeMinutes);
            })
            .toList();

        int totalWorkMinutes = dailySummaries.stream()
            .mapToInt(DailyAttendanceSummary::workMinutes)
            .sum();
        int totalOvertimeMinutes = dailySummaries.stream()
            .mapToInt(DailyAttendanceSummary::overtimeMinutes)
            .sum();

        return new MonthlyAttendanceResponse(
            employeeId,
            employee.getName(),
            yearMonth.toString(),
            dailySummaries,
            totalWorkMinutes,
            totalOvertimeMinutes
        );
    }

    @Override
    public int calculateDailyWorkMinutes(List<AttendanceRecord> records) {
        long totalMinutes = records.stream()
            .filter(r -> r.getClockOut() != null)
            .mapToLong(r -> Duration.between(r.getClockIn(), r.getClockOut()).toMinutes())
            .sum();

        int workMinutes = (int) totalMinutes - BREAK_MINUTES;
        return Math.max(0, workMinutes);
    }

    @Override
    public int calculateOvertimeMinutes(int workMinutes) {
        return Math.max(0, workMinutes - STANDARD_WORK_MINUTES);
    }
}
