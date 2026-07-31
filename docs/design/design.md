# Design — 個人圖書管理系統
> 產出者：Alice（架構師 / Architect Agent）  
> 版本：v1.0　|　日期：2026-07-31

---

## 1. 系統架構

```
┌─────────────────────────────────────────────────────┐
│                  Spring Boot 3.3                    │
│                                                     │
│  ┌──────────────────┐   ┌──────────────────────┐   │
│  │   book 模組       │   │   reading 模組         │   │
│  │                  │   │                      │   │
│  │  BookController  │   │  ReadingController   │   │
│  │  BookService     │   │  ReadingService      │   │
│  │  TagService      │   │  ReadingCalendar-    │   │
│  │  CategoryService │   │    Service           │   │
│  │  BookRepository  │   │  ReadingRepository   │   │
│  │                  │   │                      │   │
│  │ «interface»      │───►  BookQueryPort        │   │
│  │ BookQueryPort    │   │  (依賴注入，同步查詢)   │   │
│  └──────────────────┘   └──────────────────────┘   │
│            │                       │               │
│            └───────┬───────────────┘               │
│                    ▼                               │
│           Spring Data JPA                          │
│                    │                               │
│          H2 (dev) / PostgreSQL (prod)              │
└─────────────────────────────────────────────────────┘
```

> 兩模組間僅存在單一方向的耦合：reading→book 的同步介面查詢（`BookQueryPort`）。
> 書本刪除為單純的狀態變更（軟刪除），**不會**觸發任何跨模組動作，reading 模組的
> 閱讀記錄生命週期與書本刪除狀態完全independent；book 不允許反向依賴或存取
> reading 模組的 Repository / Entity。

---

## 2. 套件結構

```
src/main/java/com/iisi/bookmanager/
├── book/
│   ├── controller/    BookController.java
│   ├── service/       BookService.java, TagService.java, CategoryService.java
│   ├── repository/    BookRepository.java, TagRepository.java, CategoryRepository.java
│   ├── domain/        Book.java, Tag.java, Category.java
│   ├── dto/           BookRequest.java, BookResponse.java
│   ├── port/          BookQueryPort.java          ← 對外介面
│   └── exception/     BookNotFoundException.java, DuplicateIsbnException.java,
│                      CategoryInUseException.java, CategoryNotFoundException.java
├── reading/
│   ├── controller/    ReadingController.java
│   ├── service/       ReadingService.java, ReadingCalendarService.java
│   ├── repository/    ReadingRecordRepository.java
│   ├── domain/        ReadingRecord.java
│   ├── dto/           ReadingRequest.java, ReadingResponse.java, CalendarResponse.java
│   └── exception/     ReadingRecordNotFoundException.java
└── common/
    ├── exception/     GlobalExceptionHandler.java, ErrorResponse.java
    └── config/        JpaConfig.java
```

---

## 3. 資料模型

### Book
| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK, auto) | 主鍵 |
| isbn | VARCHAR(13) | ISBN 碼（10 或 13 位）|
| title | VARCHAR(200) NOT NULL | 書名 |
| author | VARCHAR(100) NOT NULL | 作者 |
| bookType | ENUM(PHYSICAL, EBOOK) | 書本類型 |
| categoryId | Long (FK) | 分類 ID（可為 null）|
| deleted | BOOLEAN DEFAULT false | 軟刪除旗標 |
| createdAt | TIMESTAMP | 建立時間 |
| updatedAt | TIMESTAMP | 更新時間 |

### Tag / Book_Tag（多對多）
| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK) | Tag 主鍵 |
| name | VARCHAR(50) UNIQUE | Tag 名稱 |

### Category
| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK) | 主鍵 |
| name | VARCHAR(100) UNIQUE | 分類名稱 |

### ReadingRecord
| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK, auto) | 主鍵 |
| bookId | Long (FK → book.id) | 書本 ID |
| readDate | DATE NOT NULL | 閱讀日期 |
| durationMinutes | INT NOT NULL (≥ 1) | 本次閱讀時長（分鐘）|
| progressPercent | INT (0-100) | 進度百分比 |
| createdAt | TIMESTAMP | 建立時間 |

---

## 4. API 規格

### 書本管理（base: `/api/books`）

| Method | Path | 說明 | Request Body | Response |
|--------|------|------|-------------|---------|
| POST | `/api/books` | 新增書本 | BookRequest | 201 BookResponse / 400 / 409(ISBN 重複) |
| GET | `/api/books/{id}` | 取得書本 | — | 200 BookResponse / 404 |
| PUT | `/api/books/{id}` | 更新書本 | BookRequest | 200 BookResponse / 400(categoryId 不存在) / 404 / 409(ISBN 與其他書重複) |
| DELETE | `/api/books/{id}` | 刪除書本（僅軟刪除狀態變更，不影響其閱讀記錄）| — | 204 / 404 |
| GET | `/api/books?keyword=&tag=&category=&page=&size=` | 搜尋書本 | — | 200 Page\<BookResponse\> |

