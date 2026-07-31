---
name: 'SSDLC Guardrails'
description: '綜合性 SSDLC 護欄 Hook，涵蓋 Agent 會話完整生命週期：SessionStart 注入專案上下文、PreToolUse 阻擋危險命令、PostToolUse 自動格式化與 Lint 檢查、Stop 產生會話報告。'
tags:
  - ssdlc
  - security
  - guardrails
  - enterprise
---

# SSDLC Guardrails

綜合性 SSDLC（Secure Software Development Lifecycle）護欄 Hook，在 Agent 會話的關鍵生命週期節點自動執行安全與品質檢查。

## 涵蓋事件

| 事件 | 用途 |
|------|------|
| `SessionStart` | 注入專案上下文（版本、分支、環境資訊） |
| `PreToolUse` | 阻擋危險命令（`rm -rf`、`DROP TABLE`、`git push --force`） |
| `PostToolUse` | 自動格式化（Prettier）+ Lint 檢查 |
| `Stop` | 產生 Agent 會話摘要報告 |

## 使用方式

將 `hooks.json` 複製至 `.github/hooks/ssdlc-guardrails.json`，並將 `scripts/` 目錄下的腳本放入專案根目錄的 `scripts/` 資料夾。

```bash
cp hooks.json .github/hooks/ssdlc-guardrails.json
cp scripts/* ./scripts/
chmod +x ./scripts/*.sh
```

## 腳本說明

- **inject-project-context.sh** — 會話開始時注入 Git 分支、最後 commit、Node/Java 版本等上下文
- **block-dangerous-commands.sh** — 阻擋危險的 Shell 命令與 SQL 操作
- **lint-check.sh** — 執行 ESLint 或專案配置的 Linter
- **generate-session-report.sh** — 會話結束時產生變更摘要報告
