package com.iisi.bookmanager.book.service;

import com.iisi.bookmanager.book.dto.BookRequest;
import com.iisi.bookmanager.book.dto.BookResponse;
import com.iisi.bookmanager.book.port.BookQueryPort;

import java.util.List;

/**
 * 書本核心業務邏輯（design.md 5. / 5a. / tasks.md A2）。
 *
 * <p>每個方法第一個參數皆為 {@code userId}（由 Controller 透過
 * {@code CurrentUser.id()} 取得後傳入），實作 {@link BookQueryPort} 供 reading 模組使用。
 *
 * <p>TODO: 實作去重、軟刪除、分頁搜尋等商業邏輯，屬於功能模組開發階段
 * （本階段僅建立方法簽章骨架）。
 */
public interface BookService extends BookQueryPort {

    BookResponse createBook(Long userId, BookRequest request);

    BookResponse updateBook(Long userId, Long bookId, BookRequest request);

    void deleteBook(Long userId, Long bookId);

    List<BookResponse> searchBooks(Long userId, String keyword);
}
