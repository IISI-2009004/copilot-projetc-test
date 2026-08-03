---
name: "jsp-to-vue3"
description: "將伺服端渲染的 JSP 頁面遷移至前後端分離的 Vue3 SPA 架構"
skill: jsp-to-vue3
version: 1.0
tags: [jsp, vue3, frontend-migration]
applicable-to: [JSP, Vue 3]
last-updated: 2026-07-20
---

# JSP 遷移技能（JSP → Vue3）

## 適用情境

將伺服端渲染的 JSP 頁面遷移至前後端分離的 Vue3 SPA 架構。

## 核心原則

1. 先盤點 JSP 頁面中混雜的邏輯：純顯示、表單驗證、商業規則，分別對應遷移策略
2. JSP 中內嵌的 Java Scriptlet 商業邏輯需先抽成後端 API，前端只負責顯示與互動
3. 依頁面重要度與變動頻率排序遷移優先級，而非依技術難度
4. 遷移期間新舊頁面共存，透過共用版型（Layout）維持一致的使用者體驗

## 標準做法

### JSP 內嵌邏輯 → 後端 API + 前端元件

```jsp
<%-- Before: JSP Scriptlet 內嵌商業邏輯 --%>
<% if (account.getBalance().compareTo(new BigDecimal(1000)) > 0) { %>
    <span>VIP 會員</span>
<% } %>
```

```vue
<!-- After: 邏輯移至後端 API，前端只做顯示 -->
<script setup lang="ts">
const { data: account } = await useAccountApi().getAccount(accountId);
</script>
<template>
  <span v-if="account.isVip">VIP 會員</span>
</template>
```

## 禁止事項

- 不得把 JSP 中的商業邏輯原封不動搬到前端 JavaScript（前端不應持有授權/計算等商業規則）
- 不得在遷移過程中讓同一功能同時存在兩套互相衝突的實作而未標明過渡狀態
- 不得忽略 SEO 或首屏效能需求就直接改為純 CSR（視情況評估 SSR/預渲染）

## 驗收標準

- [ ] JSP 中的商業邏輯已抽離至後端 API，前端不含授權或計算類商業規則
- [ ] 已遷移頁面與待遷移頁面共用一致的版型與導覽體驗
- [ ] 遷移後頁面之核心功能已通過 E2E 測試（見 e2e-testing-playwright 技能）

## 參考資料

- 本手冊第八章 Web Application 開發實戰
