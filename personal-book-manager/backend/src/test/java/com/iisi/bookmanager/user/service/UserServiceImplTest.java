package com.iisi.bookmanager.user.service;

import com.iisi.bookmanager.user.domain.User;
import com.iisi.bookmanager.user.dto.LoginRequest;
import com.iisi.bookmanager.user.dto.LoginResponse;
import com.iisi.bookmanager.user.dto.RegisterRequest;
import com.iisi.bookmanager.user.dto.UserResponse;
import com.iisi.bookmanager.user.exception.DuplicateUsernameException;
import com.iisi.bookmanager.user.exception.InvalidCredentialsException;
import com.iisi.bookmanager.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link UserServiceImpl} 單元測試（tasks.md C6）。
 *
 * <p>驗證商業邏輯核心規則：帳號重複註冊、登入帳號列舉防護（帳號不存在／密碼錯誤
 * 回傳相同錯誤訊息）、目前登入者查詢等分支。
 *
 * <p>本測試檔實體位置放在 {@code user} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），package 宣告仍對應正確的來源套件 {@code com.iisi.bookmanager.user.service}，
 * Maven 編譯不要求測試原始碼實體目錄與 package 完全一致。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    private static final String INVALID_CREDENTIALS_MESSAGE = "帳號或密碼錯誤";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private UserServiceImpl userService;

    private UserServiceImpl newService() {
        return new UserServiceImpl(userRepository, passwordEncoder, jwtTokenProvider);
    }

    // ------------------------------------------------------------
    // register
    // ------------------------------------------------------------

    @Test
    @DisplayName("當帳號已存在時，register 應該拋出 DuplicateUsernameException")
    void should_ThrowDuplicateUsernameException_When_UsernameAlreadyExists() {
        // Arrange
        userService = newService();
        RegisterRequest request = new RegisterRequest("existingUser", "password123");
        when(userRepository.existsByUsernameIgnoreCase("existingUser")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessage("帳號已被使用");
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    @DisplayName("當帳號未被使用時，register 應該雜湊密碼並儲存後回傳不含密碼的使用者資訊")
    void should_ReturnUserResponseWithoutPassword_When_UsernameNotExists() {
        // Arrange
        userService = newService();
        RegisterRequest request = new RegisterRequest("newUser", "password123");
        when(userRepository.existsByUsernameIgnoreCase("newUser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        User persisted = buildUser(1L, "newUser", "hashed-password", Instant.parse("2026-01-01T00:00:00Z"));
        when(userRepository.save(any(User.class))).thenReturn(persisted);

        // Act
        UserResponse response = userService.register(request);

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getUsername()).isEqualTo("newUser");
        assertThat(userCaptor.getValue().getPasswordHash()).isEqualTo("hashed-password");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("newUser");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    }

    // ------------------------------------------------------------
    // login — 帳號列舉防護：帳號不存在／密碼錯誤必須回傳相同錯誤訊息
    // ------------------------------------------------------------

    @Test
    @DisplayName("當帳號不存在時，login 應該拋出訊息為「帳號或密碼錯誤」的 InvalidCredentialsException")
    void should_ThrowInvalidCredentialsExceptionWithGenericMessage_When_UsernameNotFound() {
        // Arrange
        userService = newService();
        LoginRequest request = new LoginRequest("ghost", "anyPassword1");
        when(userRepository.findByUsernameIgnoreCase("ghost")).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(INVALID_CREDENTIALS_MESSAGE);
        verify(jwtTokenProvider, never()).generateToken(any(), anyString());
    }

    @Test
    @DisplayName("當密碼不符時，login 應該拋出與帳號不存在情況「相同訊息」的 InvalidCredentialsException（避免帳號列舉）")
    void should_ThrowInvalidCredentialsExceptionWithSameGenericMessage_When_PasswordMismatch() {
        // Arrange
        userService = newService();
        LoginRequest request = new LoginRequest("realUser", "wrongPassword");
        User existing = buildUser(2L, "realUser", "hashed-password", Instant.now());
        when(userRepository.findByUsernameIgnoreCase("realUser")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("wrongPassword", "hashed-password")).thenReturn(false);

        // Act & Assert：與帳號不存在情境訊息必須完全相同
        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(INVALID_CREDENTIALS_MESSAGE);
        verify(jwtTokenProvider, never()).generateToken(any(), anyString());
    }

    @Test
    @DisplayName("當帳號密碼皆正確時，login 應該簽發 JWT 並回傳含 token 與存活秒數的 LoginResponse")
    void should_ReturnLoginResponseWithToken_When_CredentialsValid() {
        // Arrange
        userService = newService();
        LoginRequest request = new LoginRequest("realUser", "correctPassword");
        User existing = buildUser(3L, "realUser", "hashed-password", Instant.now());
        when(userRepository.findByUsernameIgnoreCase("realUser")).thenReturn(Optional.of(existing));
        when(passwordEncoder.matches("correctPassword", "hashed-password")).thenReturn(true);
        when(jwtTokenProvider.generateToken(3L, "realUser")).thenReturn("signed-jwt-string");
        when(jwtTokenProvider.getExpirationSeconds()).thenReturn(86400L);

        // Act
        LoginResponse response = userService.login(request);

        // Assert
        assertThat(response.token()).isEqualTo("signed-jwt-string");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(86400L);
    }

    // ------------------------------------------------------------
    // getCurrentUser
    // ------------------------------------------------------------

    @Test
    @DisplayName("當 userId 找不到對應使用者時，getCurrentUser 應該拋出 InvalidCredentialsException（而非 404）")
    void should_ThrowInvalidCredentialsException_When_UserIdNotFound() {
        // Arrange
        userService = newService();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> userService.getCurrentUser(999L))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage(INVALID_CREDENTIALS_MESSAGE);
    }

    @Test
    @DisplayName("當 userId 存在時，getCurrentUser 應該回傳不含密碼的使用者資訊")
    void should_ReturnUserResponseWithoutPassword_When_UserIdFound() {
        // Arrange
        userService = newService();
        User existing = buildUser(4L, "realUser", "hashed-password", Instant.parse("2026-02-02T00:00:00Z"));
        when(userRepository.findById(4L)).thenReturn(Optional.of(existing));

        // Act
        UserResponse response = userService.getCurrentUser(4L);

        // Assert
        assertThat(response.id()).isEqualTo(4L);
        assertThat(response.username()).isEqualTo("realUser");
        assertThat(response.createdAt()).isEqualTo(Instant.parse("2026-02-02T00:00:00Z"));
    }

    // ------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------

    /**
     * 透過反射建立含 id/createdAt 的 {@link User}（User 無對應建構子，
     * id 由 JPA {@code @GeneratedValue} 產生、createdAt 由 {@code @PrePersist} 填入，
     * 測試情境下需手動注入這兩個欄位以模擬持久化後的狀態）。
     */
    private User buildUser(Long id, String username, String passwordHash, Instant createdAt) {
        try {
            Constructor<User> constructor = User.class.getDeclaredConstructor(String.class, String.class);
            constructor.setAccessible(true);
            User user = constructor.newInstance(username, passwordHash);

            Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, id);

            Field createdAtField = User.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(user, createdAt);

            return user;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("測試用 User 建立失敗", ex);
        }
    }
}
