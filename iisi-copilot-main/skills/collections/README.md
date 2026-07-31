# Skill Library Collections

依《Loop Engineering 教學手冊》[4.3.1　Skill Library 目錄結構](../../tutorials/AI/Loop%20Engineering%20%E6%95%99%E5%AD%B8%E6%89%8B%E5%86%8A.md) 與 [第十五章　Skill Library](../../tutorials/AI/Loop%20Engineering%20%E6%95%99%E5%AD%B8%E6%89%8B%E5%86%8A.md) 落地的企業技能庫，共 5 大分類、26 個 Skill（Skill-01～Skill-26）。

每個 Skill 為獨立目錄 `<category>/<skill-name>/SKILL.md`，格式依 [skills/README.md](../README.md) 所述的 Agent Skill 開放標準（frontmatter 必要欄位 `name` + `description`，可被 Claude Code／GitHub Copilot 等工具依情境自動載入），並保留手冊的企業級中繼資料（`skill`、`version`、`tags`、`applicable-to`、`last-updated`）。內文統一採用：**適用情境 → 核心原則 → 標準做法 → 禁止事項 → 驗收標準 → 參考資料**（附錄 C 範本）。

在 Prompt 或 Sub-agent 設定中可用「載入 `<skill-name>` 技能後再執行任務」的方式引用。

## 15.1　Coding（`coding/`，Skill-01～06）

| Skill | 說明 |
|---|---|
| [java-spring-boot](coding/java-spring-boot/SKILL.md) | Spring Boot 開發技能（Java 21 + Spring Boot 3.x 三層架構） |
| [vue3-typescript](coding/vue3-typescript/SKILL.md) | Vue3 + TypeScript 元件與 Composable 開發技能 |
| [clean-code](coding/clean-code/SKILL.md) | Clean Code 可讀性與可維護性技能 |
| [design-patterns](coding/design-patterns/SKILL.md) | 設計模式應用技能 |
| [api-design](coding/api-design/SKILL.md) | RESTful API 設計技能 |
| [database-optimization](coding/database-optimization/SKILL.md) | 資料庫優化技能（N+1、索引設計） |

## 15.2　Architecture（`architecture/`，Skill-07～11）

| Skill | 說明 |
|---|---|
| [microservices](architecture/microservices/SKILL.md) | 微服務架構技能（邊界劃分、Saga） |
| [hexagonal-architecture](architecture/hexagonal-architecture/SKILL.md) | 六角形架構技能（Ports & Adapters） |
| [ddd](architecture/ddd/SKILL.md) | DDD 領域驅動設計技能 |
| [event-driven](architecture/event-driven/SKILL.md) | 事件驅動架構技能（Kafka、冪等消費） |
| [cloud-native](architecture/cloud-native/SKILL.md) | Cloud Native 技能（Kubernetes、Twelve-Factor） |

## 15.3　Testing（`testing/`，Skill-12～16）

| Skill | 說明 |
|---|---|
| [unit-testing-junit5](testing/unit-testing-junit5/SKILL.md) | JUnit 5 單元測試技能 |
| [integration-testing](testing/integration-testing/SKILL.md) | 整合測試技能（Testcontainers） |
| [e2e-testing-playwright](testing/e2e-testing-playwright/SKILL.md) | E2E 測試技能（Playwright） |
| [performance-testing](testing/performance-testing/SKILL.md) | 效能測試技能（k6） |
| [security-testing](testing/security-testing/SKILL.md) | 安全測試技能（水平/垂直權限提升） |

## 15.4　Security（`security/`，Skill-17～21）

| Skill | 說明 |
|---|---|
| [owasp-top10](security/owasp-top10/SKILL.md) | OWASP Top 10 技能 |
| [sast-analysis](security/sast-analysis/SKILL.md) | 靜態分析技能（SAST／SonarQube Quality Gate） |
| [dependency-scanning](security/dependency-scanning/SKILL.md) | 相依套件掃描技能（CVE／SCA） |
| [secrets-management](security/secrets-management/SKILL.md) | 秘密管理技能（Vault） |
| [threat-modeling](security/threat-modeling/SKILL.md) | 威脅建模技能（STRIDE） |

## 15.5　Migration（`migration/`，Skill-22～26）

| Skill | 說明 |
|---|---|
| [java6-to-java21](migration/java6-to-java21/SKILL.md) | Java 版本升級技能（Java 6 → Java 21） |
| [struts-to-spring-boot](migration/struts-to-spring-boot/SKILL.md) | Struts 遷移技能（Struts → Spring Boot） |
| [jsp-to-vue3](migration/jsp-to-vue3/SKILL.md) | JSP 遷移技能（JSP → Vue3） |
| [spring-boot-2-to-3](migration/spring-boot-2-to-3/SKILL.md) | Spring Boot 升級技能（2.x → 3.x） |
| [monolith-to-microservices](migration/monolith-to-microservices/SKILL.md) | 單體到微服務技能 |

## 與 `skills/ssdlc/` 的關係

`skills/ssdlc/` 收錄的是流程類 Skill（security-review、junit-generator、pr-checker、api-reviewer、reverse-analysis、doc-generator），偏向「怎麼做一件審查/產生任務」。`skills/collections/` 收錄的是依技術主題分類的知識型 Skill，偏向「這個技術領域的標準做法與禁止事項」，兩者互補、可同時載入使用。
