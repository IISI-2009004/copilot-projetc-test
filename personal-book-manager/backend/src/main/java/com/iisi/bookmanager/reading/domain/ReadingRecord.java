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
 * 閱讀記錄 Entity（design.md 3./5. / tasks.md B1）。
 *
 * <p>{@code source = OWNED} 時 {@code bookId} 必填、{@code externalTitle} 必為 null，
 * 新增前需經 {@code BookQueryPort} 驗證書本存在且屬於自己；
 * {@code source = BORROWED_FRIEND}/{@code BORROWED_LIBRARY} 時改為 {@code bookId} 必為
 * null、{@code externalTitle} 必填，完全不涉及 book 模組。
 *
 * <p>DB CHECK 約束（{@code source_book_external_mutex}）作為上述互斥規則的最後防線，
 * Service 層（{@code ValidReadingSourceValidator}）於寫入前已重複驗證。
 *
 * <p>{@code source}/{@code bookId}/{@code externalTitle}/{@code externalAuthor} 建立後
 * 不可修改（tasks.md B2），因此僅提供 {@link #accumulateDuration(int)} 與
 * {@link #updateProgress(int)} 供 {@code updateRecord} 使用，不提供欄位 setter。
 */
@Entity
@Table(name = "reading_records")
@Check(constraints = "(source = 'OWNED' AND book_id IS NOT NULL AND external_title IS NULL) "
        + "OR (source <> 'OWNED' AND book_id IS NULL AND external_title IS NOT NULL)")
public class ReadingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 30, nullable = false)
    private ReadingSource source;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "external_title", length = 200)
    private String externalTitle;

    @Column(name = "external_author", length = 100)
    private String externalAuthor;

    @Column(name = "read_date", nullable = false)
    private LocalDate readDate;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** JPA 要求的無參建構子。 */
    protected ReadingRecord() {
    }

    /**
     * 建立新閱讀記錄（僅用於新增流程，{@code createdAt} 由 {@link #prePersist()} 自動填入）。
     *
     * @param userId          擁有者 userId
     * @param source          閱讀來源
     * @param bookId          書本 ID（僅 {@code source=OWNED} 適用，可為 null）
     * @param externalTitle   外部書名（僅借閱來源適用，可為 null）
     * @param externalAuthor  外部作者（僅借閱來源適用，可為 null）
     * @param readDate        閱讀日期
     * @param durationMinutes 本次閱讀時長（分鐘，須 ≥ 1）
     * @param progressPercent 閱讀進度百分比（0-100）
     */
    public ReadingRecord(Long userId, ReadingSource source, Long bookId, String externalTitle,
                          String externalAuthor, LocalDate readDate, int durationMinutes, int progressPercent) {
        this.userId = userId;
        this.source = source;
        this.bookId = bookId;
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

    /**
     * 累加閱讀時長（{@code updateRecord} 用於跨日/多次閱讀時長累計，design.md 5.）。
     *
     * @param additionalMinutes 本次新增的閱讀分鐘數
     */
    public void accumulateDuration(int additionalMinutes) {
        this.durationMinutes += additionalMinutes;
    }

    /**
     * 更新閱讀進度百分比（{@code updateRecord} 用，覆蓋而非累加）。
     *
     * @param progressPercent 最新進度百分比（0-100）
     */
    public void updateProgress(int progressPercent) {
        this.progressPercent = progressPercent;
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

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public int getProgressPercent() {
        return progressPercent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
