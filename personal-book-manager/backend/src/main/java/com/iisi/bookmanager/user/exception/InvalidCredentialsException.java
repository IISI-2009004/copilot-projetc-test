package com.iisi.bookmanager.user.exception;

/**
 * 登入帳號不存在或密碼錯誤時拋出（design.md 5c / tasks.md C2）。
 *
 * <p>訊息刻意不區分「帳號不存在」與「密碼錯誤」，避免帳號列舉（Account Enumeration）攻擊，
 * 由 GlobalExceptionHandler 轉換為 HTTP 401。
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
