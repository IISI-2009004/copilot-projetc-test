# Tasks — 個人圖書管理系統實作計畫
> 產出者：Alice（架構師 / Architect Agent）  
> 版本：v1.0　|　日期：2026-07-31

---

## 模組 C：user（負責人 Dave，分支 `feature/user-management`，**其他模組依賴此模組先完成**）

- [x] **C1** `User` Entity + `UserRepository`  
  - 欄位：username(VARCHAR(30) UNIQUE NOT NULL), passwordHash(VARCHAR(60) NOT NULL), createdAt  
  - 自訂查詢：`existsByUsernameIgnoreCase`、`findByUsernameIgnoreCase`

- [x] **C2** `UserService`（註冊/登入核心邏輯）  
  - `register(username, rawPassword)`：檢查 `existsByUsernameIgnoreCase` → 存在拋 `DuplicateUsernameException`（409）；否則以 `BCryptPasswordEncoder`（cost ≥ 10）雜湊密碼後儲存  
  - `login(username, rawPassword)`：查無帳號或密碼比對失敗，一律拋同一種 `InvalidCredentialsException`（401），訊息不區分原因（避免帳號列舉）；成功則委由 `JwtTokenProvider` 簽發 JWT  
  - `getCurrentUser(userId)`：供 `GET /api/users/me` 使用，404 用不到（JWT 有效即代表用戶存在）

- [x] **C3** `JwtTokenProvider`（簽發/驗證 JWT）  
  - `generateToken(userId, username)`：HS256 簽章，claim 含 `sub`(userId)、`username`、`iat`、`exp`（預設 24 小時）；密鑰自 `jwt.secret` 環境變數/設定檔讀取，禁止硬編碼  
  - `validateToken(token)` / `getUserId(token)`：驗證簽章與過期時間，失敗回傳 empty/拋例外供 Filter 統一轉換為 401

- [x] **C4** `JwtAuthenticationFilter` + `SecurityConfig`  
  - `JwtAuthenticationFilter`（`OncePerRequestFilter`）：解析 `Authorization: Bearer <token>`，驗證通過後將 `userId` 寫入 `SecurityContextHolder`；驗證失敗回 401（不透露具體失敗原因）  
  - `SecurityConfig`：`/api/auth/**` 設為 `permitAll()`，其餘路徑 `authenticated()`；停用 CSRF（純 REST + Bearer Token，無 Cookie session）；停用預設表單登入/HTTP Basic  
  - `CurrentUser`（工具類別）：靜態方法 `CurrentUser.id()`，供 book/reading 模組的 Controller/Service 取得目前登入者 `userId`，不需依賴 `user` 模組其他任何類別

- [x] **C5** `AuthController` + `UserController` + Bean Validation  
  - `RegisterRequest`（@NotBlank, @Pattern `^[a-zA-Z0-9_-]{3,30}$` username；@NotBlank, @Size(min=8) password）  
  - `LoginRequest`（username, password）、`LoginResponse`（token, tokenType, expiresIn）、`UserResponse`（id, username, createdAt，**不含**密碼欄位）  
  - 端點：`POST /api/auth/register`、`POST /api/auth/login`、`GET /api/users/me`  
  - `GlobalExceptionHandler` 新增 `DuplicateUsernameException`→409、`InvalidCredentialsException`→401

- [x] **C6** UserService / AuthController / JwtTokenProvider 單元測試 ≥ 80%  
  - 必含：註冊成功（201）、username 重複（409）、username 格式錯誤（400）、password 過短（400）、登入成功回傳有效 JWT、帳號不存在登入失敗（401，且訊息與密碼錯誤時相同）、密碼錯誤登入失敗（401）、密碼確實以 bcrypt 雜湊儲存（不等於明文）、`GET /api/users/me` 未帶 JWT（401）、JWT 過期（401）、JWT 簽章竄改（401）  
  - Mockito mock Repository；AssertJ 斷言  
  - 測試命名：`should_預期行為_When_條件`

---

## 模組 A：book（負責人 Bob，分支 `feature/book-management`，**依賴模組 C 的 `CurrentUser` 完成**）

