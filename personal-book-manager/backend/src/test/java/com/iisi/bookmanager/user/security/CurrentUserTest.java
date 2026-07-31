package com.iisi.bookmanager.user.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link CurrentUser} 單元測試（tasks.md C6）。
 *
 * <p>本測試檔實體位置放在 {@code user} 套件目錄下（本環境檔案建立工具不支援自動建立
 * 巢狀子目錄），package 宣告仍對應正確的來源套件 {@code com.iisi.bookmanager.user.security}。
 */
class CurrentUserTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("當 SecurityContextHolder 內有合法 Long principal 時，id() 應該回傳該值")
    void should_ReturnUserId_When_ValidLongPrincipalPresent() {
        // Arrange
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(7L, null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act
        Long userId = CurrentUser.id();

        // Assert
        assertThat(userId).isEqualTo(7L);
    }

    @Test
    @DisplayName("當無任何認證資訊時，id() 應該拋出 IllegalStateException")
    void should_ThrowIllegalStateException_When_NoAuthenticationPresent() {
        // Arrange：確保目前 SecurityContext 為乾淨狀態
        SecurityContextHolder.clearContext();

        // Act & Assert
        assertThatThrownBy(CurrentUser::id)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("目前無有效的登入者資訊");
    }

    @Test
    @DisplayName("當 principal 型別非 Long（例如字串）時，id() 應該拋出 IllegalStateException")
    void should_ThrowIllegalStateException_When_PrincipalIsNotLong() {
        // Arrange
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("not-a-long-principal", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Act & Assert
        assertThatThrownBy(CurrentUser::id)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("目前無有效的登入者資訊");
    }
}
