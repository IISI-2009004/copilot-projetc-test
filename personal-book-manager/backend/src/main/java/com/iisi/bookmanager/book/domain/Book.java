package com.iisi.bookmanager.book.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.Checks;

import java.time.Instant;

/**
 * 書本 Entity（design.md 3. 資料模型 / tasks.md A1）。
 *
 * <p>{@code userId} 為外鍵，關聯至 user 模組的 {@code User.id}，用於多租戶資料隔離；
 * book 模組不直接依賴 user 模組的 Entity/Repository，僅將 userId 視為一般外部識別碼欄位。
 *
 * <p>DB CHECK 約束（design.md 3.）：
 * <ul>
 *     <li>{@code book_type_url_isbn_mutex}：實體／電子書類必須 {@code url IS NULL}；
 *     線上內容類必須 {@code isbn IS NULL AND url IS NOT NULL}。</li>
 *     <li>{@code cover_fields_consistency}：{@code coverImageUrl}／{@code coverImageSource}
 *     須同時為 null 或同時有值。</li>
 * </ul>
 * Service 層（{@code ValidBookTypeValidator}）會於寫入前重複驗證，DB CHECK 作為最後防線。
 */
@Entity
@Table(name = "books")
@Checks({
        @Check(name = "book_type_url_isbn_mutex", constraints =
                "(book_type IN ('PHYSICAL_BOOK','PHYSICAL_DOUJINSHI','EBOOK') AND url IS NULL) "
                        + "OR (book_type IN ('WEB_NOVEL','BLOG_POST','ONLINE_FANFIC') AND isbn IS NULL AND url IS NOT NULL)"),
        @Check(name = "cover_fields_consistency", constraints =
                "(cover_image_url IS NULL AND cover_image_source IS NULL) "
                        + "OR (cover_image_url IS NOT NULL AND cover_image_source IS NOT NULL)")
})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "isbn", length = 13)
    private String isbn;

    @Column(name = "url", length = 500)
    private String url;

    @Column(name = "source_platform", length = 100)
    private String sourcePlatform;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "author", length = 100, nullable = false)
    private String author;

    @Enumerated(EnumType.STRING)
    @Column(name = "book_type", length = 30, nullable = false)
    private BookType bookType;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "purchase_url", length = 500)
    private String purchaseUrl;

    @Column(name = "author_url", length = 500)
    private String authorUrl;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "cover_image_source", length = 20)
    private CoverImageSource coverImageSource;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** JPA 要求的無參建構子。 */
    protected Book() {
    }

    /**
     * 建立新書本（僅用於新增流程，{@code createdAt}/{@code updatedAt} 由
     * {@link #prePersist()} 自動填入，{@code deleted} 預設 false）。
     *
     * @param userId         擁有者 userId
     * @param isbn           ISBN（僅實體／電子書類適用，可為 null）
     * @param url            來源網址（僅線上內容類適用，可為 null）
     * @param sourcePlatform 來源平台名稱（選填）
     * @param title          書名／作品名
     * @param author         作者
     * @param bookType       書本類型
     * @param categoryId     分類 ID（可為 null）
     * @param purchaseUrl    購買網址（選填）
     * @param authorUrl      作者網址（選填）
     */
    public Book(Long userId, String isbn, String url, String sourcePlatform, String title,
                String author, BookType bookType, Long categoryId, String purchaseUrl, String authorUrl) {
        this.userId = userId;
        this.isbn = isbn;
        this.url = url;
        this.sourcePlatform = sourcePlatform;
        this.title = title;
        this.author = author;
        this.bookType = bookType;
        this.categoryId = categoryId;
        this.purchaseUrl = purchaseUrl;
        this.authorUrl = authorUrl;
        this.deleted = false;
    }

    @PrePersist
    protected void prePersist() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    protected void preUpdate() {
        updatedAt = Instant.now();
    }

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
