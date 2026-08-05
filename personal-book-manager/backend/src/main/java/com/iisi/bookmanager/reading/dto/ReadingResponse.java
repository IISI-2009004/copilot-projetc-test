package com.iisi.bookmanager.reading.dto;

import com.iisi.bookmanager.reading.domain.ReadingSource;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 閱讀記錄回應 DTO（design.md 5. / tasks.md B4）。
 *
 * <p>{@code bookId} 於借閱記錄（{@code source≠OWNED}）為 null。
 */
public record ReadingResponse(Long id, ReadingSource source, Long bookId, String externalTitle,
                               String externalAuthor, LocalDate readDate, int durationMinutes,
                               int progressPercent, Instant createdAt) {
}

