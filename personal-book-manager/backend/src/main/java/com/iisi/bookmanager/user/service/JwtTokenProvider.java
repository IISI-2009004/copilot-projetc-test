package com.iisi.bookmanager.user.service;

/**
 * JWT 簽發/驗證（design.md 5c / tasks.md C3）。
 *
 * <p>TODO: 以 HS256 簽章實作，claim 含 {@code sub}(userId)、{@code username}、
 * {@code iat}、{@code exp}；密鑰自 {@code jwt.secret} 設定讀取，禁止硬編碼。
 * 屬於功能模組開發階段（本階段僅建立方法簽章與骨架）。
 */
public interface JwtTokenProvider {

    String generateToken(Long userId, String username);

    boolean validateToken(String token);

    Long getUserId(String token);
}
