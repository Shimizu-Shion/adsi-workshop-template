# Unit 4: カレンダー・残業アラート

## 概要

休日マスタ管理と、月次残業30時間超過のアラート機能を実装する。

## 依存

- Unit 2（打刻・勤怠一覧）— 残業時間の集計結果を使用

## ユーザーストーリー

- US-40: 残業30時間超過アラート（本人）
- US-41: 残業30時間超過アラート（部門長）
- カレンダーマスタ管理（ビジネスルール: 休日・祝日）

## スコープ

### Backend

- [ ] CalendarService (interface + impl)
  - 休日一覧取得（年指定）
  - 休日登録（管理者）
  - 休日削除（管理者）
  - 指定日が休日か判定
- [ ] CalendarController
  - GET /api/calendar/holidays?year=YYYY
  - POST /api/calendar/holidays
  - DELETE /api/calendar/holidays/{id}
- [ ] OvertimeAlertService (interface + impl)
  - 月次残業集計（AttendanceService の計算結果を使用）
  - 30時間（1800分）超過判定
  - アラート生成・保存
  - アラート一覧取得（本人 / 部門長は自部門）
- [ ] AlertController
  - GET /api/alerts?yearMonth=YYYY-MM
- [ ] 勤怠一覧に休日表示を統合
  - MonthlyAttendanceResponse の isHoliday フラグ設定
- [ ] DTO（record）
  - CreateHolidayRequest, HolidayResponse
  - OvertimeAlertResponse
- [ ] テスト
  - CalendarService ユニットテスト
  - OvertimeAlertService ユニットテスト
    - 30時間未満 → アラートなし
    - 30時間超過 → アラート生成
    - 既にアラート済み → 重複しない
  - Controller WebMvcTest

### Frontend

- [ ] カレンダー管理画面（`/admin/calendar`）
  - 年選択
  - 休日一覧テーブル
  - 休日追加フォーム（日付 + 名称）
  - 削除ボタン
- [ ] アラート一覧画面（`/alerts`）
  - 月選択
  - アラート一覧（社員名、残業時間）
  - 部門長: 自部門のアラートも表示
- [ ] ダッシュボードにアラート表示追加
- [ ] 勤怠一覧の休日行にマーキング
- [ ] テスト
  - カレンダー管理 コンポーネントテスト
  - アラート一覧 コンポーネントテスト

## テーブル

- calendar_holidays（Unit 0 で DDL 作成済み）
- overtime_alerts（Unit 0 で DDL 作成済み）

## API

| メソッド | パス | 認可 |
|---------|------|------|
| GET | /api/calendar/holidays | 認証済み |
| POST | /api/calendar/holidays | ADMIN |
| DELETE | /api/calendar/holidays/{id} | ADMIN |
| GET | /api/alerts | 認証済み（自分/部門長:部門） |

## ドメインルール

```
月次残業時間 = Σ(当月の各日の残業時間)
アラート条件: 月次残業時間 > 1800分（30時間）
通知先: 本人 + 部門長
```

- アラートは月に1回のみ生成（重複防止: employee_id + year_month の UNIQUE）
- 退勤打刻時にその月の累計を再計算し、閾値超過でアラート生成

## 完了条件

- [ ] 管理者が休日を登録・削除できる
- [ ] 勤怠一覧に休日マークが表示される
- [ ] 残業30時間超過時にアラートが生成される
- [ ] 本人と部門長がアラートを閲覧できる
- [ ] テストカバレッジ 80% 以上
