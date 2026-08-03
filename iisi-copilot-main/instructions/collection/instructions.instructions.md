---
description: "建立高品質 GitHub Copilot 自訂指令檔的指南，涵蓋結構、內容和最佳實踐，以指導 AI 生成符合專案慣例的領域特定程式碼。"
applyTo: "**/*.instructions.md"
---

# Custom Instructions File Guidelines

建立有效且易於維護的自訂指令檔案的說明，這些檔案可指導 GitHub Copilot 產生特定領域的程式碼並遵循專案約定。

## Project Context

- Target audience: 使用特定領域程式碼的開發者和 GitHub Copilot 用戶
- File format: 使用 YAML 前置標記的 Markdown
- File naming convention: 小寫加連字號 (例如 `react-best-practices.instructions.md`)
- Location: `.github/instructions/` 目錄
- Purpose: 提供上下文感知的指導，用於程式碼生成、審查和文件編寫

## Required Frontmatter

每個指令檔案都必須包含 YAML 前元數據，其中包含以下欄位：

```yaml
---
description: "指令的目的和範圍的簡要描述"
applyTo: "目標檔案的 glob 模式 (例如 **/*.ts, **/*.py)"
---
```

### Frontmatter Guidelines

- **description**: 用單引號括起來的字串，長度為 1-500 個字元，清楚說明用途
- **applyTo**: 指定這些指令套用於哪些檔案的 Glob 模式
  - Single pattern: `'**/*.ts'`
  - Multiple patterns: `'**/*.ts, **/*.tsx, **/*.js'`
  - Specific files: `'src/**/*.py'`
  - All files: `'**'`

## File Structure

一份結構完善的說明書應包含以下幾個部分：

### 1. Title and Overview

- 使用 # 標題 的清晰、描述性標題
- 簡要介紹說明檔的目的和範圍
- 可選：專案上下文部分，包含關鍵技術和版本資訊

### 2. Core Sections

根據領域將內容組織成邏輯部分：

- **General Instructions**: 高層次的指導原則和準則
- **Best Practices**: 推薦的模式和方法
- **Code Standards**: 命名規範、格式化、風格規則
- **Architecture/Structure**: 專案組織和設計模式
- **Common Patterns**: 常用實作方式
- **Security**: 安全注意事項（如適用）
- **Performance**: 優化指南（如適用）
- **Testing**: 測試標準和方法（如適用）

### 3. Examples and Code Snippets

提供帶有清晰標籤的具體範例：

```markdown
### Good Example

\`\`\`language
// Recommended approach
code example here
\`\`\`

### Bad Example

\`\`\`language
// Avoid this pattern
code example here
\`\`\`
```

### 4. Validation and Verification (Optional but Recommended)

- 建置命令以驗證程式碼
- 程式碼檢查和格式化工具
- 測試需求
- 驗證步驟

## Content Guidelines

### Writing Style

- 使用清晰簡潔的語言
- 使用命令式語氣 ("使用", "實作", "避免")
- 具體且可操作
- 避免模糊的術語，如 "應該", "可能", "或許"
- 使用項目符號和列表以提高可讀性
- 保持章節專注且易於掃描

### Best Practices

- **Be Specific**: 提供具體例子，而不是抽象概念。
- **Show Why**: 解釋提出建議的理由，尤其是在建議能帶來價值的情況下。
- **Use Tables**: 用於比較選項、列出規則或顯示模式
- **Include Examples**: 實際程式碼片段比文字描述更有效。
- **Stay Current**: 參考最新版本和最佳實踐
- **Link Resources**: 包括官方文件和權威來源

### Instruction Altitude (Goldilocks Zone)

- 首先制定一套能夠完整定義預期結果的最小規則集
- 在觀察到失敗之後添加約束，而不是在假設的極端情況下添加約束。
- 比起詳盡的決策表，更傾向於選擇高訊號範例。

| Altitude 高度            | Failure Mode 故障模式                     | Result 結果                                      |
| ------------------------ | ----------------------------------------- | ------------------------------------------------ |
| Over-specified 過度指定  | Brittle if-else prose 脆弱的 if-else 語句 | Breaks on unlisted cases 未列入案件的案件        |
| Under-specified 過度簡化 | Assumes shared context 假設共享上下文     | Generic outputs 通用輸出                         |
| Right altitude 適中高度  | Heuristics + examples 啟發式 + 範例       | Stable, generalizable quality 穩定且可泛化的品質 |

