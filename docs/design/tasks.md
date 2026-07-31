# Tasks — 個人圖書管理系統實作計畫
> 產出者：Alice（架構師 / Architect Agent）  
> 版本：v1.0　|　日期：2026-07-31

---

## 模組 A：book（負責人 Bob，分支 `feature/book-management`）

- [ ] **A1** `Book` Entity + `BookRepository`  
  - 欄位：isbn(**nullable**), url(**nullable**), sourcePlatform(**nullable**), title, author, bookType(ENUM: PHYSICAL_BOOK/PHYSICAL_DOUJINSHI/EBOOK/WEB_NOVEL/BLOG_POST/ONLINE_FANFIC), categoryId(FK), deleted, createdAt, updatedAt  
  - DB CHECK 約束：實體/電子書類 `url IS NULL`；線上內容類 `isbn IS NULL AND url IS NOT NULL`  
  - 自訂查詢：`findByIsbnAndBookTypeAndDeletedFalse`（ISBN 去重）、`findByUrlAndDeletedFalse`（URL 去重）、`findAllByDeletedFalse`（含 keyword/tag/category/bookType 過濾）

- [ ] **A2** `BookService`（核心業務邏輯）  
  - `createBook`：依 `BookType.requiresIsbnDedup()` 決定是否執行 ISBN+bookType 去重（`PHYSICAL_BOOK`/`PHYSICAL_DOUJINSHI`，僅當 isbn 有值）；`bookType.isOnline()` 時執行 URL 去重（`DuplicateUrlException`）；`EBOOK` 不做任何去重  
  - `updateBook`：更新欄位，404 處理；ISBN/URL 變更時比照上述規則重新檢查；`categoryId` 不存在回 400（CategoryNotFoundException）  
  - `deleteBook`：僅軟刪除（`deleted = true`）、移除 Book_Tag 映射；**不觸碰**任何 `ReadingRecord`，也不呼叫/發布任何跨模組事件（reading 的閱讀記錄為獨立歷史資料，不隨書本刪除而變動）  
  - `searchBooks`：分頁搜尋（keyword、tag、category、bookType 可選）  
  - 實作 `BookQueryPort.existsBook(bookId)`

- [ ] **A6** `ValidBookType` 自訂 Bean Validation（class-level）  
  - 驗證 `BookRequest` 互斥規則：`bookType.isPhysicalOrEbook()` ⇒ `url` 必為 null，`isbn` 選填（若有值需符合 10/13 碼格式）；`bookType.isOnline()` ⇒ `isbn` 必為 null，`url` 必填且僅接受 `http`/`https` scheme  
  - 驗證失敗回 400，錯誤訊息清楚指出違反互斥規則（不洩漏內部細節）

- [ ] **A3** `BookController` + Bean Validation  
  - `BookRequest`（@NotBlank title/author、@NotNull bookType、`@ValidBookType` class-level 驗證）  
  - `BookResponse`（id, isbn, url, sourcePlatform, title, author, bookType, categoryId, tags, createdAt）  
  - 全域 `GlobalExceptionHandler`（404/409/400 統一 ErrorResponse 格式，含 CategoryInUseException/DuplicateUrlException→409）

- [ ] **A4** `TagService` / `CategoryService`（自訂 Tag 與分類目錄的 CRUD）  
  - Tag：建立、列表、刪除（刪除時一併移除 Book_Tag 映射）；書本加/移除 Tag  
  - Category：建立、列表、刪除（若仍有書本歸類則拋 `CategoryInUseException` → 409，不自動級聯刪書）

- [ ] **A5** BookService / Controller / TagService / CategoryService 單元測試 ≥ 80%  
  - 每個 public 方法 ≥ 3 個案例（Happy / Boundary / Error）  
  - 新增案例：更新書本 ISBN 重複（409）、categoryId 不存在（400）、分類刪除時仍有書本歸類（409）、刪除書本後其既有閱讀記錄仍可查詢（驗證 deleteBook 未觸及 ReadingRecord）、六種 bookType 各自新增 Happy Path、線上內容類 URL 重複（409）、線上內容類誤帶 isbn（400）、實體類誤帶 url（400）、EBOOK 重複 isbn 不觸發 409（驗證不去重）、同人誌無 isbn 新增成功且不觸發去重  
  - Mockito mock Repository；AssertJ 斷言  
  - 測試命名：`should_預期行為_When_條件`

---

## 模組 B：reading（負責人 Carol，分支 `feature/reading-record`）

