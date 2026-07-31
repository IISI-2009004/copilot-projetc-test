## Prompt 設計原則

### 好的 Prompt 設計

| 原則 | 說明 | 範例 |
|------|------|------|
| **角色明確** | 明確定義 AI 的角色 | 「你是一位資深 Java 開發工程師」 |
| **任務具體** | 明確描述要完成的任務 | 「產生 UserService 的單元測試」 |
| **輸出格式** | 定義具體的輸出格式 | 「使用 Markdown 表格列出所有 API」 |
| **限制條件** | 設定明確的限制 | 「使用 JUnit 5，不使用 JUnit 4」 |
| **變數化** | 使用 `{{}}` 參數化可變內容 | `{{target_class}}` |
| **安全內建** | 在每個 Prompt 中內建安全考量 | 「包含 OWASP Top 10 相關檢查」 |

### 反模式

| 反模式 | 問題 | 改善 |
|--------|------|------|
| 「幫我寫好程式」 | 太模糊，無法產出一致結果 | 明確描述功能、規範、限制 |
| 「做所有事」 | 範圍太大，品質無法保證 | 拆分為多個 Prompt |
| 無安全要求 | 忽略安全考量 | 加入安全檢查項目 |
| 無輸出格式 | 每次輸出不一致 | 定義明確的輸出模板 |
| 硬編碼內容 | 無法重複使用 | 使用 `{{}}` 變數 |

## Prompt 管理策略

### 目錄結構

```
.github/prompts/sdlc/
├── requirements/         # 需求階段
│   └── analyze-user-story.prompt.md
├── design/              # 設計階段
│   └── api-design.prompt.md
├── coding/              # 開發階段
│   └── implement-feature.prompt.md
├── testing/             # 測試階段
│   └── generate-unit-tests.prompt.md
├── security/            # 安全階段
│   └── threat-model.prompt.md
├── review/              # 審查階段
│   └── code-review.prompt.md
└── reverse-engineering/  # 逆向工程
    └── analyze-legacy-module.prompt.md
```

### 命名規範

```
{動詞}-{目標}.prompt.md

範例：
- analyze-user-story.prompt.md
- generate-unit-tests.prompt.md
- review-security.prompt.md
- implement-feature.prompt.md
```

### 團隊使用方式

```
VS Code 中使用 Prompt：
1. 開啟 Copilot Chat
2. 輸入 / 或點選附件圖示
3. 選擇「Prompt...」
4. 從列表中選擇 Prompt
5. 系統會提示填入變數
6. 送出後 Agent 會依據 Prompt 執行
```

### Prompt File 格式

````markdown
---
mode: "agent"
tools:
  - "editFiles"
  - "search"
  - "runTerminalCommand"
description: "Prompt 的用途說明"
---

# Prompt 標題

## 你的角色
{角色定義}

## 任務
{具體任務描述}

## 輸入
- `{{變數1}}`：{變數說明}
- `{{變數2}}`：{變數說明}

## 輸出要求
{輸出格式和品質要求}

## 限制
{限制條件}


### Frontmatter 欄位

| 欄位 | 類型 | 說明 |
|------|------|------|
| `agent` | string | 執行模式：`agent`（可使用工具）或省略（純對話） |
| `tools` | string[] | 可用工具清單（需 `mode: agent`） |
| `description` | string | Prompt 用途說明（顯示在選擇介面） |
```
