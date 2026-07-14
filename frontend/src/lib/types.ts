export type Role = 'EMPLOYEE' | 'MANAGER' | 'ADMIN'
export type LeaveStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface EmployeeResponse {
  id: number
  employeeCode: string
  name: string
  email: string
  role: Role
  departmentId: number
  departmentName: string
  active: boolean
  version: number
}

export interface LoginRequest {
  employeeCode: string
  password: string
}

export interface LoginResponse {
  employee: EmployeeResponse
}

export interface CreateEmployeeRequest {
  employeeCode: string
  name: string
  email: string
  password: string
  role: Role
  departmentId: number
}

export interface UpdateEmployeeRequest {
  name: string
  email: string
  role: Role
  departmentId: number
  version: number
}

export interface AttendanceRecordResponse {
  id: number
  employeeId: number
  date: string
  clockIn: string
  clockOut: string | null
  workMinutes: number
}

export interface DailyAttendanceSummary {
  date: string
  records: AttendanceRecordResponse[]
  workMinutes: number
  overtimeMinutes: number
  isHoliday: boolean
}

export interface MonthlyAttendanceResponse {
  employeeId: number
  employeeName: string
  yearMonth: string
  records: DailyAttendanceSummary[]
  totalWorkMinutes: number
  totalOvertimeMinutes: number
}

export interface ClockInRequest {
  timestamp?: string
}

export interface ClockOutRequest {
  timestamp?: string
}

export interface UpdateAttendanceRequest {
  clockIn: string
  clockOut?: string
  version: number
}

export interface CreateLeaveRequest {
  date: string
  reason?: string
}

export interface LeaveRequestResponse {
  id: number
  employeeId: number
  employeeName: string
  date: string
  status: LeaveStatus
  approvedBy: number | null
  approverName: string | null
  reason: string | null
  createdAt: string
}

export interface LeaveBalanceResponse {
  fiscalYear: number
  totalDays: number
  carriedOver: number
  usedDays: number
  remainingDays: number
}

export interface HolidayResponse {
  id: number
  date: string
  name: string
}

export interface CreateHolidayRequest {
  date: string
  name: string
}

export interface OvertimeAlertResponse {
  id: number
  employeeId: number
  employeeName: string
  yearMonth: string
  totalOvertimeMinutes: number
  notifiedAt: string
}

export interface ErrorResponse {
  message: string
  code: string
  errors?: { field: string; message: string }[]
}
