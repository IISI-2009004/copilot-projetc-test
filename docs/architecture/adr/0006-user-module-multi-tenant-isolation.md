# ADR-0006: 新增使用者管理模組（user），book/reading 資料以 userId 隔離

## Decision - 2026-07-31
**Decision**: 新增最底層 `user` 模組，提供帳號註冊（`username`+`password`）與登入功能，登入成功
核發 **JWT**（無狀態、Stateless）；密碼一律以 **bcrypt** 雜湊儲存。除 `/api/auth/register`、
`/api/auth/login` 外，所有 API 皆需於 `Authorization: Bearer <token>` 攜帶有效 JWT，由
`JwtAuthenticationFilter` 解析出 `userId` 寫入 Spring Security `SecurityContext`。
`Book`、`Tag`、`Category`、`ReadingRecord` 四張表皆新增 `userId` 外鍵欄位，Service 層所有
查詢/新增/更新/刪除操作一律以 `userId = CurrentUser.id()` 過濾，實現多租戶（multi-tenant）
資料隔離；跨用戶存取一律回 404。`BookQueryPort.existsBook(bookId)` 簽章調整為
`existsBook(bookId, userId)`，同時檢查書本存在且屬於呼叫者。

**Context**:
- 使用者需求：「我想再新增一個模組，是使用者管理，此系統網站可以註冊/登入，需要調整資料欄位是哪位使用者的資料。」
- 目前系統（requirements.md/design.md/threat-model.md 舊版）為單用戶假設：無身分驗證，任何呼叫端
  皆可操作所有資料，`Book`/`ReadingRecord` 無 `userId` 概念；threat-model.md 中 Spoofing 與
  Elevation of Privilege 兩項威脅皆標記「高風險、待後續 Sprint 補上」。
- 與使用者確認後決定：帳號機制採最簡單的 `username`+`password`（不需 email）；登入機制採
  JWT（無狀態），與 threat-model.md 原先預留的技術債方向一致。

**Options**:
1. **僅加登入驗證（Authentication），不做資料隔離（Authorization/多租戶）**
   → 任何登入用戶仍可操作所有書本/閱讀記錄，等於只驗證「你是誰」但不限制「你能碰什麼」，
   無法達成「這是我自己的書庫」的基本多用戶期待，且與 Elevation of Privilege 威脅项的
   既定待辦方向不符。
2. **加入登入驗證 + `userId` 資料隔離（Authentication + Authorization）**
   → 每位用戶只能存取自己的資料，符合「多用戶各自獨立藏書」的直覺期待，且與
   threat-model.md 已預留的技術債方向一致，是本次需求的合理完整解讀。
3. **Session-based 驗證（Spring Session + Cookie）取代 JWT**
   → 需要伺服器端保存 session 狀態，未來如需水平擴展需額外導入 Session 共享機制
   （如 Redis），且使用者已表明偏好 JWT（無狀態），故不採用。

**Rationale**:
選擇選項 2 + JWT（選項 3 的替代方案已由使用者明確排除）：
- 「使用者管理」與「資料歸屬」是一體兩面的需求——單純加登入驗證卻不做資料隔離，無法滿足
  「這是我自己的書庫」的基本期待，因此本次一併調整 `Book`/`Tag`/`Category`/`ReadingRecord`
  欄位。
- JWT 為無狀態設計，符合現有系統「輕量、單機可跑」的 MVP 特性，不需額外的 session store，
  部屬（H2 本機/PostgreSQL）與現有 Spring Boot 分層架構相容度最高。
- 密碼採 bcrypt 雜湊（不可逆，含隨機 salt），是業界標準密碼儲存作法，避免資料庫外洩時
  密碼明文或可逆加密被還原。
- `user` 模組設計為系統最底層、不依賴 `book`/`reading`，`book`/`reading` 亦不直接依賴
  `user` 模組內部類別（`UserRepository`/`UserService`），而是透過 Spring Security 框架層級
  的 `SecurityContext` 取得 `userId`，延續既有的「模組間僅能透過對外介面耦合，禁止直接
  存取對方 Repository」設計原則（與 `BookQueryPort` 的介面隔離精神一致）。
- `userId` 欄位的角色定位等同既有的 `categoryId`——是資料列的一般外部識別碼/過濾條件，
  而非跨模組事件或反向依賴，因此不會破壞既有 book↔reading 的單向依賴關係（`BookQueryPort`
  簽章新增 `userId` 參數，僅是查詢條件增加一項，架構模式不變）。
- 登入失敗訊息刻意不區分「帳號不存在」與「密碼錯誤」，是防止帳號列舉（Account Enumeration）
  攻擊的標準作法。
- 跨用戶存取一律回 404（而非 403 Forbidden），避免讓攻擊者透過狀態碼差異推測某 `bookId`
  是否存在（僅是屬於別人 vs. 完全不存在，攻擊者無法區分），降低資訊洩漏風險。

**Impact**:
- `requirements.md`：新增「模組 C — 使用者管理（user）」與 US-U01–U04；系統概述更新為
  多用戶系統；§4 模組邊界契約新增 `user` 模組定位；US-B03/B04/B05、US-R02/R03/R04/R05 補充
  `userId` 隔離敘述；非功能需求新增密碼雜湊/JWT 相關安全要求。
- `design.md`：架構圖新增 `user` 模組與 Spring Security Filter 層；套件結構新增 `user/` 套件；
  新增 `User` 資料模型；`Book`/`Tag`/`Category`/`ReadingRecord` 皆新增 `userId` 欄位；API 規格
  新增 `/api/auth/*`、`/api/users/me`，其餘端點註記「需登入」；`BookQueryPort` 簽章調整為
  `existsBook(bookId, userId)`；新增 §5c 說明 JWT 驗證流程與 `CurrentUser` 工具類別；STRIDE
  摘要新增 Account Enumeration 威脅列；新增 §7a/7b 序列圖（註冊登入、JWT 驗證流程）；新增 §9
  說明多用戶隔離的欄位定位。
- `tasks.md`：新增「模組 C：user」（C1–C6，含 Entity/JWT/SecurityConfig/Controller/測試）；
  A1–A5、B1–B5 全數調整為帶 `userId` 參數/欄位/查詢條件；介面契約區塊新增 `CurrentUser`
  工具類別定義；里程碑調整為 Dave（user 模組負責人）優先完成 C1–C4，供 Bob/Carol 銜接。
- `docs/security/threat-model.md`：Spoofing、Elevation of Privilege 兩項由「高風險/待補」
  更新為「已緩解」並附殘餘風險說明；新增「JWT 相關風險」威脅列；待辦技術債新增登入失敗次數
  限制、Refresh Token/登出黑名單、密碼複雜度規則等項目。

**Review**:
- 本次僅實作最簡登入機制（帳號+密碼），未包含 email 驗證、忘記密碼、第三方登入（OAuth）、
  Refresh Token、登出黑名單等進階功能，皆列為技術債，待後續 Sprint 依實際需求評估優先級。
- 若未來需要「多人共享書庫」或「家庭帳號」等群組協作情境，現行「一人一份資料完全隔離」的
  簡單模型需重新設計（例如加入 `BookShare`/群組概念），屬於本次範圍外的架構擴充點。
- 登入失敗次數限制與 JWT 密鑰輪替（Key Rotation）機制目前為技術債，正式上線前應優先評估
  導入時程，因為目前的 JWT 一旦簽發即無法主動撤銷，若密鑰外洩影響範圍較大。
