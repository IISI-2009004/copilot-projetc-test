package com.iisi.bookmanager.user.service;

/**
 * JWT 簽發/驗證（design.md 5c / tasks.md C3）。
 *
 * <p>以 HS256 簽章實作，claim 含 {@code sub}(userId)、{@code username}、
 * {@code iat}、{@code exp}；密鑰自 {@code jwt.secret} 設定讀取，禁止硬編碼。
 */
public interface JwtTokenProvider {

    /**
     * 簽發 JWT。
     *
     * @param userId   使用者 ID（寫入 {@code sub} claim）
     * @param username 帳號（寫入 {@code username} claim）
     * @return JWT 字串
     */
    String generateToken(Long userId, String username);

    /**
     * 驗證 JWT 簽章與過期時間。
     *
     * @param token JWT 字串
     * @return 簽章有效且未過期回傳 {@code true}，其餘（含格式錯誤、簽章竄改、過期）回傳 {@code false}
     */
    boolean validateToken(String token);

    /**
     * 解析 JWT 取得使用者 ID。
     *
     * @param token JWT 字串
     * @return 使用者 ID；解析失敗回傳 {@code null}（呼叫端應先呼叫 {@link #validateToken(String)}）
     */
    Long getUserId(String token);

    /**
     * 取得目前設定的存活秒數（供 {@code LoginResponse.expiresIn} 使用，需與實際簽發的 {@code exp} 一致）。
     *
     * @return 存活秒數
     */
    long getExpirationSeconds();
}
