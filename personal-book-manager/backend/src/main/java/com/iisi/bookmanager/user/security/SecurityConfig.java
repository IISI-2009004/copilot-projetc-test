package com.iisi.bookmanager.user.security;

import com.iisi.bookmanager.user.service.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 設定（design.md 5c / tasks.md C4）。
 *
 * <p>設計原則：/api/auth/** 設為 permitAll()，其餘路徑一律 authenticated()；
 * 停用 CSRF（純 REST API，狀態改由 JWT 攜帶，不依賴傳統的 session 機制）；
 * 停用預設表單登入/HTTP Basic；Session 設為 STATELESS（每次請求皆以 JWT 重新驗證）。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 建構子注入。
     *
     * <p>刻意注入 {@link JwtTokenProvider} 而非直接注入 {@code JwtAuthenticationFilter} Bean：
     * {@code JwtAuthenticationFilter} 不加 {@code @Component}，僅在此以 {@code new} 手動建立並加入
     * Security Filter Chain，避免 Spring Boot 對所有 {@code jakarta.servlet.Filter} Bean 的自動
     * 全域註冊機制（{@code FilterRegistrationBean}）讓此 Filter 對所有請求額外執行一次。
     *
     * @param jwtTokenProvider JWT 驗證/解析元件
     */
    public SecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 密碼雜湊器（bcrypt，cost factor 10），供 UserServiceImpl 注入使用，
     * 不在 Service 內直接 new，以利未來測試 mock。
     *
     * @return PasswordEncoder Bean
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    /**
     * 設定 HTTP 安全規則：/api/auth/** 與 H2 Console 設為 permitAll，其餘路徑 authenticated；
     * 停用 CSRF/表單登入/HTTP Basic；Session 設為 STATELESS；
     * 將 JwtAuthenticationFilter 加在 UsernamePasswordAuthenticationFilter 之前；
     * 停用表單登入/HTTP Basic 後，Spring Security 對未認證請求預設回應 403（{@code Http403ForbiddenEntryPoint}），
     * 故明確指定 {@code authenticationEntryPoint} 為 {@link HttpStatusEntryPoint}(401)，符合 design.md 規格。
     *
     * <p>H2 Console（/h2-console/**）僅於 dev profile 有效（prod 已停用），為求開發除錯
     * 便利性一併排除於 JWT 驗證之外（design.md 4. API 規格 附註）；因 H2 Console 頁面使用 iframe，
     * 需一併停用 frameOptions 避免瀏覽器阻擋同源 frame。
     *
     * @param http HttpSecurity 設定物件
     * @return SecurityFilterChain Bean
     * @throws Exception Spring Security 設定例外
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(org.springframework.http.HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
