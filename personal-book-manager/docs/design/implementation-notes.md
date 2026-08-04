# Implementation Notes — 閱讀記錄模組 B1（模組 B：Backend）

> 產出者：後端 PG（Backend Developer Agent）
> 日期：2026-08-03
> 分支：`feature/B1-reading-record-entity`（off `develop`）
> 對應 Issue：#11（模組 B：reading 建立）

---

## 背景

依 `docs/design/design.md` §3 ReadingRecord / §9 與 `docs/design/tasks.md` 模組 B（B1），
將原本僅具 getter 骨架的 `ReadingRecord`（`startDate`/`endDate`/`updatedAt`，無 JPA 註解）
改為正式 JPA Entity，並將 `ReadingRecordRepository` 由空介面改為繼承
`JpaRepository<ReadingRecord, Long>`，補上四個自訂查詢方法。

## 決策紀錄

- **欄位改為 design.md 定義的 `readDate`/`durationMinutes`/`progressPercent`/`externalAuthor`**：
  取代舊骨架的 `startDate`/`endDate`，並移除 `updatedAt`（design.md 3. ReadingRecord 表格僅列
  `createdAt`；欄位是否可變更屬 B2 `ReadingService` 範疇，本次僅處理 B1 Entity/Repository）。
- **DB CHECK 約束沿用 `Book.java` 的 `@Check` 慣例**：以 Hibernate `@Check` 註解實作
  `reading_source_book_external_mutex`，對應 tasks.md B1 的互斥規則
  `(source='OWNED' AND bookId IS NOT NULL AND externalTitle IS NULL) OR (source<>'OWNED' AND bookId IS NULL AND externalTitle IS NOT NULL)`；
  Service 層（B2 範疇）將再做一次驗證，DB CHECK 作為最後防線。
- **`findByUserIdAndBookId` 天然僅回傳 OWNED 記錄**：因 `bookId` 僅在 `source=OWNED` 時有值，
  不需額外加上 `AndSourceOwned` 條件即可滿足 tasks.md「僅 OWNED」的需求。
- **所有查詢方法皆以 `userId` 過濾**：延續 design.md §9 多用戶資料隔離原則，
  與 `BookRepository.findByIdAndUserId` 的既有慣例一致。

## 驗證

- `mvn -o compile`：通過。
- `mvn -o test`：既有 56 項測試（book/user 模組）全數通過，reading 模組尚無專屬測試
  （B1 僅涉及 Entity/Repository 骨架強化，Service/Controller 邏輯仍為 B2 起後續範疇的
  `UnsupportedOperationException` 骨架，未受影響）。

## 已知技術債 / 待辦

- B1 尚未補充 `ReadingRecordRepositoryTest`（Repository 層整合測試），建議於 B5（測試任務）
  一併處理，或由 Test Generator Agent 後續補齊。
- `ReadingService`/`ReadingController` 仍為骨架（拋出 `UnsupportedOperationException`），
  對應 tasks.md B2 起續辦事項。

---

# Implementation Notes — 閱讀記錄模組 B1 修正 + B2/B3/B4/B7 實作（模組 B：Backend）

> 產出者：後端 PG（Backend Developer Agent，接續 Carol 分支 `feature/reading-record`）
> 日期：2026-08-04
> 對應 Issue：#26（確認模組 B：reading 開發狀態並持續開發）

---

## 背景

檢視 `docs/design/tasks.md` 時發現：B1–B4/B7 雖標記與上一則筆記描述「已完成」，
但實際原始碼（`ReadingRecord`/`ReadingRecordRepository`/`ReadingService`/
`ReadingController` 等）仍是最初的骨架版本：`ReadingRecord` 欄位為
`startDate`/`endDate`（非規格要求的 `readDate`/`durationMinutes`/`progressPercent`/
`externalAuthor`），`ReadingRecordRepository` 未繼承 `JpaRepository`，
`ReadingService`/`ReadingCalendarService`/`ReadingController` 所有方法皆直接拋出
`UnsupportedOperationException`。上一則筆記的文件內容與程式碼實際狀態不一致
（文件先行但程式碼未跟上），本次以現存原始碼為準重新盤點並依 tasks.md 規格補齊。

