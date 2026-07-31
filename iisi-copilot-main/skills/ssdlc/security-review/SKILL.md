---
name: "security-review"
description: '執行 OWASP Top 10 安全審查，識別程式碼中的安全漏洞'
---

# Security Review Skill

## 能力
此 Skill 可以：
1. 基於 OWASP Top 10 審查程式碼安全性
2. 識別常見的安全漏洞模式
3. 執行依賴安全掃描
4. 產出結構化的安全審查報告

## 使用方式
當需要進行安全審查時，請：

1. **識別審查範圍**：確定要審查的檔案或模組
2. **執行靜態分析**：掃描程式碼中的安全漏洞模式
3. **依賴掃描**：如果存在支援的依賴管理檔案，執行：
   ```bash
   ./scripts/owasp-check.sh
   ```
4. **產出報告**：使用以下格式

## 安全漏洞檢查清單

### A01 - Broken Access Control
- [ ] 是否有缺少授權檢查的端點
- [ ] 是否有 IDOR（Insecure Direct Object Reference）
- [ ] CORS 配置是否正確

### A02 - Cryptographic Failures
- [ ] 是否使用了過時的加密算法（MD5、SHA-1、DES）
- [ ] 密碼是否使用 BCrypt/Argon2 雜湊
- [ ] 敏感資料是否加密傳輸

### A03 - Injection
- [ ] SQL 查詢是否使用參數化
- [ ] 是否有 OS Command Injection 風險
- [ ] 是否有 LDAP/XPath Injection

### A04 - Insecure Design
- [ ] 是否實作 Rate Limiting
- [ ] 是否有適當的輸入驗證

### A05 - Security Misconfiguration
- [ ] 是否暴露了 debug/stack trace 資訊
- [ ] 預設帳號/密碼是否已移除
- [ ] 不必要的功能或服務是否已停用

### A06 - Vulnerable and Outdated Components
- [ ] 第三方套件是否有已知 CVE 漏洞
- [ ] 依賴版本是否固定（lockfile）
- [ ] 是否有自動化依賴更新機制（Dependabot / Renovate）

### A07 - Identification and Authentication Failures
- [ ] Session 是否在登出後正確失效
- [ ] 是否實作多因素驗證（MFA）
- [ ] 密碼政策是否符合最低強度要求
- [ ] 是否有帳號枚舉（Account Enumeration）風險
- [ ] JWT 簽章演算法是否安全（禁止 alg: none）

### A08 - Software and Data Integrity Failures
- [ ] CI/CD pipeline 是否有完整性驗證
- [ ] 第三方套件是否驗證來源與完整性（SRI / checksum）
- [ ] 反序列化操作是否對不受信任的資料進行驗證
- [ ] 自動更新功能是否驗證簽章

### A09 - Security Logging and Monitoring Failures
- [ ] 身份驗證失敗是否記錄日誌
- [ ] 高風險操作（刪除、權限變更）是否有稽核日誌
- [ ] 日誌是否包含敏感資料（密碼、Token）
- [ ] 是否有異常告警機制

### A10 - Server-Side Request Forgery (SSRF)
- [ ] 外部 URL 請求是否驗證目標白名單
- [ ] 是否阻擋對內部網路（169.254.x.x、10.x.x.x）的請求
- [ ] URL 重導向是否限制目標範圍
- [ ] 回應內容是否洩漏內部服務資訊

## 報告格式

```markdown
## 安全審查報告

**掃描日期**：{date}
**掃描範圍**：{scope}

| # | OWASP 類別 | 風險等級 | 檔案 | 行號 | 說明 | 修正建議 |
|---|-----------|---------|------|------|------|---------|
```
