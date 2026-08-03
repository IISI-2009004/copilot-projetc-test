package com.iisi.bookmanager.book.controller;

import com.iisi.bookmanager.book.dto.BookRequest;
import com.iisi.bookmanager.book.dto.BookResponse;
import com.iisi.bookmanager.book.service.BookService;
import com.iisi.bookmanager.user.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 書本管理端點（design.md 5./5a./5b. / tasks.md A3）。
 *
 * <p>每個端點方法第一步呼叫 {@code CurrentUser.id()} 取得目前登入者 userId，
 * 傳入對應 Service 方法。
 *
 * <p>TODO: {@code updateBook}/{@code deleteBook}/{@code searchBooks} 屬於後續任務範圍，
 * 本次僅實作 {@code createBook}。
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        Long userId = CurrentUser.id();
        BookResponse response = bookService.createBook(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @RequestBody BookRequest request) {
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