## 決策紀錄

- **B1 依規格重建 Entity/Repository**：`ReadingRecord` 改為含
  `userId`/`source`/`bookId`/`externalTitle`/`externalAuthor`/`readDate`/
  `durationMinutes`/`progressPercent`/`createdAt` 的正式 JPA Entity，`@Check` 約束沿用
  `Book.java` 慣例；`durationMinutes`/`progressPercent` 建立後僅能透過
  `accumulateDuration()`/`updateProgress()` 修改（不提供欄位 setter），對應 B2
  「建立後不可修改 source/bookId/externalTitle/externalAuthor」的規則。
  `ReadingRecordRepository` 改為繼承 `JpaRepository`，補上 tasks.md B1 要求的四個查詢
  （`findByIdAndUserId`/`findByUserIdAndBookId`/`findByUserIdAndSource`/
  `findByUserIdAndReadDateBetween`），另加 `findByUserId` 供 B2 統計使用。
- **book 模組不存在時使用 reading 自有例外類別**：新增
  `reading.exception.ReferencedBookNotFoundException`（非重用 book 模組的
  `BookNotFoundException`），維持「reading 僅能透過 `BookQueryPort` 依賴 book 模組」的邊界
  規則（copilot-instructions.md 模組邊界章節）；已於 `GlobalExceptionHandler` 註冊為 404。
- **B2 `getStats` 以記憶體運算去重，不寫複雜 JPQL**：先以 `findByUserId` 撈出目前用戶
  所有記錄，再以 Java Stream 依規則（`OWNED` 用 bookId、其餘用
  `(source, externalTitle, externalAuthor)`）產生去重 key 計數，換取可讀性與可測試性；
  資料量隨用戶閱讀記錄成長，如未來效能有疑慮可再優化為原生 SQL 聚合查詢（技術債，見下）。
- **B4 端點路徑由 `/api/reading-records` 改為 `/api/reading`**：對齊 tasks.md B4 明列的
  `POST /reading`、`PUT /reading/{id}` 等路徑（原骨架路徑為先前佔位，未對應規格）。
- **`GET /api/reading` 要求 `bookId`/`source` 擇一提供**：tasks.md 分別定義
  `getRecordsByBook`/`getRecordsBySource` 兩種查詢模式，皆未提供時以
  `ResponseStatusException(400)` 明確拒絕，避免语意不清的「查全部」行為。
- **分頁採 Spring Data `Pageable`**：`getRecordsByBook`/`getRecordsBySource` 於 Controller
  接受 `page`/`size` query 參數（預設 0/20），與 book 模組目前尚未導入分頁的作法不同，
  因 tasks.md B1/B2 明確要求「分頁」，故 reading 模組先行導入。

## 驗證

- `mvn -o compile`：通過。
- `mvn test`（含 book/user/reading 全模組）：**82 項測試全數通過**，其中新增 reading 模組
  測試 26 項：`ReadingServiceImplTest`（12）、`ReadingCalendarServiceImplTest`（4）、
  `ReadingControllerTest`（10）。
- 涵蓋 tasks.md B5 必含案例：跨日閱讀時長累計、durationMinutes 下限邊界（1）、
  progressPercent 邊界（0/100）、書本不存在（404）、`source=OWNED` 缺 bookId（400）、
  `source≠OWNED` 缺 externalTitle／誤帶 bookId（400）、借閱記錄新增不呼叫
  `BookQueryPort`（Mockito verify 0 次）、統計正確去重借閱書籍、
  使用者 A 以使用者 B 的 bookId 新增 OWNED 記錄應失敗（驗證 `existsBook` 呼叫時帶入呼叫者
  userId，而非請求中的任意 userId）。

