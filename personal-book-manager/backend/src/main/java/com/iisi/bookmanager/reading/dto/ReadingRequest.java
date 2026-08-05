package com.iisi.bookmanager.reading.dto;

import com.iisi.bookmanager.reading.domain.ReadingSource;

import java.time.LocalDate;

/**
 * 閱讀記錄新增/更新請求 DTO（design.md 5. / ADR-0003）。
 *
 * <p>TODO: 補上 {@code @ValidReadingSource} class-level 驗證註解與各欄位 Bean Validation，
 * 屬於功能模組開發階段。
 */
public record ReadingRequest(ReadingSource source, Long bookId, String externalTitle,
                              LocalDate startDate, LocalDate endDate) {
}
