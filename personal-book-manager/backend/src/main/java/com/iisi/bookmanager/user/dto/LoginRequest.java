package com.iisi.bookmanager.user.dto;

/**
 * 登入請求 DTO（design.md 5c / tasks.md C5）。
 */
public record LoginRequest(String username, String password) {
}
