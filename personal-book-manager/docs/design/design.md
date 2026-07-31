# Design — 個人圖書管理系統
> 產出者：Alice（架構師 / Architect Agent）  
> 版本：v1.0　|　日期：2026-07-31

---

## 1. 系統架構

```
┌───────────────────────────────────────────────────────────────┐
│                      Spring Boot 3.3                          │
│                                                                │
│  ┌──────────────────┐                                         │
│  │   user 模組        │  （最底層，不依賴 book/reading）          │
│  │  AuthController   │  POST /api/auth/register, /login       │
│  │  UserController   │  GET  /api/users/me                    │
│  │  UserService      │                                        │
│  │  UserRepository   │  bcrypt 密碼雜湊                         │
│  │  JwtTokenProvider │  簽發/驗證 JWT                           │
│  └──────────────────┘                                         │
│           ▲ Spring Security Filter（解析 JWT → userId）          │
│           │ 注入 SecurityContext，供下方兩模組取用 userId          │
│  ┌──────────────────┐   ┌──────────────────────┐              │
│  │   book 模組       │   │   reading 模組         │              │
│  │                  │   │                      │              │
│  │  BookController  │   │  ReadingController   │              │
│  │  BookService     │   │  ReadingService      │              │
│  │  TagService      │   │  ReadingCalendar-    │              │
│  │  CategoryService │   │    Service           │              │
│  │  BookRepository  │   │  ReadingRepository   │              │
│  │                  │   │                      │              │
│  │ «interface»      │───►  BookQueryPort        │              │
│  │ BookQueryPort    │   │  (依賴注入，同步查詢，    │              │
│  │ (existsBook(id,  │   │   帶 userId 比對)        │              │
│  │  userId))        │   │                      │              │
│  └──────────────────┘   └──────────────────────┘              │
│            │                       │                          │
│            └───────┬───────────────┘                          │
│                    ▼                                          │
│           Spring Data JPA                                     │
│                    │                                          │
│          H2 (dev) / PostgreSQL (prod)                         │
└───────────────────────────────────────────────────────────────┘
```

> 兩模組間僅存在單一方向的耦合：reading→book 的同步介面查詢（`BookQueryPort`）。
> 書本刪除為單純的狀態變更（軟刪除），**不會**觸發任何跨模組動作，reading 模組的
> 閱讀記錄生命週期與書本刪除狀態完全independent；book 不允許反向依賴或存取
> reading 模組的 Repository / Entity。
>
> `user` 模組為系統最底層模組，不依賴 `book`/`reading`（避免循環依賴）。`book`/`reading`
> **不得**直接注入 `UserRepository`／`UserService`，一律透過 Spring Security
> （`SecurityContextHolder`／`@AuthenticationPrincipal`）取得目前登入者 `userId`，
> 將其視為與 `categoryId` 相同性質的一般外部識別碼欄位使用；此設計維持既有的
> 單向模組依賴原則，`user` 模組完全不知道 `book`/`reading` 的存在。

---

## 2. 套件結構

```
src/main/java/com/iisi/bookmanager/
├── user/
│   ├── controller/    AuthController.java（register/login）, UserController.java（/me）
│   ├── service/       UserService.java, JwtTokenProvider.java
│   ├── repository/    UserRepository.java
│   ├── domain/        User.java
│   ├── dto/           RegisterRequest.java, LoginRequest.java, LoginResponse.java, UserResponse.java
│   ├── security/      JwtAuthenticationFilter.java, SecurityConfig.java, CurrentUser.java
│   │                  （`CurrentUser` 為輔助工具，供 book/reading 從 SecurityContext 取得 userId）
│   └── exception/     DuplicateUsernameException.java, InvalidCredentialsException.java
├── book/
│   ├── controller/    BookController.java
│   ├── service/       BookService.java, TagService.java, CategoryService.java
│   ├── repository/    BookRepository.java, TagRepository.java, CategoryRepository.java
│   ├── domain/        Book.java, BookType.java (ENUM), CoverImageSource.java (ENUM), Tag.java, Category.java
│   ├── dto/           BookRequest.java, BookResponse.java, CoverResponse.java
│   ├── validation/    ValidBookType.java（自訂 class-level 驗證註解 + Validator，isbn/url 互斥）
│   ├── storage/       CoverStorageService.java（介面）, LocalDiskCoverStorageService.java（實作）
│   ├── port/          BookQueryPort.java          ← 對外介面
│   └── exception/     BookNotFoundException.java, DuplicateIsbnException.java,
│                      DuplicateUrlException.java, CategoryInUseException.java,
│                      CategoryNotFoundException.java, InvalidImageFileException.java,
│                      ImageTooLargeException.java
├── reading/
│   ├── controller/    ReadingController.java
│   ├── service/       ReadingService.java, ReadingCalendarService.java
│   ├── repository/    ReadingRecordRepository.java
│   ├── domain/        ReadingRecord.java, ReadingSource.java (ENUM)
│   ├── dto/           ReadingRequest.java, ReadingResponse.java, CalendarResponse.java
│   ├── validation/    ValidReadingSource.java（自訂 class-level 驗證註解 + Validator）
│   └── exception/     ReadingRecordNotFoundException.java
└── common/
    ├── exception/     GlobalExceptionHandler.java, ErrorResponse.java
    └── config/        JpaConfig.java
```

