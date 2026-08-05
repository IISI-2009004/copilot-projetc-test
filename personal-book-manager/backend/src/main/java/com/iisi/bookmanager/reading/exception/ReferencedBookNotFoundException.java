package com.iisi.bookmanager.reading.exception;

/**
 * {@code source=OWNED} 閱讀記錄所引用的 {@code bookId} 不存在（或不屬於目前用戶）時拋出，
 * 由 GlobalExceptionHandler 轉換為 HTTP 404。
 *
 * <p>刻意不重用 book 模組的 {@code BookNotFoundException}：reading 對外只能透過
 * {@code BookQueryPort} 查詢 book 模組，不得依賴其例外類別（維持模組邊界的單向依賴原則）。
 */
public class ReferencedBookNotFoundException extends RuntimeException {
    public ReferencedBookNotFoundException(String message) {
        super(message);
    }
}
