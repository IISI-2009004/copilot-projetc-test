package com.iisi.bookmanager.book.exception;

/** 分類不存在或不屬於目前用戶時拋出，由 GlobalExceptionHandler 轉換為 HTTP 400。 */
public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}
