---
name: 'Protect Sensitive Files'
description: 'PreToolUse Hook，保護敏感檔案不被 Agent 自動修改或刪除，包括 .env 設定檔、CI/CD Workflow、安全設定檔等。修改這些檔案時需要人工確認。'
tags:
  - ssdlc
  - security
  - file-protection
  - pre-tool-use
---

# Protect Sensitive Files

PreToolUse Hook，當 Agent 試圖修改或刪除敏感檔案時，要求人工確認或直接阻擋。

## 保護的檔案類別

| 類別 | 檔案模式 | 動作 |
|------|---------|------|
| 環境設定 | `.env`、`.env.*`（非 `.env.example`） | 要求確認（ask） |
| CI/CD 配置 | `.github/workflows/*` | 阻擋（deny） |
| 安全設定 | `**/security*.yml`、`**/auth*config*` | 要求確認（ask） |
| 基礎設施 | `Dockerfile`、`docker-compose.yml`、`*.tf` | 要求確認（ask） |
| 套件鎖定 | `package-lock.json`、`yarn.lock`、`pom.xml` | 要求確認（ask） |
| Hook 腳本 | `.github/hooks/*`、`scripts/*.sh` | 阻擋（deny） |

## 使用方式

```bash
cp hooks.json .github/hooks/protect-sensitive-files.json
cp scripts/protect-sensitive-files.sh ./scripts/
chmod +x ./scripts/protect-sensitive-files.sh
```

## 自訂

編輯 `scripts/protect-sensitive-files.sh` 中的 `DENY_PATTERNS` 和 `ASK_PATTERNS` 陣列以調整保護範圍。
