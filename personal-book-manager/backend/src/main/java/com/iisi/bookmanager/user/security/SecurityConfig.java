package com.iisi.bookmanager.user.security;

import org.springframework.context.annotation.Configuration;

/**
 * Spring Security 設定（design.md 5c / tasks.md C4）。
 *
 * <p>設計原則：{@code /api/auth/**} 設為 permitAll()，其餘路徑一律 authenticated()；
 * 停用 CSRF（純 REST API + Bearer Token，無 Cookie-based session）；停用預設表單登入/HTTP Basic。
 *
 * <p>TODO: 補上 {@code @EnableWebSecurity}、{@code SecurityFilterChain} Bean 設定，
 * 屬於功能模組開發階段（本階段僅建立骨架）。
 */
@Configuration
public class SecurityConfig {
}
