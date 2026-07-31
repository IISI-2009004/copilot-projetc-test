package com.iisi.bookmanager.book.exception;

/** 同用戶下 ISBN + BookType 重複時拋出（design.md 5a），由 GlobalExceptionHandler 轉換為 HTTP 409。 */
public class DuplicateIsbnException extends RuntimeException {
    public DuplicateIsbnException(String message) {
        super(message);
    }
}
