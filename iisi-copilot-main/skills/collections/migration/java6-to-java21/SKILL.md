---
name: "java6-to-java21"
description: "將執行於 Java 6/7/8 的遺留系統升級至 Java 21 LTS"
skill: java6-to-java21
version: 1.0
tags: [java-migration, java21, legacy]
applicable-to: [Java]
last-updated: 2026-07-20
---

# Java 版本升級技能（Java 6 → Java 21）

## 適用情境

將執行於 Java 6/7/8 的遺留系統升級至 Java 21 LTS。

## 核心原則

1. 分階段升級（6→8→11→17→21），不跨過多個 LTS 版本一次到位
2. 每個階段升級後皆需完整跑過既有測試套件，確認行為未變
3. 優先替換已被移除或棄用的 API（如 `sun.misc.*`），而非等到編譯失敗才處理
4. 升級後才評估是否導入新語法（Record、Pattern Matching、Virtual Threads），不與版本升級同一批次混雜風險

## 標準做法

### 分階段升級路線圖

```text
Phase 1：Java 6 → Java 8   （Lambda、Stream 可選用，非強制重寫既有程式碼）
Phase 2：Java 8 → Java 11  （移除 Java EE 模組依賴，改用獨立套件）
Phase 3：Java 11 → Java 17 （處理 Strong Encapsulation 對反射的影響）
Phase 4：Java 17 → Java 21 （評估導入 Virtual Threads 於高並發服務）
```

### 常見阻塞點：反射存取內部 API

```java
// Java 8 可行，Java 17+ 因 Strong Encapsulation 需加開放模組
// 需在 module-info.java 或啟動參數加入：
// --add-opens java.base/java.lang=ALL-UNNAMED
```

## 禁止事項

- 不得一次直接從 Java 6 跳到 Java 21（風險無法分階段驗證）
- 不得在升級的同一個 PR 中混雜大量新語法重寫（無法區分「升級破壞」與「重構破壞」）
- 不得忽略第三方函式庫的相容性（部分舊函式庫可能不支援新版 JVM）

## 驗收標準

- [ ] 每個升級階段後既有測試套件 100% 通過
- [ ] 第三方相依套件皆已確認支援目標 Java 版本
- [ ] 升級與語法現代化（Record/Pattern Matching）為分開的 PR

## 參考資料

- Oracle Java SE 支援路線圖（JDK 8/11/17/21 LTS）
