package com.iisi.bookmanager.reading.service;

import com.iisi.bookmanager.book.port.BookQueryPort;
import com.iisi.bookmanager.reading.domain.ReadingRecord;
import com.iisi.bookmanager.reading.domain.ReadingSource;
import com.iisi.bookmanager.reading.dto.ReadingRequest;
import com.iisi.bookmanager.reading.dto.ReadingResponse;
import com.iisi.bookmanager.reading.dto.StatsResponse;
import com.iisi.bookmanager.reading.exception.ReadingRecordNotFoundException;
import com.iisi.bookmanager.reading.exception.ReferencedBookNotFoundException;
import com.iisi.bookmanager.reading.repository.ReadingRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link ReadingService} 實作（design.md 5. / tasks.md B2）。
 *
 * <p>{@code source=OWNED} 記錄的書本存在性檢查透過注入的 {@link BookQueryPort} 完成，
 * reading 模組不直接依賴 book 模組的 Repository/Entity，維持模組邊界。
 */
@Service
public class ReadingServiceImpl implements ReadingService {

    private final ReadingRecordRepository readingRecordRepository;
    private final BookQueryPort bookQueryPort;

    public ReadingServiceImpl(ReadingRecordRepository readingRecordRepository, BookQueryPort bookQueryPort) {
        this.readingRecordRepository = readingRecordRepository;
        this.bookQueryPort = bookQueryPort;
    }

    /**
     * 新增閱讀記錄（design.md 5.）：{@code source=OWNED} 時先呼叫
     * {@code BookQueryPort.existsBook(bookId, userId)}，不存在或非自己所有拋
     * {@link ReferencedBookNotFoundException}（404）；{@code source≠OWNED} 時完全不呼叫
     * book 模組，{@code bookId}/{@code externalTitle} 互斥規則已由
     * {@code @ValidReadingSource} 於 Controller 層驗證。
     */
    @Override
    @Transactional
    public ReadingResponse addRecord(Long userId, ReadingRequest request) {
        if (request.source() == ReadingSource.OWNED
                && !bookQueryPort.existsBook(request.bookId(), userId)) {
            throw new ReferencedBookNotFoundException("書本不存在或不屬於目前用戶：" + request.bookId());
        }

        ReadingRecord record = new ReadingRecord(userId, request.source(), request.bookId(),
                request.externalTitle(), request.externalAuthor(), request.readDate(),
                request.durationMinutes(), request.progressPercent());
        ReadingRecord saved = readingRecordRepository.save(record);
        return toResponse(saved);
    }

    /**
     * 更新閱讀記錄：先以 {@code findByIdAndUserId} 確認記錄屬於自己，否則
     * {@link ReadingRecordNotFoundException}（404）；累加 durationMinutes、覆蓋
     * progressPercent；{@code source}/{@code bookId}/{@code externalTitle}/
     * {@code externalAuthor} 建立後不可修改。
     */
    @Override
    @Transactional
    public ReadingResponse updateRecord(Long userId, Long recordId, ReadingRequest request) {
        ReadingRecord record = findOwnedRecord(userId, recordId);
        record.accumulateDuration(request.durationMinutes());
        record.updateProgress(request.progressPercent());
        return toResponse(record);
    }

    @Override
    public List<ReadingResponse> getRecordsByBook(Long userId, Long bookId, int page, int size) {
        Page<ReadingRecord> result = readingRecordRepository.findByUserIdAndBookId(
                userId, bookId, PageRequest.of(page, size));
        return toResponseList(result);
    }

    @Override
    public List<ReadingResponse> getRecordsBySource(Long userId, ReadingSource source, int page, int size) {
        Page<ReadingRecord> result = readingRecordRepository.findByUserIdAndSource(
                userId, source, PageRequest.of(page, size));
        return toResponseList(result);
    }

    /**
     * 統計目前用戶的總閱讀時長（SUM，不分來源）與已完成相異書籍數：
     * {@code source=OWNED} 以 {@code bookId} 去重，{@code source≠OWNED} 以
     * {@code (source, externalTitle, externalAuthor)} 去重。
     */
    @Override
    public StatsResponse getStats(Long userId) {
        List<ReadingRecord> records = readingRecordRepository.findByUserId(userId);
        long totalMinutes = records.stream().mapToLong(ReadingRecord::getDurationMinutes).sum();
        Set<String> distinctBooks = records.stream()
                .map(this::completionKey)
                .collect(Collectors.toSet());
        return new StatsResponse(totalMinutes, distinctBooks.size());
    }

    private ReadingRecord findOwnedRecord(Long userId, Long recordId) {
        return readingRecordRepository.findByIdAndUserId(recordId, userId)
                .orElseThrow(() -> new ReadingRecordNotFoundException(
                        "閱讀記錄不存在或不屬於目前用戶：" + recordId));
    }

    private String completionKey(ReadingRecord record) {
        if (record.getSource() == ReadingSource.OWNED) {
            return "OWNED:" + record.getBookId();
        }
        return record.getSource() + ":" + record.getExternalTitle() + ":" + record.getExternalAuthor();
    }

    private List<ReadingResponse> toResponseList(Page<ReadingRecord> page) {
        return page.getContent().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ReadingResponse toResponse(ReadingRecord record) {
        return new ReadingResponse(record.getId(), record.getSource(), record.getBookId(),
                record.getExternalTitle(), record.getExternalAuthor(), record.getReadDate(),
                record.getDurationMinutes(), record.getProgressPercent(), record.getCreatedAt());
    }
}

