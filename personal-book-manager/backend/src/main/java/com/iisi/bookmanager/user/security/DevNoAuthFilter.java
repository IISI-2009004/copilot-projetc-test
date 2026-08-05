package com.iisi.bookmanager.user.security;

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
 * 開發／測試階段暫時性 Filter：前端尚未完成登入流程串接前，若請求未帶
 * {@code Authorization} 標頭（即 {@link JwtAuthenticationFilter} 未設定
 * {@code SecurityContext}），自動以固定的測試用 {@code userId} 帶入認證資訊，
 * 使 {@code CurrentUser.id()} 仍可正常取值，讓 book/reading 等需登入端點得以在
 * 無登入頁面的情況下先行開發／驗證。
 *
 * <p><b>僅限 {@code dev} profile 啟用</b>（見 {@code SecurityConfig}），且僅在
 * {@code SecurityContext} 尚無認證資訊時才補上預設值，若請求本身帶有效 JWT，
 * 仍以 {@link JwtAuthenticationFilter} 解析出的真實 userId 為準，不會被覆蓋。
 *
 * <p><b>TODO（技術債務）</b>：待前端完成登入頁面/JWT 串接後，應移除本 Filter
 * 並改為要求所有 {@code /api/books/**}、{@code /api/reading/**} 等端點皆須提供
 * 合法 JWT，避免正式環境或未來 dev 環境誤用固定 userId 造成資料混淆。
 */
public class DevNoAuthFilter extends OncePerRequestFilter {

    /** 開發階段暫時使用的固定測試 userId（對應 dev H2 DB 中預期存在或可任意寫入的使用者）。 */
    static final Long DEV_DEFAULT_USER_ID = 1L;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(DEV_DEFAULT_USER_ID, null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }
}
