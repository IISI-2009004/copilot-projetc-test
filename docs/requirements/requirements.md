# Requirements — 個人圖書管理系統
> 產出者：Alice（架構師 / Planner Agent）  
> 版本：v1.0　|　日期：2026-07-31

---

## 1. 系統概述

個人圖書管理系統（Personal Book Manager）供個人用戶管理藏書與閱讀記錄，  
防止重複購買、協助整理分類、並提供閱讀進度可視化。閱讀記錄不限於個人藏書，  
亦可記錄向朋友或圖書館借閱的書籍。

---

## 2. 用戶故事（EARS 格式）

### 模組 A — 書本管理（book）

> **書本類型（BookType）**：藏書不限於有 ISBN 的實體出版品，涵蓋以下六種類型：
> - `PHYSICAL_BOOK`　實體書籍（一般出版品，通常有 ISBN）
> - `PHYSICAL_DOUJINSHI`　實體同人誌（紙本二創作品，通常**無** ISBN）
> - `EBOOK`　電子書（可能有 ISBN）
> - `WEB_NOVEL`　網路小說（連載平台文章，如巴哈姆特、Wattpad）
> - `BLOG_POST`　Blog 文章
> - `ONLINE_FANFIC`　同人文網站作品（如 AO3）
>
> 前三者（`PHYSICAL_BOOK`/`PHYSICAL_DOUJINSHI`/`EBOOK`）為「實體／電子書」類，`isbn` 為**選填**
> （非所有實體書皆有 ISBN，例如同人誌）；後三者（`WEB_NOVEL`/`BLOG_POST`/`ONLINE_FANFIC`）為
> 「線上內容」類，改以 `url`（來源網址，必填）與 `sourcePlatform`（來源平台名稱，選填，如
> "AO3"、"Wattpad"、"巴哈姆特"）記錄；`isbn` 與 `url` 為互斥欄位，不可同時提供。

| ID | 用戶故事 | EARS 需求 | 驗收標準 |
|----|----------|-----------|---------|
| US-B01 | 作為用戶，我想新增各種類型的藏書（實體書、同人誌、電子書、網路小說、Blog文章、AO3同人文等），以便建立我的書庫 | WHEN 用戶提交新增書本表單 THE SYSTEM SHALL 依 `bookType` 儲存對應欄位並回傳 201 | 書名、作者、`bookType` 為必填；`bookType` 屬「實體／電子書」類時 `isbn` 選填（格式驗證 10/13碼，若有提供）且 `url` 必為空；`bookType` 屬「線上內容」類時 `url` 必填（合法 URL 格式）且 `isbn` 必為空 |
| US-B02 | 作為用戶，我想避免重複建立同一本實體書或同一個線上連結 | WHEN 新增 `bookType ∈ {PHYSICAL_BOOK, PHYSICAL_DOUJINSHI}` 且 `isbn` 已存在於相同類型的未刪除書本 THE SYSTEM SHALL 回傳 409；WHEN 新增 `bookType ∈ {WEB_NOVEL, BLOG_POST, ONLINE_FANFIC}` 且 `url` 已存在於未刪除書本 THE SYSTEM SHALL 回傳 409 | `EBOOK` 不做去重（可能從不同平台購買同一本電子書）；未提供 `isbn` 的實體書/同人誌不觸發去重檢查（無法比對）|
| US-B03 | 作為用戶，我想編輯書本資訊（書名、作者、類型、Tag、分類、ISBN/URL） | WHEN 用戶更新書本資訊 THE SYSTEM SHALL 驗證並持久化更新後內容 | 更新不存在的書本回傳 404；`isbn`/`url` 互斥規則與 US-B01 相同；WHEN 更新後 `isbn` 與其他未刪除同類型書本重複（限 PHYSICAL_BOOK/PHYSICAL_DOUJINSHI）THE SYSTEM SHALL 回傳 409；WHEN 更新後 `url` 與其他未刪除書本重複（線上內容類）THE SYSTEM SHALL 回傳 409；WHEN `categoryId` 不存在 THE SYSTEM SHALL 回傳 400 |
| US-B04 | 作為用戶，我想刪除書本 | WHEN 用戶刪除書本 THE SYSTEM SHALL 僅將該書本狀態標記為已刪除（`deleted = true`）並移除其 Tag 映射，**不刪除**任何關聯的閱讀記錄 | 軟刪除；刪除後查詢該書本回 404；既有閱讀記錄不受影響、仍完整保留於資料庫（歷史資料），可繼續被查詢（US-R03/US-R04/US-R05）；書本被刪除後 `BookQueryPort.existsBook()` 回傳 false，故**新增**閱讀記錄會被拒絕（404，見 US-R01），但**既有**記錄不受此限制 |
| US-B05 | 作為用戶，我想用關鍵字（書名/作者）、Tag、分類、書本類型搜尋書本 | WHEN 用戶送出搜尋條件 THE SYSTEM SHALL 回傳符合條件的書本列表（分頁） | 支援空條件（回傳全部）；分頁預設 20 筆；支援 `bookType` 篩選（例如只看網路小說或同人誌）|
| US-B06 | 作為用戶，我想建立自訂 Tag 並套用到書本 | THE SYSTEM SHALL 提供 Tag CRUD；WHEN Tag 套用到書本 THE SYSTEM SHALL 建立多對多映射 | Tag 名稱全域唯一（同一用戶）；WHEN Tag 被刪除 THE SYSTEM SHALL 一併移除其與所有書本的映射 |
| US-B07 | 作為用戶，我想建立分類目錄並將書本歸類 | THE SYSTEM SHALL 提供 Category CRUD；書本可屬於一個 Category | Category 名稱唯一；WHEN 分類仍有書本歸類時嘗試刪除 THE SYSTEM SHALL 回傳 409（阻擋刪除，需用戶先將書本移出或改分類，不自動級聯刪除書本）|

