---
name: "owasp-top10"
description: "程式碼審查或安全掃描時，逐項比對 OWASP Top 10（2021）常見弱點類別"
skill: owasp-top10
version: 1.0
tags: [owasp, security, web-security]
applicable-to: [Web Application]
last-updated: 2026-07-20
---

# OWASP Top 10 技能

## 適用情境

程式碼審查或安全掃描時，逐項比對 OWASP Top 10（2021）常見弱點類別。

## 核心原則

1. 每次審查逐項檢查：注入、失效的存取控制、加密機制失效等十大類別
2. 修復需針對根因（如使用參數化查詢），而非表面遮蔽（如過濾特定關鍵字）
3. 高風險類別（注入、存取控制）優先於低風險類別（如組態設定問題）處理
4. 每個發現需標註對應的 OWASP 類別編號，便於追蹤與教育訓練

## 標準做法

### A03 注入攻擊防護（參數化查詢）

```java
// 錯誤：字串拼接易被 SQL Injection
String sql = "SELECT * FROM accounts WHERE id = '" + accountId + "'";

// 正確：使用參數化查詢
@Query("SELECT a FROM Account a WHERE a.id = :accountId")
Optional<Account> findById(@Param("accountId") String accountId);
```

### A01 失效的存取控制檢查清單

```text
- 是否每個端點都驗證了「這個使用者能不能操作這筆資料」，而非只驗證「有沒有登入」？
- 是否所有敏感端點都在伺服端二次驗證權限，而非僅依賴前端隱藏按鈕？
```

## 禁止事項

- 不得用黑名單過濾（Blacklist）取代參數化查詢等根本性防護
- 不得僅在前端做權限控制而伺服端不驗證
- 不得將加密金鑰、密碼等機密資訊寫死在原始碼中（見 secrets-management 技能）

## 驗收標準

- [ ] SAST 掃描（見 sast-analysis 技能）無 Critical/High 等級的 OWASP 對應弱點
- [ ] 所有資料庫查詢皆使用參數化查詢或 ORM
- [ ] 所有敏感端點皆有伺服端授權驗證

## 參考資料

- OWASP Top 10 (2021)（https://owasp.org/Top10/）
