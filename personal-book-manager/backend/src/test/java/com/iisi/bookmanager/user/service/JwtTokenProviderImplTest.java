package com.iisi.bookmanager.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link JwtTokenProviderImpl} 單元測試（tasks.md C6）。
 *
 * <p>驗證簽發/驗證/解析的完整生命週期，以及竄改權杖字串、不同簽章密鑰、明顯無效
 * 輸入等安全相關邊界情境（OWASP A07：認證失敗）。
 *
 * <p>本測試檔實體位置放在 {@code user} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），package 宣告仍對應正確的來源套件 {@code com.iisi.bookmanager.user.service}。
 */
class JwtTokenProviderImplTest {

    private static final String SECRET = "test-secret-key-please-make-it-long-enough-1234567890";
    private static final String OTHER_SECRET = "another-secret-key-also-long-enough-for-hs256-0987654321";
    private static final long EXPIRATION_SECONDS = 3600L;

    private final JwtTokenProviderImpl provider = new JwtTokenProviderImpl(SECRET, EXPIRATION_SECONDS);

    @Test
    @DisplayName("當使用合法密鑰簽發權杖後，validateToken 應該回傳 true")
    void should_ReturnTrue_When_TokenSignedWithSameSecret() {
        // Arrange
        String token = provider.generateToken(42L, "alice");

        // Act
        boolean valid = provider.validateToken(token);

        // Assert
        assertThat(valid).isTrue();
    }

    @Test
    @DisplayName("當權杖合法時，getUserId 應該解出原本簽發時的 userId")
    void should_ReturnOriginalUserId_When_TokenValid() {
        // Arrange
        String token = provider.generateToken(42L, "alice");

        // Act
        Long userId = provider.getUserId(token);

        // Assert
        assertThat(userId).isEqualTo(42L);
    }

    @Test
    @DisplayName("當權杖字串遭竄改（結尾字元被修改）時，validateToken 應該回傳 false")
    void should_ReturnFalse_When_TokenTampered() {
        // Arrange
        String token = provider.generateToken(42L, "alice");
        String tampered = token.substring(0, token.length() - 1)
                + (token.charAt(token.length() - 1) == 'a' ? 'b' : 'a');

        // Act
        boolean valid = provider.validateToken(tampered);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("當權杖字串遭竄改時，getUserId 應該回傳 null 而非拋出例外")
    void should_ReturnNull_When_TokenTamperedOnGetUserId() {
        // Arrange
        String token = provider.generateToken(42L, "alice");
        String tampered = token.substring(0, token.length() - 1)
                + (token.charAt(token.length() - 1) == 'a' ? 'b' : 'a');

        // Act
        Long userId = provider.getUserId(tampered);

        // Assert
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("當輸入為明顯無效字串（非 JWT 格式）時，validateToken 應該回傳 false")
    void should_ReturnFalse_When_TokenIsObviouslyInvalidString() {
        // Act & Assert
        assertThat(provider.validateToken("not-a-jwt-token")).isFalse();
        assertThat(provider.validateToken("")).isFalse();
        assertThat(provider.validateToken("' OR '1'='1")).isFalse();
        assertThat(provider.validateToken("<script>alert(1)</script>")).isFalse();
    }

    @Test
    @DisplayName("當輸入為明顯無效字串時，getUserId 應該回傳 null 而非拋出例外")
    void should_ReturnNull_When_TokenIsObviouslyInvalidString() {
        // Act & Assert
        assertThat(provider.getUserId("not-a-jwt-token")).isNull();
        assertThat(provider.getUserId("")).isNull();
    }

    @Test
    @DisplayName("當使用不同密鑰簽發的權杖交由原 provider 驗證時，validateToken 應該回傳 false")
    void should_ReturnFalse_When_TokenSignedWithDifferentSecret() {
        // Arrange
        JwtTokenProviderImpl otherProvider = new JwtTokenProviderImpl(OTHER_SECRET, EXPIRATION_SECONDS);
        String tokenFromOtherProvider = otherProvider.generateToken(42L, "alice");

        // Act
        boolean valid = provider.validateToken(tokenFromOtherProvider);

        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("當使用不同密鑰簽發的權杖交由原 provider 解析時，getUserId 應該回傳 null")
    void should_ReturnNull_When_TokenSignedWithDifferentSecretOnGetUserId() {
        // Arrange
        JwtTokenProviderImpl otherProvider = new JwtTokenProviderImpl(OTHER_SECRET, EXPIRATION_SECONDS);
        String tokenFromOtherProvider = otherProvider.generateToken(42L, "alice");

        // Act
        Long userId = provider.getUserId(tokenFromOtherProvider);

        // Assert
        assertThat(userId).isNull();
    }

    @Test
    @DisplayName("當 provider 建構時傳入存活秒數，getExpirationSeconds 應該回傳相同數值")
    void should_ReturnConfiguredExpirationSeconds_When_GetExpirationSecondsCalled() {
        // Act & Assert
        assertThat(provider.getExpirationSeconds()).isEqualTo(EXPIRATION_SECONDS);
    }

    @Test
    @DisplayName("當已過期的權杖交由 validateToken 驗證時，應該回傳 false")
    void should_ReturnFalse_When_TokenExpired() {
        // Arrange：存活秒數設為 0，簽發後立即視為已過期（exp <= iat）
        JwtTokenProviderImpl shortLivedProvider = new JwtTokenProviderImpl(SECRET, -10L);
        String expiredToken = shortLivedProvider.generateToken(42L, "alice");

        // Act
        boolean valid = provider.validateToken(expiredToken);

        // Assert
        assertThat(valid).isFalse();
    }
}
