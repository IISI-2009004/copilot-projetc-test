---
name: "struts-to-spring-boot"
description: '將 Struts 1/2 的 Action/Form 架構遷移至 Spring Boot Controller/Service 架構'
skill: struts-to-spring-boot
version: 1.0
tags: [struts, spring-boot, migration]
applicable-to: [Struts, Spring Boot]
last-updated: 2026-07-20
---

# Struts 遷移技能（Struts → Spring Boot）

## 適用情境
將 Struts 1/2 的 Action/Form 架構遷移至 Spring Boot Controller/Service 架構。

## 核心原則
1. 使用 Strangler Fig 模式漸進遷移：新舊系統並存，逐路由切換，而非一次性重寫
2. Struts Action 中混雜的商業邏輯，遷移時同步拆分到 Service 層（見 hexagonal-architecture 技能）
3. ActionForm 對應為 Spring 的 DTO/Record，並補上 Bean Validation
4. 每遷移一個模組即建立對應測試，確保行為與舊系統一致（Characterization Test）

## 標準做法

### Strangler Fig：反向代理依路由分流
```nginx
location /api/v2/ {
    proxy_pass http://spring-boot-service;
}
location / {
    proxy_pass http://legacy-struts-service;
}
```

### Struts Action → Spring Controller 對應
```java
// Struts Action（舊）：doGet 中混雜驗證、查詢、格式轉換
// 遷移後：Controller 只做協定轉換，邏輯移至 Service
@GetMapping("/api/v2/accounts/{id}")
public AccountDto getAccount(@PathVariable String id) {
    return accountService.findById(id); // 對應舊 Action 中的查詢邏輯
}
```

## 禁止事項
- 不得一次性切斷所有舊路由（應以 Strangler Fig 漸進切換，保留回退能力）
- 不得直接複製 Struts Action 中的商業邏輯到 Controller（違反六角形架構原則）
- 不得在無 Characterization Test 保護的情況下遷移含複雜業務規則的模組

## 驗收標準
- [ ] 每個遷移模組皆有遷移前後行為一致的 Characterization Test
- [ ] 新舊系統可並存運行，可依路由灰度切換與回退
- [ ] 商業邏輯已從 Action 層搬移至獨立 Service 層

## 參考資料
- Martin Fowler — Strangler Fig Application