> `user/security` 套件下的 `SecurityConfig`／`JwtAuthenticationFilter` 是**全域**基礎設施
> （攔截所有進入的 HTTP 請求），雖物理上放在 `user` 套件內，但邏輯上屬於 `common` 層級的
> 橫切關注點；`book`/`reading` 僅依賴其暴露的 `CurrentUser` 工具類別（靜態方法
> `CurrentUser.id()`），不依賴 `user` 模組內其他任何類別。

---

## 3. 資料模型

### User

> 決策紀錄：[`ADR-0006`](../architecture/adr/0006-user-module-multi-tenant-isolation.md)（新增使用者管理模組，book/reading 資料以 userId 隔離）

| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK, auto) | 主鍵，即 `userId`，供 book/reading 資料表外鍵參照 |
| username | VARCHAR(30) UNIQUE NOT NULL | 帳號（3-30 字元，僅英數字/底線/連字號，不分大小寫比對唯一性）|
| passwordHash | VARCHAR(60) NOT NULL | bcrypt 雜湊後的密碼（不可逆），**絕不**出現在任何 API 回應 |
| createdAt | TIMESTAMP | 建立時間 |

> `User` 為最底層 Entity，不持有任何指向 `book`/`reading` 的關聯（避免 `user` 模組
> 反向依賴其他模組）；`Book`/`Tag`/`Category`/`ReadingRecord` 反向持有 `userId` 外鍵。


### Book

> 決策紀錄：[`ADR-0004`](../architecture/adr/0004-expand-book-type-taxonomy.md)（擴充藏書類型，支援無 ISBN 的實體/線上內容）

| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK, auto) | 主鍵 |
| userId | Long (FK → user.id, NOT NULL) | 藏書擁有者；所有查詢/更新/刪除皆以此欄位過濾，防止跨用戶存取 |
| isbn | VARCHAR(13) (**nullable**) | ISBN 碼（10 或 13 位）；僅「實體／電子書」類（`PHYSICAL_BOOK`/`PHYSICAL_DOUJINSHI`/`EBOOK`）可填，選填 |
| url | VARCHAR(500) (**nullable**) | 來源網址；僅「線上內容」類（`WEB_NOVEL`/`BLOG_POST`/`ONLINE_FANFIC`）必填，限 `http`/`https` |
| sourcePlatform | VARCHAR(100) (**nullable**) | 來源平台名稱（選填，如 "AO3"、"Wattpad"、"巴哈姆特"）；僅「線上內容」類適用 |
| title | VARCHAR(200) NOT NULL | 書名／作品名 |
| author | VARCHAR(100) NOT NULL | 作者 |
| bookType | ENUM(PHYSICAL_BOOK, PHYSICAL_DOUJINSHI, EBOOK, WEB_NOVEL, BLOG_POST, ONLINE_FANFIC) NOT NULL | 書本類型 |
| categoryId | Long (FK) | 分類 ID（可為 null）|
| purchaseUrl | VARCHAR(500) (**nullable**) | 購買網址（選填，限 `http`/`https`，任何 `bookType` 皆可填） |
| authorUrl | VARCHAR(500) (**nullable**) | 作者網址（選填，限 `http`/`https`，任何 `bookType` 皆可填） |
| coverImageUrl | VARCHAR(500) (**nullable**) | 封面圖片位址；`coverImageSource=UPLOADED` 時為本機儲存路徑／靜態資源 URL，`coverImageSource=EXTERNAL_URL` 時為外部圖片網址 |
| coverImageSource | ENUM(UPLOADED, EXTERNAL_URL) (**nullable**) | 封面來源；null 表示尚未設定封面 |
| deleted | BOOLEAN DEFAULT false | 軟刪除旗標 |
| createdAt | TIMESTAMP | 建立時間 |
| updatedAt | TIMESTAMP | 更新時間 |

