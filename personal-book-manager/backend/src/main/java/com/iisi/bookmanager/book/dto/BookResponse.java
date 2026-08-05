package com.iisi.bookmanager.book.dto;

import com.iisi.bookmanager.book.domain.BookType;
import com.iisi.bookmanager.book.domain.CoverImageSource;

import java.time.Instant;

/**
 * 書本回應 DTO（design.md 5a/5b / tasks.md A3）。
 */
public record BookResponse(Long id, String isbn, String url, String sourcePlatform, String title,
                            String author, BookType bookType, Long categoryId,
                            String purchaseUrl, String authorUrl, String coverImageUrl,
                            CoverImageSource coverImageSource, Instant createdAt, Instant updatedAt) {
}