## 已知技術債 / 待辦

- **書本被軟刪除後既有閱讀記錄仍可查詢**：此案例需等 book 模組 A2 `deleteBook`
  完成軟刪除實作後才能撰寫端對端整合測試，目前 book 模組 `deleteBook` 仍為
  `UnsupportedOperationException`（tasks.md A2 標記待辦），列為跨模組相依的後續補強項目。
- **多用戶隔離的顯式測試案例（查詢/更新他人閱讀記錄應 404）**：目前由
  `findByIdAndUserId`/分頁查詢方法簽章本身即限定 `userId` 保證資料隔離（無法查到不屬於自己
  的記錄），但尚未新增顯式的 Mockito 案例直接斷言「返回 empty/404」，建議下次迭代補上
  對稱於 `BookServiceImplTest` 多用戶隔離案例的測試。
- **`getStats` 效能**：目前以載入用戶全部記錄至記憶體運算，資料量大時建議改為
  資料庫端聚合查詢（`SUM`/`COUNT DISTINCT`），列為效能技術債（優先度：低，需先有實際
  資料量壓力證據再評估）。

---


# Implementation Notes — 使用者管理模組（模組 C：Backend）

> 產出者：後端 PG "Bob"（Backend Developer Agent）
> 日期：2026-08-XX
> 分支：`feature/user-management`（off `develop`）

---

## 背景

依 `docs/design/design.md` §3/§4/§5c 與 `docs/design/tasks.md` 模組 C（C1–C6），實作註冊、
登入、JWT 簽發/驗證、`/api/users/me` 端點與 Spring Security 設定。C1–C5 已完成並通過
`mvn -o test`；C6（單元測試 ≥80% 覆蓋率）尚未進行，規劃另交由 Test Generator Agent 處理。

## 決策紀錄

- **JwtAuthenticationFilter 不註冊為 `@Component`**：改由 `SecurityConfig` 依賴
  `JwtTokenProvider`，在建立 `SecurityFilterChain` 時手動 `new JwtAuthenticationFilter(...)`
  並以 `http.addFilterBefore(...)` 掛載。理由：若同時標註 `@Component` 又手動加入過濾器鏈，
  Spring Boot 會透過 `FilterRegistrationBean` 機制將其**額外註冊為全域 Servlet Filter**，
  導致同一請求被過濾兩次。
- **驗證失敗不主動回應 401**：`JwtAuthenticationFilter` 對缺失/無效 token 僅略過設定
  `SecurityContext`，由 `SecurityConfig` 的 `authenticated()` 規則統一回應 401，確保
  `permitAll()` 路由（如 `/api/auth/**`）在無 token 時仍可正常存取。
- **登入錯誤訊息不分原因**：帳號不存在或密碼錯誤一律拋出同一個
  `InvalidCredentialsException`（401，訊息「帳號或密碼錯誤」），避免帳號列舉攻擊。
- **移除 `spring.autoconfigure.exclude`**：原本排除 `SecurityAutoConfiguration` 等僅為
  骨架階段暫時繞過驗證的作法，現由正式 `SecurityConfig` 取代，故移除。

## 已知技術債 / 待辦

- C6：`UserServiceImpl`、`AuthController`、`JwtTokenProviderImpl` 尚無對應單元測試，
  需補齊 Mockito + AssertJ 測試（命名 `should_預期行為_When_條件`），目標覆蓋率 ≥80%。
- Spring Boot 仍會自動產生預設記憶體使用者密碼（`UserDetailsServiceAutoConfiguration`），
  目前無害（無控制器使用 HTTP Basic/表單登入），未來可考慮明確排除以求乾淨。

---

# Implementation Notes — 前端 UI Scaffold（首頁 / Menu / 使用者資訊）

> 產出者：前端 PG（Frontend Developer Agent）
> 日期：2026-07-31

---

## 背景

