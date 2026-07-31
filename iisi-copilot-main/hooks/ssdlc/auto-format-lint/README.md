---
name: 'Auto Format and Lint'
description: 'PostToolUse Hook，在 Agent 編輯檔案後自動執行 Prettier 格式化與 ESLint/Ruff/Checkstyle Lint 檢查，確保程式碼風格一致性。支援 JavaScript、TypeScript、Python、Java 等語言。'
tags:
  - ssdlc
  - quality
  - formatting
  - linting
  - post-tool-use
---

# Auto Format and Lint

PostToolUse Hook，在 Agent 每次編輯檔案後自動執行格式化與 Lint 檢查。

## 支援語言

| 語言 | 格式化工具 | Lint 工具 |
|------|-----------|----------|
| JavaScript / TypeScript | Prettier | ESLint |
| Python | — | Ruff / Flake8 |
| Java | — | Checkstyle（Maven） |
| JSON / YAML / CSS / HTML | Prettier | — |

## 使用方式

```bash
cp hooks.json .github/hooks/auto-format-lint.json
cp scripts/auto-format.sh ./scripts/
cp scripts/auto-lint.sh ./scripts/
chmod +x ./scripts/auto-format.sh ./scripts/auto-lint.sh
```

## 前置需求

- **JavaScript/TypeScript**：`npm install --save-dev prettier eslint`
- **Python**：`pip install ruff` 或 `pip install flake8`
- **Java**：Maven 專案需配置 `maven-checkstyle-plugin`
