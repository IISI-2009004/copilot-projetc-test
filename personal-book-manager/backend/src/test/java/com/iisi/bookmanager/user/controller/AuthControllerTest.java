package com.iisi.bookmanager.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iisi.bookmanager.user.dto.LoginRequest;
import com.iisi.bookmanager.user.dto.LoginResponse;
import com.iisi.bookmanager.user.dto.RegisterRequest;
import com.iisi.bookmanager.user.dto.UserResponse;
import com.iisi.bookmanager.user.exception.DuplicateUsernameException;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link AuthController} 整合測試（tasks.md C6）。
 *
 * <p>使用 {@code @WebMvcTest} 載入實際 {@link SecurityConfig}，驗證
 * {@code /api/auth/**} 為 permitAll（未帶認證標頭仍可存取）、成功/失敗商業邏輯分支
 * 對應的 HTTP 狀態碼，以及 Bean Validation 對無效輸入的攔截（OWASP A03）。
 *
 * <p>本測試檔實體位置放在 {@code user} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），package 宣告仍對應正確的來源套件 {@code com.iisi.bookmanager.user.controller}。
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    // ------------------------------------------------------------
    // 未認證存取測試：/api/auth/** 為 permitAll
    // ------------------------------------------------------------

    @Test
    @DisplayName("當未帶認證標頭存取 /api/auth/register 時，應仍可放行（permitAll）而非回 401")
    void should_AllowAccessWithoutToken_When_RegisteringNewAccount() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("newUser", "password123");
        UserResponse response = new UserResponse(1L, "newUser", Instant.now());
        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act & Assert：未帶 Authorization 標頭，僅驗證非 401
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("當未帶認證標頭存取 /api/auth/login 時，應仍可放行（permitAll）而非回 401")
    void should_AllowAccessWithoutToken_When_LoggingIn() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("realUser", "correctPassword");
        LoginResponse response = new LoginResponse("signed-jwt-string", "Bearer", 86400L);
        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // ------------------------------------------------------------
    // register
    // ------------------------------------------------------------

    @Test
    @DisplayName("當註冊請求合法時，應回傳 201 與不含密碼的使用者資訊")
    void should_Return201WithUserResponse_When_RegisterRequestValid() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("newUser", "password123");
        UserResponse response = new UserResponse(1L, "newUser", Instant.parse("2026-01-01T00:00:00Z"));
        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("newUser"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("當帳號已存在時，register 應該回傳 409 且錯誤訊息不洩漏堆疊細節")
    void should_Return409WithoutStackTrace_When_UsernameAlreadyExists() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("existingUser", "password123");
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateUsernameException("帳號已被使用"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("帳號已被使用"))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    @DisplayName("當 username 不符合英數字/底線/連字號格式時，register 應該回傳 400（Bean Validation）")
    void should_Return400_When_UsernameFormatInvalid() throws Exception {
        // Arrange：含空白與特殊符號，不符合 ^[a-zA-Z0-9_-]{3,30}$
        RegisterRequest request = new RegisterRequest("invalid user!!", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("當 password 少於 8 字元時，register 應該回傳 400（Bean Validation）")
    void should_Return400_When_PasswordTooShort() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("validUser", "short");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("當 username 帶有 SQL Injection 攻擊載荷時，Bean Validation 應攔截並回傳 400（不觸及 Service 層）")
    void should_Return400WithoutInvokingService_When_UsernameContainsSqlInjectionPayload() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("'; DROP TABLE users; --", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(userService, org.mockito.Mockito.never()).register(any(RegisterRequest.class));
    }

    @Test
    @DisplayName("當 username 帶有 XSS 攻擊載荷時，Bean Validation 應攔截並回傳 400（不觸及 Service 層）")
    void should_Return400WithoutInvokingService_When_UsernameContainsXssPayload() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest("<script>alert(1)</script>", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(userService, org.mockito.Mockito.never()).register(any(RegisterRequest.class));
    }

    // ------------------------------------------------------------
    // login
    // ------------------------------------------------------------

    @Test
    @DisplayName("當登入成功時，應回傳 200 與含 token 的 LoginResponse")
    void should_Return200WithLoginResponse_When_LoginSucceeds() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("realUser", "correctPassword");
        LoginResponse response = new LoginResponse("signed-jwt-string", "Bearer", 86400L);
        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("signed-jwt-string"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(86400));
    }

    @Test
    @DisplayName("當帳號不存在時，login 應回傳 401 且訊息為通用訊息（不透露帳號是否存在）")
    void should_Return401WithGenericMessage_When_UsernameNotFound() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("ghost", "anyPassword1");
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("帳號或密碼錯誤"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("帳號或密碼錯誤"));
    }

    @Test
    @DisplayName("當密碼不符時，login 應回傳 401 且與帳號不存在情況訊息相同")
    void should_Return401WithSameGenericMessage_When_PasswordMismatch() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest("realUser", "wrongPassword");
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("帳號或密碼錯誤"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("帳號或密碼錯誤"));
    }

    @Test
    @DisplayName("當 username 帶有 SQL Injection 攻擊載荷時，login 應正常呼叫 Service（無 Bean Validation 限制）且不因此拋出未預期例外")
    void should_InvokeServiceAndReturn401_When_UsernameContainsSqlInjectionPayload() throws Exception {
        // Arrange：LoginRequest 無格式驗證，字串會原樣傳給 Service（此處以 mock 模擬帳號不存在的回應）
        LoginRequest request = new LoginRequest("' OR '1'='1", "' OR '1'='1");
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("帳號或密碼錯誤"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("帳號或密碼錯誤"));
    }
}
