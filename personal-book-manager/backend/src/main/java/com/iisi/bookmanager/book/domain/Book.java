package com.iisi.bookmanager.book.domain;

import java.time.Instant;

/**
 * 書本 Entity（design.md 3. 資料模型 / tasks.md A1）。
 *
 * <p>{@code userId} 為外鍵，關聯至 user 模組的 {@code User.id}，用於多租戶資料隔離；
 * book 模組不直接依賴 user 模組的 Entity/Repository，僅將 userId 視為一般外部識別碼欄位。
 *
 * <p>TODO: 補上 JPA {@code @Entity}/{@code @Table}/{@code @Id}/DB CHECK 約束對應設定，
 * 屬於功能模組開發階段（本階段僅建立欄位骨架）。
 */
public class Book {

    private Long id;
    private Long userId;
    private String isbn;
    private String url;
    private String sourcePlatform;
    private String title;
    private String author;
    private BookType bookType;
    private Long categoryId;
    private String purchaseUrl;
    private String authorUrl;
    private String coverImageUrl;
    private CoverImageSource coverImageSource;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getUrl() {
        return url;
    }

    public String getSourcePlatform() {
        return sourcePlatform;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public BookType getBookType() {
        return bookType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getPurchaseUrl() {
        return purchaseUrl;
    }

    public String getAuthorUrl() {
        return authorUrl;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public CoverImageSource getCoverImageSource() {
        return coverImageSource;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
