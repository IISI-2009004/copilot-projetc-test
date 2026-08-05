package com.iisi.bookmanager.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 註冊請求 DTO（design.md 5c / tasks.md C5）。
 *
 * <p>{@code username}：3-30 字元，僅限英數字/底線/連字號；{@code password}：至少 8 字元。
 */
public record RegisterRequest(

        @NotBlank
        @Pattern(regexp = "^[a-zA-Z0-9_-]{3,30}$", message = "帳號需為 3-30 字元的英數字/底線/連字號")
        String username,

        @NotBlank
        @Size(min = 8, message = "密碼至少需 8 字元")
        String password) {
}
