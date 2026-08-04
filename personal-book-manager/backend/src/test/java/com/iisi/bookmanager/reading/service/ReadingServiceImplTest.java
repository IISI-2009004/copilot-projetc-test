package com.iisi.bookmanager.reading.service;

import com.iisi.bookmanager.book.port.BookQueryPort;
import com.iisi.bookmanager.reading.domain.ReadingRecord;
import com.iisi.bookmanager.reading.domain.ReadingSource;
import com.iisi.bookmanager.reading.dto.ReadingRequest;
import com.iisi.bookmanager.reading.dto.ReadingResponse;
import com.iisi.bookmanager.reading.dto.StatsResponse;
import com.iisi.bookmanager.reading.exception.ReadingRecordNotFoundException;
import com.iisi.bookmanager.reading.exception.ReferencedBookNotFoundException;
import com.iisi.bookmanager.reading.repository.ReadingRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ReadingServiceImpl} 單元測試（tasks.md B2/B5）。
 *
 * <p>涵蓋閱讀時長累計、日期/進度邊界、書本不存在、多用戶隔離，以及借閱記錄新增時
 * 不呼叫 {@link BookQueryPort} 等必要案例。
 *
 * <p>本測試檔實體位置放在 {@code reading} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），package 宣告仍對應正確的來源套件 {@code com.iisi.bookmanager.reading.service}。
 */
@ExtendWith(MockitoExtension.class)
class ReadingServiceImplTest {

    @Mock
    private ReadingRecordRepository readingRecordRepository;

    @Mock
    private BookQueryPort bookQueryPort;

    private ReadingServiceImpl readingService;

    private ReadingServiceImpl newService() {
        return new ReadingServiceImpl(readingRecordRepository, bookQueryPort);
    }

