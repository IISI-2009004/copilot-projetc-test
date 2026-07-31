package com.iisi.bookmanager.book.exception;

/** 封面圖片檔案超過 5MB 上限時拋出（design.md 5b），轉換為 HTTP 400。 */
public class ImageTooLargeException extends RuntimeException {
    public ImageTooLargeException(String message) {
        super(message);
    }
}