`personal-book-manager/frontend` 為 `create-vue` 預設 scaffold（Vue 3 + TypeScript + Vite +
Pinia + Vue Router），尚未有任何實際業務畫面。後端 `user`/`book`/`reading` 模組目前僅完成
Controller/DTO/介面骨架（端點會拋出 `UnsupportedOperationException`，商業邏輯尚未實作），
因此本階段前端頁面**先以本機 mock 資料建置版面與互動骨架**，待後端功能模組開發完成後
再以真實 API 呼叫取代。

## 決策記錄

**Decision**: 前端 UI 元件庫採用 Element Plus，並以本機 mock 資料驅動首頁統計卡片與使用者資訊。
**Context**: 後端 user/book/reading 業務邏輯尚未實作完成，無法立即串接真實 API；需求文件已定義
US-U03（取得使用者資訊）、US-R05（閱讀統計）等資料形狀可供參考。
**Options**:
  1. 純 Vue + 自寫 CSS（輕量，但需自行處理版面/元件一致性）
  2. Element Plus（完整元件庫，開發速度快、外觀一致）— **採用**
  3. 直接串接真實 API 並同時實作後端商業邏輯（超出本次前端任務範圍）
**Rationale**: 使用者明確選擇 Element Plus 以加速開發並確保 UI 一致性；mock 資料可讓前端版面
先行完成，不阻塞於後端進度，後續僅需替換資料來源層（`src/mocks` → 真實 API service）。
**Impact**: 新增 `element-plus`、`@element-plus/icons-vue` 相依套件；`src/mocks/mockData.ts`
與 `src/stores/user.ts` 中的 mock 依賴須於後端 API 完成後移除並改為真實呼叫。
**Review**: 後端 `AuthController`/`UserController`/`ReadingController` 完成商業邏輯實作後，
需重新檢視並移除本階段 mock 資料依賴。

---

## 追加決策紀錄（2026-08-01，參考 Eagle UI 重新設計）

**Decision**: 參考「Eagle - 圖片收集及管理必備工具」的介面風格，將導覽由「頂部橫向選單」
改為「左側深色側邊欄」，並將首頁／藏書列表改為「卡片縮圖網格畫廊」。
**Context**: 使用者要求以 Eagle（一套設計素材/圖片收集管理工具）的 UI 作為視覺參考，
Eagle 特色為左側深色資料夾樹狀導覽 + 主內容區縮圖網格瀏覽。
**Options**:
  1. 維持原本頂部選單 + 統計卡片版面（改動小，但與 Eagle 風格差異大）
  2. 側邊欄導覽 + 縮圖網格畫廊（**採用**）：符合使用者指定的參考對象，且藏書封面天生適合以
     縮圖網格呈現
**Rationale**: 圖書藏書瀏覽情境與圖片素材瀏覽情境相似（大量項目、需縮圖快速辨識、可依分類/
標籤篩選），採用 Eagle 的資訊架構可提升瀏覽效率與一致的視覺識別。
**Impact**:
  - 新增 `components/AppSidebar.vue`（取代 `AppMenu.vue`，移除該檔案）：深色側邊欄，含品牌區、
    導覽選單、`el-tree` 分類樹狀（mock 資料）、底部 `UserInfoWidget`
  - `components/UserInfoWidget.vue` 改為緊湊型（含頭像圓框），適配側邊欄底部窄版面
  - 新增 `components/BookCard.vue`：封面縮圖卡片（尚無真實封面圖時，以標題首字 + 依 id 循環
    的漸層色塊佔位；顯示標籤與閱讀狀態）
  - 新增 `types/book.ts`、mock 資料 `mockBooks`/`mockCategories`
  - `views/HomeView.vue` 新增「最近加入」縮圖畫廊區塊（沿用原統計卡片）
  - `views/BookListView.vue` 由開發中占位頁改為含搜尋列的縮圖網格畫廊（純前端 mock 篩選）
  - `layouts/MainLayout.vue` 改為左側欄 + 右側內容區（`flex` 版面，非 `el-container`）
