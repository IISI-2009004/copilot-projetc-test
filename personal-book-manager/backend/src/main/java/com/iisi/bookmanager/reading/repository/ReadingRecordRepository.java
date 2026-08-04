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
 * ReadingRecord 資料存取介面（design.md 3./5. / tasks.md B1）。
 */
public interface ReadingRecordRepository extends JpaRepository<ReadingRecord, Long> {

    /** 單筆查詢，限同一用戶（隔離存取，供 updateRecord 使用）。 */
    Optional<ReadingRecord> findByIdAndUserId(Long id, Long userId);

    /** 依 bookId 分頁查詢（僅 OWNED 記錄會帶有 bookId，限同一用戶）。 */
    Page<ReadingRecord> findByUserIdAndBookId(Long userId, Long bookId, Pageable pageable);

    /** 依 source 分頁查詢（借閱記錄用，限同一用戶）。 */
    Page<ReadingRecord> findByUserIdAndSource(Long userId, ReadingSource source, Pageable pageable);

    /** 依日期區間查詢（日曆用，不分來源，限同一用戶）。 */
    List<ReadingRecord> findByUserIdAndReadDateBetween(Long userId, LocalDate startInclusive, LocalDate endInclusive);

    /** 查詢目前用戶的所有閱讀記錄（統計用，limit 由呼叫端自行控制資料量）。 */
    List<ReadingRecord> findByUserId(Long userId);
}
