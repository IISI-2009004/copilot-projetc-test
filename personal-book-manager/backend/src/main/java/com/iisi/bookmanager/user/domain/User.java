package com.iisi.bookmanager.user.domain;

import java.time.Instant;

/**
 * 使用者 Entity（最底層 Entity，詳見 design.md 3. 資料模型 / ADR-0006）。
 *
 * <p>不持有任何指向 book/reading 模組的關聯，避免 user 模組反向依賴其他模組；
 * {@code Book}/{@code Tag}/{@code Category}/{@code ReadingRecord} 反向持有 {@code userId} 外鍵。
 *
 * <p>TODO: 補上 JPA {@code @Entity}/{@code @Table}/{@code @Id} 等註解與欄位驗證，
 * 屬於功能模組開發階段（本階段僅建立專案骨架）。
 */
public class User {

    private Long id;
    private String username;
    private String passwordHash;
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
