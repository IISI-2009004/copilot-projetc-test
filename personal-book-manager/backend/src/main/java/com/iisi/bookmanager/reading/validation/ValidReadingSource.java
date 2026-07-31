package com.iisi.bookmanager.reading.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * class-level 自訂驗證註解：驗證 {@code ReadingRequest} 依 {@code ReadingSource} 分類的
 * bookId/externalTitle 互斥規則（design.md 5. / ADR-0003）。
 *
 * <p>TODO: 補上對應 {@code Validator} 實作，屬於功能模組開發階段。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ValidReadingSource {

    String message() default "閱讀來源與書本/外部標題欄位不符合互斥規則";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
