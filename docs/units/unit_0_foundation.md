# Unit 0: 共通基盤

## 概要

テストまで実行できるプロジェクト骨格を構築する。
全 Unit がこの基盤の上に構築されるため、最初に2人共同で完成させる。

## ユーザーストーリー

（直接のユーザーストーリーはなし。全 US の前提となるインフラ）

## スコープ

### Backend（Java / Spring Boot）

- [ ] Gradle プロジェクト初期化（Spring Boot 3.x, Java 21）
- [ ] 依存ライブラリ設定
  - Spring Web, Spring Security, Spring Data JPA
  - Flyway, PostgreSQL driver
  - Lombok, JUnit 5, Mockito, AssertJ
  - H2（テスト用インメモリ DB）
- [ ] パッケージ構成の作成
  ```
  com.example.attendance/
  ├── config/          # Security, CORS 等
  ├── common/          # 共通例外、エラーハンドラ
  ├── employee/        # Entity, Repository, Service, Controller
  ├── attendance/
  ├── leave/
  ├── calendar/
  └── alert/
  ```
- [ ] 共通設定
  - `application.yml`（プロファイル: default, test）
  - `application-test.yml`（H2 インメモリ）
  - SecurityFilterChain（初期: 全許可。Unit 1 で認証を入れる）
  - `@RestControllerAdvice` グローバル例外ハンドラ
  - CORS 設定（localhost:3000 許可）
- [ ] Flyway マイグレーション
  - `V1__create_departments.sql`
  - `V2__create_employees.sql`
  - `V3__create_attendance_records.sql`
  - `V4__create_leave_requests.sql`
  - `V5__create_leave_balances.sql`
  - `V6__create_calendar_holidays.sql`
  - `V7__create_overtime_alerts.sql`
  - `V8__seed_departments.sql`（シードデータ）
  - `V9__seed_admin_user.sql`（初期管理者）
- [ ] 全 Entity クラス（JPA アノテーション付き）
- [ ] 全 Enum（Role, LeaveStatus）
- [ ] ArchUnit テスト（レイヤー依存ルール）
- [ ] ヘルスチェック API（`GET /api/health`）

### Frontend（TypeScript / Next.js）

- [ ] Next.js プロジェクト初期化（App Router, TypeScript）
- [ ] 依存ライブラリ設定
  - Tailwind CSS
  - Vitest + Testing Library
- [ ] ディレクトリ構成
  ```
  src/
  ├── app/
  │   └── layout.tsx
  ├── components/ui/
  ├── lib/
  │   ├── api-client.ts   # fetch ラッパー（withBasePath対応）
  │   └── types.ts        # API レスポンス型（全DTO）
  └── __tests__/
  ```
- [ ] API クライアント基盤（`api-client.ts`）
  - ベース URL 設定
  - エラーハンドリング共通化
  - SageMaker basePath 対応（`withBasePath`）
- [ ] 全 DTO の TypeScript 型定義（`types.ts`）
- [ ] 共通 UI コンポーネント（Button, Input, Table, Modal）
- [ ] レイアウト（ナビゲーション骨格 — リンク先は各 Unit で実装）

### インフラ・CI

- [ ] `docker-compose.yml`（PostgreSQL）
- [ ] npm scripts（`dev`, `build`, `test`, `lint`）
- [ ] Gradle tasks（`test`, `bootRun`）
- [ ] テスト実行確認（Backend: `./gradlew test`, Frontend: `npm test`）

## テーブル

全テーブルの DDL をこの Unit で作成する（Flyway マイグレーション）。

## API

| メソッド | パス | 説明 |
|---------|------|------|
| GET | /api/health | ヘルスチェック |

## 完了条件

- [ ] `./gradlew test` が通る（ArchUnit + ヘルスチェックテスト）
- [ ] `npm test` が通る（API クライアントのユニットテスト）
- [ ] `./gradlew bootRun` でアプリが起動し `/api/health` が 200 を返す
- [ ] `npm run dev` でフロントが起動しレイアウトが表示される
- [ ] Flyway マイグレーションが全テーブルを作成する