> **互斥約束**（DB CHECK 約束 + Service 層雙重驗證，依 `bookType` 分兩類）：
> - 實體／電子書類（`PHYSICAL_BOOK`/`PHYSICAL_DOUJINSHI`/`EBOOK`）：`url IS NULL`，`isbn` 可為 null 或合法格式
> - 線上內容類（`WEB_NOVEL`/`BLOG_POST`/`ONLINE_FANFIC`）：`isbn IS NULL`，`url IS NOT NULL`
>
> `purchaseUrl`／`authorUrl`／封面欄位不受 `bookType` 分類限制，六種類型皆可自由填寫（僅需符合 URL 格式驗證）。
>
> **封面欄位一致性約束**（DB CHECK）：`coverImageUrl IS NULL` ⇔ `coverImageSource IS NULL`（兩者需同時為 null 或同時有值，避免只設定其中一個造成資料不一致）。
>
> **多用戶去重範圍**：ISBN／URL 去重（US-B02）皆以 `(userId, isbn/url, bookType)` 為範圍，僅在**同一用戶**
> 名下比對重複，不同用戶各自可收藏相同 ISBN 或 URL 的書籍（例如兩位用戶都買了同一本書），不視為衝突。

### Tag / Book_Tag（多對多）
| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK) | Tag 主鍵 |
| userId | Long (FK → user.id, NOT NULL) | Tag 擁有者 |
| name | VARCHAR(50) | Tag 名稱（`UNIQUE(userId, name)`，同一用戶內唯一，不同用戶可重複命名）|

### Category
| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK) | 主鍵 |
| userId | Long (FK → user.id, NOT NULL) | 分類擁有者 |
| name | VARCHAR(100) | 分類名稱（`UNIQUE(userId, name)`，同一用戶內唯一）|

### ReadingRecord

> 決策紀錄：[`ADR-0003`](../architecture/adr/0003-reading-record-supports-borrowed-books.md)（閱讀記錄支援借閱來源）

| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK, auto) | 主鍵 |
| userId | Long (FK → user.id, NOT NULL) | 閱讀記錄擁有者；所有查詢/更新/刪除皆以此欄位過濾 |
| bookId | Long (FK → book.id, **nullable**) | 書本 ID；僅 `source = OWNED` 時提供 |
| source | ENUM(OWNED, BORROWED_FRIEND, BORROWED_LIBRARY) NOT NULL | 書本來源：自己藏書 / 向朋友借 / 向圖書館借 |
| externalTitle | VARCHAR(200) (**nullable**) | 借閱書籍的書名；`source ≠ OWNED` 時必填，`source = OWNED` 時必為 null |
| externalAuthor | VARCHAR(100) (**nullable**) | 借閱書籍的作者；`source ≠ OWNED` 時選填 |
| readDate | DATE NOT NULL | 閱讀日期 |
| durationMinutes | INT NOT NULL (≥ 1) | 本次閱讀時長（分鐘）|
| progressPercent | INT (0-100) | 進度百分比 |
| createdAt | TIMESTAMP | 建立時間 |

> **互斥約束**（由 DB CHECK 約束 + Service 層雙重驗證）：
> `(source = 'OWNED' AND bookId IS NOT NULL AND externalTitle IS NULL)`
> **OR**
> `(source <> 'OWNED' AND bookId IS NULL AND externalTitle IS NOT NULL)`

---

## 4. API 規格

### 使用者管理（base: `/api/auth`, `/api/users`）— **公開**（`/api/auth/**` 不需 JWT，其餘皆需）

| Method | Path | 說明 | Request Body | Response |
|--------|------|------|-------------|---------|
| POST | `/api/auth/register` | 註冊帳號 | RegisterRequest | 201 UserResponse / 400(格式錯誤) / 409(username 已存在) |
| POST | `/api/auth/login` | 登入取得 JWT | LoginRequest | 200 LoginResponse / 401(帳密錯誤) |
| GET | `/api/users/me` | 取得目前登入者資料 | — | 200 UserResponse / 401(未帶或無效 JWT) |

**RegisterRequest 欄位**：`username`（@NotBlank, @Pattern `^[a-zA-Z0-9_-]{3,30}$`）、`password`（@NotBlank, @Size(min=8)）。

**LoginRequest 欄位**：`username`、`password`。

**LoginResponse 欄位**：`token`（JWT 字串）、`tokenType`（固定 `"Bearer"`）、`expiresIn`（秒數，預設 86400）。

**UserResponse 欄位**：`id, username, createdAt`（**不含**任何密碼相關欄位）。

> 除 `/api/auth/register`、`/api/auth/login`（以及 H2 Console／Actuator health，若有開放）外，
> 所有 API（含全部 book／reading 端點）皆須於 `Authorization: Bearer <token>` 標頭攜帶有效 JWT，
> 由 `JwtAuthenticationFilter` 驗證簽章與過期時間，解析出 `userId` 寫入 `SecurityContext`；
> 缺少或無效 JWT 一律回 **401**（`GlobalExceptionHandler` 統一格式，不透露是否過期／簽章錯誤等細節）。

