---
name: "spring-boot-2-to-3"
description: "將 Spring Boot 2.x（javax.*）專案升級至 Spring Boot 3.x（jakarta.* + Java 17+）"
skill: spring-boot-2-to-3
version: 1.0
tags: [spring-boot, migration, jakarta-ee]
applicable-to: [Spring Boot]
last-updated: 2026-07-20
---

# Spring Boot 升級技能（2.x → 3.x）

## 適用情境

將 Spring Boot 2.x（`javax.*`）專案升級至 Spring Boot 3.x（`jakarta.*` + Java 17+）。

## 核心原則

1. 先確認執行環境已達 Java 17+（Spring Boot 3 最低需求），對應 java6-to-java21 技能的升級路線圖
2. `javax.*` → `jakarta.*` 的命名空間遷移可先用自動化工具批次處理，再人工複查
3. 第三方函式庫需逐一確認是否已釋出對應 Jakarta EE 9+ 相容版本
4. 升級後需重新檢視 Actuator、Security 等模組的組態變更（版本間常有預設值調整）

## 標準做法

### 命名空間批次遷移

```bash
# 使用 OpenRewrite 自動化遷移 javax → jakarta
mvn org.openrewrite.maven:rewrite-maven-plugin:run \
  -Drewrite.activeRecipes=org.openrewrite.java.migrate.jakarta.JavaxMigrationToJakarta
```

### 常見組態差異（需人工複查）

```yaml
# Spring Boot 2.x 預設 management.endpoints.web.exposure.include=info,health
# Spring Boot 3.x 起建議明確列出，避免預設值變動造成非預期端點曝露
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## 禁止事項

- 不得跳過 Java 版本前置需求直接嘗試升級 Spring Boot（會導致編譯失敗）
- 不得只做字串取代（`javax` → `jakarta`）而不確認語意是否正確（部分套件命名空間規則不同）
- 不得忽略升級後 Actuator/Security 預設組態的變化就直接上線

## 驗收標準

- [ ] 專案已運行於 Java 17+（Spring Boot 3 最低需求）
- [ ] 所有第三方相依套件皆已確認 Jakarta EE 相容版本
- [ ] 升級後 Actuator/Security 相關組態已人工複查並明確設定

## 參考資料

- Spring Boot 3.0 Migration Guide
