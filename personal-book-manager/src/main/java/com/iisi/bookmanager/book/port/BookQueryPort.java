package com.iisi.bookmanager.book.port;

/**
 * reading 模組透過此介面查詢 book 模組，禁止跨模組直接存取 Repository（design.md 5.）。
 *
 * <p>僅適用於 {@code source = OWNED} 的閱讀記錄；向朋友或圖書館借閱的書不呼叫此介面。
 */
public interface BookQueryPort {

    /**
     * 確認書本是否存在（未被軟刪除）且屬於指定用戶。
     *
     * @param bookId 書本 ID
     * @param userId 目前登入用戶 ID（由 reading 模組從 SecurityContext 取得後傳入）
     * @return true = 存在、未刪除、且屬於該 userId
     */
    boolean existsBook(Long bookId, Long userId);
}
