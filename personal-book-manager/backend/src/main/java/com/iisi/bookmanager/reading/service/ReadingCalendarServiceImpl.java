package com.iisi.bookmanager.reading.service;

import com.iisi.bookmanager.reading.dto.CalendarResponse;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

/**
 * {@link ReadingCalendarService} 骨架實作（本階段僅供 Spring context 正常啟動，不含商業邏輯）。
 *
 * <p>TODO: 實作依月份彙整閱讀記錄的商業邏輯，屬於功能模組開發階段。
 */
@Service
public class ReadingCalendarServiceImpl implements ReadingCalendarService {

    @Override
    public CalendarResponse getCalendar(Long userId, YearMonth month) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }
}
