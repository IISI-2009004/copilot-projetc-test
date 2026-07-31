---
name: 'Validate No Secrets'
description: 'PreToolUse Hook，在 Agent 建立或編輯檔案前掃描內容是否包含硬編碼的密碼、API Key、Token 等敏感資訊，偵測到時阻擋操作並提醒使用環境變數替代。'
tags:
  - ssdlc
  - security
  - secrets
  - pre-tool-use
---

# Validate No Secrets

PreToolUse Hook，防止 Agent 在程式碼中寫入硬編碼的敏感資訊。

## 偵測的敏感模式

| 類別 | 模式範例 |
|------|---------|
| AWS 金鑰 | `AKIA...`（AWS Access Key ID） |
| API 金鑰 | `api_key =`、`apiKey:`、`API_KEY=` |
| 密碼 | `password =`、`passwd:`、`secret =` |
| Token | `token =`、`bearer`、`jwt` |
| 連線字串 | `jdbc:...password=`、`mongodb+srv://...` |
| 私鑰 | `BEGIN RSA PRIVATE KEY`、`BEGIN OPENSSH PRIVATE KEY` |

## 使用方式

```bash
cp hooks.json .github/hooks/validate-no-secrets.json
cp scripts/validate-no-secrets.sh ./scripts/
chmod +x ./scripts/validate-no-secrets.sh
```

## 建議

搭配 `.gitignore` 排除 `.env` 檔案，並使用環境變數或 Secret Manager 管理敏感資訊。
