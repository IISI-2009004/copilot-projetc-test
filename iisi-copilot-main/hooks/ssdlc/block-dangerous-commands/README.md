---
name: "Block Dangerous Commands"
description: "PreToolUse Hook，在 Agent 執行 Shell 命令前檢查是否包含危險模式（如 rm -rf、DROP TABLE、格式化磁碟等），偵測到時自動阻擋並通知開發者。"
tags:
  - ssdlc
  - security
  - pre-tool-use
---

# Block Dangerous Commands

PreToolUse Hook，攔截並阻擋 Agent 試圖執行的危險 Shell 命令。

## 阻擋的危險模式

| 類別           | 命令模式                                        |
| -------------- | ----------------------------------------------- |
| 檔案系統破壞   | `rm -rf /`、`rm -rf ~`、`mkfs.*`                |
| 資料庫破壞     | `DROP TABLE`、`DROP DATABASE`、`TRUNCATE TABLE` |
| Git 危險操作   | `git push --force`、`git reset --hard`          |
| 權限過度開放   | `chmod 777`                                     |
| 遠端程式碼執行 | `curl \| bash`、`wget \| bash`                  |
| Fork 炸彈      | `:(){ :\|:& };:`                                |

## 使用方式

```bash
cp hooks.json .github/hooks/block-dangerous-commands.json
cp scripts/block-dangerous-commands.sh ./scripts/
chmod +x ./scripts/block-dangerous-commands.sh
```

## 自訂

編輯 `scripts/block-dangerous-commands.sh` 中的 `DANGEROUS_PATTERNS` 陣列以新增或移除阻擋規則。
