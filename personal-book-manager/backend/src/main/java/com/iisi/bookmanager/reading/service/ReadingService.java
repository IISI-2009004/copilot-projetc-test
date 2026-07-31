package com.iisi.bookmanager.reading.service;

import com.iisi.bookmanager.reading.dto.ReadingRequest;
import com.iisi.bookmanager.reading.dto.ReadingResponse;

import java.util.List;

/**
 * 閱讀記錄核心業務邏輯（design.md 5. / tasks.md 模組 B）。
 *
 * <p>新增 {@code source = OWNED} 記錄前必須呼叫 {@code BookQueryPort.existsBook()} 確認書本存在；
 * 借閱來源（{@code BORROWED_FRIEND}/{@code BORROWED_LIBRARY}）不呼叫 book 模組。
 *
 * <p>TODO: 實作商業邏輯，屬於功能模組開發階段（本階段僅建立方法簽章骨架）。
 */
public interface ReadingService {

    ReadingResponse createRecord(Long userId, ReadingRequest request);

    ReadingResponse updateRecord(Long userId, Long recordId, ReadingRequest request);

    void deleteRecord(Long userId, Long recordId);

    List<ReadingResponse> listRecords(Long userId);
}
