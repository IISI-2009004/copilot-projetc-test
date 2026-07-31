package com.iisi.bookmanager.user.dto;

import java.time.Instant;

/**
 * 使用者資訊回應 DTO（design.md 5c / tasks.md C5）。
 *
 * <p>刻意不含密碼欄位，避免密碼雜湊值經 API 外洩。
 */
public record UserResponse(Long id, String username, Instant createdAt) {
}
