package com.iisi.bookmanager.book.repository;

import com.iisi.bookmanager.book.domain.Book;

import java.util.Optional;

/**
 * Book 資料存取介面（design.md 3./5a. / tasks.md A1）。
 *
 * <p>TODO: 改為繼承 {@code JpaRepository<Book, Long>} 並補上分頁/過濾查詢，
 * 屬於功能模組開發階段。
 */
public interface BookRepository {

    Optional<Book> findByIdAndUserId(Long id, Long userId);

    Optional<Book> findByUserIdAndIsbnAndBookTypeAndDeletedFalse(Long userId, String isbn,
                                                                  com.iisi.bookmanager.book.domain.BookType bookType);

    Optional<Book> findByUserIdAndUrlAndDeletedFalse(Long userId, String url);
}
