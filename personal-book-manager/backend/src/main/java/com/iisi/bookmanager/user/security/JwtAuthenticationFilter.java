package com.iisi.bookmanager.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 解析 {@code Authorization: Bearer <JWT>} 標頭，驗證後將 userId 寫入
 * {@code SecurityContextHolder}（design.md 5c / tasks.md C4）。
 *
 * <p>TODO: 實作解析/驗證邏輯；驗證失敗一律回 401，不透露具體失敗原因。
 * 屬於功能模組開發階段（本階段僅建立骨架）。
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }
}
