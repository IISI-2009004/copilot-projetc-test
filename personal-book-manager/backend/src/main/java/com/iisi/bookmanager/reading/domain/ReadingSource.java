package com.iisi.bookmanager.reading.domain;

/**
 * 閱讀記錄來源（design.md 5. / ADR-0003）。
 */
public enum ReadingSource {
    /** 個人藏書（book 模組管理），需透過 BookQueryPort 驗證書本存在。 */
    OWNED,
    /** 向朋友借閱，book 模組不知情，僅需本地 externalTitle 必填。 */
    BORROWED_FRIEND,
    /** 向圖書館借閱，book 模組不知情，僅需本地 externalTitle 必填。 */
    BORROWED_LIBRARY
}
