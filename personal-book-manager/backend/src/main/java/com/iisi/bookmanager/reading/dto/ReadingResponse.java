package com.iisi.bookmanager.reading.dto;

import com.iisi.bookmanager.reading.domain.ReadingSource;

import java.time.LocalDate;

/**
 * 閱讀記錄回應 DTO（design.md 5.）。
 */
public record ReadingResponse(Long id, ReadingSource source, Long bookId, String externalTitle,
                               LocalDate startDate, LocalDate endDate) {
}
