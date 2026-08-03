---
name: "threat-modeling"
description: "新系統設計階段或既有系統重大變更前，系統性識別潛在威脅與對應防護措施"
skill: threat-modeling
version: 1.0
tags: [threat-modeling, stride, security-design]
applicable-to: [System Design]
last-updated: 2026-07-20
---

# 威脅建模技能

## 適用情境

新系統設計階段或既有系統重大變更前，系統性識別潛在威脅與對應防護措施。

## 核心原則

1. 使用 STRIDE 模型逐一檢視：Spoofing、Tampering、Repudiation、Information Disclosure、Denial of Service、Elevation of Privilege
2. 威脅建模應在設計階段進行，而非上線後才補做（成本差異可達數十倍）
3. 每個識別出的威脅都需要對應的緩解措施與負責人
4. 資料流圖（DFD）是威脅建模的基礎，需先畫出信任邊界（Trust Boundary）

## 標準做法

### STRIDE 分析表

```text
| 威脅類別 | 範例情境 | 緩解措施 |
|---|---|---|
| Spoofing | 偽造使用者身分呼叫轉帳 API | mTLS + JWT 簽章驗證 |
| Tampering | 竄改傳輸中的交易金額 | HTTPS + 訊息簽章 |
| Repudiation | 使用者事後否認曾發起轉帳 | 不可竄改的稽核日誌 + 電子簽章 |
| Information Disclosure | 錯誤訊息洩漏內部堆疊 | 統一錯誤格式，隱藏內部細節 |
| Denial of Service | 高頻請求耗盡連線池 | Rate Limiting + 熔斷器 |
| Elevation of Privilege | 一般使用者呼叫管理端點 | 伺服端角色驗證（RBAC） |
```

## 禁止事項

- 不得在系統上線後才第一次進行威脅建模
- 不得只列出威脅而無對應的緩解措施與驗收方式
- 不得忽略信任邊界內部（如內部服務間）的威脅，假設內網一定安全

## 驗收標準

- [ ] 資料流圖（DFD）已標示所有信任邊界
- [ ] STRIDE 六大類別皆有對應分析，無跳過類別
- [ ] 每個識別出的威脅皆有負責人與緩解措施驗收紀錄

## 參考資料

- Microsoft STRIDE 威脅建模方法論
