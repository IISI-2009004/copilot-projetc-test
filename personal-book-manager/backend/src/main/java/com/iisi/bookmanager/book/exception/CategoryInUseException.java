package com.iisi.bookmanager.book.exception;

/** 分類仍被書本使用中，禁止刪除時拋出，由 GlobalExceptionHandler 轉換為 HTTP 409。 */
public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(String message) {
        super(message);
    }
}
