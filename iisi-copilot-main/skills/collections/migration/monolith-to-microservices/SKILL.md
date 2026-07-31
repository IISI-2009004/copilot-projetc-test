---
name: "monolith-to-microservices"
description: '評估單體應用是否該拆分為微服務，並規劃漸進式拆分路線圖時使用'
skill: monolith-to-microservices
version: 1.0
tags: [monolith, microservices, strangler-fig]
applicable-to: [Java, Microservices]
last-updated: 2026-07-20
---

# 單體到微服務技能

## 適用情境
評估單體應用是否該拆分為微服務，並規劃漸進式拆分路線圖時使用（與 microservices 技能搭配使用）。

## 核心原則
1. 先確認拆分動機（團隊擴張、獨立部署需求、局部擴展需求），而非為拆而拆
2. 使用 Strangler Fig 模式：先拆最獨立、變動最頻繁的模組作為第一個微服務
3. 拆分前先在單體內部劃清 Bounded Context 邊界（模組化單體），驗證邊界正確後再實體拆分
4. 每個拆分階段都需定義明確的回退（Rollback）計畫

## 標準做法

### 拆分優先順序評估矩陣
```text
| 模組 | 變動頻率 | 與其他模組耦合度 | 獨立擴展需求 | 拆分優先級 |
|---|---|---|---|---|
| 通知服務 | 高 | 低 | 低 | 高（先拆） |
| 帳務核心 | 低 | 高 | 中 | 低（最後拆，需最謹慎） |
```

### 拆分前先模組化單體（驗證邊界）
```java
// 先用套件（package）邊界模擬未來的服務邊界，並禁止跨邊界直接呼叫內部類別
// com.bank.notification（對外只暴露 NotificationFacade）
// com.bank.account（對外只暴露 AccountFacade）
```

## 禁止事項
- 不得在沒有驗證 Bounded Context 邊界前就直接進行實體拆分（邊界錯誤會造成頻繁跨服務呼叫）
- 不得一次拆分多個高耦合模組（應從低耦合、高變動頻率的模組開始）
- 不得拆分後才發現沒有回退計畫，導致問題只能硬著頭皮往前修

## 驗收標準
- [ ] 已完成拆分優先順序評估矩陣，優先拆分模組有明確理由
- [ ] 拆分前已在模組化單體內驗證邊界（跨邊界呼叫僅透過 Facade）
- [ ] 每個拆分階段皆有明確的回退計畫與判斷指標

## 參考資料
- 本手冊第九章 Legacy System 逆向工程
- `skills/collections/architecture/microservices` 微服務架構技能
