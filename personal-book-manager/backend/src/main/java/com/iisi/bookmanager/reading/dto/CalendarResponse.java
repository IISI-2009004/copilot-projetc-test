package com.iisi.bookmanager.reading.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * 閱讀日曆回應 DTO（design.md 2. 套件結構 / ReadingCalendarService）。
 */
public record CalendarResponse(LocalDate month, List<ReadingResponse> records) {
}
