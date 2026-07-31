package com.iisi.bookmanager.reading.service;

import com.iisi.bookmanager.reading.dto.ReadingRequest;
import com.iisi.bookmanager.reading.dto.ReadingResponse;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * {@link ReadingService} 骨架實作（本階段僅供 Spring context 正常啟動，不含商業邏輯）。
 *
 * <p>TODO: 注入 {@code ReadingRecordRepository}/{@code BookQueryPort} 並實作 tasks.md
 * 模組 B 的商業邏輯，屬於功能模組開發階段。
 */
@Service
public class ReadingServiceImpl implements ReadingService {

    @Override
    public ReadingResponse createRecord(Long userId, ReadingRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @Override
    public ReadingResponse updateRecord(Long userId, Long recordId, ReadingRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @Override
    public void deleteRecord(Long userId, Long recordId) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @Override
    public List<ReadingResponse> listRecords(Long userId) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }
}
