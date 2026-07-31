package com.iisi.bookmanager.user.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * 使用者 Entity（最底層 Entity，詳見 design.md 3. 資料模型 / ADR-0006）。
 *
 * <p>不持有任何指向 book/reading 模組的關聯，避免 user 模組反向依賴其他模組；
 * {@code Book}/{@code Tag}/{@code Category}/{@code ReadingRecord} 反向持有 {@code userId} 外鍵。
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", length = 30, nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", length = 60, nullable = false)
    private String passwordHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** JPA 要求的無參建構子。 */
    protected User() {
    }

    /**
     * 建立新使用者（僅用於註冊流程，{@code createdAt} 由 {@link #prePersist()} 自動填入）。
     *
     * @param username     帳號
     * @param passwordHash bcrypt 雜湊後的密碼
     */
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    protected void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

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
