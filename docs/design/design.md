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

---

## 3. 資料模型

### Book

> 決策紀錄：[`ADR-0004`](../architecture/adr/0004-expand-book-type-taxonomy.md)（擴充藏書類型，支援無 ISBN 的實體/線上內容）

| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK, auto) | 主鍵 |
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

> 決策紀錄：[`ADR-0003`](../architecture/adr/0003-reading-record-supports-borrowed-books.md)（閱讀記錄支援借閱來源）

| 欄位 | 型別 | 說明 |
|------|------|------|
| id | Long (PK, auto) | 主鍵 |
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

### 書本管理（base: `/api/books`）

| Method | Path | 說明 | Request Body | Response |
|--------|------|------|-------------|---------|
| POST | `/api/books` | 新增書本（六種類型皆可）| BookRequest | 201 BookResponse / 400(互斥驗證失敗) / 409(ISBN 或 URL 重複) |
| GET | `/api/books/{id}` | 取得書本 | — | 200 BookResponse / 404 |
| PUT | `/api/books/{id}` | 更新書本 | BookRequest | 200 BookResponse / 400(categoryId 不存在或互斥驗證失敗) / 404 / 409(ISBN 或 URL 與其他書重複) |
| DELETE | `/api/books/{id}` | 刪除書本（僅軟刪除狀態變更，不影響其閱讀記錄）| — | 204 / 404 |
| GET | `/api/books?keyword=&tag=&category=&bookType=&page=&size=` | 搜尋書本（可依類型篩選）| — | 200 Page\<BookResponse\> |
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
| POST | `/api/reading` | 新增閱讀記錄（自己藏書或借閱書皆可）| ReadingRequest | 201 ReadingResponse / 400(互斥驗證失敗或缺必填欄位) / 404(`source=OWNED` 但書本不存在) |
| PUT | `/api/reading/{id}` | 更新閱讀記錄（僅可更新 readDate/durationMinutes/progressPercent，來源相關欄位建立後不可變更）| ReadingRequest | 200 ReadingResponse / 404 |
| GET | `/api/reading?bookId=&source=&page=&size=` | 查詢閱讀歷史（`bookId` 僅適用於 `source=OWNED`；`source` 可單獨篩選借閱記錄）| — | 200 Page\<ReadingResponse\> |
| GET | `/api/reading/calendar?year=&month=` | 閱讀日曆（彙整所有來源）| — | 200 List\<CalendarResponse\> |
| GET | `/api/reading/stats` | 閱讀統計（彙整所有來源）| — | 200 StatsResponse |

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
     * 確認書本是否存在（未被軟刪除）。
     * @param bookId 書本 ID
     * @return true = 存在且未刪除
     */
    boolean existsBook(Long bookId);
}
```

BookService 實作此介面，並以 `@Service` 注入供 reading 模組使用。

> **`BookQueryPort` 僅適用於 `source = OWNED` 的閱讀記錄**：向朋友或圖書館借閱的書
>（`source = BORROWED_FRIEND` / `BORROWED_LIBRARY`）不在個人藏書系統內，reading 模組
> 新增此類記錄時**完全不呼叫** `BookQueryPort`，僅在本地驗證 `externalTitle` 必填即可儲存。

> **書本刪除與閱讀記錄的關係**：書本刪除為軟刪除（僅變更 `deleted` 狀態），不涉及任何跨模組事件或
> 對 reading 模組的呼叫。既有閱讀記錄不會被刪除或修改，永久保留作為歷史資料；僅影響
> `BookQueryPort.existsBook()` 回傳 false，導致該書本**無法再新增** `source=OWNED` 的閱讀記錄（US-R01），
> 但既有記錄的查詢（歷史、日曆、統計）不受影響；借閱書籍的記錄本就不受此限制。

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
2. IF `bookType.requiresIsbnDedup()` AND `isbn` 不為 null：查詢是否已存在相同 `isbn` + 相同 `bookType` 的未刪除書本，存在則拋 `DuplicateIsbnException`（409）。
3. IF `bookType.isOnline()`：查詢是否已存在相同 `url` 的未刪除書本（不分類型，避免同一連結被建立為不同類型的重複收藏），存在則拋 `DuplicateUrlException`（409）。
4. `EBOOK` 一律不做 4 的去重檢查（允許同一本電子書從不同平台重複購買/收藏，沿用既有設計）。

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

## 6. 安全 STRIDE 威脅摘要

> 完整威脅模型與緩解措施詳見獨立文件：[`docs/security/threat-model.md`](../security/threat-model.md)

| 威脅類型 | 攻擊面 | 緩解措施 |
|---------|--------|---------|
| Spoofing | API 端點 | 後續 Sprint 加入 Spring Security JWT |
| Tampering | 書本/閱讀記錄篡改 | Bean Validation + JPA 參數化查詢 |
| Information Disclosure | 錯誤訊息洩漏 | GlobalExceptionHandler 統一包裝，不輸出 stack |
| Elevation of Privilege | 他人書本操作 | 後續加 userId 隔離（本 Sprint 先建基礎架構）|
| Injection | ISBN / 關鍵字查詢 | JPA Criteria 或 JPQL 參數化 |
| Malicious/Unsafe URL | 線上內容類（`WEB_NOVEL`/`BLOG_POST`/`ONLINE_FANFIC`）的 `url` 欄位、`purchaseUrl`、`authorUrl`、`coverImageUrl`（EXTERNAL_URL）| 限制 scheme 僅 `http`/`https`（拒絕 `javascript:` 等）；系統**不主動抓取**任一 URL 內容（無 SSRF 風險）；前端渲染時需 HTML escape 並加 `rel="noopener noreferrer"` 避免 Stored XSS |
| Malicious File Upload | 封面圖片上傳（`POST /api/books/{id}/cover`）| 以檔案 magic bytes 驗證實際格式（拒絕偽裝副檔名的可執行檔／webshell）；大小上限 5MB 防止 DoS；儲存檔名以 UUID 產生，不使用使用者輸入，避免路徑穿越 |

---

## 7. 序列圖（新增閱讀記錄，含來源分支）

```
用戶         ReadingController    ReadingService       BookQueryPort    ReadingRepository
 │                │                    │                    │                 │
 │─POST /reading─►│                    │                    │                 │
 │                │─validateInput()    │                    │                 │
 │                │  (互斥驗證: source=OWNED⇔bookId有值 / source≠OWNED⇔externalTitle有值)
 │                │──────────────────►│                    │                 │
 │                │                   │─[source=OWNED]─────►│                 │
 │                │                   │   existsBook(id)    │                 │
 │                │                   │◄────true/false──────│                 │
 │                │                   │  [false] throw BookNotFoundException  │
 │                │                   │                                       │
 │                │                   │─[source≠OWNED] 略過 BookQueryPort，直接使用 externalTitle/externalAuthor
 │                │                   │──────────────────────────────────────►│
 │                │                   │                                       │─save()
 │                │◄──ReadingResponse─│                                       │
 │◄──201──────────│                    │                    │                 │
```

---

## 8. 補充說明（刪除書本行為）

刪除書本（`DELETE /api/books/{id}`）僅執行：① 將 `Book.deleted` 設為 `true`、② 移除該書本的
Book_Tag 映射。**不會**呼叫 reading 模組、不會刪除或修改任何 `ReadingRecord`。已刪除書本的
閱讀歷史、日曆、統計資料皆維持不變，可持續被查詢；僅新增閱讀記錄會因 `BookQueryPort.existsBook()`
回傳 false 而被拒絕（404）。此設計避免了跨模組直接存取 Repository，同時保留完整的閱讀歷史資料。
