---
applyTo: "src/main/java/**/*.java"
---

# Java 後端程式碼規範

## 通用規則
- 使用 Java 21 語法特性
- 所有 public 類別和方法必須有 JavaDoc 註解
- 使用 Lombok 的 @Slf4j 替代手動建立 Logger
- 例外處理使用自訂 BusinessException 體系

## Controller 規則
- 使用 @RestController 和 @RequestMapping
- 參數驗證使用 @Valid 和 Bean Validation 註解
- 回傳統一的 ApiResponse<T> 包裝

## Service 規則
- 使用 @Service 註解
- 事務管理使用 @Transactional（只在需要時）
- 複雜業務邏輯拆分為私有方法

## Repository 規則
- 繼承 JpaRepository
- 複雜查詢使用 @Query 或 Specification
- 禁止在 Repository 中寫業務邏輯
