package com.iisi.bookmanager.reading.validation;

import com.iisi.bookmanager.reading.domain.ReadingSource;
import com.iisi.bookmanager.reading.dto.ReadingRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link ValidReadingSource} 驗證邏輯實作（design.md 5. / tasks.md B7）。
 *
 * <ul>
 *     <li>{@code source=OWNED} ⇔ {@code bookId} 有值且 {@code externalTitle} 為 null。</li>
 *     <li>{@code source≠OWNED} ⇔ {@code bookId} 為 null 且 {@code externalTitle} 有值。</li>
 * </ul>
 * {@code source} 為 null 時交由 {@code @NotNull} 負責，本驗證器視為通過（避免重複報錯）。
 */
public class ValidReadingSourceValidator implements ConstraintValidator<ValidReadingSource, ReadingRequest> {

    @Override
    public boolean isValid(ReadingRequest request, ConstraintValidatorContext context) {
        if (request == null || request.source() == null) {
            return true;
        }
        if (request.source() == ReadingSource.OWNED) {
            return request.bookId() != null && request.externalTitle() == null;
        }
        return request.bookId() == null && request.externalTitle() != null;
    }
}
