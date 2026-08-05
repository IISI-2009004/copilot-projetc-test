package com.iisi.bookmanager.reading.dto;

import java.time.LocalDate;

/**
 * 閱讀日曆回應 DTO（design.md 2. 套件結構 / tasks.md B3）。
 *
 * @param date         日期
 * @param totalMinutes 該日累計閱讀分鐘數（不分來源）
 */
public record CalendarResponse(LocalDate date, long totalMinutes) {
}

