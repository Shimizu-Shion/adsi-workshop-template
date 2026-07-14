# ドメインモデル設計

## ドメイン概要

勤怠管理ドメインを以下の Bounded Context に分割する。

| Context | 責務 |
|---------|------|
| 認証 (Auth) | ログイン・ログアウト・セッション管理 |
| 社員管理 (Employee) | 社員・部門マスタの CRUD |
| 打刻 (Attendance) | 出退勤記録・修正・勤務時間計算 |
| 有給休暇 (Leave) | 有給申請・承認・残日数管理 |
| アラート (Alert) | 残業超過の検知・通知 |
| カレンダー (Calendar) | 休日・祝日マスタ管理 |

## Entity

### Employee（社員）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK、自動採番 |
| employeeCode | String | 社員コード（ログインID） |
| name | String | 氏名 |
| email | String | メールアドレス |
| passwordHash | String | BCrypt ハッシュ |
| role | Role (enum) | EMPLOYEE / MANAGER / ADMIN |
| departmentId | Long | FK → Department |
| active | boolean | 有効/無効 |
| version | Long | 楽観ロック |

### Department（部門）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| name | String | 部門名 |

### AttendanceRecord（打刻レコード）

1日に複数回の出退勤を記録するため、1レコード = 1回の出勤〜退勤ペア。

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| date | LocalDate | 勤務日 |
| clockIn | LocalDateTime | 出勤時刻 |
| clockOut | LocalDateTime | 退勤時刻（nullable: 退勤前） |
| version | Long | 楽観ロック |

### LeaveRequest（有給申請）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| date | LocalDate | 休暇日 |
| status | LeaveStatus (enum) | PENDING / APPROVED / REJECTED |
| approvedBy | Long | FK → Employee（承認者）nullable |
| reason | String | 申請理由（nullable） |
| version | Long | 楽観ロック |

### LeaveBalance（有給残日数）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| fiscalYear | int | 年度 |
| totalDays | int | 付与日数（20） |
| usedDays | int | 消化日数 |
| carriedOver | int | 前年繰越日数 |
| version | Long | 楽観ロック |

### CalendarHoliday（休日マスタ）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| date | LocalDate | 休日の日付 |
| name | String | 休日名（例: 元旦） |
| year | int | 対象年度 |

### OvertimeAlert（残業アラート）

| フィールド | 型 | 説明 |
|-----------|---|------|
| id | Long | PK |
| employeeId | Long | FK → Employee |
| yearMonth | String | 対象年月（"2026-07"形式） |
| totalOvertimeMinutes | int | 累計残業分 |
| notifiedAt | LocalDateTime | 通知日時 |

## Value Object

| VO | 説明 | 構成 |
|----|------|------|
| WorkDuration | 勤務時間 | totalMinutes: int |
| TimeRange | 出勤〜退勤の時間区間 | start: LocalDateTime, end: LocalDateTime |
| YearMonth | 年月 | year: int, month: int |

## Enum

| Enum | 値 |
|------|-----|
| Role | EMPLOYEE, MANAGER, ADMIN |
| LeaveStatus | PENDING, APPROVED, REJECTED |

## ドメインサービス

### AttendanceService

- 打刻（出勤/退勤）
- 打刻修正
- 日次勤務時間の計算（休憩1時間自動控除）
- 月次勤務実績の集計

### LeaveService

- 有給申請
- 承認/却下
- 残日数の計算（付与 + 繰越 - 消化）

### OvertimeAlertService

- 月次残業時間の集計
- 30時間超過の判定・アラート生成

## ドメインルール

### 勤務時間計算

```
実働時間 = Σ(各区間の退勤 - 出勤) - 休憩控除(60分)
残業時間 = max(0, 実働時間 - 所定労働時間(435分))
```

- 休憩控除: 1日の実働合計から60分を差し引く
- 所定労働時間: 7時間15分 = 435分
- 残業割増率: x1.25（レポート出力時に使用）

### 有給残日数

```
利用可能日数 = 付与日数(20) + 繰越日数 - 消化日数
```

## 関連図

```
Department 1 ──── * Employee
Employee   1 ──── * AttendanceRecord
Employee   1 ──── * LeaveRequest
Employee   1 ──── * LeaveBalance
Employee   1 ──── * OvertimeAlert
```
