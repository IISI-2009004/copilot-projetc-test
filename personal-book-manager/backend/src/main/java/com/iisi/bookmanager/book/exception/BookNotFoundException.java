package com.iisi.bookmanager.book.exception;

/** 書本不存在（或不屬於目前用戶）時拋出，由 GlobalExceptionHandler 轉換為 HTTP 404。 */
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(String message) {
        super(message);
    }
}