### 書本管理（base: `/api/books`，**需登入**）

| Method | Path | 說明 | Request Body | Response |
|--------|------|------|-------------|---------|
| POST | `/api/books` | 新增書本（六種類型皆可，自動綁定目前登入者 `userId`）| BookRequest | 201 BookResponse / 400(互斥驗證失敗) / 409(ISBN 或 URL 重複，同一用戶內比對) |
| GET | `/api/books/{id}` | 取得書本（僅限自己的書本）| — | 200 BookResponse / 404（不存在或非自己所有）|
| PUT | `/api/books/{id}` | 更新書本（僅限自己的書本）| BookRequest | 200 BookResponse / 400(categoryId 不存在或互斥驗證失敗) / 404 / 409(ISBN 或 URL 與其他書重複，同一用戶內比對) |
| DELETE | `/api/books/{id}` | 刪除書本（僅軟刪除狀態變更，不影響其閱讀記錄；僅限自己的書本）| — | 204 / 404 |
| GET | `/api/books?keyword=&tag=&category=&bookType=&page=&size=` | 搜尋書本（僅限自己的藏書，可依類型篩選）| — | 200 Page\<BookResponse\> |
| POST | `/api/books/{id}/cover` | 上傳／取代封面圖片（`multipart/form-data`；適用「上傳檔案」與「貼上圖片」兩種前端操作，皆送出同一種檔案 blob）| `file`（multipart） | 200 CoverResponse / 400(格式或大小不符) / 404 |
| DELETE | `/api/books/{id}/cover` | 移除封面（若為 `UPLOADED` 則同時刪除實體檔案）| — | 204 / 404 |

**BookRequest 欄位**：`title`、`author`、`bookType`（必填，六選一）、`categoryId`（選填）、
`isbn`（實體／電子書類選填，線上內容類必為 null）、`url`（線上內容類必填，實體／電子書類必為 null）、
`sourcePlatform`（選填，僅線上內容類適用）、`purchaseUrl`（選填，`http`/`https`）、
`authorUrl`（選填，`http`/`https`）、`coverImageUrl`（選填 —「貼上圖片網址」流程：直接於本欄位帶入外部網址，
Service 會將 `coverImageSource` 設為 `EXTERNAL_URL`；**不適用**於檔案上傳，檔案上傳一律走 `POST /api/books/{id}/cover`）。

**BookResponse 欄位**：`id, title, author, bookType, isbn, url, sourcePlatform, categoryId, purchaseUrl, authorUrl, coverImageUrl, coverImageSource, tags, createdAt`
（依 `bookType` 不同，`isbn` 或 `url`/`sourcePlatform` 其中一組為 null；封面欄位未設定時皆為 null）。

**CoverResponse 欄位**：`bookId, coverImageUrl, coverImageSource`。

> **封面圖片三種輸入方式與後端對應**（決策紀錄：[`ADR-0005`](../architecture/adr/0005-book-cover-purchase-author-urls.md)）：
> 1. 「上傳檔案」（檔案選擇器）→ 前端組成 `multipart/form-data` → `POST /api/books/{id}/cover`
> 2. 「貼上圖片」（剪貼簿貼上圖片）→ 前端將剪貼簿內容轉為檔案 blob → 與方式 1 走**同一支** `POST /api/books/{id}/cover`（後端無法也無須區分來源，兩者是同一種資料型態）
> 3. 「貼上圖片網址」→ 前端直接呼叫 `PUT /api/books/{id}`，於 `coverImageUrl` 帶入外部網址，後端**不主動下載/抓取**該圖片內容（避免 SSRF），僅驗證 URL scheme 為 `http`/`https` 並原樣儲存
>
> 因此後端僅需實作**兩條真實流程**（檔案上傳 vs. 外部網址），不需要為「上傳」與「貼上圖片」分別開發。

