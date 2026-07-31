# Tasks — 個人圖書管理系統實作計畫
> 產出者：Alice（架構師 / Architect Agent）  
> 版本：v1.0　|　日期：2026-07-31

---

## 模組 A：book（負責人 Bob，分支 `feature/book-management`）

- [ ] **A1** `Book` Entity + `BookRepository`  
  - 欄位：isbn, title, author, bookType(ENUM), categoryId(FK), deleted, createdAt, updatedAt  
  - 自訂查詢：`findByIsbnAndBookTypeAndDeletedFalse`、`findAllByDeletedFalse`（含 keyword/tag/category 過濾）

- [ ] **A2** `BookService`（核心業務邏輯）  
  - `createBook`：ISBN + PHYSICAL 去重（DuplicateIsbnException）  
  - `updateBook`：更新欄位，404 處理；ISBN 變更時同樣檢查 PHYSICAL 去重（409）；`categoryId` 不存在回 400（CategoryNotFoundException）  
  - `deleteBook`：軟刪除（`deleted = true`）、移除 Book_Tag 映射、發布 `BookDeletedEvent(bookId)`（`ApplicationEventPublisher`）  
  - `searchBooks`：分頁搜尋（keyword、tag、category 可選）  
  - 實作 `BookQueryPort.existsBook(bookId)`

- [ ] **A3** `BookController` + Bean Validation  
  - `BookRequest`（@NotBlank title/author、@Pattern ISBN、@NotNull bookType）  
  - `BookResponse`（id, isbn, title, author, bookType, categoryId, tags, createdAt）  
  - 全域 `GlobalExceptionHandler`（404/409/400 統一 ErrorResponse 格式，含 CategoryInUseException→409）

- [ ] **A4** `TagService` / `CategoryService`（自訂 Tag 與分類目錄的 CRUD）  
  - Tag：建立、列表、刪除（刪除時一併移除 Book_Tag 映射）；書本加/移除 Tag  
  - Category：建立、列表、刪除（若仍有書本歸類則拋 `CategoryInUseException` → 409，不自動級聯刪書）

- [ ] **A6** `BookDeletedEvent`（book/event 套件）  
  - 定義 `record BookDeletedEvent(Long bookId)`；由 `BookService.deleteBook()` 於同交易內發布  
  - 單元測試驗證事件確實被發布（`ApplicationEvents` / Mockito 驗證 publisher 呼叫）

- [ ] **A5** BookService / Controller / TagService / CategoryService 單元測試 ≥ 80%  
  - 每個 public 方法 ≥ 3 個案例（Happy / Boundary / Error）  
  - 新增案例：更新書本 ISBN 重複（409）、categoryId 不存在（400）、分類刪除時仍有書本歸類（409）、刪除書本後事件內容正確（bookId 相符）  
  - Mockito mock Repository；AssertJ 斷言  
  - 測試命名：`should_預期行為_When_條件`

---

## 模組 B：reading（負責人 Carol，分支 `feature/reading-record`）

- [ ] **B1** `ReadingRecord` Entity + `ReadingRecordRepository`  
  - 欄位：bookId(FK), readDate, durationMinutes(≥1), progressPercent(0-100), createdAt  
  - 自訂查詢：`findByBookId`（分頁）、`findByReadDateBetween`（日曆用）

- [ ] **B2** `ReadingService`（新增/更新閱讀記錄 + 閱讀時長累計）  
  - `addRecord`：先呼叫 `BookQueryPort.existsBook(bookId)`；書本不存在拋 404  
  - `updateRecord`：累加 durationMinutes、更新 progressPercent  
  - `getRecordsByBook`：分頁查詢  
  - `getStats`：總時長（SUM）、已完成書籍數（progressPercent = 100）

- [ ] **B3** `ReadingCalendarService`（依日期彙整閱讀時長）  
  - `getMonthlyCalendar(year, month)`：以 `readDate` group by，彙整每日總分鐘  
  - 回傳 `List<CalendarResponse>`（date, totalMinutes）

- [ ] **B6** `BookDeletedEventListener`（reading/listener 套件）  
  - `@EventListener` 監聽 `book.event.BookDeletedEvent`  
  - 呼叫 `readingRecordRepository.deleteAllByBookId(event.bookId())`（同交易，確保與書本軟刪除具原子性）  
  - 僅依賴 book 模組公開的 `BookDeletedEvent`，禁止 import book 模組其他類別  
  - 單元測試：事件觸發後對應 bookId 的所有記錄被刪除；無記錄時不拋例外

- [ ] **B4** `ReadingController` + Bean Validation  
  - `ReadingRequest`（@NotNull bookId/readDate、@Min(1) durationMinutes、@Min(0)@Max(100) progressPercent）  
  - `ReadingResponse`、`CalendarResponse`、`StatsResponse`  
  - 端點：POST /reading、PUT /reading/{id}、GET /reading、GET /reading/calendar、GET /reading/stats

- [ ] **B5** 閱讀時長計算、日期邊界測試 ≥ 80%  
  - 必含：跨日閱讀累計、時長為零邊界、書本不存在、progressPercent 邊界(0/100)、BookDeletedEvent 觸發清除  
  - Mock `BookQueryPort`（測試不依賴 book 模組實作）

---

## 介面契約（Alice 先定，Bob/Carol 各自對著實作）

```java
// 位置：book/port/BookQueryPort.java（reading → book，同步查詢）
public interface BookQueryPort {
    boolean existsBook(Long bookId);
}
// BookService implements BookQueryPort（book 模組提供實作）
// ReadingService 注入 BookQueryPort（reading 模組消費）

// 位置：book/event/BookDeletedEvent.java（book → reading，事件通知）
public record BookDeletedEvent(Long bookId) {}
// BookService.deleteBook() 發布事件；reading 模組的 BookDeletedEventListener 監聽並清除記錄
// 兩個方向皆不得讓任一模組直接注入或呼叫對方的 Repository / Entity
```

---

## 里程碑

| 時間點 | 目標 |
|--------|------|
| Sprint 0 完成 | 本文件 + design.md + requirements.md commit 到 develop |
| Sprint 1 中 | Bob、Carol 分別完成 A1–A4、B1–B4 並 push feature 分支 |
| Sprint 1 末 | A5/B5 測試完成；開 PR → Alice Review → merge develop |
| 發布 | develop → main PR；`v1.0.0` tag；CHANGELOG 產出 |
