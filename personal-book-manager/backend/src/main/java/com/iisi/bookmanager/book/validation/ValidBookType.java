package com.iisi.bookmanager.book.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * class-level 自訂驗證註解：驗證 {@code BookRequest} 依 {@code BookType} 分類的
 * ISBN/URL 互斥規則（design.md 5a / tasks.md A6）。
 *
 * <p>TODO: 補上對應 {@code Validator} 實作，屬於功能模組開發階段。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
public @interface ValidBookType {

    String message() default "書本類型與 ISBN/URL 欄位不符合互斥規則";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
