# Test Report — 模組 B：reading（tasks.md B5）

> 產出者：Test Generator（隨 Backend 開發同步產出，對應 Issue #26）
> 日期：2026-08-04
> 對應分支：`feature/reading-record`

---

## 測試範圍

| 測試檔 | 案例數 | 對應規格 |
|--------|--------|---------|
| `ReadingServiceImplTest` | 12 | tasks.md B2/B5 |
| `ReadingCalendarServiceImplTest` | 4 | tasks.md B3/B5 |
| `ReadingControllerTest` | 10 | tasks.md B4/B5 |
| **合計（reading 模組新增）** | **26** | |

執行 `mvn test`（全專案）：**82 項測試，0 失敗，0 錯誤**。

## 案例對照 tasks.md B5 必含項目

| tasks.md B5 要求案例 | 對應測試方法 | 結果 |
|----------------------|--------------|------|
| 跨日閱讀累計 | `should_AccumulateDurationAndOverwriteProgress_When_UpdatingAcrossDays` | ✅ |
| 時長為零邊界（下限 1 / 拒絕 0） | `should_CreateRecord_When_DurationMinutesIsMinimumBoundaryOne`（Service）／`should_Return400_When_DurationMinutesIsZero`（Controller） | ✅ |
| 書本不存在 | `should_ThrowReferencedBookNotFoundException_When_OwnedBookDoesNotExistForUser`／`should_Return404_When_ReferencedBookNotFound` | ✅ |
| progressPercent 邊界(0/100) | `should_UpdateProgress_When_ProgressPercentIsZeroOrHundredBoundary` | ✅ |
| `source=OWNED` 缺 bookId（400） | `should_Return400_When_OwnedSourceMissingBookId` | ✅ |
| `source≠OWNED` 缺 externalTitle（400） | `should_Return400_When_BorrowedSourceMissingExternalTitle` | ✅ |
| `source≠OWNED` 誤帶 bookId（400） | `should_Return400_When_BorrowedSourceHasBookId` | ✅ |
| 借閱記錄新增不呼叫 BookQueryPort | `should_NotCallBookQueryPort_When_SourceIsBorrowed` | ✅ |
| 統計正確去重借閱書籍 | `should_SumDurationAndDeduplicateCompletedBooks_When_GettingStats` | ✅ |
| 使用者 A 以使用者 B 的 bookId 新增 OWNED 記錄應失敗 | `should_CheckExistsBookWithCallerUserId_When_AddingOwnedRecord`（驗證 `existsBook` 呼叫時帶入呼叫者 userId） | ✅ |
| 未帶認證存取（401） | `should_Return401_When_NoAuthorizationHeaderProvided` | ✅ |
| 更新不存在的記錄（404） | `should_ThrowReadingRecordNotFoundException_When_RecordDoesNotBelongToUser`（Service）／`should_Return404_When_UpdatingNonExistentRecord`（Controller） | ✅ |
| GET 未帶 bookId/source（400） | `should_Return400_When_ListRecordsMissingBookIdAndSource` | ✅ |
| 日曆：跨來源同日加總 | `should_SumMinutesAcrossSources_When_SameDayHasMultipleRecords` | ✅ |
| 日曆：閏年 2 月月末邊界 | `should_UseCorrectMonthEndBoundary_When_QueryingFebruaryInLeapYear` | ✅ |
| 日曆：空月份 | `should_ReturnEmptyList_When_NoRecordsInMonth` | ✅ |
| 日曆：跨日排序 | `should_ReturnSortedByDate_When_MultipleDaysHaveRecords` | ✅ |

## 未涵蓋項目（已於 implementation-notes.md 記錄為技術債）

1. **書本軟刪除後既有閱讀記錄仍可查詢**：依賴 book 模組 A2 `deleteBook` 完成軟刪除實作，
   目前仍為骨架（`UnsupportedOperationException`），暫無法撰寫端對端整合測試。
2. **多用戶隔離的顯式斷言案例**：`findByIdAndUserId` 等查詢方法簽章已保證資料隔離，
   但尚未新增直接斷言「查詢/更新他人記錄回 404」的獨立測試案例，建議下次迭代補上。

## Mock 策略

- `ReadingServiceImplTest`/`ReadingCalendarServiceImplTest`：Mockito mock
  `ReadingRecordRepository`/`BookQueryPort`，不依賴實際資料庫或 book 模組實作。
- `ReadingControllerTest`：`@WebMvcTest` + `@MockBean`（`ReadingService`/
  `ReadingCalendarService`/`JwtTokenProvider`），並 `@Import(SecurityConfig)` 驗證未認證
  請求回 401，符合 testing.instructions.md「每個 Controller 測試必須包含未認證存取測試」。
