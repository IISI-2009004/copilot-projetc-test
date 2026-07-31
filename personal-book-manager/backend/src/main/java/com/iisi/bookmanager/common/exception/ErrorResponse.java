package com.iisi.bookmanager.common.exception;

import java.time.Instant;

/**
 * 統一錯誤回應格式（design.md copilot-instructions.md：錯誤訊息不可洩漏堆疊細節給前端）。
 */
public record ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
}
