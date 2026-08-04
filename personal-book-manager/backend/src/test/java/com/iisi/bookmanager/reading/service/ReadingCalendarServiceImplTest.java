package com.iisi.bookmanager.reading.service;

import com.iisi.bookmanager.reading.domain.ReadingRecord;
import com.iisi.bookmanager.reading.domain.ReadingSource;
import com.iisi.bookmanager.reading.dto.CalendarResponse;
import com.iisi.bookmanager.reading.repository.ReadingRecordRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * {@link ReadingCalendarServiceImpl} 單元測試（tasks.md B3/B5）。
 *
 * <p>驗證依 {@code readDate} 彙整每日總分鐘數（不分來源），以及月份邊界（首日/末日）。
 *
 * <p>本測試檔實體位置放在 {@code reading} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），package 宣告仍對應正確的來源套件 {@code com.iisi.bookmanager.reading.service}。
 */
@ExtendWith(MockitoExtension.class)
class ReadingCalendarServiceImplTest {

    @Mock
    private ReadingRecordRepository readingRecordRepository;

    private ReadingCalendarServiceImpl calendarService;

    @Test
    @DisplayName("當同一天有多筆不同來源的閱讀記錄時，getMonthlyCalendar 應該加總為單日總分鐘數")
    void should_SumMinutesAcrossSources_When_SameDayHasMultipleRecords() {
        // Arrange
        calendarService = new ReadingCalendarServiceImpl(readingRecordRepository);
        ReadingRecord owned = new ReadingRecord(1L, ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 15), 30, 50);
        ReadingRecord borrowed = new ReadingRecord(1L, ReadingSource.BORROWED_FRIEND, null, "借來的書", "作者",
                LocalDate.of(2026, 8, 15), 20, 80);
        when(readingRecordRepository.findByUserIdAndReadDateBetween(
                1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(owned, borrowed));

        // Act
        List<CalendarResponse> calendar = calendarService.getMonthlyCalendar(1L, 2026, 8);

        // Assert
        assertThat(calendar).hasSize(1);
        assertThat(calendar.get(0).date()).isEqualTo(LocalDate.of(2026, 8, 15));
        assertThat(calendar.get(0).totalMinutes()).isEqualTo(50L);
    }

    @Test
    @DisplayName("當月份查詢範圍為 2 月（含閏年）時，getMonthlyCalendar 應該正確計算月末邊界日期")
    void should_UseCorrectMonthEndBoundary_When_QueryingFebruaryInLeapYear() {
        // Arrange
        calendarService = new ReadingCalendarServiceImpl(readingRecordRepository);
        when(readingRecordRepository.findByUserIdAndReadDateBetween(
                1L, LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29)))
                .thenReturn(List.of());

        // Act
        List<CalendarResponse> calendar = calendarService.getMonthlyCalendar(1L, 2028, 2);

        // Assert
        assertThat(calendar).isEmpty();
    }

    @Test
    @DisplayName("當該月無任何閱讀記錄時，getMonthlyCalendar 應該回傳空清單")
    void should_ReturnEmptyList_When_NoRecordsInMonth() {
        // Arrange
        calendarService = new ReadingCalendarServiceImpl(readingRecordRepository);
        when(readingRecordRepository.findByUserIdAndReadDateBetween(
                2L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of());

        // Act
        List<CalendarResponse> calendar = calendarService.getMonthlyCalendar(2L, 2026, 8);

        // Assert
        assertThat(calendar).isEmpty();
    }

    @Test
    @DisplayName("當跨多天有閱讀記錄時，getMonthlyCalendar 應該依日期排序回傳")
    void should_ReturnSortedByDate_When_MultipleDaysHaveRecords() {
        // Arrange
        calendarService = new ReadingCalendarServiceImpl(readingRecordRepository);
        ReadingRecord day10 = new ReadingRecord(1L, ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 10), 15, 30);
        ReadingRecord day5 = new ReadingRecord(1L, ReadingSource.OWNED, 10L, null, null,
                LocalDate.of(2026, 8, 5), 25, 60);
        when(readingRecordRepository.findByUserIdAndReadDateBetween(
                1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31)))
                .thenReturn(List.of(day10, day5));

        // Act
        List<CalendarResponse> calendar = calendarService.getMonthlyCalendar(1L, 2026, 8);

        // Assert
        assertThat(calendar).extracting(CalendarResponse::date)
                .containsExactly(LocalDate.of(2026, 8, 5), LocalDate.of(2026, 8, 10));
    }
}
