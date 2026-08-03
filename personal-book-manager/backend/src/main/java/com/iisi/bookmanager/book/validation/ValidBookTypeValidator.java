package com.iisi.bookmanager.book.validation;

import com.iisi.bookmanager.book.domain.BookType;
import com.iisi.bookmanager.book.dto.BookRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link ValidBookType} 驗證邏輯實作（design.md 5a / tasks.md A6）。
 *
 * <p>依 {@code bookType} 分兩類驗證 ISBN/URL 互斥規則：
 * <ul>
 *     <li>實體／電子書類（{@code isPhysicalOrEbook()}）：{@code url} 必須為 null。</li>
 *     <li>線上內容類（{@code isOnline()}）：{@code isbn} 必須為 null，且 {@code url} 不可為 null。</li>
 * </ul>
 * {@code bookType} 為 null 時交由 {@code @NotNull} 負責，本驗證器視為通過（避免重複報錯）。
 */
public class ValidBookTypeValidator implements ConstraintValidator<ValidBookType, BookRequest> {

    @Override
    public boolean isValid(BookRequest request, ConstraintValidatorContext context) {
        if (request == null || request.bookType() == null) {
            return true;
        }
        BookType bookType = request.bookType();
        if (bookType.isPhysicalOrEbook()) {
            return request.url() == null;
        }
        return request.isbn() == null && request.url() != null;
    }
}
