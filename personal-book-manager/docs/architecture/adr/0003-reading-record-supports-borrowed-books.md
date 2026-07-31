# ADR-0003: 閱讀記錄支援借閱來源（不限個人藏書）

## Decision - 2026-07-31
**Decision**: `ReadingRecord` 新增 `source` 欄位（ENUM：`OWNED` / `BORROWED_FRIEND` / `BORROWED_LIBRARY`），
`bookId` 改為 **nullable**。當 `source = OWNED` 時沿用既有邏輯（`bookId` 必填，經 `BookQueryPort.existsBook()`
驗證）；當 `source = BORROWED_FRIEND` 或 `BORROWED_LIBRARY` 時，改以 `externalTitle`（必填）、
`externalAuthor`（選填）兩個自由文字欄位記錄書籍資訊，**完全不呼叫、不依賴 book 模組**。
本次僅新增來源標記與書名/作者欄位，**不**實作借閱對象、應歸還日期、歸還狀態等借閱管理功能（依使用者決議延後）。

**Context**:
- 使用者需求：「閱讀書籍紀錄不限於自己的藏書，可能是會跟其他人借、跟圖書館借書」。
- 原設計中 `ReadingRecord.bookId` 為 NOT NULL 外鍵，且新增閱讀記錄前一律透過 `BookQueryPort.existsBook(bookId)`
  驗證書本存在於個人藏書（book 模組）。若書籍是借來的，該書根本不會（也不應該）被建到 book 模組的藏書目錄中
  （book 模組的核心語意是「防止重複購買」的個人藏書管理，借來的書不涉及購買）。
- 若強迫使用者將借閱的書也建成 book 模組的一筆記錄，會混淆「藏書」與「借閱」的語意，且會誤觸發 ISBN 去重、
  分類/Tag 等專屬於個人藏書的管理功能。

**Options**:
1. **要求所有閱讀記錄都必須先在 book 模組建立書本資料**（不論是否擁有），並新增 `bookType=BORROWED` 之類的類型。
   → 會混淆 book 模組「個人藏書」的核心語意，且違反現有 US-B02（ISBN 防重複購買）的邏輯前提（借的書不該做去重）。
2. **`ReadingRecord` 允許 `bookId` 為 null，並以 `source` + 自由文字欄位記錄借閱書籍**。
   → 不影響 book 模組既有語意與邊界，reading 模組獨立擴充，符合模組邊界（reading 仍只單向依賴
   `BookQueryPort`，借閱書籍分支完全不觸碰 book 模組）。
3. **建立第三個獨立模組（如 `library` 或 `borrow`）專門管理借閱書目**。
   → 對目前規模（依使用者決議，本次不做完整借閱管理）而言過度設計，增加不必要的模組與複雜度。

**Rationale**:
選擇選項 2。以「來源分支」取代「強制建檔」，讓 reading 模組能各自處理兩種情境：
- `OWNED` 分支延續既有的 book 模組整合（`BookQueryPort` 驗證），沒有破壞既有行為。
- 借閱分支則是 reading 模組的**內部**擴充（純文字欄位 + 一個 ENUM），不新增跨模組依賴，
  維持「book 對外只暴露 `BookQueryPort`」的邊界規則不變。
- 以 DB CHECK 約束 + Service 層驗證雙重把關互斥規則（`OWNED`⇔`bookId` / `非OWNED`⇔`externalTitle`），
  避免資料進入無法歸類的中間態。
- 依使用者明確決議，本次刻意**不**做借閱對象、到期日、歸還狀態等欄位，避免範圍蔓延（scope creep）；
  待未來有實際需求時再評估。

**Impact**:
- `requirements.md`：新增 `ReadingSource` 概念說明；US-R01/R02/R03/R04/R05 更新為涵蓋所有來源；
  §4 模組邊界契約補充「借閱書籍完全不呼叫 BookQueryPort」。
- `design.md`：`ReadingRecord` 資料模型新增 `source`/`externalTitle`/`externalAuthor` 欄位與 CHECK 約束；
  `/api/reading` API 新增 `source` 查詢參數與 Request/Response 欄位；新增序列圖分支說明；
  套件結構新增 `reading/validation/ValidReadingSource.java`（互斥規則的 class-level Bean Validation）。
- `tasks.md`：B1（Entity/CHECK 約束）、B2（依 source 分支的 addRecord 邏輯 + 完成書籍去重邏輯）、
  B4（Request/Response 欄位、查詢參數）、B7（新增 `ValidReadingSource` 驗證器任務）、
  B5（新增互斥驗證、來源查詢、BookQueryPort 零呼叫等測試案例）。
- `docs/security/threat-model.md`：Tampering 項目補充自由文字欄位（`externalTitle`/`externalAuthor`）
  無法驗證真實性的殘餘風險說明（屬功能設計上可接受，非系統漏洞）。

**Review**:
若未來需求擴充為完整借閱管理（借閱對象、到期日、歸還狀態、逾期提醒），應重新評估是否將借閱相關欄位
獨立為 `reading` 模組下的子聚合（如 `BorrowedBookInfo` 值物件）或獨立模組，並重新檢視本 ADR 的適用性。
