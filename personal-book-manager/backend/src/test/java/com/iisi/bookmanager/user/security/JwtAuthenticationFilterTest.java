package com.iisi.bookmanager.user.security;

import com.iisi.bookmanager.user.service.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link JwtAuthenticationFilter} 單元測試（tasks.md C6）。
 *
 * <p>驗證 Filter「本身不直接回 401」的設計：無論驗證成功或失敗，
 * {@code filterChain.doFilter(...)} 皆應被呼叫；差異僅在於是否設定
 * {@code SecurityContextHolder}（OWASP A07：認證流程正確性）。
 *
 * <p>本測試檔實體位置放在 {@code user} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），package 宣告仍對應正確的來源套件 {@code com.iisi.bookmanager.user.security}。
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("當認證標頭帶有合法權杖時，SecurityContext 應被設定且 principal 為對應 userId")
    void should_SetSecurityContextWithUserId_When_TokenValid() throws Exception {
        // Arrange
        String token = "valid-token-string";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserId(token)).thenReturn(99L);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(99L);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("當缺少 Authorization 標頭時，不應設定 SecurityContext，但仍應放行至下一個 Filter")
    void should_NotSetSecurityContext_When_AuthorizationHeaderMissing() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("當認證標頭格式不是「認證方案 + 權杖」時，不應設定 SecurityContext")
    void should_NotSetSecurityContext_When_AuthorizationHeaderFormatInvalid() throws Exception {
        // Arrange：既非標準認證標頭格式，亦帶有明顯攻擊字元
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("當權杖驗證失敗（簽章竄改／過期）時，不應設定 SecurityContext")
    void should_NotSetSecurityContext_When_TokenValidationFails() throws Exception {
        // Arrange
        String token = "tampered-or-expired-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("當權杖驗證通過但解析 userId 失敗（回傳 null）時，不應設定 SecurityContext")
    void should_NotSetSecurityContext_When_TokenValidButUserIdParsingReturnsNull() throws Exception {
        // Arrange：理論上不應發生（validateToken 通過代表簽章有效），但仍需防禦此邊界情境
        String token = "valid-signature-but-unparseable-subject";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserId(token)).thenReturn(null);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
