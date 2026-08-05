package com.iisi.bookmanager.reading.service;

import com.iisi.bookmanager.reading.dto.CalendarResponse;

import java.util.List;

/**
 * 閱讀日曆檢視邏輯（design.md 2. 套件結構 / tasks.md B3）。
 *
 * <p>依 {@code readDate} 彙整每日總分鐘（不分來源，限同一用戶）。
 */
public interface ReadingCalendarService {

    /**
     * 取得指定月份的每日閱讀時長彙整。
     *
     * @param userId 目前登入者 userId
     * @param year   年份
     * @param month  月份（1-12）
     * @return 該月每日累計分鐘數（僅包含有閱讀記錄的日期）
     */
    List<CalendarResponse> getMonthlyCalendar(Long userId, int year, int month);
}

