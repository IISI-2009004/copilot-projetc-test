package com.iisi.bookmanager.book.dto;

import com.iisi.bookmanager.book.domain.BookType;

/**
 * 書本新增/更新請求 DTO（design.md 5a / tasks.md A3/A6）。
 *
 * <p>TODO: 補上 {@code @ValidBookType} class-level 驗證註解與各欄位 Bean Validation，
 * 屬於功能模組開發階段。
 */
public record BookRequest(String isbn, String url, String sourcePlatform, String title,
                           String author, BookType bookType, Long categoryId,
                           String purchaseUrl, String authorUrl) {
}