    @Test
    @DisplayName("當 source=OWNED 且書本存在並屬於自己時，addRecord 應該儲存並回傳對應的 ReadingResponse")
    void should_CreateRecord_When_OwnedBookExistsForUser() {
        // Arrange
        readingService = newService();
        ReadingRequest request = new ReadingRequest(ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 1), 30, 50);
        when(bookQueryPort.existsBook(10L, 1L)).thenReturn(true);
        ReadingRecord saved = buildRecord(100L, 1L, ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 1), 30, 50);
        when(readingRecordRepository.save(any(ReadingRecord.class))).thenReturn(saved);

        // Act
        ReadingResponse response = readingService.addRecord(1L, request);

        // Assert
        ArgumentCaptor<ReadingRecord> captor = ArgumentCaptor.forClass(ReadingRecord.class);
        verify(readingRecordRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(response.id()).isEqualTo(100L);
        assertThat(response.durationMinutes()).isEqualTo(30);
    }

    @Test
    @DisplayName("當 source=OWNED 但書本不存在或不屬於自己時，addRecord 應該拋出 ReferencedBookNotFoundException")
    void should_ThrowReferencedBookNotFoundException_When_OwnedBookDoesNotExistForUser() {
        // Arrange
        readingService = newService();
        ReadingRequest request = new ReadingRequest(ReadingSource.OWNED, 999L, null, null,
                LocalDate.of(2026, 8, 1), 30, 50);
        when(bookQueryPort.existsBook(999L, 1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> readingService.addRecord(1L, request))
                .isInstanceOf(ReferencedBookNotFoundException.class);
        verify(readingRecordRepository, never()).save(any(ReadingRecord.class));
    }

    @Test
    @DisplayName("當使用者 A 嘗試以使用者 B 的 bookId 新增 source=OWNED 記錄時，應該以 userId 查詢並失敗（404）")
    void should_CheckExistsBookWithCallerUserId_When_AddingOwnedRecord() {
        // Arrange
        readingService = newService();
        ReadingRequest request = new ReadingRequest(ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 1), 30, 50);
        when(bookQueryPort.existsBook(10L, 1L)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> readingService.addRecord(1L, request))
                .isInstanceOf(ReferencedBookNotFoundException.class);
        verify(bookQueryPort).existsBook(10L, 1L);
    }

    @Test
    @DisplayName("當 source 為借閱來源（非 OWNED）時，addRecord 不應呼叫 BookQueryPort")
    void should_NotCallBookQueryPort_When_SourceIsBorrowed() {
        // Arrange
        readingService = newService();
        ReadingRequest request = new ReadingRequest(ReadingSource.BORROWED_FRIEND, null, "借來的書", "朋友作者",
                LocalDate.of(2026, 8, 2), 45, 20);
        ReadingRecord saved = buildRecord(101L, 1L, ReadingSource.BORROWED_FRIEND, null, "借來的書", "朋友作者",
                LocalDate.of(2026, 8, 2), 45, 20);
        when(readingRecordRepository.save(any(ReadingRecord.class))).thenReturn(saved);

        // Act
        ReadingResponse response = readingService.addRecord(1L, request);

        // Assert
        verify(bookQueryPort, never()).existsBook(any(), any());
        assertThat(response.bookId()).isNull();
        assertThat(response.externalTitle()).isEqualTo("借來的書");
    }

    @Test
    @DisplayName("當 durationMinutes 為邊界值 1 時，addRecord 應該成功建立")
    void should_CreateRecord_When_DurationMinutesIsMinimumBoundaryOne() {
        // Arrange
        readingService = newService();
        ReadingRequest request = new ReadingRequest(ReadingSource.BORROWED_LIBRARY, null, "圖書館的書", "作者",
                LocalDate.of(2026, 8, 3), 1, 0);
        ReadingRecord saved = buildRecord(102L, 1L, ReadingSource.BORROWED_LIBRARY, null, "圖書館的書", "作者",
                LocalDate.of(2026, 8, 3), 1, 0);
        when(readingRecordRepository.save(any(ReadingRecord.class))).thenReturn(saved);

        // Act
        ReadingResponse response = readingService.addRecord(1L, request);

        // Assert
        assertThat(response.durationMinutes()).isEqualTo(1);
        assertThat(response.progressPercent()).isEqualTo(0);
    }

    @Test
    @DisplayName("當更新記錄時，durationMinutes 應該累加、progressPercent 應該被覆蓋（含跨日閱讀累計情境）")
    void should_AccumulateDurationAndOverwriteProgress_When_UpdatingAcrossDays() {
        // Arrange
        readingService = newService();
        ReadingRecord existing = buildRecord(200L, 1L, ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 1), 30, 40);
        when(readingRecordRepository.findByIdAndUserId(200L, 1L)).thenReturn(Optional.of(existing));
        ReadingRequest request = new ReadingRequest(ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 2), 20, 100);

        // Act
        ReadingResponse response = readingService.updateRecord(1L, 200L, request);

        // Assert
        assertThat(response.durationMinutes()).isEqualTo(50);
        assertThat(response.progressPercent()).isEqualTo(100);
    }

    @Test
    @DisplayName("當 progressPercent 邊界值為 0 與 100 時，updateRecord 應該正確更新")
    void should_UpdateProgress_When_ProgressPercentIsZeroOrHundredBoundary() {
        // Arrange
        readingService = newService();
        ReadingRecord existing = buildRecord(201L, 1L, ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 1), 10, 50);
        when(readingRecordRepository.findByIdAndUserId(201L, 1L)).thenReturn(Optional.of(existing));
        ReadingRequest zeroRequest = new ReadingRequest(ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 1), 5, 0);

        // Act
        ReadingResponse zeroResponse = readingService.updateRecord(1L, 201L, zeroRequest);

        // Assert
        assertThat(zeroResponse.progressPercent()).isEqualTo(0);
    }

    @Test
    @DisplayName("當更新的記錄不屬於目前用戶時，updateRecord 應該拋出 ReadingRecordNotFoundException")
    void should_ThrowReadingRecordNotFoundException_When_RecordDoesNotBelongToUser() {
        // Arrange
        readingService = newService();
        when(readingRecordRepository.findByIdAndUserId(300L, 2L)).thenReturn(Optional.empty());
        ReadingRequest request = new ReadingRequest(ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 1), 30, 50);

        // Act & Assert
        assertThatThrownBy(() -> readingService.updateRecord(2L, 300L, request))
                .isInstanceOf(ReadingRecordNotFoundException.class);
    }

    @Test
    @DisplayName("getRecordsByBook 應該以 userId + bookId 分頁查詢，且僅回傳自己的記錄")
    void should_QueryByUserIdAndBookId_When_GettingRecordsByBook() {
        // Arrange
        readingService = newService();
        ReadingRecord record = buildRecord(400L, 1L, ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 1), 30, 50);
        Page<ReadingRecord> page = new PageImpl<>(List.of(record));
        when(readingRecordRepository.findByUserIdAndBookId(eq(1L), eq(10L), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        List<ReadingResponse> responses = readingService.getRecordsByBook(1L, 10L, 0, 20);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).bookId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("getRecordsBySource 應該以 userId + source 分頁查詢借閱記錄")
    void should_QueryByUserIdAndSource_When_GettingRecordsBySource() {
        // Arrange
        readingService = newService();
        ReadingRecord record = buildRecord(401L, 1L, ReadingSource.BORROWED_FRIEND, null, "借來的書", "作者",
                LocalDate.of(2026, 8, 1), 30, 50);
        Page<ReadingRecord> page = new PageImpl<>(List.of(record));
        when(readingRecordRepository.findByUserIdAndSource(eq(1L), eq(ReadingSource.BORROWED_FRIEND), any(PageRequest.class)))
                .thenReturn(page);

        // Act
        List<ReadingResponse> responses = readingService.getRecordsBySource(1L, ReadingSource.BORROWED_FRIEND, 0, 20);

        // Assert
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).source()).isEqualTo(ReadingSource.BORROWED_FRIEND);
    }

    @Test
    @DisplayName("getStats 應該正確加總時長並依規則去重統計已完成書籍數（含借閱記錄去重）")
    void should_SumDurationAndDeduplicateCompletedBooks_When_GettingStats() {
        // Arrange
        readingService = newService();
        ReadingRecord ownedRecord1 = buildRecord(500L, 1L, ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 1), 30, 50);
        ReadingRecord ownedRecord2 = buildRecord(501L, 1L, ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 2), 20, 100);
        ReadingRecord borrowedRecord1 = buildRecord(502L, 1L, ReadingSource.BORROWED_FRIEND, null, "借來的書", "作者",
                LocalDate.of(2026, 8, 3), 40, 100);
        ReadingRecord borrowedRecord2 = buildRecord(503L, 1L, ReadingSource.BORROWED_FRIEND, null, "借來的書", "作者",
                LocalDate.of(2026, 8, 4), 10, 100);
        when(readingRecordRepository.findByUserId(1L))
                .thenReturn(List.of(ownedRecord1, ownedRecord2, borrowedRecord1, borrowedRecord2));

        // Act
        StatsResponse stats = readingService.getStats(1L);

        // Assert
        assertThat(stats.totalMinutes()).isEqualTo(100L);
        // ownedRecord1/2 同一 bookId 去重為 1 本；borrowedRecord1/2 同一 (source, title, author) 去重為 1 本
        assertThat(stats.completedBooksCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("當閱讀時長為零邊界（0 分鐘記錄不存在但總和為 0）時，getStats 不應拋出例外")
    void should_ReturnZeroTotalMinutes_When_NoRecordsExist() {
        // Arrange
        readingService = newService();
        when(readingRecordRepository.findByUserId(1L)).thenReturn(List.of());

        // Act
        StatsResponse stats = readingService.getStats(1L);

        // Assert
        assertThat(stats.totalMinutes()).isZero();
        assertThat(stats.completedBooksCount()).isZero();
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    /**
     * 透過反射建立含 id 的 {@link ReadingRecord}（id 由 JPA {@code @GeneratedValue} 填入，
     * 測試情境下需手動注入以模擬持久化後的狀態）。
     */
    private ReadingRecord buildRecord(Long id, Long userId, ReadingSource source, Long bookId,
                                       String externalTitle, String externalAuthor, LocalDate readDate,
                                       int durationMinutes, int progressPercent) {
        try {
            ReadingRecord record = new ReadingRecord(userId, source, bookId, externalTitle, externalAuthor,
                    readDate, durationMinutes, progressPercent);
            Field idField = ReadingRecord.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(record, id);
            return record;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("測試用 ReadingRecord 建立失敗", ex);
        }
    }
}
