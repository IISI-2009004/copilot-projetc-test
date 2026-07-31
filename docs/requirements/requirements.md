# Requirements — 個人圖書管理系統
> 產出者：Alice（架構師 / Planner Agent）  
> 版本：v1.0　|　日期：2026-07-31

---

## 1. 系統概述

個人圖書管理系統（Personal Book Manager）供個人用戶管理藏書與閱讀記錄，  
防止重複購買、協助整理分類、並提供閱讀進度可視化。

---

## 2. 用戶故事（EARS 格式）

### 模組 A — 書本管理（book）

| ID | 用戶故事 | EARS 需求 | 驗收標準 |
|----|----------|-----------|---------|
| US-B01 | 作為用戶，我想新增書本（含 ISBN、書名、作者、類型），以便建立我的書庫 | WHEN 用戶提交新增書本表單 THE SYSTEM SHALL 儲存書本資訊並回傳 201 | ISBN 格式驗證（10/13碼）；書名、作者為必填 |
| US-B02 | 作為用戶，我想防止重複購買，系統應於新增前檢查 ISBN 是否已存在 | WHEN 用戶新增相同 ISBN 的實體書 THE SYSTEM SHALL 回傳 409 Conflict | 僅對 PHYSICAL 類型執行去重；EBOOK 允許多筆 |
| US-B03 | 作為用戶，我想編輯書本資訊（書名、作者、類型、Tag、分類） | WHEN 用戶更新書本資訊 THE SYSTEM SHALL 驗證並持久化更新後內容 | 更新不存在的書本回傳 404；WHEN 更新後 ISBN 與其他未刪除 PHYSICAL 書本重複 THE SYSTEM SHALL 回傳 409；WHEN `categoryId` 不存在 THE SYSTEM SHALL 回傳 400 |
| US-B04 | 作為用戶，我想刪除書本 | WHEN 用戶刪除書本 THE SYSTEM SHALL（1）軟刪除書本、（2）移除該書本的 Tag 映射、（3）發布 `BookDeletedEvent` 由 reading 模組非同步/同交易監聽並清除關聯閱讀記錄 | 軟刪除；刪除後查詢回 404；reading 模組**不得**被 book 模組直接呼叫刪除，僅透過事件通知（見 §4 模組邊界契約） |
| US-B05 | 作為用戶，我想用關鍵字（書名/作者）、Tag、分類搜尋書本 | WHEN 用戶送出搜尋條件 THE SYSTEM SHALL 回傳符合條件的書本列表（分頁） | 支援空條件（回傳全部）；分頁預設 20 筆 |
| US-B06 | 作為用戶，我想建立自訂 Tag 並套用到書本 | THE SYSTEM SHALL 提供 Tag CRUD；WHEN Tag 套用到書本 THE SYSTEM SHALL 建立多對多映射 | Tag 名稱全域唯一（同一用戶）；WHEN Tag 被刪除 THE SYSTEM SHALL 一併移除其與所有書本的映射 |
| US-B07 | 作為用戶，我想建立分類目錄並將書本歸類 | THE SYSTEM SHALL 提供 Category CRUD；書本可屬於一個 Category | Category 名稱唯一；WHEN 分類仍有書本歸類時嘗試刪除 THE SYSTEM SHALL 回傳 409（阻擋刪除，需用戶先將書本移出或改分類，不自動級聯刪除書本）|

### 模組 B — 閱讀記錄（reading）

| ID | 用戶故事 | EARS 需求 | 驗收標準 |
|----|----------|-----------|---------|
| US-R01 | 作為用戶，我想新增某本書的閱讀記錄（開始日期、閱讀時長、進度%） | WHEN 用戶新增閱讀記錄 THE SYSTEM SHALL 先確認書本存在（BookQueryPort），再儲存 | 書本不存在回 404；時長必須 > 0 |
| US-R02 | 作為用戶，我想更新閱讀記錄（繼續計時、更新進度） | WHEN 用戶更新閱讀記錄 THE SYSTEM SHALL 累加時長並更新進度百分比 | 進度 0-100；時長累計不可負數 |
| US-R03 | 作為用戶，我想查看某本書的完整閱讀歷史 | WHEN 用戶查詢某 bookId 的閱讀記錄 THE SYSTEM SHALL 按日期回傳所有閱讀紀錄 | 支援分頁；無記錄回空陣列 |
| US-R04 | 作為用戶，我想查看閱讀日曆（每天閱讀幾分鐘） | WHEN 用戶查詢月份閱讀日曆 THE SYSTEM SHALL 以每日彙整閱讀時長（分鐘）回傳 | 無記錄的日期不出現；跨日閱讀分別計入對應日期 |
| US-R05 | 作為用戶，我想看整體閱讀統計（總時長、已完成書籍數） | THE SYSTEM SHALL 計算所有書本累計閱讀時長與進度 = 100% 的書籍數 | 即時計算；效能 < 500ms |
| US-R06 | 作為系統，當書本被刪除時，我想自動清除該書的閱讀記錄，避免孤兒資料 | WHEN reading 模組接收到 `BookDeletedEvent` THE SYSTEM SHALL 刪除該 `bookId` 底下所有閱讀記錄 | 事件監聽與書本軟刪除在同一交易內完成（同步、交易一致）；監聽失敗需記錄錯誤日誌並可重試（不可靜默丟失）|

---

## 3. 非功能需求

| 類別 | 需求 |
|------|------|
| 效能 | API 回應 p95 < 500ms（本機 H2 環境） |
| 安全 | 所有輸入 Bean Validation；JPA 參數化查詢；錯誤訊息不洩漏 stack trace |
| 可測試性 | 單元測試覆蓋率 ≥ 80%；Service 層以 Mockito 隔離 Repository |
| 可維護性 | 分層架構 Controller→Service→Repository；模組邊界由 BookQueryPort 介面隔離 |
| 相容性 | 開發環境 H2（in-memory）；正式環境 PostgreSQL（Spring profile 切換） |

---

## 4. 模組邊界契約

```
方向一：reading → book（查詢，同步呼叫）
  book 模組 → 對外介面：BookQueryPort
    boolean existsBook(Long bookId)
  reading 模組 → 依賴 BookQueryPort（透過 Spring Bean 注入）
  禁止直接存取 BookRepository

方向二：book → reading（通知，事件驅動，非直接呼叫）
  book 模組刪除書本時發布：BookDeletedEvent(bookId)
  reading 模組以 @EventListener（同交易）監聽，刪除該 bookId 之閱讀記錄
  禁止 book 模組直接存取 ReadingRecordRepository
```

> 決策依據：US-B04 要求刪除書本時一併清除閱讀記錄，但模組邊界規定禁止跨模組直接存取對方 Repository。
> 若無事件機制，兩者互相矛盾。改採單向事件通知（book 為發布方、reading 為訂閱方），
> 維持模組邊界單向依賴（reading 依賴 book，book 不反向依賴 reading 的實作類別），僅透過 Spring `ApplicationEventPublisher`/`ApplicationListener` 的事件物件耦合。
