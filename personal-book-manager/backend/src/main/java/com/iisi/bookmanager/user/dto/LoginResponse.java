package com.iisi.bookmanager.user.dto;

/**
 * 登入回應 DTO（design.md 5c / tasks.md C5）。
 */
public record LoginResponse(String token, String tokenType, long expiresIn) {
}
