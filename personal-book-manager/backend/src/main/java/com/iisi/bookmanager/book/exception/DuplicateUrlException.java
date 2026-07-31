package com.iisi.bookmanager.book.exception;

/** 同用戶下線上內容 URL 重複時拋出（design.md 5a），由 GlobalExceptionHandler 轉換為 HTTP 409。 */
public class DuplicateUrlException extends RuntimeException {
    public DuplicateUrlException(String message) {
        super(message);
    }
}