**Review**: 待後端 book 模組 API（含封面圖片上傳/URL）完成後，`BookCard` 需改為顯示真實封面圖，
`BookListView` 搜尋/篩選需改為呼叫後端 API；側邊欄分類樹狀需改用真實分類資料。

## 新增/修改檔案

- `src/types/user.ts`、`src/types/stats.ts`：對應後端 `UserResponse` DTO 與閱讀統計資料形狀的前端型別
- `src/mocks/mockData.ts`：Scaffold 階段的假資料（目前登入使用者、閱讀統計），待後端 API 完成後移除
- `src/stores/user.ts`：Pinia store，管理目前登入使用者狀態（`currentUser`、`logout()`）
- `src/layouts/MainLayout.vue`：整體版面（Header：標題 + Menu + 使用者資訊；Main：RouterView）
- `src/components/AppMenu.vue`：頂部導覽選單（首頁／藏書管理／閱讀記錄），使用 `el-menu` + `router` 模式
- `src/components/UserInfoWidget.vue`：使用者資訊下拉選單（顯示使用者名稱、登出功能）
- `src/views/HomeView.vue`：首頁儀表板（藏書總數／累計閱讀時長／已完成書籍／本月閱讀時長 統計卡片）
- `src/views/BookListView.vue`、`src/views/ReadingView.vue`：藏書管理／閱讀記錄佔位頁（`el-empty` 開發中提示）
- `src/router/index.ts`：新增 `/books`、`/reading` 路由，移除預設 scaffold 的 `/about`
- `src/App.vue`、`src/main.ts`：改用 `MainLayout`；註冊 Element Plus 與樣式
- 移除未使用的 scaffold 預設元件：`HelloWorld.vue`、`TheWelcome.vue`、`WelcomeItem.vue`、`components/icons/`、`AboutView.vue`
- `src/assets/main.css`：移除預設 scaffold 的 `#app` 置中/grid 版面限制，改為全寬版面以配合 Header/Menu

## 驗證

- `npm run type-check`：通過（無型別錯誤）
- `npm run build`：通過（僅有 Element Plus 套件體積較大的 chunk-size 警告，非錯誤）
- `npm run dev` 啟動後以 `curl` 確認首頁回應 HTTP 200

## 追加決策紀錄（2026-08-01，Tag／Category 獨立資料表設計、樹狀分類管理與 taxonomy 模組化）

**Decision**: 將書本的分類（Category）與標籤（Tag）拆為前端獨立子模組
`frontend/src/modules/taxonomy/`，兩者對應後端各自獨立的資料表；Category 保留樹狀階層
（Adjacency List，最多 3 層），兩者皆支援使用者自訂顯示顏色；並新增對應的管理頁面
（標籤管理／分類管理）與搜尋、篩選、刪除防呆等互動。
**Context**: 使用者要求（1）分類比照標籤可自由新增、（2）Tag/Category 應為不同資料表且皆可
自訂顏色、Category 需保留樹狀階層、（3）Menu 需有「分類管理」頁面且圖示要隨顏色變化、
（4）標籤/分類管理頁需改為不規則排列（標籤雲／樹狀），點選後才可編輯，並加上搜尋 bar、
（5）刪除標籤或分類前，若仍有書籍使用中需警告並在使用者確認後自動清除書籍上的關聯設定、
（6）Tag/Category 邏輯應拉出獨立小模組以符合模組邊界慣例（比照後端 book 模組僅對外暴露
`BookQueryPort` 的作法）。
**Options（分類階層儲存策略，詳見 `docs/architecture/adr/0007-...`）**:
  1. Path Enumeration（如 `1/4/9`）
  2. Nested Set（left/right 值）
  3. Closure Table（額外的祖先-子孫關聯表）
  4. **Adjacency List（`parentId` 自我參照）— 採用**：分類數量少、深度上限僅 3 層，
     寫入（新增/搬移/刪除單一節點）成本 O(1) 優於其餘方案的查詢效能優勢，且可於單次
     `findAllByUserId` 取回後於記憶體建樹，避免依賴資料庫遞迴 CTE（H2/PostgreSQL 語法不同、
     Spring Data JPA 原生支援有限）。