### 模組 B — 閱讀記錄（reading）

> **書本來源（ReadingSource）**：閱讀記錄不限於個人藏書。每筆閱讀記錄需標記來源：
> - `OWNED`　自己的藏書（對應 book 模組的 `bookId`）
> - `BORROWED_FRIEND`　向朋友借閱（無 `bookId`，僅記錄書名/作者文字）
> - `BORROWED_LIBRARY`　向圖書館借閱（無 `bookId`，僅記錄書名/作者文字）
>
> `source = OWNED` 時必須提供 `bookId`（且不可同時提供 `externalTitle`）；
> `source = BORROWED_FRIEND` 或 `BORROWED_LIBRARY` 時必須提供 `externalTitle`（書名，必填）、
> `externalAuthor`（作者，選填），且不可提供 `bookId`。此為互斥規則，本 Sprint 暫不做借閱對象、
> 應歸還日期、歸還狀態等借閱管理功能。

| ID | 用戶故事 | EARS 需求 | 驗收標準 |
|----|----------|-----------|---------|
| US-R01 | 作為用戶，我想新增閱讀記錄（自己的藏書、或向朋友/圖書館借閱的書），記錄開始日期、閱讀時長、進度% | WHEN 用戶新增閱讀記錄且 `source = OWNED` THE SYSTEM SHALL 先呼叫 BookQueryPort 確認 `bookId` 存在，再儲存；WHEN `source` 為 `BORROWED_FRIEND` 或 `BORROWED_LIBRARY` THE SYSTEM SHALL 直接儲存 `externalTitle`/`externalAuthor`，不查詢 book 模組 | `source=OWNED` 但書本不存在回 404；`source≠OWNED` 但缺少 `externalTitle` 回 400；`bookId` 與 `externalTitle` 同時提供或同時缺漏回 400（互斥驗證失敗）；時長必須 > 0 |
| US-R02 | 作為用戶，我想更新閱讀記錄（繼續計時、更新進度） | WHEN 用戶更新閱讀記錄 THE SYSTEM SHALL 累加時長並更新進度百分比 | 進度 0-100；時長累計不可負數；`source`/`bookId`/`externalTitle` 等來源資訊建立後不可修改（如需修正應刪除重建）|
| US-R03 | 作為用戶，我想查看某本書（含借閱的書）的完整閱讀歷史 | WHEN 用戶查詢某 `bookId`（自己藏書）或以 `source`+關鍵字（借閱書）查詢 THE SYSTEM SHALL 按日期回傳所有符合條件的閱讀紀錄 | 支援分頁；無記錄回空陣列；可依 `source` 篩選 |
| US-R04 | 作為用戶，我想查看閱讀日曆（每天閱讀幾分鐘） | WHEN 用戶查詢月份閱讀日曆 THE SYSTEM SHALL 以每日彙整閱讀時長（分鐘）回傳，**不分來源、涵蓋自己藏書與借閱書籍** | 無記錄的日期不出現；跨日閱讀分別計入對應日期 |
| US-R05 | 作為用戶，我想看整體閱讀統計（總時長、已完成書籍數） | THE SYSTEM SHALL 計算所有閱讀記錄（不分來源）之累計閱讀時長，以及進度 = 100% 的相異書本數 | 即時計算；效能 < 500ms；統計計算**不排除**已被軟刪除書本的歷史閱讀記錄（保留完整歷史）；「相異書本」之判斷：`source=OWNED` 以 `bookId` 為身分識別，`source≠OWNED` 以 `(source, externalTitle, externalAuthor)` 為身分識別 |

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
reading → book（查詢，同步呼叫）
  book 模組 → 對外介面：BookQueryPort
    boolean existsBook(Long bookId)
  reading 模組 → 依賴 BookQueryPort（透過 Spring Bean 注入）
  禁止直接存取 BookRepository
```

> 書本刪除採軟刪除（僅變更 `deleted` 狀態），不物理刪除資料、也不觸發任何跨模組刪除動作，
> 因此 book 與 reading 之間**僅存在單一方向**的耦合（reading 依賴 book 的 `BookQueryPort` 查詢書本是否存在／未刪除），
> 不需要事件通知或反向依賴，模組邊界維持單純的單向依賴關係。閱讀記錄一旦建立即為獨立的歷史資料，
> 其生命週期不與書本的刪除狀態綁定。
>
> **借閱書籍（`source ≠ OWNED`）完全不呼叫 `BookQueryPort`**：向朋友或圖書館借閱的書不屬於個人藏書，
> book 模組對其毫無所知，reading 模組僅在本地儲存 `externalTitle`/`externalAuthor` 文字欄位，
> 不建立、不查詢 book 模組的任何資料。僅當 `source = OWNED` 時才需驗證 `bookId` 是否存在。