### Tag 管理（base: `/api/tags`，**需登入**）

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/tags` | 建立 Tag |
| GET | `/api/tags` | 取得所有 Tag |
| DELETE | `/api/tags/{id}` | 刪除 Tag |
| POST | `/api/books/{bookId}/tags/{tagId}` | 書本加 Tag |
| DELETE | `/api/books/{bookId}/tags/{tagId}` | 書本移除 Tag |

### 分類管理（base: `/api/categories`，**需登入**）

| Method | Path | 說明 |
|--------|------|------|
| POST | `/api/categories` | 建立分類 |
| GET | `/api/categories` | 取得所有分類（僅自己）|
| DELETE | `/api/categories/{id}` | 刪除分類（僅自己的分類；409 若仍有書本歸類於此分類）|

### 閱讀記錄（base: `/api/reading`，**需登入**）

| Method | Path | 說明 | Request Body | Response |
|--------|------|------|-------------|---------|
| POST | `/api/reading` | 新增閱讀記錄（自己藏書或借閱書皆可，自動綁定目前登入者 `userId`）| ReadingRequest | 201 ReadingResponse / 400(互斥驗證失敗或缺必填欄位) / 404(`source=OWNED` 但書本不存在或非自己所有) |
| PUT | `/api/reading/{id}` | 更新閱讀記錄（僅限自己的記錄；僅可更新 readDate/durationMinutes/progressPercent，來源相關欄位建立後不可變更）| ReadingRequest | 200 ReadingResponse / 404 |
| GET | `/api/reading?bookId=&source=&page=&size=` | 查詢閱讀歷史（僅限自己的記錄；`bookId` 僅適用於 `source=OWNED`；`source` 可單獨篩選借閱記錄）| — | 200 Page\<ReadingResponse\> |
| GET | `/api/reading/calendar?year=&month=` | 閱讀日曆（僅限自己的記錄，彙整所有來源）| — | 200 List\<CalendarResponse\> |
| GET | `/api/reading/stats` | 閱讀統計（僅限自己的記錄，彙整所有來源）| — | 200 StatsResponse |

**ReadingRequest 欄位**：`source`（enum，必填，預設 `OWNED`）、`bookId`（`source=OWNED` 時必填）、
`externalTitle`（`source≠OWNED` 時必填）、`externalAuthor`（選填）、`readDate`、`durationMinutes`、`progressPercent`。

**ReadingResponse 欄位**：新增 `source`、`externalTitle`、`externalAuthor`；`bookId` 於借閱記錄時為 `null`。

---

## 5. 模組介面契約（BookQueryPort）

```java
package com.iisi.bookmanager.book.port;

/**
 * reading 模組透過此介面查詢 book 模組，禁止跨模組直接存取 Repository。
 */
public interface BookQueryPort {
    /**
     * 確認書本是否存在（未被軟刪除）且屬於指定用戶。
     * @param bookId 書本 ID
     * @param userId 目前登入用戶 ID（由 reading 模組從 SecurityContext 取得後傳入）
     * @return true = 存在、未刪除、且屬於該 userId
     */
    boolean existsBook(Long bookId, Long userId);
}
```

BookService 實作此介面，並以 `@Service` 注入供 reading 模組使用。

> **`BookQueryPort` 僅適用於 `source = OWNED` 的閱讀記錄**：向朋友或圖書館借閱的書
>（`source = BORROWED_FRIEND` / `BORROWED_LIBRARY`）不在個人藏書系統內，reading 模組
> 新增此類記錄時**完全不呼叫** `BookQueryPort`，僅在本地驗證 `externalTitle` 必填即可儲存。

> **書本刪除與閱讀記錄的關係**：書本刪除為軟刪除（僅變更 `deleted` 狀態），不涉及任何跨模組事件或
> 對 reading 模組的呼叫。既有閱讀記錄不會被刪除或修改，永久保留作為歷史資料；僅影響
> `BookQueryPort.existsBook(bookId, userId)` 回傳 false，導致該書本**無法再新增** `source=OWNED` 的閱讀記錄（US-R01），
> 但既有記錄的查詢（歷史、日曆、統計）不受影響；借閱書籍的記錄本就不受此限制。

> **`userId` 參數的必要性**：加入 `userId` 參數（而非僅 `bookId`）是為了防止使用者 A 猜測或得知
> 使用者 B 的 `bookId` 後，於自己名下建立指向該書本的 `source=OWNED` 閱讀記錄（跨用戶資料混淆）。
> `ReadingService` 呼叫時一律傳入 `CurrentUser.id()`（自 SecurityContext 取得），不接受由
> Request Body 指定的 `userId`。


---

## 5a. BookType 分類與去重邏輯

```java
package com.iisi.bookmanager.book.domain;

public enum BookType {
    PHYSICAL_BOOK,       // 實體書籍（一般出版品，通常有 ISBN）
    PHYSICAL_DOUJINSHI,  // 實體同人誌（紙本二創，通常無 ISBN）
    EBOOK,               // 電子書（可能有 ISBN）
    WEB_NOVEL,           // 網路小說（連載平台，如巴哈姆特、Wattpad）
    BLOG_POST,           // Blog 文章
    ONLINE_FANFIC;       // 同人文網站作品（如 AO3）

    /** 是否為「實體／電子書」類（可能有 ISBN，不可有 URL） */
    public boolean isPhysicalOrEbook() {
        return this == PHYSICAL_BOOK || this == PHYSICAL_DOUJINSHI || this == EBOOK;
    }

    /** 是否為「線上內容」類（必須有 URL，不可有 ISBN） */
    public boolean isOnline() {
        return !isPhysicalOrEbook();
    }

