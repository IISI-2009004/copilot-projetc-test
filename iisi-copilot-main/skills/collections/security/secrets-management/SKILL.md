---
name: "secrets-management"
description: '處理 API Key、資料庫密碼、憑證等機密資訊的存放、注入與輪換時使用'
skill: secrets-management
version: 1.0
tags: [secrets-management, vault, credentials]
applicable-to: [Vault, Kubernetes]
last-updated: 2026-07-20
---

# 秘密管理技能

## 適用情境
處理 API Key、資料庫密碼、憑證等機密資訊的存放、注入與輪換時使用。

## 核心原則
1. 機密資訊一律不進入原始碼、不進入 AI 的上下文
2. 使用集中式密鑰管理服務（Vault/KMS/Secret Manager），而非環境變數明文散落各處
3. 機密資訊需定期輪換，且輪換流程需自動化以降低人工疏漏風險
4. 稽核所有機密存取行為，異常存取需觸發告警

## 標準做法

### 從 Vault 注入機密（而非寫死於設定檔）
```yaml
spring:
  config:
    import: "vault://secret/data/my-banking-app"
  cloud:
    vault:
      uri: https://vault.company.com
      authentication: KUBERNETES
```

### .gitignore 與掃描防止機密外洩
```bash
# pre-commit hook：提交前掃描是否誤入機密
trufflehog filesystem . --only-verified --fail
```

## 禁止事項
- 不得把任何 API Key／密碼／Token 提交進版本控制（即使之後刪除也已留在歷史紀錄中）
- 不得讓 AI Agent 的 Prompt 或 Context 中包含明文機密（應以參考路徑代替實際值）
- 不得讓多個環境（Dev/Staging/Prod）共用同一組機密

## 驗收標準
- [ ] 所有機密透過集中式密鑰管理服務注入，程式碼與版本控制中無明文機密
- [ ] Pre-commit／CI 皆有機密掃描關卡（如 TruffleHog）
- [ ] 機密輪換與存取皆有稽核紀錄

## 參考資料
- HashiCorp Vault 官方文件（https://developer.hashicorp.com/vault）
