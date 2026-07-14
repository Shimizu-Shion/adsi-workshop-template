# DB 設計

## 概要

- RDBMS: PostgreSQL
- マイグレーション: Flyway
- 楽観ロック: `version` カラム（`@Version`）

## ER 図

```
┌──────────────┐       ┌──────────────────────┐
│  departments │       │      employees       │
├──────────────┤       ├──────────────────────┤
│ id (PK)      │───┐   │ id (PK)              │
│ name         │   └──>│ department_id (FK)    │
└──────────────┘       │ employee_code (UQ)   │
                       │ name                 │
                       │ email (UQ)           │
                       │ password_hash        │
                       │ role                 │
                       │ active               │
                       │ version              │
                       │ created_at           │
                       │ updated_at           │
                       └──────────────────────┘
                              │
          ┌───────────────────┼───────────────────┐
          │                   │                   │
          v                   v                   v
┌─────────────────┐  ┌────────────────┐  ┌───────────────┐
│attendance_records│  │ leave_requests │  │leave_balances │
├─────────────────┤  ├────────────────┤  ├───────────────┤
│ id (PK)         │  │ id (PK)        │  │ id (PK)       │
│ employee_id(FK) │  │ employee_id(FK)│  │ employee_id(FK)│
│ date            │  │ date           │  │ fiscal_year   │
│ clock_in        │  │ status         │  │ total_days    │
│ clock_out       │  │ approved_by(FK)│  │ used_days     │
│ version         │  │ reason         │  │ carried_over  │
│ created_at      │  │ version        │  │ version       │
│ updated_at      │  │ created_at     │  │ created_at    │
└─────────────────┘  │ updated_at     │  │ updated_at    │
                     └────────────────┘  └───────────────┘

┌──────────────────┐  ┌─────────────────┐
│calendar_holidays │  │overtime_alerts  │
├──────────────────┤  ├─────────────────┤
│ id (PK)          │  │ id (PK)         │
│ date (UQ)        │  │ employee_id(FK) │
│ name             │  │ year_month      │
│ year             │  │ total_overtime  │
│ created_at       │  │ notified_at     │
└──────────────────┘  │ created_at      │
                      └─────────────────┘
```

## テーブル定義

### departments

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| name | VARCHAR(100) | NOT NULL | 部門名 |

### employees

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_code | VARCHAR(20) | NOT NULL, UNIQUE | ログインID |
| name | VARCHAR(100) | NOT NULL | 氏名 |
| email | VARCHAR(255) | NOT NULL, UNIQUE | メールアドレス |
| password_hash | VARCHAR(255) | NOT NULL | BCrypt ハッシュ |
| role | VARCHAR(20) | NOT NULL | EMPLOYEE / MANAGER / ADMIN |
| department_id | BIGINT | FK → departments(id) | 所属部門 |
| active | BOOLEAN | NOT NULL DEFAULT TRUE | 有効フラグ |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

### attendance_records

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | NOT NULL, FK → employees(id) | |
| date | DATE | NOT NULL | 勤務日 |
| clock_in | TIMESTAMP | NOT NULL | 出勤時刻 |
| clock_out | TIMESTAMP | | 退勤時刻（未退勤は NULL） |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

インデックス: `(employee_id, date)`

### leave_requests

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | NOT NULL, FK → employees(id) | 申請者 |
| date | DATE | NOT NULL | 休暇日 |
| status | VARCHAR(20) | NOT NULL DEFAULT 'PENDING' | PENDING / APPROVED / REJECTED |
| approved_by | BIGINT | FK → employees(id) | 承認者 |
| reason | VARCHAR(500) | | 申請理由 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

インデックス: `(employee_id, date)`, `(status)`

### leave_balances

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | NOT NULL, FK → employees(id) | |
| fiscal_year | INT | NOT NULL | 年度 |
| total_days | INT | NOT NULL DEFAULT 20 | 付与日数 |
| used_days | INT | NOT NULL DEFAULT 0 | 消化日数 |
| carried_over | INT | NOT NULL DEFAULT 0 | 前年繰越 |
| version | BIGINT | NOT NULL DEFAULT 0 | 楽観ロック |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |
| updated_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

UNIQUE制約: `(employee_id, fiscal_year)`

### calendar_holidays

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| date | DATE | NOT NULL, UNIQUE | 休日日付 |
| name | VARCHAR(100) | NOT NULL | 休日名 |
| year | INT | NOT NULL | 対象年度 |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

インデックス: `(year)`

### overtime_alerts

| カラム | 型 | 制約 | 説明 |
|--------|---|------|------|
| id | BIGSERIAL | PK | |
| employee_id | BIGINT | NOT NULL, FK → employees(id) | |
| year_month | VARCHAR(7) | NOT NULL | "2026-07" 形式 |
| total_overtime_minutes | INT | NOT NULL | 累計残業分 |
| notified_at | TIMESTAMP | NOT NULL | 通知日時 |
| created_at | TIMESTAMP | NOT NULL DEFAULT NOW() | |

UNIQUE制約: `(employee_id, year_month)`

## シードデータ

### departments

| name |
|------|
| 総務部 |
| 営業部 |
| 開発部 |
| 人事部 |

### employees（初期管理者）

| employee_code | name | role |
|---------------|------|------|
| admin | 管理者 | ADMIN |
