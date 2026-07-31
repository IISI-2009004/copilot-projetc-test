package com.iisi.bookmanager.book.domain;

/**
 * 封面圖片來源（design.md 5b / ADR-0005）。
 */
public enum CoverImageSource {
    /** 使用者上傳的圖片檔案，由 CoverStorageService 儲存至本機磁碟。 */
    UPLOADED,
    /** 使用者貼上的外部圖片網址，系統不下載/不預覽，僅做 scheme 驗證。 */
    EXTERNAL_URL
}
