package com.iisi.bookmanager.book.service;

import com.iisi.bookmanager.book.dto.BookRequest;
import com.iisi.bookmanager.book.dto.BookResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link BookService} 骨架實作（本階段僅供 Spring context 正常啟動，不含商業邏輯）。
 *
 * <p>TODO: 注入 {@code BookRepository} 並實作 tasks.md A2 的商業邏輯，屬於功能模組開發階段。
 */
@Service
public class BookServiceImpl implements BookService {

    @Override
    public BookResponse createBook(Long userId, BookRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
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
}
