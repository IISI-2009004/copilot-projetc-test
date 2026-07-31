package com.iisi.bookmanager.reading.domain;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 閱讀記錄 Entity（design.md 3./5. / tasks.md 模組 B）。
 *
 * <p>{@code source = OWNED} 時 {@code bookId} 必填並經 {@code BookQueryPort} 驗證；
 * {@code source = BORROWED_FRIEND}/{@code BORROWED_LIBRARY} 時改用 {@code externalTitle}，
 * 不涉及 book 模組。
 *
 * <p>TODO: 補上 JPA 註解與欄位驗證，屬於功能模組開發階段。
 */
public class ReadingRecord {

    private Long id;
    private Long userId;
    private ReadingSource source;
    private Long bookId;
    private String externalTitle;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public ReadingSource getSource() {
        return source;
    }

    public Long getBookId() {
        return bookId;
    }

    public String getExternalTitle() {
        return externalTitle;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
