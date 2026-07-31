package com.iisi.bookmanager.book.controller;

import com.iisi.bookmanager.book.dto.BookRequest;
import com.iisi.bookmanager.book.dto.BookResponse;
import com.iisi.bookmanager.book.service.BookService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 書本管理端點（design.md 5./5a./5b. / tasks.md A3）。
 *
 * <p>每個端點方法第一步應呼叫 {@code CurrentUser.id()} 取得目前登入者 userId，
 * 傳入對應 Service 方法。
 *
 * <p>TODO: 補上 {@code @Valid} 請求驗證、CurrentUser 呼叫與商業邏輯串接，屬於功能模組
 * 開發階段（本階段僅建立端點簽章骨架）。
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public BookResponse createBook(BookRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, BookRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @GetMapping
    public List<BookResponse> searchBooks(@RequestParam(required = false) String keyword) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }
}