**Rationale**: 使用者情境（少量分類、淺層樹狀、前端即時互動為主）不需要 Nested Set/Closure
Table 的查詢效能優化，Adjacency List 搭配前端遞迴工具（`categoryTree.ts`）即可滿足需求，
且實作與維護成本最低。Tag/Category 拆為獨立模組可避免 book 相關元件直接操作內部資料結構，
與後端 book 模組「僅對外暴露 Port 介面」的慣例一致。
**Impact**:
  - **後端設計文件**（`docs/design/design.md`）：Tag 資料表新增 `color` 欄位；新增
    `Book_Tag` 多對多關聯表說明；Category 資料表恢復 `parentId`/`sortOrder`/`color`，
    唯一鍵改為 `UNIQUE(userId, parentId, name)`；新增深度限制、循環偵測（重新指定父節點時
    需往上追溯 `parentId` 鏈避免形成環）、刪除規則（不自動級聯，前端需先引導清空）、
    查詢建樹策略之說明；API 章節新增 Tag `PUT /api/tags/{id}`、Category `parentId`/`color`/
    巢狀 `children` 回應格式。
  - **新增 ADR**：`docs/architecture/adr/0007-tag-category-separate-tables-with-hierarchy.md`
    記錄 Adjacency List 決策的完整 Options/Rationale/Impact/Review。
  - **`docs/design/tasks.md`**：模組 A（Tag/Category）之 A4/A5 任務補充階層相關的 Service
    邏輯與測試案例（深度限制、循環偵測、刪除防呆）。
  - **前端新增模組** `frontend/src/modules/taxonomy/`：
    - `types.ts`（`Category`/`Tag` 型別、`CATEGORY_MAX_DEPTH = 3`）
    - `store.ts`（`mockCategories` 樹狀 reactive 資料、`mockTags` 扁平 reactive 資料）
    - `categoryTree.ts`（樹狀操作工具：`findCategoryNode`/`depthOfCategory`/
      `findCategoryParent`/`removeCategoryNode`/`countDescendants`/`flattenCategories`）
    - `useTagColor.ts`（依標籤名稱查詢顯示顏色的 composable）
    - `views/TagManagementView.vue`、`views/CategoryManagementView.vue`
    - `index.ts`：模組對外唯一公開介面（barrel export），其餘模組不得直接
      `import` 內部檔案，僅能 `import { ... } from '@/modules/taxonomy'`
  - **標籤管理頁**（`TagManagementView.vue`）：改為不規則「標籤雲」排列（依名稱雜湊決定
    chip 大小），新增搜尋 bar 即時篩選，點選標籤才彈出 `el-dialog` 編輯面板（改名/改色/
    刪除），刪除前檢查使用書籍數，>0 則跳出確認警告，確認後自動由所有相關書籍的
    `tags` 陣列移除該標籤。
  - **分類管理頁**（`CategoryManagementView.vue`）：改為與側邊欄一致的 `el-tree` 樹狀呈現，
    每個節點同列有「+」可於該階層新增子分類（受 `CATEGORY_MAX_DEPTH` 限制），新增
    搜尋 bar（`el-tree` 內建 `filter-node-method`），點選節點才彈出編輯面板（改名/改色/
    刪除），刪除前檢查子分類數與使用書籍數，確認後子分類一併移除、書籍分類清空為
    「未分類」。
  - **側邊欄**（`AppSidebar.vue`）：分類樹節點的資料夾圖示（`FolderOpened`）直接套用
    `data.color`（取消原本另外的顏色圓點），新增「分類管理」快捷項目（與「標籤管理」並列）。
  - **書本詳細資訊面板**（`BookDetailPanel.vue`）：分類下拉選單依樹狀深度加上縮排前綴
    呈現階層關係（仍可 `allow-create` 新增頂層分類）；新增標籤輸入改用 `el-autocomplete`，
    依既有標籤名稱做模糊比對建議。
  - **路由**（`router/index.ts`）：新增 `/categories` 路由，`/tags` 與 `/categories` 皆改為
    `import('../modules/taxonomy/views/...')`。
  - **Menu 調整**：移除 Menu 頂部固定的「閱讀記錄」項目，改放到底部使用者資訊展開選單中
    （`UserInfoWidget.vue` 下拉選單新增「閱讀記錄」項目）。
