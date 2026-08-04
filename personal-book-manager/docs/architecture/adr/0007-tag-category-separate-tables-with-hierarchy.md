# ADR-0007: Tag／Category 分離為獨立資料表，Category 恢復樹狀階層並支援使用者自訂顏色

## Decision - 2026-08-01
**Decision**: `Tag` 與 `Category` 維持**各自獨立的資料表**（原設計即為兩張表，本次無需合併/拆分），
並在兩者上新增 `color VARCHAR(7)`（nullable，`#RRGGBB` 格式）供使用者自訂顯示顏色。

`Category` 額外新增 `parentId`（FK → `category.id`，nullable）與 `sortOrder`（INT）欄位，
採 **Adjacency List（自我參照）模式**恢復樹狀階層結構：
- 唯一性約束由 `UNIQUE(userId, name)` 調整為 `UNIQUE(userId, parentId, name)`（同層才需唯一）。
- 階層深度上限 **3 層**（頂層 → 子分類 → 孫分類），由 Service 層於建立/搬移時計算深度驗證。
- 搬移分類（變更 `parentId`）時，Service 層沿父鏈向上追溯，偵測是否形成循環參照。
- 查詢採「一次撈取該使用者所有分類、於記憶體組樹」策略（`findAllByUserId`），不使用遞迴 SQL／CTE。
- 刪除規則不變：若分類仍有子分類或書本歸類，拋 `CategoryInUseException`（409），不自動級聯刪除。

`Tag` 維持原有扁平清單設計，不受本次調整影響（僅新增 `color` 欄位）。

**Context**:
- 使用者需求：「幫我設計 Tag 和 分類 要不同的 DB table，使用者也可以自己設定顯示顏色。此外
  分類 要保留之前的樹狀階層式，要如何管理這分類階層幫我設計。」
- 前一輪迭代（前端 Eagle UI 重新設計期間）曾將 Category 簡化為與 Tag 相同的扁平清單
  （純粹因應「分類也能像 Tag 一樣自由新增」的前端需求），但原始 design.md 對 `Category`
  本就是獨立資料表，僅缺乏 `parentId` 樹狀欄位；本次需求是在保留「可自由新增」特性的同時，
  **恢復**分類原本規劃的樹狀階層能力，並為 Tag／Category 都加上顏色自訂。

**Options**（分類階層管理策略）:
1. **Adjacency List（`parentId` 自我參照）**
   → 結構簡單、單一欄位即可表達任意深度樹狀關係；新增/刪除/搬移單一節點的寫入成本為 O(1)；
     缺點是查詢「某節點的所有子孫」需遞迴或於應用層組樹（但本專案分類數量小，可接受一次性
     `findAllByUserId` 全量查詢 + 記憶體組樹，效能無虞）。
2. **Path Enumeration（如 `path = '1/4/9'` 字串欄位）**
   → 查詢子孫節點效率高（`LIKE 'path%'`），但搬移子樹時需要級聯更新所有子孫的 `path` 字串，
     寫入成本高且容易因字串處理出錯；對「使用者可自由搬移分類」的需求不友善。
3. **Nested Set Model（`lft`/`rgt` 區間編碼）**
   → 查詢效能最佳，但每次新增/刪除/搬移節點都需要重新計算大量鄰近節點的 `lft`/`rgt` 值，
     寫入成本最高、實作與維護複雜度也最高，明顯不符合「小型個人藏書分類」的資料規模與
     使用頻率（讀多寫少但寫入操作複雜度不應過高）。
4. **Closure Table（額外一張「祖先-子孫」關係表）**
   → 查詢彈性最高（任意深度祖先/子孫查詢皆為單一 JOIN），但需要額外維護一張關聯表，
     對於深度上限僅 3 層、資料量小（單一使用者的分類數通常為個位數到數十筆）的場景而言，
     屬於過度工程（over-engineering）。

**Rationale**:
選擇 **Adjacency List** 作為分類階層管理策略：
- 深度已限制為 3 層，資料量以「個人藏書分類」規模而言非常小（單一使用者可能僅十餘筆），
  Path Enumeration／Nested Set／Closure Table 換取的查詢效能優勢在此規模下並不明顯，
  但它們在「搬移子樹」時的寫入複雜度／額外資料維護成本，相較 Adjacency List 单純更新
  一個 `parentId` 欄位，明顯不划算。
