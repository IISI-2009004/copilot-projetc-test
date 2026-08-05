package com.iisi.bookmanager.book.repository;

import com.iisi.bookmanager.book.domain.Book;
import com.iisi.bookmanager.book.domain.BookType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Book 資料存取介面（design.md 3./5a. / tasks.md A1）。
 */
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIdAndUserId(Long id, Long userId);

    Optional<Book> findByUserIdAndIsbnAndBookTypeAndDeletedFalse(Long userId, String isbn, BookType bookType);

    Optional<Book> findByUserIdAndUrlAndDeletedFalse(Long userId, String url);
}
