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

## 後續待辦（技術債務）

**標題**: [技術債務] - 前端 mock 資料需替換為真實 API 呼叫
**優先級**: 中（依賴後端 user/book/reading 商業邏輯完成進度）
**位置**: `frontend/src/mocks/mockData.ts`、`frontend/src/stores/user.ts`
**原因**: 後端 API 尚未實作完成，Scaffold 階段暫以 mock 資料建置前端版面
**影響**: 目前使用者資訊與首頁統計為假資料，未反映真實登入狀態與資料庫內容
**補救措施**: 待 `POST /api/auth/login`、`GET /api/users/me`、`GET /api/reading/stats` 等
API 完成後，新增 `src/services/` API 呼叫層，移除 `mockData.ts` 依賴並改為實際請求
**Effort**: M
