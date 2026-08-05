package com.iisi.bookmanager.reading.dto;

/**
 * 閱讀統計回應 DTO（design.md 5. / tasks.md B2/B4）。
 *
 * @param totalMinutes         總閱讀時長（SUM，不分來源，限同一用戶）
 * @param completedBooksCount  已完成相異書籍數（{@code source=OWNED} 以 bookId 去重，
 *                             {@code source≠OWNED} 以 (source, externalTitle, externalAuthor) 去重）
 */
public record StatsResponse(long totalMinutes, long completedBooksCount) {
}
