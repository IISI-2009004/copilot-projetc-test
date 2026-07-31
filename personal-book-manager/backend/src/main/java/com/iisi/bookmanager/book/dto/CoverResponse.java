package com.iisi.bookmanager.book.dto;

import com.iisi.bookmanager.book.domain.CoverImageSource;

/**
 * 封面圖片回應 DTO（design.md 5b）。
 */
public record CoverResponse(String coverImageUrl, CoverImageSource coverImageSource) {
}