- [x] **A1** `Book` Entity + `BookRepository`  
  - 欄位：userId(FK → user.id, NOT NULL), isbn(**nullable**), url(**nullable**), sourcePlatform(**nullable**), title, author, bookType(ENUM: PHYSICAL_BOOK/PHYSICAL_DOUJINSHI/EBOOK/WEB_NOVEL/BLOG_POST/ONLINE_FANFIC), categoryId(FK), purchaseUrl(**nullable**), authorUrl(**nullable**), coverImageUrl(**nullable**), coverImageSource(ENUM: UPLOADED/EXTERNAL_URL, **nullable**), deleted, createdAt, updatedAt  
  - DB CHECK 約束：實體/電子書類 `url IS NULL`；線上內容類 `isbn IS NULL AND url IS NOT NULL`；封面欄位一致性 `(coverImageUrl IS NULL) = (coverImageSource IS NULL)`  
  - 自訂查詢：`findByUserIdAndIsbnAndBookTypeAndDeletedFalse`（ISBN 去重，限同一用戶）、`findByUserIdAndUrlAndDeletedFalse`（URL 去重，限同一用戶）、`findByIdAndUserId`（單筆查詢，隔離存取）、`findAllByUserIdAndDeletedFalse`（含 keyword/tag/category/bookType 過濾，限同一用戶）
  - ✅ PG2（2026-08-03）：已完成 JPA 轉換與 DB CHECK 約束；`findAllByUserIdAndDeletedFalse` 屬於 `searchBooks` 範圍，尚未實作。

- [x] **A2** `BookService`（核心業務邏輯）— ⚠️ 僅完成 `createBook`，其餘方法維持骨架  
  - 每個 public 方法第一個參數皆為 `Long userId`（由 Controller 透過 `CurrentUser.id()` 取得後傳入）  
  - `createBook(userId, ...)`：依 `BookType.requiresIsbnDedup()` 決定是否執行 ISBN+bookType 去重（`PHYSICAL_BOOK`/`PHYSICAL_DOUJINSHI`，僅當 isbn 有值，範圍限同一 `userId`）；`bookType.isOnline()` 時執行 URL 去重（`DuplicateUrlException`，範圍限同一 `userId`）；`EBOOK` 不做任何去重；新書自動綁定 `userId`  
  - ✅ PG2（2026-08-03）：`createBook` 已完成並通過單元測試（`BookServiceImplTest`）。
  - `updateBook(userId, bookId, ...)`：先以 `findByIdAndUserId` 確認書本存在且屬於自己，否則 404；ISBN/URL 變更時比照上述規則重新檢查；`categoryId` 不存在或不屬於自己回 400（CategoryNotFoundException）— ⏳ 尚未實作（維持 `UnsupportedOperationException`）
  - `deleteBook(userId, bookId)`：先確認書本屬於自己，僅軟刪除（`deleted = true`）、移除 Book_Tag 映射；**不觸碰**任何 `ReadingRecord`，也不呼叫/發布任何跨模組事件（reading 的閱讀記錄為獨立歷史資料，不隨書本刪除而變動）— ⏳ 尚未實作
  - `searchBooks(userId, ...)`：分頁搜尋（keyword、tag、category、bookType 可選，範圍限同一 `userId`）— ⏳ 尚未實作
  - 實作 `BookQueryPort.existsBook(bookId, userId)`：查詢限同一 `userId` 且未刪除 — ⏳ 尚未實作

- [x] **A6** `ValidBookType` 自訂 Bean Validation（class-level）  
  - 驗證 `BookRequest` 互斥規則：`bookType.isPhysicalOrEbook()` ⇒ `url` 必為 null，`isbn` 選填（若有值需符合 10/13 碼格式）；`bookType.isOnline()` ⇒ `isbn` 必為 null，`url` 必填且僅接受 `http`/`https` scheme  
  - 驗證失敗回 400，錯誤訊息清楚指出違反互斥規則（不洩漏內部細節）
  - ✅ PG2（2026-08-03）：`ValidBookTypeValidator` 已完成並通過 `BookControllerTest` 驗證。

- [x] **A3** `BookController` + Bean Validation — ⚠️ 僅完成 `createBook` 端點，其餘端點維持骨架  
  - 每個端點方法第一步呼叫 `CurrentUser.id()` 取得目前登入者 `userId`，傳入對應 Service 方法  
  - `BookRequest`（@NotBlank title/author、@NotNull bookType、`@ValidBookType` class-level 驗證、purchaseUrl/authorUrl/coverImageUrl 皆 @Pattern 限 `http`/`https`；**不含** `userId` 欄位，禁止由前端指定）  
  - `BookResponse`（id, isbn, url, sourcePlatform, title, author, bookType, categoryId, purchaseUrl, authorUrl, coverImageUrl, coverImageSource, tags, createdAt）  
  - 全域 `GlobalExceptionHandler`（404/409/400 統一 ErrorResponse 格式，含 CategoryInUseException/DuplicateUrlException→409、InvalidImageFileException/ImageTooLargeException→400）
  - ✅ PG2（2026-08-03）：`POST /api/books` 已完成，回傳 201 + BookResponse；`updateBook`/`deleteBook`/`searchBooks` 端點簽章維持骨架（`UnsupportedOperationException`）。

