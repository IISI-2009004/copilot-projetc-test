---
applyTo: '*'
description: 'Quarkus 開發標準和說明'
---

- 使用 Java 17 或更高版本開發高品質 Quarkus 應用程式的說明。

## Project Context 項目背景

- 最新 Quarkus 版本：3.x
- Java 版本：17 或更高
- 使用 Maven 或 Gradle 進行構建管理。
- 專注於乾淨的架構、可維護性和性能。

## Development Standards 開發標準

  - 為每個類、方法和複雜邏輯撰寫清晰簡明的註解。
  - 使用 Javadoc 為公共 API 和方法提供清晰的說明。
  - 在整個項目中保持一致的編碼風格，遵循 Java 約定。
  - 遵循 Quarkus 編碼標準和最佳實踐，以實現最佳性能和可維護性。
  - 遵循 Jakarta EE 和 MicroProfile 約定，確保包組織清晰。
  - 在適當的情況下使用 Java 17 或更高版本的特性，如 records 和 sealed classes。


## Naming Conventions 命名約定
  - 使用 PascalCase 命名類（例如，`ProductService`，`ProductResource`）。
  - 使用 camelCase 命名方法和變量（例如，`findProductById`，`isProductAvailable`）。
  - 使用 ALL_CAPS 命名常量（例如，`DEFAULT_PAGE_SIZE`）。

##  Quarkus
  - 利用 Quarkus Dev Mode 加快開發週期。
  - 使用 Quarkus 擴展和最佳實踐實現構建時優化。
  - 配置 GraalVM 的原生構建以獲得最佳性能（例如，使用 quarkus-maven-plugin）。
  - 使用 Quarkus 日誌功能（JBoss、SL4J 或 JUL）以保持一致的日誌實踐。

### Quarkus-Specific Patterns 和最佳實踐
- 對於單例 bean，請使用 @ApplicationScoped 而不是 @Singleton
- 使用 `@Inject` 進行依賴注入
- 優先使用 Panache 存儲庫而不是傳統的 JPA 存儲庫
- 在修改數據的服務方法上使用 `@Transactional`
- 為 REST 端點應用描述性的 `@Path`
- 對於 REST 資源，使用 `@Consumes(MediaType.APPLICATION_JSON)` 和 `@Produces(MediaType.APPLICATION_JSON)`

### REST Resources REST 資源
- 始終使用 JAX-RS 註解（`@Path`、`@GET`、`@POST` 等）
- 返回適當的 HTTP 狀態碼（200、201、400、404、500）
- 對於複雜的響應，使用 `Response` 類
- 使用 try-catch 塊進行適當的錯誤處理
- 使用 Bean Validation 註解驗證輸入參數
- 為公共端點實施速率限制

### Data Access 數據訪問
- 優先使用 Panache 實體（繼承 `PanacheEntity`）而不是傳統的 JPA
- 使用 Panache 存儲庫（`PanacheRepository<T>`）進行複雜查詢
- 對於數據修改操作，始終使用 `@Transactional`
- 對於複雜的數據庫操作，使用命名查詢
- 為列表端點實現適當的分頁


### Configuration 配置
- 使用 `application.properties` 或 `application.yaml` 進行簡單配置
- 使用 `@ConfigProperty` 進行類型安全的配置
- 對於敏感數據，優先使用環境變量
- 使用配置文件區分不同環境（開發、測試、生產）


### Testing 測試
- 使用 `@QuarkusTest` 進行集成測試
- 使用 JUnit 5 進行單元測試
- 使用 `@QuarkusIntegrationTest` 進行原生構建測試
- 使用 `@QuarkusTestResource` 模擬外部依賴
- 使用 RestAssured 測試 REST 端點（`@QuarkusTestResource`）
- 對修改數據的測試使用 `@Transactional`
- 使用測試容器進行數據庫集成測試

### Don't use these patterns: 不要使用這些模式
- 不要在測試中使用字段注入（使用構造函數注入）
- 不要硬編碼配置值
- 不要忽略異常


## Development Workflow 開發工作流程

### 建立新功能時： 
1. Create entity with proper validation 建立具有適當驗證的實體
2. Create repository with custom queries 建立具有自定義查詢的存儲庫
3. Create service with business logic 建立具有業務邏輯的服務
4. Create REST resource with proper endpoints 建立具有適當端點的 REST 資源
5. Write comprehensive tests 編寫全面的測試
6. Add proper error handling 添加適當的錯誤處理
7. Update documentation 更新文檔

## Security Considerations 安全考量

### When implementing security: 實施安全性時
- Use Quarkus Security extensions (e.g., `quarkus-smallrye-jwt`, `quarkus-oidc`) 使用 Quarkus 安全擴展（例如，`quarkus-smallrye-jwt`，`quarkus-oidc`）。
- Implement role-based access control (RBAC) using MicroProfile JWT or OIDC 使用 MicroProfile JWT 或 OIDC 實現基於角色的訪問控制（RBAC）。
- Validate all input parameters 驗證所有輸入參數
