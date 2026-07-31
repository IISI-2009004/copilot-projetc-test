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
│  │ BookQueryPort    │   │  (依賴注入)            │   │
│  └──────────────────┘   └──────────────────────┘   │
│            │                       │               │
│            └───────┬───────────────┘               │
│                    ▼                               │
│           Spring Data JPA                          │
│                    │                               │
│          H2 (dev) / PostgreSQL (prod)              │
└─────────────────────────────────────────────────────┘
```

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
│   └── exception/     BookNotFoundException.java, DuplicateIsbnException.java
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
| POST | `/api/books` | 新增書本 | BookRequest | 201 BookResponse |
| GET | `/api/books/{id}` | 取得書本 | — | 200 BookResponse / 404 |
| PUT | `/api/books/{id}` | 更新書本 | BookRequest | 200 BookResponse / 404 |
| DELETE | `/api/books/{id}` | 刪除書本（軟刪除）| — | 204 / 404 |
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
| DELETE | `/api/categories/{id}` | 刪除分類 |

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

---

## 6. 安全 STRIDE 威脅摘要

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
