package com.iisi.bookmanager.reading.service;

import com.iisi.bookmanager.reading.domain.ReadingRecord;
import com.iisi.bookmanager.reading.dto.CalendarResponse;
import com.iisi.bookmanager.reading.repository.ReadingRecordRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * {@link ReadingCalendarService} 實作（design.md 2. 套件結構 / tasks.md B3）。
 */
@Service
public class ReadingCalendarServiceImpl implements ReadingCalendarService {

    private final ReadingRecordRepository readingRecordRepository;

    public ReadingCalendarServiceImpl(ReadingRecordRepository readingRecordRepository) {
        this.readingRecordRepository = readingRecordRepository;
    }

    @Override
    public List<CalendarResponse> getMonthlyCalendar(Long userId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate start = yearMonth.atDay(1);
        LocalDate end = yearMonth.atEndOfMonth();

        List<ReadingRecord> records = readingRecordRepository.findByUserIdAndReadDateBetween(userId, start, end);

        Map<LocalDate, Long> totalsByDate = records.stream()
                .collect(Collectors.groupingBy(ReadingRecord::getReadDate,
                        Collectors.summingLong(ReadingRecord::getDurationMinutes)));

        return totalsByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new CalendarResponse(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}