    /** 是否需要於新增/更新時執行 ISBN 去重（僅實體類，EBOOK 允許多平台重複購買不去重）*/
    public boolean requiresIsbnDedup() {
        return this == PHYSICAL_BOOK || this == PHYSICAL_DOUJINSHI;
    }
}
```

**BookService 去重邏輯**（`createBook`/`updateBook` 共用）：
1. `@ValidBookType` class-level Bean Validation 先擋互斥規則（實體/電子書類禁止有 `url`；線上內容類禁止有 `isbn` 且 `url` 必填、格式須為 `http`/`https`）。
2. IF `bookType.requiresIsbnDedup()` AND `isbn` 不為 null：查詢**目前登入用戶名下**是否已存在相同 `isbn` + 相同 `bookType` 的未刪除書本，存在則拋 `DuplicateIsbnException`（409）。
3. IF `bookType.isOnline()`：查詢**目前登入用戶名下**是否已存在相同 `url` 的未刪除書本（不分類型，避免同一連結被建立為不同類型的重複收藏），存在則拋 `DuplicateUrlException`（409）。
4. `EBOOK` 一律不做 4 的去重檢查（允許同一本電子書從不同平台重複購買/收藏，沿用既有設計）。

> 所有去重查詢皆以 `userId = CurrentUser.id()` 為第一過濾條件（即 `findByUserIdAndIsbnAndBookTypeAndDeletedFalse`／
> `findByUserIdAndUrlAndDeletedFalse`），不同用戶之間互不影響去重判斷。

---

## 5b. 封面圖片儲存架構（CoverStorageService）

> 決策紀錄：[`ADR-0005`](../architecture/adr/0005-book-cover-purchase-author-urls.md)

```java
package com.iisi.bookmanager.book.storage;

public interface CoverStorageService {
    /** 儲存檔案並回傳可對外存取的 URL（相對路徑或靜態資源路徑） */
    String store(Long bookId, MultipartFile file);

