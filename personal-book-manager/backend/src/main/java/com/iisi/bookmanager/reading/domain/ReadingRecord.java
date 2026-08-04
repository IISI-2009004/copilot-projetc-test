package com.iisi.bookmanager.reading.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;

import java.time.Instant;
import java.time.LocalDate;

/**
 * 閱讀記錄 Entity（design.md 3./5. / tasks.md 模組 B B1）。
 *
 * <p>{@code source = OWNED} 時 {@code bookId} 必填並經 {@code BookQueryPort} 驗證，
 * {@code externalTitle}/{@code externalAuthor} 必為 null；
 * {@code source = BORROWED_FRIEND}/{@code BORROWED_LIBRARY} 時改用
 * {@code externalTitle}（必填）/{@code externalAuthor}（選填），{@code bookId} 必為 null，
 * 不涉及 book 模組（僅將 {@code userId} 視為一般外部識別碼，不直接依賴 book/user Entity）。
 *
 * <p>DB CHECK 約束（design.md 3. ReadingRecord / tasks.md B1）：
 * <ul>
 *     <li>{@code reading_source_book_external_mutex}：
 *     {@code (source='OWNED' AND bookId IS NOT NULL AND externalTitle IS NULL)}
 *     OR {@code (source<>'OWNED' AND bookId IS NULL AND externalTitle IS NOT NULL)}。</li>
 * </ul>
 * Service 層（{@code ReadingServiceImpl}）會於寫入前重複驗證，DB CHECK 作為最後防線。
 */
@Entity
@Table(name = "reading_records")
@Check(name = "reading_source_book_external_mutex", constraints =
        "(source = 'OWNED' AND book_id IS NOT NULL AND external_title IS NULL) "
                + "OR (source <> 'OWNED' AND book_id IS NULL AND external_title IS NOT NULL)")
public class ReadingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "book_id")
    private Long bookId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 30, nullable = false)
    private ReadingSource source;

    @Column(name = "external_title", length = 200)
    private String externalTitle;

    @Column(name = "external_author", length = 100)
    private String externalAuthor;

    @Column(name = "read_date", nullable = false)
    private LocalDate readDate;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "progress_percent")
    private Integer progressPercent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** JPA 要求的無參建構子。 */
    protected ReadingRecord() {
    }

    /**
     * 建立新閱讀記錄（僅用於新增流程，{@code createdAt} 由 {@link #prePersist()} 自動填入）。
     *
     * @param userId          記錄擁有者 userId
     * @param bookId          藏書 ID（{@code source=OWNED} 時必填，其餘必為 null）
     * @param source          書本來源
     * @param externalTitle   借閱書籍的書名（{@code source≠OWNED} 時必填，其餘必為 null）
     * @param externalAuthor  借閱書籍的作者（選填）
     * @param readDate        閱讀日期
     * @param durationMinutes 本次閱讀時長（分鐘，≥1）
     * @param progressPercent 進度百分比（0-100，選填）
     */
    public ReadingRecord(Long userId, Long bookId, ReadingSource source, String externalTitle,
                          String externalAuthor, LocalDate readDate, Integer durationMinutes,
                          Integer progressPercent) {
        this.userId = userId;
        this.bookId = bookId;
        this.source = source;
        this.externalTitle = externalTitle;
        this.externalAuthor = externalAuthor;
        this.readDate = readDate;
        this.durationMinutes = durationMinutes;
        this.progressPercent = progressPercent;
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

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

    public String getExternalAuthor() {
        return externalAuthor;
    }

    public LocalDate getReadDate() {
        return readDate;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public Integer getProgressPercent() {
        return progressPercent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