### Tag 管理（base: `/api/tags`）

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/tags` | 建立 Tag |
| GET | `/api/tags` | 取得所有 Tag |
| DELETE | `/api/tags/{id}` | 刪除 Tag |
| POST | `/api/books/{bookId}/tags/{tagId}` | 書本加 Tag |
| DELETE | `/api/books/{bookId}/tags/{tagId}` | 書本移除 Tag |

### 分類管理（base: `/api/categories`）

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/categories` | 建立分類 |
| GET | `/api/categories` | 取得所有分類 |
| DELETE | `/api/categories/{id}` | 刪除分類（409 若仍有書本歸類於此分類）|

### 閱讀記錄（base: `/api/reading`）

| Method | Path | 說明 | Request Body | Response |
|--------|------|------|-------------|---------|
| POST | `/api/reading` | 新增閱讀記錄 | ReadingRequest | 201 ReadingResponse |
| PUT | `/api/reading/{id}` | 更新閱讀記錄 | ReadingRequest | 200 ReadingResponse / 404 |
| GET | `/api/reading?bookId=&page=&size=` | 查詢閱讀歷史 | — | 200 Page\<ReadingResponse\> |
| GET | `/api/reading/calendar?year=&month=` | 閱讀日曆 | — | 200 List\<CalendarResponse\> |
| GET | `/api/reading/stats` | 閱讀統計 | — | 200 StatsResponse |

---

## 5. 模組介面契約（BookQueryPort）

```java
package com.iisi.bookmanager.book.port;

/**
 * reading 模組透過此介面查詢 book 模組，禁止跨模組直接存取 Repository。
 */
public interface BookQueryPort {
    /**
     * 確認書本是否存在（未被軟刪除）。
     * @param bookId 書本 ID
     * @return true = 存在且未刪除
     */
    boolean existsBook(Long bookId);
}
```

BookService 實作此介面，並以 `@Service` 注入供 reading 模組使用。

> **書本刪除與閱讀記錄的關係**：書本刪除為軟刪除（僅變更 `deleted` 狀態），不涉及任何跨模組事件或
> 對 reading 模組的呼叫。既有閱讀記錄不會被刪除或修改，永久保留作為歷史資料；僅影響
> `BookQueryPort.existsBook()` 回傳 false，導致該書本**無法再新增**閱讀記錄（US-R01），
> 但既有記錄的查詢（歷史、日曆、統計）不受影響。

---

## 6. 安全 STRIDE 威脅摘要

> 完整威脅模型與緩解措施詳見獨立文件：[`docs/security/threat-model.md`](../security/threat-model.md)

| 威脅類型 | 攻擊面 | 緩解措施 |
|---------|--------|---------|
| Spoofing | API 端點 | 後續 Sprint 加入 Spring Security JWT |
| Tampering | 書本/閱讀記錄篡改 | Bean Validation + JPA 參數化查詢 |
| Information Disclosure | 錯誤訊息洩漏 | GlobalExceptionHandler 統一包裝，不輸出 stack |
| Elevation of Privilege | 他人書本操作 | 後續加 userId 隔離（本 Sprint 先建基礎架構）|
| Injection | ISBN / 關鍵字查詢 | JPA Criteria 或 JPQL 參數化 |

---

## 7. 序列圖（新增閱讀記錄）

```
用戶         ReadingController    ReadingService    BookQueryPort    ReadingRepository
 │                │                    │                 │                 │
 │─POST /reading─►│                    │                 │                 │
 │                │─validateInput()    │                 │                 │
 │                │──────────────────►│                 │                 │
 │                │                   │─existsBook(id)─►│                 │
 │                │                   │                 │─BookService     │
 │                │                   │◄────true/false──│                 │
 │                │                   │                 │                 │
 │                │  [false] throw BookNotFoundException                   │
 │                │                   │─────────────────────────────────►│
 │                │                   │                                   │─save()
 │                │◄──ReadingResponse─│                                   │
 │◄──201──────────│                    │                 │                 │
```

---

## 8. 補充說明（刪除書本行為）

刪除書本（`DELETE /api/books/{id}`）僅執行：① 將 `Book.deleted` 設為 `true`、② 移除該書本的
Book_Tag 映射。**不會**呼叫 reading 模組、不會刪除或修改任何 `ReadingRecord`。已刪除書本的
閱讀歷史、日曆、統計資料皆維持不變，可持續被查詢；僅新增閱讀記錄會因 `BookQueryPort.existsBook()`
回傳 false 而被拒絕（404）。此設計避免了跨模組直接存取 Repository，同時保留完整的閱讀歷史資料。
