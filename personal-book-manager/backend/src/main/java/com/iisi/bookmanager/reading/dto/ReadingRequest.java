package com.iisi.bookmanager.reading.dto;

import com.iisi.bookmanager.reading.domain.ReadingSource;
import com.iisi.bookmanager.reading.validation.ValidReadingSource;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 閱讀記錄新增/更新請求 DTO（design.md 5. / tasks.md B4 / ADR-0003）。
 *
 * <p>{@code bookId}/{@code externalTitle} 之間的互斥規則由 {@link ValidReadingSource}
 * class-level 驗證；{@code userId} 不含於此 DTO，禁止由前端指定。
 */
@ValidReadingSource
public record ReadingRequest(
        @NotNull(message = "source 不可為 null")
        ReadingSource source,

        Long bookId,

        String externalTitle,

        String externalAuthor,

        @NotNull(message = "readDate 不可為 null")
        LocalDate readDate,

        @Min(value = 1, message = "durationMinutes 須至少為 1")
        int durationMinutes,

        @Min(value = 0, message = "progressPercent 須介於 0-100")
        @Max(value = 100, message = "progressPercent 須介於 0-100")
        int progressPercent) {
}