- [ ] **A7** `CoverStorageService`（介面）+ `LocalDiskCoverStorageService`（本機磁碟實作）  
  - `store(bookId, MultipartFile)`：驗證檔案 magic bytes（僅接受 image/jpeg, image/png, image/webp, image/gif，不信任副檔名/Content-Type）；驗證大小 ≤ 5MB；以 UUID 產生檔名寫入 `./data/covers/`；回傳可對外存取 URL  
  - `delete(storedPath)`：刪除既有檔案，檔案不存在則靜默略過（不拋例外）  
  - 設定 Spring 靜態資源映射 `/covers/**` → 本機磁碟目錄

- [ ] **A8** `BookController` 封面圖片端點  
  - `POST /api/books/{id}/cover`（multipart）：先確認書本屬於目前登入者，再呼叫 `CoverStorageService.store()`；若原本已有 `coverImageSource=UPLOADED` 的舊檔，先呼叫 `delete()` 清除舊檔再寫入新檔並更新 `coverImageUrl`/`coverImageSource=UPLOADED`  
  - `DELETE /api/books/{id}/cover`：先確認書本屬於目前登入者；若 `coverImageSource=UPLOADED` 則呼叫 `delete()` 移除實體檔案；無論來源皆清空 `coverImageUrl`/`coverImageSource`  
  - `PUT /api/books/{id}` 內若帶入 `coverImageUrl`（且非透過上傳端點），視為「貼上圖片網址」流程：設定 `coverImageSource=EXTERNAL_URL`；若原本為 `UPLOADED`，須先呼叫 `delete()` 清除舊上傳檔避免孤兒檔案

- [ ] **A4** `TagService` / `CategoryService`（Tag／Category 為各自獨立資料表，皆帶 `userId` 隔離與 `color` 欄位）  
  - Tag：建立、列表、更新（改名/改色）、刪除（皆限同一 `userId`；刪除時一併移除 Book_Tag 映射）；書本加/移除 Tag；`name` 加上 `UNIQUE(userId, name)` 約束；`color` 選填，格式 `#RRGGBB`  
  - Category：樹狀階層（`parentId` 自我參照，Adjacency List），建立/更新（含搬移 parentId）/列表（回傳巢狀樹狀結構）/刪除（皆限同一 `userId`）；`name` 加上 `UNIQUE(userId, parentId, name)` 約束（同層唯一）；`color` 選填；`sortOrder` 供同層排序  
    - 建立/搬移時檢查階層深度上限（最多 3 層），超過拋 `CategoryDepthExceededException`（400）  
    - 搬移 `parentId` 時需沿父鏈往上追溯偵測循環參照，偵測到拋 `InvalidCategoryParentException`（400）  
    - 刪除時若仍有子分類或書本歸類於此分類，拋 `CategoryInUseException`（409），不自動級聯刪除

- [ ] **A5** BookService / Controller / TagService / CategoryService 單元測試 ≥ 80%  
  - 每個 public 方法 ≥ 3 個案例（Happy / Boundary / Error）  
  - 新增案例：更新書本 ISBN 重複（409，限同一用戶）、categoryId 不存在（400）、分類刪除時仍有書本歸類（409）、刪除書本後其既有閱讀記錄仍可查詢（驗證 deleteBook 未觸及 ReadingRecord）、六種 bookType 各自新增 Happy Path、線上內容類 URL 重複（409，限同一用戶）、線上內容類誤帶 isbn（400）、實體類誤帶 url（400）、EBOOK 重複 isbn 不觸發 409（驗證不去重）、同人誌無 isbn 新增成功且不觸發去重、purchaseUrl/authorUrl 非 http/https 格式（400）  
  - 新增封面相關測試（A7/A8）：合法圖片上傳成功（200）、偽裝副檔名的非圖片檔案上傳失敗（400，驗證以 magic bytes 而非副檔名判斷）、超過 5MB 檔案上傳失敗（400）、取代封面時舊檔案被刪除（Mockito verify storage.delete 被呼叫）、貼上外部圖片網址設定成功且不觸發任何下載行為（verify 無 HTTP client 呼叫）、刪除封面後欄位清空且對應刪除實體檔案（僅 UPLOADED 時）  
  - 新增分類階層測試：建立子分類成功並可於 GET 樹狀回應中看到巢狀 children、超過 3 層深度建立失敗（400）、搬移分類形成循環參照失敗（400）、分類刪除時仍有子分類失敗（409）、分類刪除時仍有書本歸類失敗（409）、Tag/Category `color` 格式錯誤（400，非 `#RRGGBB`）、Tag/Category 未帶 `color` 時允許為 null  
  - 新增多用戶隔離測試：使用者 A 無法查詢/更新/刪除使用者 B 的書本（回 404）、兩位用戶各自新增相同 ISBN 的書本皆成功（不誤判為重複）、TagName/CategoryName 不同用戶可重複、同一用戶內同層重複則 409  
  - Mockito mock Repository / CoverStorageService；AssertJ 斷言  
  - 測試命名：`should_預期行為_When_條件`

