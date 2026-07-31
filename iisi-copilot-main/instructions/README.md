# 1. 建立 Custom Instructions

## 1.1 Instructions 類型總覽

| 類型 | 檔案位置 | 觸發方式 | 適用環境 | 用途 |
|------|---------|---------|---------|------|
| **Repo-wide Instructions** | `.github/copilot-instructions.md` | ✓ 自動套用至所有對話 | VS Code, JetBrains | 專案全域規範 |
| **File-based Instructions** | `.github/instructions/*.instructions.md` | ✓ 按 `applyTo` 模式自動套用 | VS Code | 針對特定檔案類型的規範 |
| **AGENTS.md** | `AGENTS.md`（Repository 根目錄） | ✓ 自動套用 | VS Code, GitHub.com, CLI | 跨 AI 工具通用指令 |
| **組織層級** | `.github-private` repo 的 `instructions/` | ✓ 自動套用 | VS Code | 組織統一規範 |
| **個人層級** | `~/.copilot/instructions/` | ✓ 自動套用 | VS Code | 個人偏好 |

### 優先順序

```
個人 Instructions > 檔案型 Instructions > Repo-wide Instructions > 組織 Instructions
```

## 1.2 Repo-wide Instructions

**檔案**：`.github/copilot-instructions.md`

```markdown
# 專案 Copilot Instructions

## 專案概述
本專案為企業級電商平台後端服務，使用 Java 21 + Spring Boot 3.3 技術棧。

## 程式碼規範

### 語言與框架
- Java 21（使用 Record、Pattern Matching、Virtual Thread 等新特性）
- Spring Boot 3.3.x
- Maven 建置

### 命名慣例
- 類別名稱：PascalCase（`UserService`）
- 方法與變數：camelCase（`findByEmail`）
- 常數：UPPER_SNAKE_CASE（`MAX_RETRY_COUNT`）
- 套件名稱：全小寫（`com.company.project.service`）

### 架構規範
- 分層架構：Controller → Service → Repository
- Controller 只做參數驗證與回應組裝
- Service 處理業務邏輯
- Repository 負責資料存取

### 日誌規範
- 使用 SLF4J + Log4j2
- ERROR：系統錯誤，需要立即處理
- WARN：可恢復的問題
- INFO：重要業務事件
- DEBUG：開發除錯資訊
- 禁止在日誌中輸出：密碼、Token、個資

### 安全規範
- 所有外部輸入必須驗證
- SQL 查詢使用參數化查詢
- 敏感資料傳輸使用 HTTPS
- 密碼儲存使用 BCrypt
- API 必須有認證與授權

### 測試規範
- 使用 JUnit 5 + Mockito + AssertJ
- 測試覆蓋率 ≥ 80%
- 測試命名：should_預期行為_When_條件

### 文件規範
- 所有 public 方法必須有 JavaDoc
- README 保持更新
- API 變更需更新 OpenAPI 規格
```

> **重要**：此檔案的內容會自動注入到每一次 Copilot 對話中，因此應保持精簡（建議 ≤ 500 行）。過長的指令會稀釋 AI 的注意力。

## 1.3 File-based Instructions

### 格式

```markdown
---
applyTo: "**/*.java"
---

# Java 檔案規範

（適用於所有 .java 檔案的規範）
```

### 範例集

**檔案**：`.github/instructions/backend-java.instructions.md`

```markdown
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
```

**檔案**：`.github/instructions/security.instructions.md`

```markdown
---
applyTo: "src/**/*.{java,ts,tsx}"
---

# 安全程式碼規範

## 輸入驗證
- 所有外部輸入（HTTP 參數、Header、Body）必須驗證
- 使用白名單驗證，不使用黑名單
- 數值輸入檢查範圍
- 字串輸入檢查長度與格式

## 注入防護
- SQL：使用 JPA/Hibernate 參數化查詢，禁止字串拼接 SQL
- XSS：輸出時進行 HTML 編碼
- Command Injection：禁止直接執行使用者輸入的系統指令
- Path Traversal：驗證檔案路徑，禁止 ../ 

## 認證與授權
- 使用 Spring Security
- API 端點使用 @PreAuthorize 或 @Secured
- 實作最小權限原則

## 敏感資料
- 密碼使用 BCrypt 雜湊
- 敏感配置使用環境變數或 Vault
- 日誌不可包含密碼、Token、個資
- API 回應不可包含內部錯誤堆疊
```

**檔案**：`.github/instructions/testing.instructions.md`

