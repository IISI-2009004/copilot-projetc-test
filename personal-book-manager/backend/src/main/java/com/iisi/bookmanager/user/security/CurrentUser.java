package com.iisi.bookmanager.user.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 供 book/reading 模組的 Controller/Service 取得目前登入者 {@code userId} 的工具類別
 * （design.md 5c）。
 *
 * <p>內部委派 {@code SecurityContextHolder.getContext().getAuthentication().getPrincipal()}，
 * 是 book/reading 模組唯一允許依賴的 user 模組類別，維持「book/reading 不得直接依賴
 * user 模組其他任何類別」的邊界規則。
 *
 * <p>{@link JwtAuthenticationFilter} 驗證 JWT 成功後，將 {@code userId}（{@link Long}）
 * 作為 principal 寫入 {@code SecurityContextHolder}，因此本類別直接將 principal 轉型為
 * {@link Long} 回傳。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    /**
     * 取得目前登入者的 {@code userId}。
     *
     * @return 目前登入者 {@code userId}
     * @throws IllegalStateException 若目前無有效認證資訊（理論上不應發生，因所有需登入的端點
     *                                皆由 {@code SecurityConfig} 設為 {@code authenticated()}，
     *                                未通過驗證的請求會先被 Spring Security 攔截回 401）
     */
    public static Long id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new IllegalStateException("目前無有效的登入者資訊");
        }
        return userId;
    }
}
