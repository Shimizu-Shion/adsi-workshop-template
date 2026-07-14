package com.example.attendance.attendance;

import com.example.attendance.employee.Department;
import com.example.attendance.employee.Employee;
import com.example.attendance.employee.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceService attendanceService;

    private Employee createEmployee() {
        var department = Department.builder().id(1L).name("開発部").build();
        return Employee.builder()
            .id(1L)
            .employeeCode("tanaka")
            .name("田中太郎")
            .email("tanaka@example.com")
            .passwordHash("hashed")
            .role(Role.EMPLOYEE)
            .department(department)
            .active(true)
            .version(0L)
            .build();
    }

    @Test
    @DisplayName("POST /api/attendance/clock-in → 201")
    void clockIn_returnsCreated() throws Exception {
        var employee = createEmployee();
        var timestamp = LocalDateTime.of(2026, 7, 14, 9, 0);
        var record = AttendanceRecord.builder()
            .id(1L)
            .employee(employee)
            .date(timestamp.toLocalDate())
            .clockIn(timestamp)
            .version(0L)
            .build();

        when(attendanceService.clockIn(eq(1L), any(LocalDateTime.class))).thenReturn(record);

        var request = new ClockInRequest(timestamp);

        mockMvc.perform(post("/attendance/clock-in")
                .param("employeeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.employeeId").value(1))
            .andExpect(jsonPath("$.date").value("2026-07-14"));
    }

    @Test
    @DisplayName("POST /api/attendance/clock-out → 200")
    void clockOut_returnsOk() throws Exception {
        var employee = createEmployee();
        var clockIn = LocalDateTime.of(2026, 7, 14, 9, 0);
        var clockOut = LocalDateTime.of(2026, 7, 14, 18, 0);
        var record = AttendanceRecord.builder()
            .id(1L)
            .employee(employee)
            .date(clockIn.toLocalDate())
            .clockIn(clockIn)
            .clockOut(clockOut)
            .version(0L)
            .build();

        when(attendanceService.clockOut(eq(1L), any(LocalDateTime.class))).thenReturn(record);

        var request = new ClockOutRequest(clockOut);

        mockMvc.perform(post("/attendance/clock-out")
                .param("employeeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clockOut").exists());
    }

    @Test
    @DisplayName("POST /api/attendance/clock-out（未出勤）→ 400")
    void clockOut_notClockedIn_returnsBadRequest() throws Exception {
        when(attendanceService.clockOut(eq(1L), any(LocalDateTime.class)))
            .thenThrow(new IllegalArgumentException("出勤打刻がありません。先に出勤してください。"));

        var request = new ClockOutRequest(LocalDateTime.of(2026, 7, 14, 18, 0));

        mockMvc.perform(post("/attendance/clock-out")
                .param("employeeId", "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/attendance/records?yearMonth=2026-07&employeeId=1 → 200")
    void getRecords_returnsOk() throws Exception {
        var response = new MonthlyAttendanceResponse(
            1L, "田中太郎", "2026-07", List.of(), 0, 0);

        when(attendanceService.getMonthlyAttendance(eq(1L), eq(YearMonth.of(2026, 7))))
            .thenReturn(response);

        mockMvc.perform(get("/attendance/records")
                .param("yearMonth", "2026-07")
                .param("employeeId", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.employeeId").value(1))
            .andExpect(jsonPath("$.yearMonth").value("2026-07"));
    }

    @Test
    @DisplayName("PUT /api/attendance/records/{id} → 200")
    void updateRecord_returnsOk() throws Exception {
        var employee = createEmployee();
        var record = AttendanceRecord.builder()
            .id(1L)
            .employee(employee)
            .date(LocalDate.of(2026, 7, 14))
            .clockIn(LocalDateTime.of(2026, 7, 14, 8, 30))
            .clockOut(LocalDateTime.of(2026, 7, 14, 17, 30))
            .version(1L)
            .build();

        when(attendanceService.updateRecord(eq(1L), any(UpdateAttendanceRequest.class)))
            .thenReturn(record);

        var request = new UpdateAttendanceRequest(
            LocalDateTime.of(2026, 7, 14, 8, 30),
            LocalDateTime.of(2026, 7, 14, 17, 30),
            0L
        );

        mockMvc.perform(put("/attendance/records/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PUT /api/attendance/records/{id}（存在しない）→ 404")
    void updateRecord_notFound_returnsNotFound() throws Exception {
        when(attendanceService.updateRecord(eq(999L), any(UpdateAttendanceRequest.class)))
            .thenThrow(new EntityNotFoundException("打刻レコードが見つかりません: 999"));

        var request = new UpdateAttendanceRequest(
            LocalDateTime.of(2026, 7, 14, 8, 30),
            null,
            0L
        );

        mockMvc.perform(put("/attendance/records/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isNotFound());
    }
}
