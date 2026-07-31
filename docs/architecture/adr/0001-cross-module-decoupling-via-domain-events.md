# ADR-0001: 以 Domain Event 解決 book→reading 模組邊界衝突

> **狀態：Superseded（已被取代）** — 見 [`ADR-0002`](0002-book-deletion-is-state-change-not-cascade.md)。
> 使用者於 2026-07-31 決議：刪除書本不應刪除閱讀記錄，書本刪除僅為資料狀態變更（軟刪除），
> 因此本 ADR 提出的 `BookDeletedEvent` 跨模組事件機制**不再需要**，本文件保留作為決策歷程記錄。

## Decision - 2026-07-31
**Decision**: 刪除書本時，book 模組發布 `BookDeletedEvent(bookId)`（`ApplicationEventPublisher`），
reading 模組以 `@EventListener` 監聽並清除該書本的閱讀記錄；book 模組不得直接呼叫或注入
`ReadingRecordRepository` / `ReadingService`。

**Context**:
- `requirements.md` US-B04 要求「刪除書本時同步刪除關聯的閱讀記錄」。
- `copilot-instructions.md` 規定「兩模組各自獨立資料夾，禁止互相直接存取對方 Repository」，
  且僅定義了 reading→book 方向的介面 `BookQueryPort`。
- 若 book 模組直接呼叫 reading 的 Service/Repository 以完成刪除，將違反模組邊界規則；
  若完全不處理，則閱讀記錄會變成參照已刪除書本的孤兒資料，違反 US-B04。

**Options**:
1. **book 直接呼叫 reading 的 Repository/Service** — 違反模組邊界規則，且會造成雙向依賴（book 已依賴 reading，reading 又依賴 book 的 BookQueryPort），形成循環耦合。
2. **在 reading 查詢時即時過濾已刪除書本，不物理刪除記錄** — 較簡單，但長期資料會不斷累積孤兒記錄，且不符合 US-B04 明確要求的「刪除」語意。
3. **事件驅動（Domain Event）** — book 發布事件、reading 監聽，僅耦合到一個輕量、不可變的事件物件（`bookId`），維持單向依賴關係。

**Rationale**:
選項 3 在滿足「刪除書本需清除閱讀記錄」的功能需求下，維持了模組邊界的單向依賴精神
（reading 依賴 book 的公開契約：介面 + 事件；book 完全不 import reading 的任何類別）。
搭配同交易（同步 `@EventListener`，非 `@Async`）確保刪除操作具原子性，避免因非同步事件遺失
導致資料不一致（對應 threat-model.md 的 Repudiation 分析）。

**Impact**:
- book 模組新增 `book/event/BookDeletedEvent.java`（對外公開的事件契約）。
- reading 模組新增 `reading/listener/BookDeletedEventListener.java`。
- `BookService.deleteBook()` 需注入 `ApplicationEventPublisher` 並於軟刪除後發布事件。
- 測試策略：book 模組測試「事件是否被發布」，reading 模組測試「收到事件後是否正確刪除記錄」，
  兩者可完全獨立以 Mockito 驗證，不需啟動對方模組的真實 Bean。

**Review**:
若未來拆分為微服務（book / reading 各自獨立部署），此事件機制需替換為訊息佇列
（如 Kafka/RabbitMQ）並改為最終一致性設計；屆時需重新評估同交易假設是否成立。
建議於導入多用戶（userId 隔離）或微服務化 Sprint 時重新檢視本決策。