- Adjacency List 與 JPA/Hibernate 的自我參照 `@ManyToOne`/`@OneToMany` 對應方式最直覺，
  符合本專案「Spring Data JPA + 分層架構」的既有慣例，維護成本最低。
- 「於 Service 層記憶體組樹」策略避免了資料庫遞迴 CTE（H2/PostgreSQL 語法可能不完全一致，
  且 Spring Data JPA 對遞迴查詢支援有限），改用一次性查詢 + 應用層遞迴組樹，簡單可靠且
  效能足夠（讀取頻率遠高於分類數量成長速度）。
- 循環參照防護與深度上限皆在 Service 層以簡單的「沿 `parentId` 向上遍歷」演算法實作，
  不需資料庫層級的複雜約束，且能提供清楚的錯誤訊息（`InvalidCategoryParentException`／
  `CategoryDepthExceededException`）。
- Tag／Category 皆新增 `color` 欄位，因為兩者都是「使用者自訂的視覺化分類標記」，讓前端
  能以顏色區分不同標籤/分類（如 Eagle 的標籤色塊風格）；欄位設計為可 null 的 `VARCHAR(7)`
  （`#RRGGBB`），null 時前端以預設色票（依 id 循環）呈現，避免強制使用者為每個項目選色。
- Tag 與 Category 维持分離資料表：兩者的語意本就不同（Tag 為多對多、可任意組合；Category
  為多對一、且现在需要階層），共用一張表反而需要額外欄位區分型別並犧牲型別安全，
  獨立資料表更符合單一職責與既有的 `TagRepository`/`CategoryRepository` 分離慣例。

**Impact**:
- `design.md`：
  - `Tag` 資料表新增 `color` 欄位；新增獨立的 `Book_Tag` 關聯表說明（複合主鍵）。
  - `Category` 資料表重新加入 `parentId`（自我參照 FK）、`color`、`sortOrder` 欄位；
    唯一性約束調整為 `UNIQUE(userId, parentId, name)`；新增階層深度上限、循環參照防護、
    刪除規則、查詢方式（記憶體組樹）等設計說明段落。
  - API 規格：`/api/tags` 新增 `PUT /api/tags/{id}`（改名/改色）、`TagRequest`/`TagResponse`
    新增 `color` 欄位；`/api/categories` 的 `POST`/`PUT` 新增 `parentId`/`color` 欄位，
    `CategoryResponse` 新增巢狀 `children` 欄位供前端直接渲染樹狀結構。
  - 套件結構：`dto/` 新增 `TagRequest`/`TagResponse`/`CategoryRequest`/`CategoryResponse`；
    `exception/` 新增 `CategoryDepthExceededException`、`InvalidCategoryParentException`、
    `TagNotFoundException`。
- `tasks.md`：A4（`TagService`/`CategoryService` 邏輯調整為含 color/階層管理）、A5（新增
  分類階層相關測試案例：巢狀 children 回應、深度上限、循環參照、color 格式驗證）。
- 前端（Scaffold 階段，Eagle UI）：`mockCategories` 由扁平字串陣列恢復為樹狀結構
  （含 `color` 欄位），`AppSidebar.vue` 分類區恢復樹狀渲染並支援顏色顯示與拖曳/選單調整
  父子關係（Scaffold 階段以簡化互動實作，正式串接後端 API 前不做複雜拖曳排序）。

**Review**:
- 目前深度上限設為 3 層是架構師依「個人藏書分類」常見複雜度所做的預設判斷（例如
  「技術／前端／Vue 生態」三層已相當細緻），未與使用者逐一確認確切層數需求；若使用者
  後續認為需要更深或更淺的階層，可調整 Service 層的深度檢查常數，不影響整體 Adjacency
  List 架構。
- 若未來分類數量大幅成長（例如支援「分類範本市集」等多租戶共享情境），或查詢子孫節點的
  頻率遠高於寫入/搬移頻率，屆時可重新評估改用 Path Enumeration 或 Closure Table，
  但以目前「個人使用」的資料規模與存取模式，Adjacency List 已足夠。
