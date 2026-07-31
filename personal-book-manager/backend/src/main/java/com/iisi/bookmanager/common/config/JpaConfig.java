package com.iisi.bookmanager.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 共用設定（design.md 2. 套件結構）。
 *
 * <p>TODO: 視需要補上 Auditing（createdAt/updatedAt 自動填入）等共用設定，
 * 屬於功能模組開發階段。
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