    /** 刪除既有的已上傳檔案（by 儲存路徑），找不到則靜默略過 */
    void delete(String storedPath);
}
```

- **MVP 實作**：`LocalDiskCoverStorageService`，將檔案寫入本機磁碟固定目錄（如 `./data/covers/`），
  以 **UUID 產生檔名**（不使用使用者提供的檔名，避免路徑穿越／檔名衝突），並透過 Spring
  靜態資源設定對外提供 `GET /covers/{uuid}.{ext}`。此為架構師依專案現況（H2 本機開發、單機部署）
  所做的預設判斷，未來若需水平擴展或多機部署，可平行實作 `S3CoverStorageService` 並替換
  Bean 而不影響 `BookService` 邏輯（介面隔離，同 `BookQueryPort` 策略）。
- **檔案驗證流程**（於 `BookService`／`CoverStorageService` 呼叫前執行）：
  1. 讀取檔案前幾個位元組（magic bytes）判斷實際圖片格式，**不採信**副檔名或 `Content-Type` 標頭；
     不符合 `image/jpeg`、`image/png`、`image/webp`、`image/gif` 其中之一則拋 `InvalidImageFileException`（400）。
  2. 檔案大小上限 **5MB**，超過則拋 `ImageTooLargeException`（400）。
- **取代／刪除舊檔**：呼叫 `POST /api/books/{id}/cover` 或 `DELETE /api/books/{id}/cover` 時，
  若原本 `coverImageSource = UPLOADED`，Service 會先呼叫 `CoverStorageService.delete()` 移除舊檔，
  避免孤兒檔案累積；若原本 `coverImageSource = EXTERNAL_URL`，僅需清空欄位，不涉及檔案系統操作。
- **書本軟刪除時的封面處理**：沿用 [`ADR-0002`](../architecture/adr/0002-book-deletion-is-state-change-not-cascade.md)
  的「軟刪除保留所有資料」原則，封面檔案與欄位皆**不隨書本軟刪除而變動**；僅當使用者之後手動刪除該
  已軟刪除書本的封面，或该書本被硬刪除（目前系統未提供硬刪除功能）時才處理。
- **外部網址型封面／購買網址／作者網址**：系統**不會**對這些 URL 發送任何伺服器端請求（不下載、不預覽、
  不做連結有效性檢查），僅做 scheme（`http`/`https`）格式驗證，避免 SSRF；前端顯示時需 HTML escape 並加上
  `rel="noopener noreferrer"`，與既有「Malicious/Unsafe URL」規則一致。

---

## 5c. 使用者驗證與模組隔離（User / JWT）

> 決策紀錄：[`ADR-0006`](../architecture/adr/0006-user-module-multi-tenant-isolation.md)

**註冊／登入流程**：
1. `POST /api/auth/register`：Bean Validation 檢查 `username` 格式與 `password` 長度 → 查詢
   `UserRepository.existsByUsernameIgnoreCase()` → 存在則拋 `DuplicateUsernameException`（409）→
   否則以 `BCryptPasswordEncoder`（cost factor 10）雜湊密碼後儲存，回傳 `UserResponse`（不含密碼）。
2. `POST /api/auth/login`：查詢 `username` → 不存在**或** `BCryptPasswordEncoder.matches()` 失敗，
   一律拋同一種 `InvalidCredentialsException`（401），**訊息不區分**「帳號不存在」與「密碼錯誤」，
   避免帳號列舉（Account Enumeration）攻擊 → 驗證成功則由 `JwtTokenProvider` 簽發 JWT
   （claim 含 `sub=userId`、`username`、`exp`），回傳 `LoginResponse`。

**每次請求的驗證流程**（`JwtAuthenticationFilter`，`OncePerRequestFilter`）：
1. 讀取 `Authorization` 標頭，若非 `/api/auth/**` 且缺少或格式錯誤的 `Bearer <token>` → 401。
2. 驗證 JWT 簽章與 `exp`（過期時間）；失敗 → 401（不透露失敗原因，避免簽章/密鑰洩漏線索）。
3. 解析出 `userId` 寫入 `SecurityContextHolder`，供後續 Controller/Service 透過 `CurrentUser.id()` 取得。
4. `SecurityConfig` 設定 `/api/auth/**` 為 `permitAll()`，其餘路徑一律 `authenticated()`；
   CSRF 停用（純 REST API + Bearer Token，非 Cookie-based session，無 CSRF 風險）。

```java
package com.iisi.bookmanager.user.domain;

public class User {
    private Long id;
    private String username;
    private String passwordHash;   // bcrypt，never exposed via API
    private Instant createdAt;
}
```

**book／reading 模組如何取得 `userId`**：
- `BookController`/`ReadingController` 的每個方法皆呼叫 `CurrentUser.id()`（靜態工具方法，
  內部委派 `SecurityContextHolder.getContext().getAuthentication().getPrincipal()`），
  取得的 `Long userId` 作為 Service 方法的第一個參數傳入，Service 內所有 Repository 查詢一律
  帶上 `userId` 條件（例如 `bookRepository.findByIdAndUserId(id, userId)`）。
- 這維持了既有「book/reading 不得直接依賴其他模組 Repository」的邊界規則：`user` 模組本身
  對 `book`/`reading` 一無所知，`book`/`reading` 也只依賴 Spring Security 的
  `SecurityContext`（框架層級的橫切機制），而非直接呼叫 `user` 模組的類別。

**JWT 密鑰管理**：簽章密鑰（`jwt.secret`）由環境變數／`application.yml` external config 注入，
**不得**硬編碼於原始碼中；本機開發可用 `application-dev.yml` 內建測試密鑰，正式環境需以
環境變數或密鑰管理服務（如 Vault、AWS Secrets Manager）覆寫。

---

## 6. 安全 STRIDE 威脅摘要

> 完整威脅模型與緩解措施詳見獨立文件：[`docs/security/threat-model.md`](../security/threat-model.md)

| 威脅類型 | 攻擊面 | 緩解措施 |
|---------|--------|---------|
| Spoofing | API 端點 | Spring Security + JWT（`user` 模組），除 `/api/auth/**` 外一律需驗證身分 |
| Tampering | 書本/閱讀記錄篡改 | Bean Validation + JPA 參數化查詢 |
| Information Disclosure | 錯誤訊息洩漏 | GlobalExceptionHandler 統一包裝，不輸出 stack |
| Elevation of Privilege | 他人書本操作 | `Book`/`Tag`/`Category`/`ReadingRecord` 皆帶 `userId` 隔離，所有查詢/更新/刪除以 `userId` 過濾，跨用戶存取一律回 404 |
| Injection | ISBN / 關鍵字查詢 | JPA Criteria 或 JPQL 參數化 |
| Malicious/Unsafe URL | 線上內容類（`WEB_NOVEL`/`BLOG_POST`/`ONLINE_FANFIC`）的 `url` 欄位、`purchaseUrl`、`authorUrl`、`coverImageUrl`（EXTERNAL_URL）| 限制 scheme 僅 `http`/`https`（拒絕 `javascript:` 等）；系統**不主動抓取**任一 URL 內容（無 SSRF 風險）；前端渲染時需 HTML escape 並加 `rel="noopener noreferrer"` 避免 Stored XSS |
| Malicious File Upload | 封面圖片上傳（`POST /api/books/{id}/cover`）| 以檔案 magic bytes 驗證實際格式（拒絕偽裝副檔名的可執行檔／webshell）；大小上限 5MB 防止 DoS；儲存檔名以 UUID 產生，不使用使用者輸入，避免路徑穿越 |
| Account Enumeration / Credential Stuffing | 登入 / 註冊 API | 登入失敗訊息不區分帳號存在與否；密碼 bcrypt 雜湊儲存；後續技術債：登入失敗次數限制（Rate Limiting）|

---

## 7. 序列圖

### 7a. 註冊／登入（發行 JWT）

```
用戶       AuthController      UserService        BCryptPasswordEncoder   JwtTokenProvider   UserRepository
 │              │                   │                      │                    │                │
 │─POST /login─►│                   │                      │                    │                │
 │              │─login(username,pw)►│                     │                    │                │
 │              │                   │─findByUsername()─────────────────────────────────────────►│
 │              │                   │◄─────User(或空)─────────────────────────────────────────────│
 │              │                   │─matches(rawPw, hash)►│                    │                │
 │              │                   │◄──true/false──────────│                    │                │
 │              │                   │  [找不到帳號 或 密碼不符] throw InvalidCredentialsException（統一 401，不分原因）
 │              │                   │─generateToken(userId, username)───────────►│                │
 │              │                   │◄──────────JWT─────────────────────────────│                │
 │              │◄─LoginResponse────│                      │                    │                │
 │◄──200────────│                   │                      │                    │                │
```

### 7b. 帶 JWT 呼叫受保護 API（以新增書本為例）

```
用戶     JwtAuthenticationFilter    SecurityContext      BookController    BookService    BookRepository
 │              │                        │                    │               │              │
 │─POST /api/books (Authorization: Bearer <token>)──────────►│                │               │
 │              │─驗證簽章/exp            │                    │               │              │
 │              │  [失敗] 401             │                    │               │              │
 │              │─解析 userId───────────►│                    │               │              │
 │              │                        │◄──寫入 userId───────│               │              │
 │              │─放行────────────────────────────────────────►│               │              │
 │                                       │                    │─CurrentUser.id()               │
 │                                       │                    │─createBook(userId, req)──────►│
 │                                       │                    │                │─save(帶userId)►│
 │                                       │                    │◄───BookResponse─│               │
 │◄──201 BookResponse────────────────────────────────────────│                │               │
```

### 7c. 新增閱讀記錄（含來源分支，含 userId 比對）

```
用戶         ReadingController    ReadingService       BookQueryPort    ReadingRepository
 │                │                    │                    │                 │
 │─POST /reading─►│                    │                    │                 │
 │                │─validateInput()    │                    │                 │
 │                │  (互斥驗證: source=OWNED⇔bookId有值 / source≠OWNED⇔externalTitle有值)
 │                │──────────────────►│                    │                 │
 │                │                   │─[source=OWNED]─────►│                 │
 │                │                   │   existsBook(id, userId)              │
 │                │                   │◄────true/false──────│                 │
 │                │                   │  [false] throw BookNotFoundException（書本不存在或非自己所有）│
 │                │                   │                                       │
 │                │                   │─[source≠OWNED] 略過 BookQueryPort，直接使用 externalTitle/externalAuthor
 │                │                   │──────────────────────────────────────►│
 │                │                   │                                       │─save(帶 userId)
 │                │◄──ReadingResponse─│                                       │
 │◄──201──────────│                    │                    │                 │
```


---

## 8. 補充說明（刪除書本行為）

刪除書本（`DELETE /api/books/{id}`）僅執行：① 將 `Book.deleted` 設為 `true`、② 移除該書本的
Book_Tag 映射。**不會**呼叫 reading 模組、不會刪除或修改任何 `ReadingRecord`。已刪除書本的
閱讀歷史、日曆、統計資料皆維持不變，可持續被查詢；僅新增閱讀記錄會因 `BookQueryPort.existsBook(bookId, userId)`
回傳 false 而被拒絕（404）。此設計避免了跨模組直接存取 Repository，同時保留完整的閱讀歷史資料。

## 9. 補充說明（多用戶資料隔離範圍）

新增 `user` 模組後，`Book`／`Tag`／`Category`／`ReadingRecord` 皆帶 `userId` 外鍵，作為
「租戶隔離」（tenant isolation）欄位，其定位與既有的 `categoryId` 等一般外部識別碼相同——
**不是**跨模組事件或反向依賴，而是每個 Service 方法內建的資料過濾條件（`WHERE userId = :currentUserId`）。
所有 Repository 查詢方法皆需相應調整方法名稱／`@Query` 加上 `userId` 條件（詳見 tasks.md A1/B1/新增
使用者模組任務）。此隔離方式與模組邊界規則（`book`/`reading` 不得互相直接存取 Repository）並不衝突，
`userId` 僅是資料列層級的過濾欄位，不涉及任何跨模組呼叫。
