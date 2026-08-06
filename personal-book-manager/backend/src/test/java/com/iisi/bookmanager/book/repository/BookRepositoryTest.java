package com.iisi.bookmanager.book.repository;

import com.iisi.bookmanager.book.domain.Book;
import com.iisi.bookmanager.book.domain.BookType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import jakarta.persistence.PersistenceException;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link BookRepository} 實際查詢行為驗證（@DataJpaTest / H2 in-memory）。
 *
 * <p>驗證項目：
 * <ul>
 *     <li>{@code findByUserIdAndIsbnAndBookTypeAndDeletedFalse} 正常查詢與 deleted 過濾語意。</li>
 *     <li>{@code findByUserIdAndUrlAndDeletedFalse} 正常查詢與 deleted 過濾語意。</li>
 *     <li>DB CHECK 約束（{@code book_type_url_isbn_mutex}）違反時拋出資料庫例外。</li>
 * </ul>
 */
@DataJpaTest
@ActiveProfiles("dev")
class BookRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookRepository bookRepository;

    // ----------------------------------------------------------------
    // findByUserIdAndIsbnAndBookTypeAndDeletedFalse
    // ----------------------------------------------------------------

    @Test
    @DisplayName("findByUserIdAndIsbnAndBookTypeAndDeletedFalse 應能查到未刪除的書本")
    void should_FindBook_When_IsbnAndBookTypeMatchAndNotDeleted() {
        // Arrange
        Book book = new Book(1L, "9780000000001", null, null, "title", "author",
                BookType.PHYSICAL_BOOK, null, null, null);
        em.persistAndFlush(book);

        // Act
        Optional<Book> result = bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(
                1L, "9780000000001", BookType.PHYSICAL_BOOK);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getIsbn()).isEqualTo("9780000000001");
        assertThat(result.get().getBookType()).isEqualTo(BookType.PHYSICAL_BOOK);
    }

    @Test
    @DisplayName("findByUserIdAndIsbnAndBookTypeAndDeletedFalse 在書本已被軟刪除時應回傳 empty")
    void should_ReturnEmpty_When_BookWithSameIsbnIsDeleted() throws Exception {
        // Arrange
        Book book = new Book(1L, "9780000000002", null, null, "title", "author",
                BookType.PHYSICAL_BOOK, null, null, null);
        em.persist(book);
        // 設定 deleted=true（透過反射，因為 setter 不公開）
        Field deletedField = Book.class.getDeclaredField("deleted");
        deletedField.setAccessible(true);
        deletedField.set(book, true);
        em.persistAndFlush(book);

        // Act
        Optional<Book> result = bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(
                1L, "9780000000002", BookType.PHYSICAL_BOOK);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdAndIsbnAndBookTypeAndDeletedFalse 在不同 userId 時應回傳 empty")
    void should_ReturnEmpty_When_IsbnMatchesButDifferentUserId() {
        // Arrange
        Book book = new Book(1L, "9780000000003", null, null, "title", "author",
                BookType.PHYSICAL_BOOK, null, null, null);
        em.persistAndFlush(book);

        // Act
        Optional<Book> result = bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(
                99L, "9780000000003", BookType.PHYSICAL_BOOK);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdAndIsbnAndBookTypeAndDeletedFalse 在不同 BookType 時應回傳 empty")
    void should_ReturnEmpty_When_IsbnMatchesButDifferentBookType() {
        // Arrange
        Book book = new Book(1L, "9780000000004", null, null, "title", "author",
                BookType.PHYSICAL_BOOK, null, null, null);
        em.persistAndFlush(book);

        // Act — 以 EBOOK 查同一 ISBN，不應命中
        Optional<Book> result = bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(
                1L, "9780000000004", BookType.EBOOK);

        // Assert
        assertThat(result).isEmpty();
    }

    // ----------------------------------------------------------------
    // findByUserIdAndUrlAndDeletedFalse
    // ----------------------------------------------------------------

    @Test
    @DisplayName("findByUserIdAndUrlAndDeletedFalse 應能查到未刪除的線上書本")
    void should_FindBook_When_UrlMatchesAndNotDeleted() {
        // Arrange
        Book book = new Book(2L, null, "https://example.com/novel/1", "某平台", "title", "author",
                BookType.WEB_NOVEL, null, null, null);
        em.persistAndFlush(book);

        // Act
        Optional<Book> result = bookRepository.findByUserIdAndUrlAndDeletedFalse(
                2L, "https://example.com/novel/1");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUrl()).isEqualTo("https://example.com/novel/1");
    }

    @Test
    @DisplayName("findByUserIdAndUrlAndDeletedFalse 在書本已被軟刪除時應回傳 empty")
    void should_ReturnEmpty_When_BookWithSameUrlIsDeleted() throws Exception {
        // Arrange
        Book book = new Book(2L, null, "https://example.com/novel/2", "某平台", "title", "author",
                BookType.WEB_NOVEL, null, null, null);
        em.persist(book);
        Field deletedField = Book.class.getDeclaredField("deleted");
        deletedField.setAccessible(true);
        deletedField.set(book, true);
        em.persistAndFlush(book);

        // Act
        Optional<Book> result = bookRepository.findByUserIdAndUrlAndDeletedFalse(
                2L, "https://example.com/novel/2");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByUserIdAndUrlAndDeletedFalse 在不同 userId 時應回傳 empty")
    void should_ReturnEmpty_When_UrlMatchesButDifferentUserId() {
        // Arrange
        Book book = new Book(2L, null, "https://example.com/novel/3", "某平台", "title", "author",
                BookType.WEB_NOVEL, null, null, null);
        em.persistAndFlush(book);

        // Act
        Optional<Book> result = bookRepository.findByUserIdAndUrlAndDeletedFalse(
                99L, "https://example.com/novel/3");

        // Assert
        assertThat(result).isEmpty();
    }

    // ----------------------------------------------------------------
    // DB CHECK 約束
    // ----------------------------------------------------------------

    @Test
    @DisplayName("當實體書類型填了 url 時，儲存應拋出例外（book_type_url_isbn_mutex 約束）")
    void should_ThrowException_When_PhysicalBookHasUrl() throws Exception {
        // Arrange: 繞過 @ValidBookType Bean Validation，直接建構違規物件
        Book book = new Book(3L, null, null, null, "title", "author",
                BookType.PHYSICAL_BOOK, null, null, null);
        Field urlField = Book.class.getDeclaredField("url");
        urlField.setAccessible(true);
        urlField.set(book, "https://example.com");

        // Act & Assert
        assertThatThrownBy(() -> em.persistAndFlush(book))
                .isInstanceOf(PersistenceException.class);
    }

    @Test
    @DisplayName("當線上內容類型未填 url 時，儲存應拋出例外（book_type_url_isbn_mutex 約束）")
    void should_ThrowException_When_OnlineTypeHasNoUrl() {
        // Arrange: 線上內容但 url 為 null
        Book book = new Book(3L, null, null, null, "title", "author",
                BookType.WEB_NOVEL, null, null, null);

        // Act & Assert
        assertThatThrownBy(() -> em.persistAndFlush(book))
                .isInstanceOf(PersistenceException.class);
    }
}
