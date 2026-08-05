package com.iisi.bookmanager.book.service;

import com.iisi.bookmanager.book.domain.Book;
import com.iisi.bookmanager.book.dto.BookRequest;
import com.iisi.bookmanager.book.dto.BookResponse;
import com.iisi.bookmanager.book.exception.DuplicateIsbnException;
import com.iisi.bookmanager.book.exception.DuplicateUrlException;
import com.iisi.bookmanager.book.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * {@link BookService} 實作（design.md 5./5a. / tasks.md A2）。
 *
 * <p>TODO: {@code updateBook}/{@code deleteBook}/{@code searchBooks}/{@code existsBook}
 * 屬於後續任務範圍，本次僅實作 {@code createBook}。
 */
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /**
     * 新增書本（design.md 5a 去重演算法）：
     * <ol>
     *     <li>{@code bookType.requiresIsbnDedup()} 且 {@code isbn} 不為 null：
     *     以 {@code (userId, isbn, bookType)} 查詢，若已存在拋 {@link DuplicateIsbnException}。</li>
     *     <li>{@code bookType.isOnline()}：以 {@code (userId, url)} 查詢，
     *     若已存在拋 {@link DuplicateUrlException}。</li>
     *     <li>{@code EBOOK} 不去重（允許跨平台重複購買）。</li>
     * </ol>
     * ISBN/URL 互斥格式規則已由 {@code @ValidBookType}/Bean Validation 於 Controller 層驗證，
     * 此處僅需處理去重。
     */
    @Override
    @Transactional
    public BookResponse createBook(Long userId, BookRequest request) {
        if (request.bookType().requiresIsbnDedup() && request.isbn() != null) {
            bookRepository.findByUserIdAndIsbnAndBookTypeAndDeletedFalse(userId, request.isbn(), request.bookType())
                    .ifPresent(existing -> {
                        throw new DuplicateIsbnException("同一用戶下已存在相同 ISBN 的書本：" + request.isbn());
                    });
        } else if (request.bookType().isOnline()) {
            bookRepository.findByUserIdAndUrlAndDeletedFalse(userId, request.url())
                    .ifPresent(existing -> {
                        throw new DuplicateUrlException("同一用戶下已存在相同來源網址的書本：" + request.url());
                    });
        }

        Book book = new Book(userId, request.isbn(), request.url(), request.sourcePlatform(),
                request.title(), request.author(), request.bookType(), request.categoryId(),
                request.purchaseUrl(), request.authorUrl());
        Book saved = bookRepository.save(book);
        return toResponse(saved);
    }

    @Override
    public BookResponse updateBook(Long userId, Long bookId, BookRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @Override
    public void deleteBook(Long userId, Long bookId) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @Override
    public List<BookResponse> searchBooks(Long userId, String keyword) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @Override
    public boolean existsBook(Long bookId, Long userId) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    private BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getIsbn(), book.getUrl(), book.getSourcePlatform(),
                book.getTitle(), book.getAuthor(), book.getBookType(), book.getCategoryId(),
                book.getPurchaseUrl(), book.getAuthorUrl(), book.getCoverImageUrl(),
                book.getCoverImageSource(), book.getCreatedAt(), book.getUpdatedAt());
    }
}

