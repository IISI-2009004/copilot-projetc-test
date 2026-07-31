package com.iisi.bookmanager.reading.controller;

import com.iisi.bookmanager.reading.dto.ReadingRequest;
import com.iisi.bookmanager.reading.dto.ReadingResponse;
import com.iisi.bookmanager.reading.service.ReadingCalendarService;
import com.iisi.bookmanager.reading.service.ReadingService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 閱讀記錄與日曆端點（design.md 5. / tasks.md 模組 B）。
 *
 * <p>TODO: 補上 {@code @Valid} 請求驗證、CurrentUser 呼叫與商業邏輯串接，屬於功能模組
 * 開發階段（本階段僅建立端點簽章骨架）。
 */
@RestController
@RequestMapping("/api/reading-records")
public class ReadingController {

    private final ReadingService readingService;
    private final ReadingCalendarService readingCalendarService;

    public ReadingController(ReadingService readingService,
                              ReadingCalendarService readingCalendarService) {
        this.readingService = readingService;
        this.readingCalendarService = readingCalendarService;
    }

    @PostMapping
    public ReadingResponse createRecord(ReadingRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @PutMapping("/{id}")
    public ReadingResponse updateRecord(@PathVariable Long id, ReadingRequest request) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @DeleteMapping("/{id}")
    public void deleteRecord(@PathVariable Long id) {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }

    @GetMapping
    public List<ReadingResponse> listRecords() {
        throw new UnsupportedOperationException("尚未實作：功能模組開發階段補上");
    }
}
