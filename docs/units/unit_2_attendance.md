# Unit 2: 打刻・勤怠一覧

## 概要

出退勤の打刻（中抜け対応）、打刻修正、月次勤怠一覧表示を実装する。
勤務時間・残業時間の計算ロジックを含む。

## 依存

- Unit 0（共通基盤）

## ユーザーストーリー

- US-10: 出勤打刻
- US-11: 退勤打刻
- US-12: 複数回出退勤（中抜け）
- US-13: 打刻修正（手動入力）
- US-20: 自分の月次勤怠一覧
- US-21: 部門の月次勤怠一覧（部門長）
- US-22: 全社員の勤怠一覧・編集（管理者）

## スコープ

### Backend

- [ ] AttendanceService (interface + impl)
  - 出勤打刻（新規 AttendanceRecord 作成）
  - 退勤打刻（最新の未退勤レコードに clock_out 設定）
  - 打刻修正（時刻の上書き、楽観ロック）
  - 日次勤務時間計算
    - 各区間の合計 − 休憩60分
    - 所定435分超過分 = 残業
  - 月次集計（日ごとのサマリー + 月合計）
- [ ] AttendanceController
  - POST /api/attendance/clock-in
  - POST /api/attendance/clock-out
  - GET /api/attendance/records?yearMonth=YYYY-MM&employeeId=X
  - PUT /api/attendance/records/{id}
- [ ] DTO（record）
  - ClockInRequest, ClockOutRequest
  - UpdateAttendanceRequest
  - AttendanceRecordResponse
  - DailyAttendanceSummary, MonthlyAttendanceResponse
- [ ] 認可ルール
  - 打刻: 自分のみ
  - 一覧: 自分 / 部門長は自部門 / 管理者は全員
  - 修正: 自分 + 管理者
- [ ] テスト
  - AttendanceService ユニットテスト（勤務時間計算ロジック重点）
    - 通常勤務（1区間）
    - 中抜け（複数区間）
    - 残業判定
    - 休憩控除
  - AttendanceController WebMvcTest
  - AttendanceRepository DataJpaTest

### Frontend

- [ ] 打刻画面（`/attendance`）
  - 出勤/退勤ボタン
  - 手動時刻入力チェックボックス
  - 当日の打刻履歴表示
- [ ] 月次勤怠一覧画面（`/attendance/monthly`）
  - 月切替（前月/次月）
  - 日ごとの出退勤・実働・残業表示
  - 打刻修正（インライン編集 or モーダル）
  - 月合計表示
- [ ] 部門勤怠一覧（`/attendance/department`）— 部門長向け
  - 社員選択 → 月次一覧表示
- [ ] ダッシュボード（`/`）
  - 今日の勤務状況カード
- [ ] テスト
  - 打刻ボタン コンポーネントテスト
  - 勤怠一覧テーブル コンポーネントテスト

## テーブル

- attendance_records（Unit 0 で DDL 作成済み）

## API

| メソッド | パス | 認可 |
|---------|------|------|
| POST | /api/attendance/clock-in | 認証済み（自分） |
| POST | /api/attendance/clock-out | 認証済み（自分） |
| GET | /api/attendance/records | 認証済み（自分/部門長:部門/管理者:全員） |
| PUT | /api/attendance/records/{id} | 認証済み（自分 + 管理者） |

## ドメインルール

```
実働時間 = Σ(各区間の clock_out - clock_in) - 60分（休憩控除）
残業時間 = max(0, 実働時間 - 435分)
```

- 休憩控除は1日の合計に対して1回のみ適用
- clock_out が null の区間は計算対象外（勤務中）
- 休日判定は CalendarHoliday を参照（Unit 4 で統合）

## 完了条件

- [ ] 出勤/退勤打刻が正常に記録される
- [ ] 1日に複数回の出退勤が記録できる
- [ ] 打刻の手動修正ができる
- [ ] 月次一覧で日ごとの実働・残業が正しく表示される
- [ ] 部門長は自部門、管理者は全社員の一覧を閲覧できる
- [ ] テストカバレッジ 80% 以上（特に勤務時間計算ロジック）
