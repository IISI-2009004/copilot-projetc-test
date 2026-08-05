package com.iisi.bookmanager.reading.service;

import com.iisi.bookmanager.reading.domain.ReadingSource;
import com.iisi.bookmanager.reading.dto.ReadingRequest;
import com.iisi.bookmanager.reading.dto.ReadingResponse;
import com.iisi.bookmanager.reading.dto.StatsResponse;

import java.util.List;

/**
 * 閱讀記錄核心業務邏輯（design.md 5. / tasks.md B2）。
 *
 * <p>每個 public 方法第一個參數皆為 {@code userId}（由 Controller 透過
 * {@code CurrentUser.id()} 取得後傳入）。新增 {@code source = OWNED} 記錄前必須呼叫
 * {@code BookQueryPort.existsBook()} 確認書本存在且屬於自己；借閱來源
 * （{@code BORROWED_FRIEND}/{@code BORROWED_LIBRARY}）不呼叫 book 模組。
 */
public interface ReadingService {

    /**
     * 新增閱讀記錄。
     *
     * @param userId  目前登入者 userId，自動綁定於新記錄
     * @param request 新增請求
     * @return 建立後的閱讀記錄
     */
    ReadingResponse addRecord(Long userId, ReadingRequest request);

    /**
     * 更新閱讀記錄（累加 durationMinutes、覆蓋 progressPercent）。
     *
     * <p>{@code source}/{@code bookId}/{@code externalTitle}/{@code externalAuthor}
     * 建立後不可修改，此方法忽略請求中對應欄位。
     *
     * @param userId   目前登入者 userId
     * @param recordId 欲更新的記錄 ID（須屬於 userId，否則 404）
     * @param request  更新請求
     * @return 更新後的閱讀記錄
     */
    ReadingResponse updateRecord(Long userId, Long recordId, ReadingRequest request);

    /**
     * 依 bookId 分頁查詢（僅 OWNED 記錄，限同一用戶）。
     *
     * @param userId 目前登入者 userId
     * @param bookId 書本 ID
     * @param page   頁碼（0-based）
     * @param size   每頁筆數
     * @return 該書本的閱讀記錄清單
     */
    List<ReadingResponse> getRecordsByBook(Long userId, Long bookId, int page, int size);

    /**
     * 依來源分頁查詢（借閱記錄用，限同一用戶）。
     *
     * @param userId 目前登入者 userId
     * @param source 閱讀來源
     * @param page   頁碼（0-based）
     * @param size   每頁筆數
     * @return 符合來源的閱讀記錄清單
     */
    List<ReadingResponse> getRecordsBySource(Long userId, ReadingSource source, int page, int size);

    /**
     * 取得目前用戶的閱讀統計。
     *
     * @param userId 目前登入者 userId
     * @return 總閱讀時長與已完成相異書籍數
     */
    StatsResponse getStats(Long userId);
}

