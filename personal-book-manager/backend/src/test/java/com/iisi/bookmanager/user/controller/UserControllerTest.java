package com.iisi.bookmanager.user.controller;

import com.iisi.bookmanager.user.dto.UserResponse;
import com.iisi.bookmanager.user.exception.InvalidCredentialsException;
import com.iisi.bookmanager.user.security.SecurityConfig;
import com.iisi.bookmanager.user.service.JwtTokenProvider;
import com.iisi.bookmanager.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link UserController} 整合測試（tasks.md C6）。
 *
 * <p>使用 {@code @WebMvcTest} 載入實際 {@link SecurityConfig}（含
 * {@code JwtAuthenticationFilter}），驗證 {@code GET /api/users/me} 需登入
 * （未帶認證標頭／權杖無效皆回 401），以及成功時的資料回傳（不含密碼）。
 *
 * <p>本測試檔實體位置放在 {@code user} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），package 宣告仍對應正確的來源套件 {@code com.iisi.bookmanager.user.controller}。
 */
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    private static final String AUTH_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String AUTH_SCHEME = "Bearer";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // ------------------------------------------------------------
    // 未認證存取測試：/api/users/me 需登入
    // ------------------------------------------------------------

    @Test
    @DisplayName("當未帶認證標頭存取 /api/users/me 時，應回傳 401")
    void should_Return401_When_NoAuthorizationHeaderProvided() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("當認證標頭格式不是「認證方案 + 權杖」時，應回傳 401")
    void should_Return401_When_AuthorizationHeaderFormatInvalid() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .header(AUTH_HEADER, "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("當權杖驗證失敗（簽章竄改或已過期）時，應回傳 401")
    void should_Return401_When_TokenInvalid() throws Exception {
        // Arrange
        String token = "tampered-or-expired-token";
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + token))
                .andExpect(status().isUnauthorized());
    }

    // ------------------------------------------------------------
    // 成功路徑
    // ------------------------------------------------------------

    @Test
    @DisplayName("當攜帶合法權杖時，/api/users/me 應回傳目前登入者資訊（不含密碼）")
    void should_ReturnCurrentUserResponse_When_TokenValid() throws Exception {
        // Arrange
        String token = "valid-token-string";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserId(token)).thenReturn(5L);
        UserResponse response = new UserResponse(5L, "realUser", Instant.parse("2026-01-01T00:00:00Z"));
        when(userService.getCurrentUser(5L)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.username").value("realUser"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("當合法權杖對應的 userId 已不存在於資料庫時，/api/users/me 應回傳 401（而非 404，避免洩漏帳號存廢資訊）")
    void should_Return401_When_UserIdFromTokenNotFoundInDatabase() throws Exception {
        // Arrange
        String token = "valid-token-but-user-deleted";
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUserId(token)).thenReturn(404L);
        when(userService.getCurrentUser(anyLong()))
                .thenThrow(new InvalidCredentialsException("帳號或密碼錯誤"));

        // Act & Assert
        mockMvc.perform(get("/api/users/me")
                        .header(AUTH_HEADER, AUTH_SCHEME + " " + token))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("帳號或密碼錯誤"));
    }
}
