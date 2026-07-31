package com.iisi.bookmanager.user.exception;

/**
 * 註冊帳號重複時拋出（design.md 5c / tasks.md C2），由 GlobalExceptionHandler 轉換為 HTTP 409。
 */
public class DuplicateUsernameException extends RuntimeException {

    public DuplicateUsernameException(String message) {
        super(message);
    }
}
