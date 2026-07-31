# ADR-0004: 擴充藏書類型（BookType），支援無 ISBN 的實體/線上內容

## Decision - 2026-07-31
**Decision**: `Book.bookType` 由原本二選一（`PHYSICAL`/`EBOOK`）擴充為六種類型：
`PHYSICAL_BOOK`（實體書籍）、`PHYSICAL_DOUJINSHI`（實體同人誌）、`EBOOK`（電子書）、
`WEB_NOVEL`（網路小說）、`BLOG_POST`（Blog 文章）、`ONLINE_FANFIC`（同人文網站，如 AO3）。
`Book.isbn` 改為 **nullable**；新增 `Book.url`（線上內容來源網址）與 `Book.sourcePlatform`
（來源平台名稱，選填）。以 `bookType` 分兩大類決定欄位互斥規則與去重邏輯：
- **實體／電子書類**（`PHYSICAL_BOOK`/`PHYSICAL_DOUJINSHI`/`EBOOK`）：`url` 必為 null，`isbn` 選填。
- **線上內容類**（`WEB_NOVEL`/`BLOG_POST`/`ONLINE_FANFIC`）：`isbn` 必為 null，`url` 必填（限 `http`/`https`）。

**Context**:
- 使用者需求：「藏書的類別，除了具有ISBN的實體書籍外，還有其他可能：二創同人誌(紙本實體)、
  網路小說、Blog文章、AO3網站等等」。
- 原設計假設所有藏書皆為「有 ISBN 的實體書」或「電子書」，`isbn` 為 NOT NULL 且做格式驗證，
  這與「同人誌通常沒有 ISBN」「網路小說/Blog/AO3 是純線上內容，沒有 ISBN 也沒有實體」的事實不符。
- 若不調整，使用者將無法把同人誌、網路小說、Blog 文章、AO3 作品登記進系統，或被迫填入假造的
  ISBN 資料，違反資料真實性。

**Options**:
1. **保留單一 ISBN 欄位，同人誌/網路小說等類型允許 ISBN 空白，但沒有對應的「來源網址」欄位**
   → 無法記錄線上內容的來源連結，日後想回頭查找原文會找不到，實用性不足。
2. **新增 `url`/`sourcePlatform` 欄位，並以 `bookType` 分類決定 `isbn`/`url` 互斥使用**
   → 同時支援「無 ISBN 的實體出版品」與「純線上內容」兩種情境，且保留可回溯的原始連結，
   資料模型清楚反映每種類型的真實特性。
3. **將「實體藏書」與「線上內容」拆成兩個完全獨立的 Entity（如 `Book` 和 `OnlineContent`）**
   → 對 reading 模組而言會需要處理兩種不同的 `bookId` 來源，增加 `BookQueryPort` 與
   `ReadingRecord.bookId` 的複雜度，且目前規模不需要此程度的拆分。

**Rationale**:
選擇選項 2。單一 `Book` Entity 搭配 `bookType` 分類與欄位互斥規則，是複雜度與彈性的最佳平衡：
- reading 模組完全不受影響——`ReadingRecord.bookId` 仍然只需指向一個 `Book.id`，
  不論該書是實體書、同人誌、網路小說或 AO3 作品，`BookQueryPort.existsBook()` 邏輯不變。
- 以 DB CHECK 約束 + `@ValidBookType` Bean Validation 雙重把關互斥規則，避免無意義的資料狀態
  （例如一本網路小說卻填了 ISBN）。
- 去重邏輯依類型調整：實體類（`PHYSICAL_BOOK`/`PHYSICAL_DOUJINSHI`）比對 ISBN 防止重複購買
  （沿用 US-B02 原意）；線上內容類改比對 `url` 防止重複收藏同一個連結；`EBOOK` 維持不去重
  （允許從不同平台購買同一本電子書）。
- `url` 欄位限制 scheme 僅 `http`/`https`，並記錄於威脅模型中，降低惡意連結（如 `javascript:`）
  被儲存並在未來前端渲染時造成 Stored XSS 的風險。

**Impact**:
- `requirements.md`：新增 BookType 分類說明（實體/電子書類 vs 線上內容類）；US-B01/B02/B03/B05 更新。
- `design.md`：Book 資料模型新增 `url`/`sourcePlatform` 欄位與互斥 CHECK 約束；新增 §5a
  說明 `BookType` enum 的分類方法（`isPhysicalOrEbook()`/`isOnline()`/`requiresIsbnDedup()`）
  與 `BookService` 去重邏輯；API 新增 `bookType` 查詢參數；套件結構新增
  `book/validation/ValidBookType.java`、`book/exception/DuplicateUrlException.java`。
- `tasks.md`：A1（Entity/CHECK約束/查詢方法）、A2（依類型分支的去重邏輯）、A3（Request/Response
  欄位、GlobalExceptionHandler 新增 DuplicateUrlException）、新增 A6（`ValidBookType` 驗證器）、
  A5（新增六種類型的測試案例、URL 去重、互斥驗證失敗等案例）。
- `docs/security/threat-model.md`：新增「惡意 URL」威脅項目，說明 scheme 限制與前端渲染的
  HTML escape 要求。

**Review**:
若未來需要對線上內容進行「自動擷取更新」（例如網路小說連載更新提醒），代表系統需要主動存取
外部 URL，屆時應重新評估 SSRF 風險並設計適當的白名單/沙箱機制，此為目前範圍外的技術債。