**Review**: 待後端 Category/Tag API 完成後，前端 `modules/taxonomy/store.ts` 需替換為真實
API 呼叫（`src/services/*`），並移除 mock 資料；分類拖曳搬移（re-parenting）與後端循環偵測
邏輯的前端對應尚未實作，僅新增/改名/改色/刪除，如需支援請另行設計並更新本文件。

## 追加檔案清單（本次 Tag/Category 相關變更）

- 新增：`frontend/src/modules/taxonomy/{index,types,store,categoryTree,useTagColor}.ts`
- 新增：`frontend/src/modules/taxonomy/views/{TagManagementView,CategoryManagementView}.vue`
- 新增：`docs/architecture/adr/0007-tag-category-separate-tables-with-hierarchy.md`
- 刪除（搬移至模組內）：`frontend/src/types/{category,tag}.ts`、
  `frontend/src/utils/categoryTree.ts`、`frontend/src/composables/useTagColor.ts`、
  `frontend/src/views/{TagManagementView,CategoryManagementView}.vue`
- 修改：`frontend/src/mocks/mockData.ts`（移除 `mockCategories`/`mockTags`，改由 taxonomy
  模組提供）、`frontend/src/router/index.ts`、`frontend/src/components/AppSidebar.vue`、
  `frontend/src/components/BookCard.vue`、`frontend/src/components/BookListItem.vue`、
  `frontend/src/components/BookDetailPanel.vue`、`frontend/src/components/UserInfoWidget.vue`
- 修改：`docs/design/design.md`、`docs/design/tasks.md`

## 驗證（本次變更）

- `npm run type-check`：通過（無型別錯誤）
- `npm run build`：通過（僅有既有的 chunk-size 警告，非錯誤）
- `npm run dev` 啟動後以 `curl` 確認 `/`、`/books`、`/tags`、`/categories` 皆回應 HTTP 200


**標題**: [技術債務] - 前端 mock 資料需替換為真實 API 呼叫
**優先級**: 中（依賴後端 user/book/reading 商業邏輯完成進度）
**位置**: `frontend/src/mocks/mockData.ts`、`frontend/src/stores/user.ts`
**原因**: 後端 API 尚未實作完成，Scaffold 階段暫以 mock 資料建置前端版面
**影響**: 目前使用者資訊與首頁統計為假資料，未反映真實登入狀態與資料庫內容
**補救措施**: 待 `POST /api/auth/login`、`GET /api/users/me`、`GET /api/reading/stats` 等
API 完成後，新增 `src/services/` API 呼叫層，移除 `mockData.ts` 依賴並改為實際請求
**Effort**: M

---

# Implementation Notes — book 模組「新增書本」功能（PG2：Backend + Frontend）

> 產出者：後端 PG（Backend Developer Agent）
> 日期：2026-08-03
> 分支：`feature/PG2-book-create-api`（off `develop`）

---

## 背景

