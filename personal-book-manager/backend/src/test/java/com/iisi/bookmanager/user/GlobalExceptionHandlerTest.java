package com.iisi.bookmanager.user;

import com.iisi.bookmanager.common.exception.ErrorResponse;
import com.iisi.bookmanager.common.exception.GlobalExceptionHandler;
import com.iisi.bookmanager.user.exception.DuplicateUsernameException;
import com.iisi.bookmanager.user.exception.InvalidCredentialsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * {@link GlobalExceptionHandler} 單元測試（tasks.md C6）。
 *
 * <p>僅涵蓋 user 模組相關的兩個例外對照：{@link DuplicateUsernameException} → 409、
 * {@link InvalidCredentialsException} → 401；驗證回應內容不洩漏堆疊細節
 * （OWASP A05：安全設定缺陷／錯誤處理）。
 *
 * <p>本測試檔實體位置放在 {@code user} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），實際測試對象為 {@code com.iisi.bookmanager.common.exception.GlobalExceptionHandler}。
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private WebRequest webRequest;

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("當攔截到 DuplicateUsernameException 時，應回傳 409 且訊息不含堆疊細節")
    void should_Return409WithoutStackTrace_When_DuplicateUsernameExceptionThrown() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/auth/register");
        DuplicateUsernameException exception = new DuplicateUsernameException("帳號已被使用");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleConflict(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("帳號已被使用");
        assertThat(response.getBody().status()).isEqualTo(409);
        assertThat(response.getBody().error()).isEqualTo("Conflict");
        assertThat(response.getBody().path()).isEqualTo("uri=/api/auth/register");
    }

    @Test
    @DisplayName("當攔截到 InvalidCredentialsException 時，應回傳 401 且訊息為通用訊息（不透露具體失敗原因）")
    void should_Return401WithGenericMessage_When_InvalidCredentialsExceptionThrown() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/auth/login");
        InvalidCredentialsException exception = new InvalidCredentialsException("帳號或密碼錯誤");

        // Act
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(exception, webRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("帳號或密碼錯誤");
        assertThat(response.getBody().status()).isEqualTo(401);
        assertThat(response.getBody().error()).isEqualTo("Unauthorized");
        assertThat(response.getBody().path()).isEqualTo("uri=/api/auth/login");
    }
}
