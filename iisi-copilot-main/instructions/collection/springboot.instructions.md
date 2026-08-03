---
description: "建立基於 Spring Boot 的應用程式的指南"
applyTo: "**/*.java, **/*.kt"
---

# Spring Boot 開發指南

## General Instructions 一般說明

- 審查程式碼變更時，只提出有高度把握的建議。
- 使用良好的可維護性實踐編寫代碼，包括註釋說明為什麼做出某些設計決策。
- 處理邊界情況並編寫清晰的異常處理。
- 對於庫或外部依賴，請在註釋中說明其用途和目的。

## Spring Boot 指南

### 依賴注入

- 對所有必需的依賴使用構造函數注入。
- 將依賴字段聲明為 `private final`。

### 配置

- 使用 YAML 文件（`application.yml`）進行外部化配置。
- 環境配置：使用 Spring 配置文件（profiles）來區分不同環境（開發、測試、生成）。
- 配置屬性：使用 `@ConfigurationProperties` 進行類型安全的配置綁定。
- 秘密管理：使用環境變量或秘密管理系統外部化秘密。

### 代碼組織

- 包結構：按功能/領域組織，而不是按層次。
- 關注點分離：保持控制器精簡，服務專注，倉庫簡單。
- 工具類：將工具類聲明為 `final` 並使用私有構造函數。

### 服務層

- 將業務邏輯放在使用 `@Service` 註解的類中。
- 服務應該是無狀態且可測試的。
- 通過構造函數注入倉庫。
- 服務方法簽名應使用領域 ID 或 DTO，而不是直接暴露倉庫實體，除非必要。

### 日誌

- 使用 SLF4J 進行所有日誌記錄 (`private static final Logger logger = LoggerFactory.getLogger(MyClass.class);`)。
- 不要直接使用具體實現（Logback、Log4j2）或 `System.out.println()`。
- 使用參數化日誌：`logger.info("User {} logged in", userId);`。

### 安全性與輸入處理

- 使用參數化查詢 | 始終使用 Spring Data JPA 或 `NamedParameterJdbcTemplate` 以防止 SQL 注入。
- 使用 JSR-380 (`@NotNull`, `@Size` 等) 註解和 `BindingResult` 驗證請求體和參數。

## Build and Verification

- 添加或修改代碼後，驗證項目是否能成功構建。
- 如果項目使用 Maven，運行 `mvn clean package`。
- 如果項目使用 Gradle，運行 `./gradlew build`（在 Windows 上運行 `gradlew.bat build`）。
- 確保所有測試在構建過程中通過。

## 常用命令

| Gradle Command             | Maven Command                    | Description                |
| :------------------------- | :------------------------------- | :------------------------- |
| `./gradlew bootRun`        | `./mvnw spring-boot:run`         | 運行應用程式。             |
| `./gradlew build`          | `./mvnw package`                 | 構建應用程式。             |
| `./gradlew test`           | `./mvnw test`                    | 運行測試。                 |
| `./gradlew bootJar`        | `./mvnw spring-boot:repackage`   | 將應用程式打包為 JAR。     |
| `./gradlew bootBuildImage` | `./mvnw spring-boot:build-image` | 將應用程式打包為容器映像。 |
