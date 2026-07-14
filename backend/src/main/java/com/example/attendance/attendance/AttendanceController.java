package com.example.attendance.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;

@Validated
@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/clock-in")
    public ResponseEntity<AttendanceRecordResponse> clockIn(
            @RequestParam @Positive Long employeeId,
            @RequestBody(required = false) ClockInRequest request) {

        LocalDateTime timestamp = (request != null && request.timestamp() != null)
            ? request.timestamp()
            : LocalDateTime.now();

        var record = attendanceService.clockIn(employeeId, timestamp);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AttendanceRecordResponse.from(record));
    }

    @PostMapping("/clock-out")
    public ResponseEntity<AttendanceRecordResponse> clockOut(
            @RequestParam @Positive Long employeeId,
            @RequestBody(required = false) ClockOutRequest request) {

        LocalDateTime timestamp = (request != null && request.timestamp() != null)
            ? request.timestamp()
            : LocalDateTime.now();

        var record = attendanceService.clockOut(employeeId, timestamp);
        return ResponseEntity.ok(AttendanceRecordResponse.from(record));
    }

    @GetMapping("/records")
    public ResponseEntity<MonthlyAttendanceResponse> getRecords(
            @RequestParam @Positive Long employeeId,
            @RequestParam String yearMonth) {

        YearMonth ym = parseYearMonth(yearMonth);
        var response = attendanceService.getMonthlyAttendance(employeeId, ym);
        return ResponseEntity.ok(response);
    }

    private YearMonth parseYearMonth(String yearMonth) {
        try {
            return YearMonth.parse(yearMonth);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("年月の形式が不正です。YYYY-MM 形式で指定してください。");
        }
    }

    @PutMapping("/records/{id}")
    public ResponseEntity<AttendanceRecordResponse> updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAttendanceRequest request) {

        var record = attendanceService.updateRecord(id, request);
        return ResponseEntity.ok(AttendanceRecordResponse.from(record));
    }
}
