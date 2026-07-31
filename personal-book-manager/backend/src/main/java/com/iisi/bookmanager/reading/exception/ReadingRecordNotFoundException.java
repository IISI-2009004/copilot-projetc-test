package com.iisi.bookmanager.reading.exception;

/** 閱讀記錄不存在（或不屬於目前用戶）時拋出，由 GlobalExceptionHandler 轉換為 HTTP 404。 */
public class ReadingRecordNotFoundException extends RuntimeException {
    public ReadingRecordNotFoundException(String message) {
        super(message);
    }
}