- [ ] **B1** `ReadingRecord` Entity + `ReadingRecordRepository`  
  - 欄位：bookId(FK, **nullable**), source(ENUM: OWNED/BORROWED_FRIEND/BORROWED_LIBRARY), externalTitle(**nullable**), externalAuthor(**nullable**), readDate, durationMinutes(≥1), progressPercent(0-100), createdAt  
  - DB CHECK 約束：`(source='OWNED' AND bookId IS NOT NULL AND externalTitle IS NULL) OR (source<>'OWNED' AND bookId IS NULL AND externalTitle IS NOT NULL)`  
  - 自訂查詢：`findByBookId`（分頁，僅 OWNED）、`findBySource`（分頁，借閱記錄）、`findByReadDateBetween`（日曆用，不分來源）

- [ ] **B2** `ReadingService`（新增/更新閱讀記錄 + 閱讀時長累計）  
  - `addRecord`：`source=OWNED` 時先呼叫 `BookQueryPort.existsBook(bookId)`（不存在拋 404）；`source≠OWNED` 時**不呼叫** BookQueryPort，直接驗證 `externalTitle` 必填後儲存  
  - `updateRecord`：累加 durationMinutes、更新 progressPercent；`source`/`bookId`/`externalTitle`/`externalAuthor` 建立後不可修改  
  - `getRecordsByBook`：分頁查詢（僅 OWNED，依 bookId）  
  - `getRecordsBySource`：分頁查詢借閱記錄（依 source 篩選）  
  - `getStats`：總時長（SUM，不分來源）、已完成相異書籍數（`source=OWNED` 以 bookId 去重，`source≠OWNED` 以 `(source, externalTitle, externalAuthor)` 去重）

- [ ] **B3** `ReadingCalendarService`（依日期彙整閱讀時長）  
  - `getMonthlyCalendar(year, month)`：以 `readDate` group by（不分來源），彙整每日總分鐘  
  - 回傳 `List<CalendarResponse>`（date, totalMinutes）

- [ ] **B7** `ValidReadingSource` 自訂 Bean Validation（class-level）  
  - 驗證 `ReadingRequest` 互斥規則：`source=OWNED` ⇔ `bookId` 有值且 `externalTitle` 為 null；`source≠OWNED` ⇔ `bookId` 為 null 且 `externalTitle` 有值  
  - 驗證失敗回 400，錯誤訊息需清楚指出違反互斥規則（不洩漏內部細節）

- [ ] **B4** `ReadingController` + Bean Validation  
  - `ReadingRequest`（@NotNull source/readDate、@Min(1) durationMinutes、@Min(0)@Max(100) progressPercent、`@ValidReadingSource` class-level 驗證）  
  - `ReadingResponse`（新增 source/externalTitle/externalAuthor 欄位，bookId 於借閱記錄為 null）、`CalendarResponse`、`StatsResponse`  
  - 端點：POST /reading、PUT /reading/{id}、GET /reading?bookId=&source=&page=&size=、GET /reading/calendar、GET /reading/stats

- [ ] **B5** 閱讀時長計算、日期邊界測試 ≥ 80%  
  - 必含：跨日閱讀累計、時長為零邊界、書本不存在、progressPercent 邊界(0/100)、書本被軟刪除後既有閱讀記錄仍可查詢（不被刪除）  
  - 新增案例：`source=OWNED` 缺 bookId（400）、`source≠OWNED` 缺 externalTitle（400）、`source≠OWNED` 誤帶 bookId（400）、借閱記錄新增時不呼叫 BookQueryPort（Mockito verify 0 次呼叫）、統計功能正確去重借閱書籍  
  - Mock `BookQueryPort`（測試不依賴 book 模組實作）

---

## 介面契約（Alice 先定，Bob/Carol 各自對著實作）

```java
// 位置：book/port/BookQueryPort.java（reading → book，同步查詢）
public interface BookQueryPort {
    boolean existsBook(Long bookId);
}
// BookService implements BookQueryPort（book 模組提供實作）
// ReadingService 注入 BookQueryPort（reading 模組消費，僅 source=OWNED 時呼叫）
// 書本刪除僅為狀態變更（軟刪除），不觸發任何跨模組呼叫或事件；
// 閱讀記錄為獨立歷史資料，禁止任一模組直接注入或呼叫對方的 Repository / Entity
// 借閱書籍（source≠OWNED）完全不進入 book 模組，reading 僅本地儲存 externalTitle/externalAuthor
```

---

## 里程碑

| 時間點 | 目標 |
|--------|------|
| Sprint 0 完成 | 本文件 + design.md + requirements.md commit 到 develop |
| Sprint 1 中 | Bob、Carol 分別完成 A1–A4、B1–B4 並 push feature 分支 |
| Sprint 1 末 | A5/B5 測試完成；開 PR → Alice Review → merge develop |
| 發布 | develop → main PR；`v1.0.0` tag；CHANGELOG 產出 |