```markdown
---
applyTo: "src/test/**/*.java"
---

# 測試程式碼規範

## 框架
- JUnit 5 + Mockito + AssertJ

## 結構
- 使用 AAA 模式：Arrange → Act → Assert
- 每個測試方法只測試一個行為
- Mock 外部依賴（資料庫、API、訊息佇列）

## 命名
- 測試類別：{被測類別}Test（如 UserServiceTest）
- 測試方法：should_{預期行為}_When_{條件}
- 使用 @DisplayName 提供中文描述

## 安全測試
- 每個 Controller 測試必須包含未認證存取測試
- 測試無效輸入（SQL Injection payload、XSS payload）
```

**檔案**：`.github/instructions/code-review.instructions.md`

```markdown
---
applyTo: "**/*.{java,ts,tsx,js,jsx}"
---

# Code Review 指引

## 審查重點
當被要求審查程式碼時，請依以下優先序檢查：

1. **安全性**（最高優先）
   - 注入漏洞、認證繞過、資料洩漏
   
2. **正確性**
   - 業務邏輯錯誤、邊界條件、Race Condition
   
3. **效能**
   - N+1 查詢、不必要的記憶體分配、阻塞操作
   
4. **可維護性**
   - SOLID 違規、過度複雜、重複程式碼
   
5. **測試**
   - 覆蓋率、邊界測試、錯誤路徑測試
```

**檔案**：`.github/instructions/pr-description.instructions.md`

```markdown
---
applyTo: "**"
---

# PR Description 指引

當被要求撰寫 PR 描述時，使用以下格式：

## 變更摘要
{一句話描述本次變更的目的}

## 變更類型
- [ ] 新功能
- [ ] Bug 修正
- [ ] 重構
- [ ] 文件
- [ ] 安全修正

## 變更內容
{詳細描述變更內容，使用條列式}

## 安全影響
{描述本次變更的安全影響，如無影響請註明}

## 測試
- [ ] 單元測試通過
- [ ] 整合測試通過
- [ ] 手動測試完成

## 相關 Issue
Closes #{issue_number}
```

## 1.4 AGENTS.md

`AGENTS.md` 是放在 Repository 根目錄的通用指令檔案，適用於所有支援此格式的 AI 工具（VS Code Copilot、Claude Code 等）。

```markdown
# AGENTS.md

## 專案背景
本專案為企業級電商平台。

## 通用規則
1. 使用繁體中文撰寫註解和文件
2. 程式碼必須遵循專案既定的架構模式
3. 所有程式碼變更必須包含對應的測試
4. 安全是第一優先考量

## 禁止事項
1. 不可刪除現有的測試案例
2. 不可降低測試覆蓋率
3. 不可在日誌中輸出敏感資訊
4. 不可使用 deprecated 的 API
5. 不可提交含有已知 CVE 的依賴
```

> **`AGENTS.md` vs `.github/copilot-instructions.md`**：`AGENTS.md` 是跨工具通用的，而 `copilot-instructions.md` 專屬於 GitHub Copilot。如果同時存在，兩者都會被載入。

## 1.5 組織層級 Instructions

### 設定步驟

```
1. 在組織中建立 `.github-private` Repository
2. 建立 instructions/ 目錄
3. 加入組織級 Instructions 檔案

.github-private/
└── instructions/
    ├── org-coding-standards.instructions.md
    ├── org-security-policy.instructions.md
    └── org-naming-convention.instructions.md
```

### 範例

**檔案**：`.github-private/instructions/org-security-policy.instructions.md`

```markdown
---
applyTo: "**"
---

# 組織安全政策

## 強制規範
1. 所有 API 必須使用 HTTPS
2. 密碼雜湊使用 BCrypt（cost factor ≥ 12）
3. JWT Token 過期時間 ≤ 1 小時
4. 所有敏感操作必須記錄稽核日誌
5. 第三方依賴必須經過安全掃描

## 禁止事項
1. 禁止使用 MD5、SHA-1 做密碼雜湊
2. 禁止在程式碼中硬編碼金鑰或密碼
3. 禁止使用 eval() 或類似的動態執行
4. 禁止關閉 CSRF 保護（除非有明確理由並經安全團隊核准）
```

## 1.6 Instructions 設計最佳實務

| 原則 | 說明 |
|------|------|
| **精簡** | `copilot-instructions.md` 建議 ≤ 500 行，避免稀釋注意力 |
| **具體** | 使用具體的程式碼範例，不要只寫抽象規則 |
| **可驗證** | 每條規則都應該可以客觀驗證（能或不能） |
| **分層** | 通用規則放 `copilot-instructions.md`，特定規則用 file-based |
| **不重複** | 避免在多個 Instructions 檔案中重複相同規則 |
| **安全優先** | 安全規範應在每層都有涵蓋 |
| **定期更新** | 隨專案演進定期審視與更新 Instructions |

---