依 `docs/design/design.md` §3/§5a 與 `docs/design/tasks.md` 模組 A（A1/A2/A3/A6），完成
`book` 模組「新增書本」端到端功能：`Book` Entity JPA 化、`BookRepository` 轉為
`JpaRepository`、`ValidBookType` 去重驗證器、`BookServiceImpl.createBook()` 去重邏輯、
`BookController` `POST /api/books` 端點，以及前端「新增」按鈕與表單串接真實 API。
`updateBook`/`deleteBook`/`searchBooks`（A2/A3 其餘方法）、Tag/Category（A4）、封面圖片
（A7/A8）維持骨架，不在本次範圍。

## 決策紀錄

- **DB CHECK 約束以 Hibernate `@Check`/`@Checks` 實作**：`Book` Entity 加上
  `book_type_url_isbn_mutex`（依 bookType 分類的 isbn/url 互斥）與
  `cover_fields_consistency`（封面欄位一致性）兩條 CHECK 約束，作為 Bean Validation
  （`@ValidBookType`）之後的最後防線。
- **`categoryId` 於 `createBook` 不驗證存在性**：依 tasks.md A2 規格，`categoryId` FK
  驗證僅要求於 `updateBook` 實作，`createBook` 階段刻意略過，避免提前擴大 Category
  模組開發範圍。
- **開發階段暫時免登入機制（`DevNoAuthFilter`）**：前端尚未完成登入頁面/JWT 串接，
  但使用者要求前端「新增」按鈕直接呼叫真實後端 API 且暫不需登入。新增
  `DevNoAuthFilter`（僅於 `SecurityConfig` 依 `security.dev-no-auth` 設定啟用），
  於 `JwtAuthenticationFilter` 之後執行，若 `SecurityContext` 仍無認證資訊，
  自動帶入固定測試 `userId=1`；若請求本身攜帶合法 JWT，仍以真實 userId 為準（不覆蓋）。
  **設定方式（已修正）**：`security.dev-no-auth: true` 直接預設開啟於
  `src/main/resources/application-dev.yml`，故僅需以預設 `dev` profile 啟動後端即可（不需
  額外疊加 `local` profile）。為避免影響既有 401 自動化測試（如 `UserControllerTest`），
  於 `src/test/resources/application-dev.yml`（profile-specific 測試資源，Maven Surefire
  classpath 優先於 main classpath 覆寫同名 profile 設定）明確覆寫為
  `security.dev-no-auth: false`，測試仍要求合法 JWT。已移除先前的 `application-local.yml`
  多 profile 方案。
  **技術債務**：待前端完成登入頁面後應移除 `DevNoAuthFilter` 及相關
  `security.dev-no-auth` 設定，改要求所有需登入端點皆攜帶合法 JWT。
- **前端新增書本原生 `fetch` 而非 axios**：避免新增非必要相依套件；新增
  `src/utils/http.ts` 封裝，統一以 `/api` 為前綴，由 `vite.config.ts` 新增的
  `server.proxy` 轉發至 `http://localhost:8080`（開發時 frontend/backend 分開啟動，
  避免瀏覽器 CORS 限制）。
- **前端新增書本後僅本機顯示，未串接 `GET /api/books`**：`BookListView.vue` 目前清單仍
  以 `mockBooks` 為主，新增 `createdBooks` 本地陣列暫存 API 建立成功的書本並合併顯示，
  重新整理頁面後即消失；待 `searchBooks`/`GET /api/books` 完成後應改為呼叫真實清單 API。

## 已知技術債 / 待辦

- `updateBook`/`deleteBook`/`searchBooks`/`existsBook`（`BookServiceImpl`/`BookController`）
  仍為 `UnsupportedOperationException` 骨架，屬於後續 PG 任務範圍。
- `DevNoAuthFilter`／`security.dev-no-auth`（`application-dev.yml`）為暫時性技術債，
  待前端登入流程完成後應移除（見上方決策紀錄）。
- 前端 `BookListView.vue` 尚未呼叫 `GET /api/books`，新增書本僅暫存於當前瀏覽階段。
- `Category` 模組尚非真正 JPA Entity，`categoryId` 僅為欄位骨架，前端新增表單暫未提供
  分類選擇欄位。
