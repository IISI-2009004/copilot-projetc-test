package com.iisi.bookmanager.user.dto;

/**
 * 註冊請求 DTO（design.md 5c / tasks.md C5）。
 *
 * <p>TODO: 補上 Bean Validation 註解
 * （{@code @NotBlank}、{@code @Pattern("^[a-zA-Z0-9_-]{3,30}$")}、{@code @Size(min = 8)}），
 * 屬於功能模組開發階段。
 */
public record RegisterRequest(String username, String password) {
}
