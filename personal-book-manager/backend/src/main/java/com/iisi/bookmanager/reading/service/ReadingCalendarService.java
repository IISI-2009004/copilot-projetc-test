package com.iisi.bookmanager.reading.service;

import com.iisi.bookmanager.reading.dto.CalendarResponse;

import java.time.YearMonth;

/**
 * 閱讀日曆檢視邏輯（design.md 2. 套件結構）。
 *
 * <p>TODO: 實作依月份彙整閱讀記錄的商業邏輯，屬於功能模組開發階段。
 */
public interface ReadingCalendarService {

    CalendarResponse getCalendar(Long userId, YearMonth month);
}
