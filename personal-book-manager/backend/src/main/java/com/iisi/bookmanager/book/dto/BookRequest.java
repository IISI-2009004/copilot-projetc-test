package com.iisi.bookmanager.book.dto;

import com.iisi.bookmanager.book.domain.BookType;
import com.iisi.bookmanager.book.validation.ValidBookType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 書本新增/更新請求 DTO（design.md 5a / tasks.md A3/A6）。
 *
 * <p>{@code isbn}/{@code url} 之間的互斥規則由 {@link ValidBookType} class-level 驗證。
 */
@ValidBookType
public record BookRequest(
        @Pattern(regexp = "\\d{10}|\\d{13}", message = "ISBN 須為 10 或 13 位數字")
        String isbn,

        @Pattern(regexp = "https?://.+", message = "url 須為 http/https 網址")
        @Size(max = 500)
        String url,

        @Size(max = 100)
        String sourcePlatform,

        @NotBlank(message = "title 不可為空")
        @Size(max = 200)
        String title,

        @NotBlank(message = "author 不可為空")
        @Size(max = 100)
        String author,

        @NotNull(message = "bookType 不可為 null")
        BookType bookType,

        Long categoryId,

        @Pattern(regexp = "https?://.+", message = "purchaseUrl 須為 http/https 網址")
        @Size(max = 500)
        String purchaseUrl,

        @Pattern(regexp = "https?://.+", message = "authorUrl 須為 http/https 網址")
        @Size(max = 500)
        String authorUrl) {
}

