package com.iisi.bookmanager.user.security;

import com.iisi.bookmanager.user.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 解析 Authorization 標頭中的認證資訊，驗證後將 userId 寫入
 * {@code SecurityContextHolder}（design.md 5c / tasks.md C4）。
 *
 * <p>本 Filter 本身不直接回應 401：若請求未帶 token 或驗證失敗，僅單純不設定
 * {@code SecurityContext}，並放行至下一個 Filter；最終是否需要驗證身分（{@code /api/auth/**}
 * 為 permitAll，其餘為 authenticated）由 {@code SecurityConfig} 的授權規則統一決定，
 * 未通過驗證且存取需登入端點者會由 Spring Security 統一回 401（不透露具體失敗原因，
 * 例如簽章竄改或已過期，避免洩漏線索）。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 建構子注入。
     *
     * @param jwtTokenProvider JWT 驗證/解析元件
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());
            if (jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserId(token);
                if (userId != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
