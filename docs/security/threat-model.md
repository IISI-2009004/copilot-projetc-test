# Threat Model — 個人圖書管理系統（STRIDE）
> 產出者：Alice（架構師 / Architect Agent）
> 版本：v1.0　|　日期：2026-07-31
> 對應 design.md §6 摘要之完整版本

---

## 1. 系統邊界與資產

- **資產**：書本資料（Book/Tag/Category）、閱讀記錄（ReadingRecord）。
- **信任邊界**：
  1. 用戶端 ↔ REST API（`/api/books`, `/api/tags`, `/api/categories`, `/api/reading`）
  2. book 模組 ↔ reading 模組（模組間邊界；僅透過 `BookQueryPort` 單向查詢，書本刪除為狀態變更，不跨模組通知）
  3. 應用程式 ↔ 資料庫（H2 / PostgreSQL）

---

## 2. STRIDE 分析

| 威脅類型 | 攻擊面 | 情境 | 緩解措施 | 殘餘風險 |
|---------|--------|------|---------|---------|
| **S**poofing | REST API | 目前無身分驗證，任何呼叫端可視為合法用戶 | 本 Sprint 為單用戶個人系統，暫不做驗證；**後續 Sprint** 需加入 Spring Security + JWT | 高（部署到多用戶/公網環境前必須補上）|
| **T**ampering | Book / ReadingRecord 更新 API | 惡意或錯誤輸入竄改資料 | Bean Validation（@NotBlank/@Pattern/@Min/@Max）；JPA 參數化查詢/JPQL，禁止字串拼接 SQL | 低 |
| **R**epudiation | 書本被刪除後，過去的閱讀行為紀錄真實性遭質疑 | 書本刪除僅為軟刪除狀態變更，不影響、不刪除任何 `ReadingRecord`；歷史資料完整保留可供追溯 | 低 |
| **I**nformation Disclosure | 例外訊息回傳前端 | Stack trace 或 SQL 錯誤細節外洩，暴露內部結構 | `GlobalExceptionHandler` 統一轉換為 `ErrorResponse`（code, message, timestamp），不含 stack trace；SLF4J 記錄詳細錯誤僅於伺服器端 log | 低 |
| **D**enial of Service | 搜尋 API 無上限查詢 / 大量分頁請求 | 惡意大量請求造成資料庫負載 | 分頁預設 20 筆並限制 `size` 上限（建議 ≤ 100）；後續可加 Rate Limiting（本 Sprint 未實作）| 中（Rate Limiting 為技術債，見 tasks.md 待辦）|
| **E**levation of Privilege | 未來多用戶情境下操作他人書本/閱讀記錄 | 缺乏 userId 隔離，任何請求可操作任意 bookId | 本 Sprint 先建立分層架構與模組邊界；**後續 Sprint** 於 Book/ReadingRecord 加入 `userId` 欄位並於 Service 層強制過濾 | 高（多用戶上線前必須補上）|
| **Injection** | ISBN / 關鍵字搜尋參數 | SQL Injection / JPQL Injection | 一律使用 Spring Data JPA 衍生查詢或 `@Query` 具名參數（`:param`），禁止字串拼接 | 低 |

---

## 3. 待辦技術債（安全相關）

| 項目 | 優先級 | 說明 |
|------|--------|------|
| 加入身分驗證（JWT） | 高 | 目前所有 API 無驗證，僅適合單機/個人使用情境 |
| userId 資料隔離 | 高 | Book / ReadingRecord 需加 `userId`，避免多用戶環境下的越權存取 |
| API Rate Limiting | 中 | 防止搜尋 API 被濫用造成 DoS |
| 分頁 `size` 上限校驗 | 中 | Bean Validation 加 `@Max(100)` 於 `size` 參數 |

---

## 4. 審查紀錄

| 日期 | 審查者 | 結論 |
|------|--------|------|
| 2026-07-31 | Alice（架構師） | 初版威脅模型建立 |
| 2026-07-31 | Alice（架構師） | 依用戶決策修正：書本刪除改為純狀態變更，不刪除閱讀記錄，移除跨模組事件相關威脅項；詳見 ADR-0001（已標記 Superseded）|
