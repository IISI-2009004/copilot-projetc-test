package com.iisi.bookmanager.reading.controller;

import com.iisi.bookmanager.reading.domain.ReadingSource;
import com.iisi.bookmanager.reading.dto.CalendarResponse;
import com.iisi.bookmanager.reading.dto.ReadingRequest;
import com.iisi.bookmanager.reading.dto.ReadingResponse;
import com.iisi.bookmanager.reading.dto.StatsResponse;
import com.iisi.bookmanager.reading.service.ReadingCalendarService;
import com.iisi.bookmanager.reading.service.ReadingService;
import com.iisi.bookmanager.user.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 閱讀記錄與日曆端點（design.md 5. / tasks.md B4）。
 *
 * <p>每個端點方法第一步呼叫 {@code CurrentUser.id()} 取得目前登入者 userId，
 * 傳入對應 Service 方法。
 */
@RestController
@RequestMapping("/api/reading")
public class ReadingController {

    private final ReadingService readingService;
    private final ReadingCalendarService readingCalendarService;

    public ReadingController(ReadingService readingService,
                              ReadingCalendarService readingCalendarService) {
        this.readingService = readingService;
        this.readingCalendarService = readingCalendarService;
    }

    @PostMapping
    public ResponseEntity<ReadingResponse> addRecord(@Valid @RequestBody ReadingRequest request) {
        Long userId = CurrentUser.id();
        ReadingResponse response = readingService.addRecord(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ReadingResponse updateRecord(@PathVariable Long id, @Valid @RequestBody ReadingRequest request) {
        Long userId = CurrentUser.id();
        return readingService.updateRecord(userId, id, request);
    }

    /**
     * 查詢閱讀記錄：{@code bookId}、{@code source} 擇一提供（皆未提供回 400）。
     */
    @GetMapping
    public List<ReadingResponse> listRecords(@RequestParam(required = false) Long bookId,
                                              @RequestParam(required = false) ReadingSource source,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        Long userId = CurrentUser.id();
        if (bookId != null) {
            return readingService.getRecordsByBook(userId, bookId, page, size);
        }
        if (source != null) {
            return readingService.getRecordsBySource(userId, source, page, size);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "bookId 或 source 須擇一提供");
    }

    @GetMapping("/calendar")
    public List<CalendarResponse> getCalendar(@RequestParam int year, @RequestParam int month) {
        Long userId = CurrentUser.id();
        return readingCalendarService.getMonthlyCalendar(userId, year, month);
    }

    @GetMapping("/stats")
    public StatsResponse getStats() {
        Long userId = CurrentUser.id();
        return readingService.getStats(userId);
    }
}