---

## 模組 B：reading（負責人 Carol，分支 `feature/reading-record`，**依賴模組 C 的 `CurrentUser` 完成**）

- [ ] **B1** `ReadingRecord` Entity + `ReadingRecordRepository`  
  - 欄位：userId(FK → user.id, NOT NULL), bookId(FK, **nullable**), source(ENUM: OWNED/BORROWED_FRIEND/BORROWED_LIBRARY), externalTitle(**nullable**), externalAuthor(**nullable**), readDate, durationMinutes(≥1), progressPercent(0-100), createdAt  
  - DB CHECK 約束：`(source='OWNED' AND bookId IS NOT NULL AND externalTitle IS NULL) OR (source<>'OWNED' AND bookId IS NULL AND externalTitle IS NOT NULL)`  
  - 自訂查詢：`findByUserIdAndBookId`（分頁，僅 OWNED，限同一用戶）、`findByUserIdAndSource`（分頁，借閱記錄，限同一用戶）、`findByUserIdAndReadDateBetween`（日曆用，不分來源，限同一用戶）、`findByIdAndUserId`（單筆查詢，隔離存取）

- [ ] **B2** `ReadingService`（新增/更新閱讀記錄 + 閱讀時長累計）  
  - 每個 public 方法第一個參數皆為 `Long userId`  
  - `addRecord(userId, ...)`：`source=OWNED` 時先呼叫 `BookQueryPort.existsBook(bookId, userId)`（不存在或非自己所有拋 404）；`source≠OWNED` 時**不呼叫** BookQueryPort，直接驗證 `externalTitle` 必填後儲存；記錄自動綁定 `userId`  
  - `updateRecord(userId, recordId, ...)`：先以 `findByIdAndUserId` 確認記錄屬於自己，否則 404；累加 durationMinutes、更新 progressPercent；`source`/`bookId`/`externalTitle`/`externalAuthor` 建立後不可修改  
  - `getRecordsByBook(userId, bookId, ...)`：分頁查詢（僅 OWNED，依 bookId，限同一用戶）  
  - `getRecordsBySource(userId, source, ...)`：分頁查詢借閱記錄（依 source 篩選，限同一用戶）  
  - `getStats(userId)`：總時長（SUM，不分來源，限同一用戶）、已完成相異書籍數（`source=OWNED` 以 bookId 去重，`source≠OWNED` 以 `(source, externalTitle, externalAuthor)` 去重）

- [ ] **B3** `ReadingCalendarService`（依日期彙整閱讀時長）  
  - `getMonthlyCalendar(userId, year, month)`：以 `readDate` group by（不分來源，限同一用戶），彙整每日總分鐘  
  - 回傳 `List<CalendarResponse>`（date, totalMinutes）

- [ ] **B7** `ValidReadingSource` 自訂 Bean Validation（class-level）  
  - 驗證 `ReadingRequest` 互斥規則：`source=OWNED` ⇔ `bookId` 有值且 `externalTitle` 為 null；`source≠OWNED` ⇔ `bookId` 為 null 且 `externalTitle` 有值  
  - 驗證失敗回 400，錯誤訊息需清楚指出違反互斥規則（不洩漏內部細節）

- [ ] **B4** `ReadingController` + Bean Validation  
  - 每個端點方法第一步呼叫 `CurrentUser.id()` 取得目前登入者 `userId`，傳入對應 Service 方法  
  - `ReadingRequest`（@NotNull source/readDate、@Min(1) durationMinutes、@Min(0)@Max(100) progressPercent、`@ValidReadingSource` class-level 驗證；**不含** `userId` 欄位，禁止由前端指定）  
  - `ReadingResponse`（新增 source/externalTitle/externalAuthor 欄位，bookId 於借閱記錄為 null）、`CalendarResponse`、`StatsResponse`  
  - 端點：POST /reading、PUT /reading/{id}、GET /reading?bookId=&source=&page=&size=、GET /reading/calendar、GET /reading/stats

