package com.iisi.bookmanager.user.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * {@link JwtTokenProvider} 實作（design.md 5c / tasks.md C3）。
 *
 * <p>以 HS256 簽章，claim 含 {@code sub}(userId)、{@code username}、{@code iat}、{@code exp}；
 * 密鑰自 {@code jwt.secret} 設定讀取（見 application.yml／application-prod.yml），禁止硬編碼於程式碼中。
 */
@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProviderImpl.class);

    private final SecretKey signingKey;
    private final long expirationSeconds;

    /**
     * 建構子，注入 {@code jwt.secret}／{@code jwt.expiration-seconds} 設定值。
     *
     * @param secret            簽章密鑰（不可硬編碼，由 application.yml / 環境變數注入）
     * @param expirationSeconds JWT 存活秒數（預設 86400，即 24 小時）
     */
    public JwtTokenProviderImpl(@Value("${jwt.secret}") String secret,
                                 @Value("${jwt.expiration-seconds:86400}") long expirationSeconds) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationSeconds = expirationSeconds;
    }

    @Override
    public String generateToken(Long userId, String username) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationSeconds);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            // 不記錄 token 內容（敏感資料不可寫入日誌），亦不透露具體失敗原因（簽章竄改／過期等）給呼叫端
            LOGGER.debug("JWT 驗證失敗：{}", ex.getClass().getSimpleName());
            return false;
        }
    }

    @Override
    public Long getUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.valueOf(claims.getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            LOGGER.debug("JWT 解析失敗：{}", ex.getClass().getSimpleName());
            return null;
        }
    }

    @Override
    public long getExpirationSeconds() {
        return expirationSeconds;
    }
}
