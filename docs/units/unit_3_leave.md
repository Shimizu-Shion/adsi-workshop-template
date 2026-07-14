# Unit 3: 有給休暇

## 概要

有給休暇の申請・承認ワークフロー、残日数管理を実装する。

## 依存

- Unit 1（認証・社員管理）— ロール判定・部門情報が必要

## ユーザーストーリー

- US-30: 有給申請
- US-31: 部門長が承認/却下
- US-32: 有給残日数確認

## スコープ

### Backend

- [ ] LeaveService (interface + impl)
  - 有給申請（残日数チェック → PENDING で作成）
  - 承認（部門長の権限チェック → APPROVED + usedDays 加算）
  - 却下（REJECTED）
  - 残日数計算（totalDays + carriedOver - usedDays）
  - 申請一覧（自分 / 部門長は自部門の PENDING）
- [ ] LeaveController
  - POST /api/leave/requests
  - GET /api/leave/requests
  - POST /api/leave/requests/{id}/approve
  - POST /api/leave/requests/{id}/reject
  - GET /api/leave/balance
- [ ] DTO（record）
  - CreateLeaveRequest
  - LeaveRequestResponse
  - LeaveBalanceResponse
- [ ] 認可ルール
  - 申請: 自分のみ
  - 承認/却下: 部門長（自部門の社員の申請のみ）
  - 残日数: 自分のみ
- [ ] テスト
  - LeaveService ユニットテスト
    - 申請（残日数あり → 成功）
    - 申請（残日数なし → 失敗）
    - 承認 → usedDays 加算
    - 却下 → usedDays 変わらず
  - LeaveController WebMvcTest
  - LeaveRepository DataJpaTest

### Frontend

- [ ] 有給申請画面（`/leave/request`）
  - 残日数表示
  - 日付選択 + 理由入力
  - 申請ボタン
- [ ] 有給申請一覧（`/leave/requests`）
  - 自分の申請履歴（ステータス表示）
  - 部門長: 部門の PENDING 一覧 + 承認/却下ボタン
- [ ] ダッシュボードに残日数カード追加
- [ ] テスト
  - 申請フォーム コンポーネントテスト
  - 承認/却下ボタン コンポーネントテスト

## テーブル

- leave_requests（Unit 0 で DDL 作成済み）
- leave_balances（Unit 0 で DDL 作成済み）

## API

| メソッド | パス | 認可 |
|---------|------|------|
| POST | /api/leave/requests | 認証済み（自分） |
| GET | /api/leave/requests | 認証済み（自分/部門長:部門） |
| POST | /api/leave/requests/{id}/approve | MANAGER（自部門） |
| POST | /api/leave/requests/{id}/reject | MANAGER（自部門） |
| GET | /api/leave/balance | 認証済み（自分） |

## ドメインルール

```
利用可能日数 = totalDays(20) + carriedOver - usedDays
申請条件: 利用可能日数 > 0 かつ 対象日が未来日
承認条件: 承認者が申請者の部門長である
```

## 完了条件

- [ ] 社員が有給を申請できる
- [ ] 残日数不足時に申請が拒否される
- [ ] 部門長が自部門の申請を承認/却下できる
- [ ] 承認時に usedDays が加算される
- [ ] 残日数が正しく表示される
- [ ] テストカバレッジ 80% 以上