### Common Patterns to Include

1. **Naming Conventions**: 如何命名變數、函數、類別和文件
2. **Code Organization**: 檔案結構、模組組織、匯入順序
3. **Error Handling**: 首選的錯誤處理模式
4. **Dependencies**: 如何管理和記錄依賴關係
5. **Comments and Documentation**: 何時以及如何編寫程式碼文檔
6. **Version Information**: 目標語言/框架版本

## Patterns to Follow

### Bullet Points and Lists

```markdown
## Security Best Practices

- 處理使用者輸入前務必進行驗證
- 使用參數化查詢以防止 SQL 注入
- 將金鑰儲存在環境變數中，切勿儲存在程式碼中
- 實施正確的身份驗證和授權機制
- 為所有生產環境端點啟用 HTTPS
```

### Tables for Structured Information

```markdown
## Common Issues

| 問題     | 解決方案     | 範例                    |
| -------- | ------------ | ----------------------- |
| 魔法數字 | 使用命名常數 | `const MAX_RETRIES = 3` |
| 深度嵌套 | 提取函數     | 重構嵌套的 if 語句      |
| 硬編碼值 | 使用設定     | 將 API URL 儲存在設定中 |
```

### Code Comparison

```markdown
### Good Example - Using TypeScript interfaces

\`\`\`typescript
interface User {
id: string;
name: string;
email: string;
}

function getUser(id: string): User {
// Implementation
}
\`\`\`

### Bad Example - Using any type

\`\`\`typescript
function getUser(id: any): any {
// Loses type safety
}
\`\`\`
```

### Conditional Guidance

```markdown
## Framework Selection

- **小型專案**：採用最小 API 架構
- **大型專案**：採用基於控制器的架構，並明確劃分功能模組
- **微服務**：考慮領域驅動設計模式
```

## Patterns to Avoid

- **解釋過於冗長** ：力求簡潔明了，方便瀏覽。
- **過時資訊**：始終參考最新版本和最佳實踐
- **模糊指導**：明確說明應該做什麼或避免什麼
- **缺少範例**：抽象規則缺乏具體程式碼範例
- **矛盾建議**：確保整個文件的一致性
- **從文件複製貼上**：通過提煉和上下文化來增加價值
- **假設規則膨脹**: 不要為尚未發生的失敗添加規則

## Testing Your Instructions

在最終確定指令檔之前：

1. **Test with Copilot**: 嘗試在 VS Code 中使用實際提示執行這些說明。
2. **Verify Examples**: 確保程式碼範例正確且能正常運行
3. **Check Glob Patterns**: 確認 `applyTo` 模式匹配預期的檔案

## Example Structure

以下是一個新建指令檔的最小範例結構：

```markdown
---
description: "簡要描述目的"
applyTo: "**/*.ext"
---

# Technology Name Development

Brief introduction and context.

## General Instructions

- High-level guideline 1
- High-level guideline 2

## Best Practices

- Specific practice 1
- Specific practice 2

## Code Standards

### Naming Conventions

- Rule 1
- Rule 2

### File Organization

- Structure 1
- Structure 2

## Common Patterns

### Pattern 1

Description and example

\`\`\`language
code example
\`\`\`

### Pattern 2

Description and example

## Validation

- Build command: `command to verify`
- Linting: `command to lint`
- Testing: `command to test`
```

## Maintenance

- 依賴項或框架更新時，請查看相關說明。
- 更新範例以反映當前最佳實踐
- 移除過時的模式或已棄用的功能
- 隨著社群的發展，添加新的模式
- 隨著專案結構的演變，保持 glob 模式的準確性

## Additional Resources

- [Custom Instructions Documentation](https://code.visualstudio.com/docs/copilot/customization/custom-instructions)
- [Awesome Copilot Instructions](https://github.com/github/awesome-copilot/tree/main/instructions)
- [System Prompt Altitude — Effective Context Engineering for AI Agents](https://www.anthropic.com/engineering/effective-context-engineering-for-ai-agents#the-anatomy-of-effective-context)
