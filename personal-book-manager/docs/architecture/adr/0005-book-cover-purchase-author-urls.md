# ADR-0005: 新增書本購買網址、作者網址、封面圖片欄位

## Decision - 2026-07-31
**Decision**: `Book` 新增四個欄位，不受 `bookType` 分類限制，六種類型皆可自由填寫：
- `purchaseUrl`（VARCHAR(500)，nullable）：購買網址，限 `http`/`https`。
- `authorUrl`（VARCHAR(500)，nullable）：作者網址，限 `http`/`https`。
- `coverImageUrl`（VARCHAR(500)，nullable）：封面圖片位址（上傳檔案時為本機靜態資源路徑，
  貼上網址時為外部圖片網址）。
- `coverImageSource`（ENUM(UPLOADED, EXTERNAL_URL)，nullable）：封面來源標記，供後續清理判斷用；
  與 `coverImageUrl` 需同時為 null 或同時有值（DB CHECK 約束）。

封面圖片的三種輸入方式（上傳檔案／貼上圖片／貼上圖片網址）在後端**僅對應兩條真實流程**：
「上傳檔案」與「貼上圖片（剪貼簿）」在前端都會被轉成同一種檔案 blob，
共用同一支 `POST /api/books/{id}/cover`（multipart）；「貼上圖片網址」則透過
`PUT /api/books/{id}` 直接寫入 `coverImageUrl` 字串，不觸發任何伺服器端下載行為。

檔案儲存採 **`CoverStorageService` 介面 + `LocalDiskCoverStorageService`（本機磁碟）** 實作，
檔案驗證以實際內容（magic bytes）判斷格式（僅接受 jpeg/png/webp/gif），大小上限 5MB，
儲存檔名一律以 UUID 產生。

**Context**:
- 使用者需求：「有關 book 的資料 table，再補加欄位紀錄購買網址、作者網址、書本封面。
  書本封面可以上傳圖片檔案或貼上圖片或貼上圖片網址。」
- 現有 `Book` 資料模型（ADR-0004 擴充後）已有 `isbn`/`url`/`sourcePlatform` 依 `bookType`
  分類互斥使用，但缺乏「購買連結」「作者連結」「封面圖片」這類**與書本類型無關**、
  單純作為附加中繼資料的欄位。
- 「封面圖片」需求描述了三種操作方式（上傳檔案、貼上圖片、貼上圖片網址），若照字面拆成
  三套後端流程會造成不必要的重複實作；需要釐清「上傳檔案」與「貼上圖片」在技術上其實是
  同一件事（都是把二進位圖片資料送到伺服器），只有「貼上圖片網址」在架構上真正不同
  （只存字串，不搬移二進位資料）。

**Options**:
1. **封面圖片直接存 Base64 字串於 `Book` 資料表欄位**
   → 實作最簡單，不需檔案系統或物件儲存；但會大幅膨脹資料表大小、拖慢一般書本查詢的
   I/O 效能（即使查詢不需要封面時也可能整列讀取），且不利未來遷移至 CDN／物件儲存。
2. **封面圖片存於本機磁碟／未來可替換的物件儲存，`Book` 僅存 URL/路徑字串 + 來源標記**
   → 資料表維持輕量，圖片可由靜態資源伺服器或 CDN 直接提供，`CoverStorageService` 介面
   隔離未來遷移雲端儲存（S3 等）的複雜度，且與既有 `BookQueryPort` 的介面隔離風格一致。
3. **為「上傳檔案」「貼上圖片」「貼上圖片網址」開發三支獨立 API**
   → 上傳檔案與貼上圖片在後端收到的資料型態完全相同（multipart 檔案 blob），開發三支
   API 只會造成程式碼重複與維護成本增加，沒有實質效益。

**Rationale**:
選擇「選項 2 + 僅開發兩條後端流程」的組合：
- 使用統一的 `coverImageUrl`/`coverImageSource` 欄位，讓 `BookResponse` 對前端呈現保持簡單
  （前端只需一個 URL 就能顯示封面圖），同時 `coverImageSource` 保留「是否為本機上傳檔案」
  的資訊，供刪除/取代時判斷是否需要清理實體檔案。
- 「上傳檔案」與「貼上圖片」在前端都會產生一個檔案 blob，兩者送到後端的 HTTP 請求格式
  完全一致（`multipart/form-data`），因此共用同一支 `POST /api/books/{id}/cover`，
  避免重複開發與測試成本；這點在 design.md/tasks.md 中特別註明，避免實作者誤解為需要
  三套邏輯。
- `CoverStorageService` 抽象化儲存邏輯（比照 `BookQueryPort` 的介面隔離策略），MVP 階段用
  本機磁碟實作最符合專案現況（H2 本機開發、單機部署），未來要換成 S3 等雲端儲存時只需
  新增一個實作類別並替換 Spring Bean，不影響 `BookService` 商業邏輯。
- 檔案驗證以 magic bytes（實際內容）而非副檔名或 Content-Type 判斷格式，防止偽裝惡意檔案；
  5MB 大小上限與 UUID 檔名可有效防止 DoS 與路徑穿越攻擊。
- `purchaseUrl`/`authorUrl` 不綁定 `bookType`，因為購買連結與作者連結是通用中繼資料，
  六種書本類型都可能需要（例如網路小說也可能有「作者的 Patreon 頁面」這類作者網址）。

**Impact**:
- `requirements.md`：US-B01（新增欄位敘述）、US-B03（可編輯這些欄位）、新增 US-B08
  （封面圖片三種輸入方式與驗證規則）；非功能需求新增檔案大小限制與內容驗證要求。
- `design.md`：Book 資料模型新增 `purchaseUrl`/`authorUrl`/`coverImageUrl`/`coverImageSource`
  欄位與一致性 CHECK 約束；API 新增 `POST /api/books/{id}/cover`、
  `DELETE /api/books/{id}/cover`；新增 §5b 說明 `CoverStorageService` 儲存架構與三種輸入
  方式對應的兩條後端流程；套件結構新增 `book/storage/`、`CoverImageSource` enum、
  `InvalidImageFileException`/`ImageTooLargeException`；STRIDE 摘要擴充惡意 URL 範圍、
  新增「惡意檔案上傳」列。
- `tasks.md`：A1（新欄位/CHECK約束）、A3（Request/Response 欄位、例外處理）、新增
  A7（`CoverStorageService`/`LocalDiskCoverStorageService`）、新增 A8（封面端點邏輯）、
  A5（新增封面/URL 相關測試案例）。
- `docs/security/threat-model.md`：擴充「惡意 URL」風險範圍至 `purchaseUrl`/`authorUrl`/
  封面外部網址；新增「惡意檔案上傳」威脅列；待辦技術債新增病毒掃描、雲端物件儲存遷移項目。

**Review**:
- 目前假設檔案大小上限 5MB、僅允許 jpeg/png/webp/gif 四種格式，屬架構師依常見網路書封圖片
  規格所做的預設判斷，未與使用者逐一確認確切數值；若使用者後續有不同期望（例如需要支援
  更大的高解析度封面），可調整常數值，不影響整體架構。
- 若未來使用者量增加或需要水平擴展部署，本機磁碟儲存將成為擴充瓶頸，屆時應依
  `CoverStorageService` 介面新增雲端物件儲存（S3 相容）實作並替換 Bean，無需更動
  Service 層邏輯。
- 若未來系統需要對封面外部圖片網址做「有效性預覽」或「快取」，代表系統需要主動抓取外部
  資源，屆時必須重新評估 SSRF 風險（比照 ADR-0004 中對線上內容 URL 的相同保留意見）。