- [ ] **B5** 閱讀時長計算、日期邊界測試 ≥ 80%  
  - 必含：跨日閱讀累計、時長為零邊界、書本不存在、progressPercent 邊界(0/100)、書本被軟刪除後既有閱讀記錄仍可查詢（不被刪除）  
  - 新增案例：`source=OWNED` 缺 bookId（400）、`source≠OWNED` 缺 externalTitle（400）、`source≠OWNED` 誤帶 bookId（400）、借閱記錄新增時不呼叫 BookQueryPort（Mockito verify 0 次呼叫）、統計功能正確去重借閱書籍  
  - 新增多用戶隔離測試：使用者 A 無法查詢/更新使用者 B 的閱讀記錄（404）、使用者 A 嘗試以使用者 B 的 `bookId` 新增 `source=OWNED` 記錄應失敗（404，`existsBook(bookId, userId)` 回 false）、日曆/統計僅彙整目前登入者自己的記錄  
  - Mock `BookQueryPort`（測試不依賴 book 模組實作）

---

## 介面契約（Alice 先定，Bob/Carol/Dave 各自對著實作）

```java
// 位置：book/port/BookQueryPort.java（reading → book，同步查詢）
public interface BookQueryPort {
    boolean existsBook(Long bookId, Long userId);
}
// BookService implements BookQueryPort（book 模組提供實作）
// ReadingService 注入 BookQueryPort（reading 模組消費，僅 source=OWNED 時呼叫，並傳入 CurrentUser.id()）
// 書本刪除僅為狀態變更（軟刪除），不觸發任何跨模組呼叫或事件；
// 閱讀記錄為獨立歷史資料，禁止任一模組直接注入或呼叫對方的 Repository / Entity
// 借閱書籍（source≠OWNED）完全不進入 book 模組，reading 僅本地儲存 externalTitle/externalAuthor

// 位置：user/security/CurrentUser.java（user → book/reading，供取用，非注入 Repository）
public final class CurrentUser {
    private CurrentUser() {}
    /** 由 SecurityContextHolder 解析 JWT 認證結果，取得目前登入者 userId */
    public static Long id() { /* ... */ }
}
// book/reading 的 Controller 一律呼叫 CurrentUser.id() 取得 userId 並傳入 Service 方法，
// 不得直接注入 user 模組的 UserRepository/UserService（維持模組邊界的單向依賴原則）
```

---

## 模組 D：frontend（負責人：前端 PG，分支 `feature/PG1-project-scaffold`，**mock 資料先行，待後端 API 完成後串接**）

- [x] **D1** 引入 Element Plus（`element-plus` + `@element-plus/icons-vue`），於 `main.ts` 全域註冊
- [x] **D2** `MainLayout.vue`（Header：標題 + `AppMenu` + `UserInfoWidget`；Main：`RouterView`）
- [x] **D3** `AppMenu.vue` 頂部導覽選單（首頁／藏書管理／閱讀記錄，`el-menu` + `router` 模式自動切換路由）
- [x] **D4** `UserInfoWidget.vue` 使用者資訊下拉選單（顯示使用者名稱、登出）+ `stores/user.ts`（Pinia，`currentUser`/`logout()`）
- [x] **D5** `HomeView.vue` 首頁儀表板（藏書總數／累計閱讀時長／已完成書籍／本月閱讀時長統計卡片，對應 US-R05 統計資料形狀）
- [x] **D6** `BookListView.vue`／`ReadingView.vue` 佔位頁（`el-empty` 開發中提示），供 Menu 連結導向
- [ ] **D7**（待後端 API 完成）新增 `src/services/` API 呼叫層，移除 `src/mocks/mockData.ts` 假資料依賴，
      改接 `POST /api/auth/login`、`GET /api/users/me`、`GET /api/reading/stats` 等真實端點

---

## 里程碑

| 時間點 | 目標 |
|--------|------|
| Sprint 0 完成 | 本文件 + design.md + requirements.md commit 到 develop |
| Sprint 1 初 | Dave 優先完成 C1–C4（`User` Entity/JWT/SecurityConfig/`CurrentUser`），供 Bob/Carol 銜接 |
| Sprint 1 中 | Bob、Carol 分別完成 A1–A4、B1–B4 並 push feature 分支；Dave 完成 C5–C6 |
| Sprint 1 末 | A5/B5/C6 測試完成；開 PR → Alice Review → merge develop |
| 發布 | develop → main PR；`v1.0.0` tag；CHANGELOG 產出 |
