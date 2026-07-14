# Unit 1: 認証・社員管理

## 概要

ログイン/ログアウトとセッション管理、および管理者による社員マスタ CRUD を実装する。

## 依存

- Unit 0（共通基盤）

## ユーザーストーリー

- US-01: ログイン（ID/パスワード認証）
- US-02: ログアウト
- US-50: 社員登録（管理者）
- US-51: 社員編集（管理者）
- US-52: 社員無効化（管理者）

## スコープ

### Backend

- [ ] Spring Security 設定
  - セッションベース認証（Cookie: SESSION）
  - BCrypt パスワードエンコーダ
  - ロールベース認可（ADMIN のみ社員管理）
  - 未認証時 401、権限不足時 403
- [ ] AuthController
  - POST /api/auth/login
  - POST /api/auth/logout
  - GET /api/auth/me
- [ ] EmployeeService (interface + impl)
  - 一覧取得（部門フィルタ、有効フラグフィルタ）
  - 詳細取得
  - 登録（パスワード BCrypt 化）
  - 更新（楽観ロック）
  - 無効化（論理削除）
- [ ] EmployeeController
  - GET /api/employees
  - GET /api/employees/{id}
  - POST /api/employees
  - PUT /api/employees/{id}
  - DELETE /api/employees/{id}
- [ ] DTO（record）
  - LoginRequest, LoginResponse
  - CreateEmployeeRequest, UpdateEmployeeRequest, EmployeeResponse
- [ ] テスト
  - AuthController テスト（ログイン成功/失敗）
  - EmployeeService ユニットテスト
  - EmployeeController WebMvcTest
  - EmployeeRepository DataJpaTest

### Frontend

- [ ] ログイン画面（`/login`）
- [ ] 認証状態管理（useAuth hook）
- [ ] 未認証時リダイレクト
- [ ] ナビゲーション（ロール別メニュー表示制御）
- [ ] 社員管理画面（`/admin/employees`）
  - 一覧テーブル
  - 登録フォーム（モーダル）
  - 編集フォーム
  - 無効化確認ダイアログ
- [ ] テスト
  - ログインフォーム コンポーネントテスト
  - 社員一覧 コンポーネントテスト

## テーブル

- employees（Unit 0 で DDL 作成済み）
- departments（Unit 0 で DDL 作成済み）

## API

| メソッド | パス | 認可 |
|---------|------|------|
| POST | /api/auth/login | 全員 |
| POST | /api/auth/logout | 認証済み |
| GET | /api/auth/me | 認証済み |
| GET | /api/employees | 認証済み |
| GET | /api/employees/{id} | 認証済み |
| POST | /api/employees | ADMIN |
| PUT | /api/employees/{id} | ADMIN |
| DELETE | /api/employees/{id} | ADMIN |

## 完了条件

- [ ] ログイン/ログアウトが動作する
- [ ] 未認証で API アクセスすると 401
- [ ] 管理者が社員の CRUD を実行できる
- [ ] 一般社員・部門長は社員管理にアクセスできない（403）
- [ ] テストカバレッジ 80% 以上
