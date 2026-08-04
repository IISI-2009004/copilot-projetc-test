package com.iisi.bookmanager.book.exception;

/** 封面圖片檔案格式驗證失敗（magic bytes 判斷）時拋出（design.md 5b），轉換為 HTTP 400。 */
public class InvalidImageFileException extends RuntimeException {
    public InvalidImageFileException(String message) {
        super(message);
    }
}
