package com.iisi.bookmanager.reading.repository;

import com.iisi.bookmanager.reading.domain.ReadingRecord;
import com.iisi.bookmanager.reading.domain.ReadingSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * ReadingRecord 資料存取介面（design.md 3./5. / tasks.md 模組 B B1）。
 *
 * <p>所有查詢皆以 {@code userId} 過濾，確保多用戶資料隔離（design.md §9），
 * 跨用戶存取一律查無資料，交由 Service 層轉換為 404。
 */
public interface ReadingRecordRepository extends JpaRepository<ReadingRecord, Long> {

    /**
     * 查詢指定用戶、指定藏書的閱讀記錄（分頁）。
     *
     * <p>{@code bookId} 僅在 {@code source=OWNED} 的記錄中有值，故此查詢天然僅回傳 OWNED 記錄。
     *
     * @param userId   目前登入者 userId
     * @param bookId   藏書 ID
     * @param pageable 分頁參數
     * @return 分頁後的閱讀記錄
     */
    Page<ReadingRecord> findByUserIdAndBookId(Long userId, Long bookId, Pageable pageable);

    /**
     * 查詢指定用戶、指定來源的閱讀記錄（分頁），用於篩選借閱記錄。
     *
     * @param userId   目前登入者 userId
     * @param source   書本來源
     * @param pageable 分頁參數
     * @return 分頁後的閱讀記錄
     */
    Page<ReadingRecord> findByUserIdAndSource(Long userId, ReadingSource source, Pageable pageable);

    /**
     * 查詢指定用戶在日期區間內的閱讀記錄（不分來源），用於日曆彙整。
     *
     * @param userId    目前登入者 userId
     * @param startDate 起始日期（含）
     * @param endDate   結束日期（含）
     * @return 區間內的閱讀記錄清單
     */
    List<ReadingRecord> findByUserIdAndReadDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 依 ID 查詢單筆閱讀記錄，並限制僅能存取自己的記錄（隔離存取）。
     *
     * @param id     閱讀記錄 ID
     * @param userId 目前登入者 userId
     * @return 找到則回傳記錄，否則為空（視為不存在或非自己所有）
     */
    Optional<ReadingRecord> findByIdAndUserId(Long id, Long userId);
}
