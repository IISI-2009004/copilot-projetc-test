package com.iisi.bookmanager.book.service;

import com.iisi.bookmanager.book.domain.Book;
import com.iisi.bookmanager.book.domain.BookType;
import com.iisi.bookmanager.book.dto.BookRequest;
import com.iisi.bookmanager.book.dto.BookResponse;
import com.iisi.bookmanager.book.exception.DuplicateIsbnException;
import com.iisi.bookmanager.book.exception.DuplicateUrlException;
import com.iisi.bookmanager.book.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link BookServiceImpl#createBook} 單元測試（tasks.md A2/A5）。
 *
 * <p>驗證 design.md 5a 去重演算法：實體／同人誌類依 {@code (userId, isbn, bookType)}
 * 去重、線上內容類依 {@code (userId, url)} 去重、{@code EBOOK} 不去重。
 *
 * <p>ISBN/URL 互斥格式規則（{@code @ValidBookType}）屬於 Bean Validation 層責任，
 * 本測試直接呼叫 Service，不重複驗證該規則。
 */
@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    private BookServiceImpl bookService;

    private BookServiceImpl newService() {
        return new BookServiceImpl(bookRepository);
    }

    @Test
    @DisplayName("當同一用戶下已存在相同 ISBN + BookType 的書本時，createBook 應該拋出 DuplicateIsbnException")
    void should_ThrowDuplicateIsbnException_When_SameUserIsbnAndBookTypeAlreadyExists() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest("9780000000001", null, null, "title", "author",
                BookType.PHYSICAL_BOOK, null, null, null);
        Book existing = buildBook(1L, 10L, "9780000000001", null, BookType.PHYSICAL_BOOK,
                Instant.now(), Instant.now());
        when(bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(10L, "9780000000001", BookType.PHYSICAL_BOOK))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> bookService.createBook(10L, request))
                .isInstanceOf(DuplicateIsbnException.class);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("當同一用戶下已存在相同來源網址的線上內容時，createBook 應該拋出 DuplicateUrlException")
    void should_ThrowDuplicateUrlException_When_SameUserUrlAlreadyExists() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest(null, "https://example.com/novel/1", "AO3", "title", "author",
                BookType.WEB_NOVEL, null, null, null);
        Book existing = buildBook(2L, 20L, null, "https://example.com/novel/1", BookType.WEB_NOVEL,
                Instant.now(), Instant.now());
        when(bookRepository.findByUserIdAndUrlAndDeletedFalse(20L, "https://example.com/novel/1"))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> bookService.createBook(20L, request))
                .isInstanceOf(DuplicateUrlException.class);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("當 bookType 為 EBOOK 時，createBook 不應執行任何去重查詢，直接儲存")
    void should_NotCheckDuplicate_When_BookTypeIsEbook() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest("9780000000002", null, null, "title", "author",
                BookType.EBOOK, null, null, null);
        Book saved = buildBook(3L, 30L, "9780000000002", null, BookType.EBOOK,
                Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:00:00Z"));
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // Act
        BookResponse response = bookService.createBook(30L, request);

        // Assert
        verify(bookRepository, never()).findByUserIdAndIsbnAndBookTypeAndDeletedFalse(any(), any(), any());
        verify(bookRepository, never()).findByUserIdAndUrlAndDeletedFalse(any(), any());
        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.bookType()).isEqualTo(BookType.EBOOK);
    }

    @Test
    @DisplayName("當實體書 ISBN 不重複時，createBook 應該儲存並回傳對應的 BookResponse")
    void should_CreateBook_When_PhysicalBookIsbnNotDuplicate() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest("9780000000003", null, null, "書名", "作者",
                BookType.PHYSICAL_BOOK, 5L, "https://shop.example.com/item", "https://author.example.com");
        when(bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(40L, "9780000000003", BookType.PHYSICAL_BOOK))
                .thenReturn(Optional.empty());
        Book saved = buildBook(4L, 40L, "9780000000003", null, BookType.PHYSICAL_BOOK,
                Instant.parse("2026-03-01T00:00:00Z"), Instant.parse("2026-03-01T00:00:00Z"));
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // Act
        BookResponse response = bookService.createBook(40L, request);

        // Assert
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());
        assertThat(bookCaptor.getValue().getUserId()).isEqualTo(40L);
        assertThat(bookCaptor.getValue().getIsbn()).isEqualTo("9780000000003");
        assertThat(bookCaptor.getValue().getTitle()).isEqualTo("書名");

        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.isbn()).isEqualTo("9780000000003");
        assertThat(response.bookType()).isEqualTo(BookType.PHYSICAL_BOOK);
    }

    @Test
    @DisplayName("當實體書未填 ISBN 時，createBook 不應執行 ISBN 去重查詢，直接儲存")
    void should_SkipIsbnDedupCheck_When_IsbnIsNull() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest(null, null, null, "同人誌", "作者",
                BookType.PHYSICAL_DOUJINSHI, null, null, null);
        Book saved = buildBook(5L, 50L, null, null, BookType.PHYSICAL_DOUJINSHI,
                Instant.now(), Instant.now());
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // Act
        bookService.createBook(50L, request);

        // Assert
        verify(bookRepository, never()).findByUserIdAndIsbnAndBookTypeAndDeletedFalse(any(), any(), any());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    @DisplayName("當線上內容 URL 不重複時，createBook 應該儲存並回傳對應的 BookResponse")
    void should_CreateBook_When_OnlineUrlNotDuplicate() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest(null, "https://example.com/blog/1", "個人 Blog", "文章標題",
                "作者", BookType.BLOG_POST, null, null, null);
        when(bookRepository.findByUserIdAndUrlAndDeletedFalse(60L, "https://example.com/blog/1"))
                .thenReturn(Optional.empty());
        Book saved = buildBook(6L, 60L, null, "https://example.com/blog/1", BookType.BLOG_POST,
                Instant.now(), Instant.now());
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // Act
        BookResponse response = bookService.createBook(60L, request);

        // Assert
        assertThat(response.url()).isEqualTo("https://example.com/blog/1");
        assertThat(response.bookType()).isEqualTo(BookType.BLOG_POST);
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    @Test
    @DisplayName("當 WEB_NOVEL 類型的 URL 在同一用戶下已存在時，createBook 應該拋出 DuplicateUrlException")
    void should_ThrowDuplicateUrlException_When_WebNovelUrlAlreadyExists() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest(null, "https://novel.example.com/story/1", "某平台", "title", "author",
                BookType.WEB_NOVEL, null, null, null);
        Book existing = buildBook(10L, 10L, null, "https://novel.example.com/story/1", BookType.WEB_NOVEL,
                Instant.now(), Instant.now());
        when(bookRepository.findByUserIdAndUrlAndDeletedFalse(10L, "https://novel.example.com/story/1"))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> bookService.createBook(10L, request))
                .isInstanceOf(DuplicateUrlException.class);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("當 ONLINE_FANFIC 類型的 URL 在同一用戶下已存在時，createBook 應該拋出 DuplicateUrlException")
    void should_ThrowDuplicateUrlException_When_OnlineFanficUrlAlreadyExists() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest(null, "https://ao3.org/works/12345", "AO3", "title", "author",
                BookType.ONLINE_FANFIC, null, null, null);
        Book existing = buildBook(11L, 10L, null, "https://ao3.org/works/12345", BookType.ONLINE_FANFIC,
                Instant.now(), Instant.now());
        when(bookRepository.findByUserIdAndUrlAndDeletedFalse(10L, "https://ao3.org/works/12345"))
                .thenReturn(Optional.of(existing));

        // Act & Assert
        assertThatThrownBy(() -> bookService.createBook(10L, request))
                .isInstanceOf(DuplicateUrlException.class);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    @DisplayName("當相同 ISBN 但不同 bookType 時，createBook 不應視為重複，應成功儲存")
    void should_CreateBook_When_SameIsbnButDifferentBookType() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest("9780000000004", null, null, "title", "author",
                BookType.PHYSICAL_BOOK, null, null, null);
        // 查詢 (userId, isbn, PHYSICAL_BOOK) 時無結果（即使 EBOOK 有相同 ISBN）
        when(bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(70L, "9780000000004", BookType.PHYSICAL_BOOK))
                .thenReturn(Optional.empty());
        Book saved = buildBook(12L, 70L, "9780000000004", null, BookType.PHYSICAL_BOOK,
                Instant.now(), Instant.now());
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // Act
        BookResponse response = bookService.createBook(70L, request);

        // Assert
        verify(bookRepository).findByUserIdAndIsbnAndBookTypeAndDeletedFalse(70L, "9780000000004", BookType.PHYSICAL_BOOK);
        assertThat(response.id()).isEqualTo(12L);
    }

    @Test
    @DisplayName("當相同 ISBN 但不同 userId 時，createBook 不應視為重複，應成功儲存")
    void should_CreateBook_When_SameIsbnButDifferentUserId() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest("9780000000005", null, null, "title", "author",
                BookType.PHYSICAL_BOOK, null, null, null);
        // userId=80 的查詢無結果（userId=70 有相同 ISBN 不影響）
        when(bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(80L, "9780000000005", BookType.PHYSICAL_BOOK))
                .thenReturn(Optional.empty());
        Book saved = buildBook(13L, 80L, "9780000000005", null, BookType.PHYSICAL_BOOK,
                Instant.now(), Instant.now());
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // Act
        BookResponse response = bookService.createBook(80L, request);

        // Assert
        assertThat(response.id()).isEqualTo(13L);
    }

    @Test
    @DisplayName("當相同 URL 但不同 userId 時，createBook 不應視為重複，應成功儲存")
    void should_CreateBook_When_SameUrlButDifferentUserId() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest(null, "https://example.com/blog/shared", "Blog", "title", "author",
                BookType.BLOG_POST, null, null, null);
        // userId=90 的查詢無結果（其他 userId 有相同 URL 不影響）
        when(bookRepository.findByUserIdAndUrlAndDeletedFalse(90L, "https://example.com/blog/shared"))
                .thenReturn(Optional.empty());
        Book saved = buildBook(14L, 90L, null, "https://example.com/blog/shared", BookType.BLOG_POST,
                Instant.now(), Instant.now());
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // Act
        BookResponse response = bookService.createBook(90L, request);

        // Assert
        assertThat(response.id()).isEqualTo(14L);
    }

    @Test
    @DisplayName("當 categoryId/purchaseUrl/authorUrl 均為 null 時，createBook 仍應成功建立書本")
    void should_CreateBook_When_OptionalFieldsAreNull() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest("9780000000006", null, null, "最簡單的書", "作者",
                BookType.PHYSICAL_BOOK, null, null, null);
        when(bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(100L, "9780000000006", BookType.PHYSICAL_BOOK))
                .thenReturn(Optional.empty());
        Book saved = buildBook(15L, 100L, "9780000000006", null, BookType.PHYSICAL_BOOK,
                Instant.now(), Instant.now());
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // Act
        BookResponse response = bookService.createBook(100L, request);

        // Assert
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoryId()).isNull();
        assertThat(captor.getValue().getPurchaseUrl()).isNull();
        assertThat(captor.getValue().getAuthorUrl()).isNull();
        assertThat(response.id()).isEqualTo(15L);
    }

    @Test
    @DisplayName("當 deleted=true 的舊書本具有相同 ISBN 時，createBook 不應視為重複，應成功儲存")
    void should_CreateBook_When_ExistingBookWithSameIsbnIsDeleted() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest("9780000000007", null, null, "復活的書", "作者",
                BookType.PHYSICAL_BOOK, null, null, null);
        // findByUserIdAndIsbnAndBookTypeAndDeletedFalse 不回傳 deleted=true 的書（語意上已過濾）
        when(bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(110L, "9780000000007", BookType.PHYSICAL_BOOK))
                .thenReturn(Optional.empty());
        Book saved = buildBook(16L, 110L, "9780000000007", null, BookType.PHYSICAL_BOOK,
                Instant.now(), Instant.now());
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // Act
        BookResponse response = bookService.createBook(110L, request);

        // Assert
        assertThat(response.id()).isEqualTo(16L);
    }

    @Test
    @DisplayName("當 deleted=true 的舊書本具有相同 URL 時，createBook 不應視為重複，應成功儲存")
    void should_CreateBook_When_ExistingBookWithSameUrlIsDeleted() {
        // Arrange
        bookService = newService();
        BookRequest request = new BookRequest(null, "https://example.com/old-post", "Blog", "再次分享", "作者",
                BookType.BLOG_POST, null, null, null);
        // findByUserIdAndUrlAndDeletedFalse 不回傳 deleted=true 的書
        when(bookRepository.findByUserIdAndUrlAndDeletedFalse(110L, "https://example.com/old-post"))
                .thenReturn(Optional.empty());
        Book saved = buildBook(17L, 110L, null, "https://example.com/old-post", BookType.BLOG_POST,
                Instant.now(), Instant.now());
        when(bookRepository.save(any(Book.class))).thenReturn(saved);

        // Act
        BookResponse response = bookService.createBook(110L, request);

        // Assert
        assertThat(response.id()).isEqualTo(17L);
    }

    /**
     * 透過反射建立含 id/createdAt/updatedAt 的 {@link Book}（這三個欄位分別由
     * JPA {@code @GeneratedValue}／{@code @PrePersist} 填入，測試情境下需手動注入
     * 以模擬持久化後的狀態）。
     */
    private Book buildBook(Long id, Long userId, String isbn, String url, BookType bookType,
                           Instant createdAt, Instant updatedAt) {
        try {
            Constructor<Book> constructor = Book.class.getDeclaredConstructor(Long.class, String.class, String.class,
                    String.class, String.class, String.class, BookType.class, Long.class, String.class, String.class);
            constructor.setAccessible(true);
            Book book = constructor.newInstance(userId, isbn, url, null, "title", "author", bookType, null, null, null);

            Field idField = Book.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(book, id);

            Field createdAtField = Book.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(book, createdAt);

            Field updatedAtField = Book.class.getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(book, updatedAt);

            return book;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("測試用 Book 建立失敗", ex);
        }
    }
}
