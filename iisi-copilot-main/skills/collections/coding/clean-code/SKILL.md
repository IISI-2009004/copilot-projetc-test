---
name: "clean-code"
description: '撰寫新程式碼或審查既有程式碼的可讀性與可維護性，適用於任何語言'
skill: clean-code
version: 1.0
tags: [clean-code, readability, maintainability]
applicable-to: [Any Language]
last-updated: 2026-07-20
---

# Clean Code 技能

## 適用情境
撰寫新程式碼或審查既有程式碼的可讀性與可維護性，適用於任何語言。

## 核心原則
1. 命名即文件：變數/函式名稱應清楚表達意圖，不需額外註解
2. 函式只做一件事（Single Responsibility），圈複雜度盡量 < 10
3. 避免超過 3 層的巢狀 if/for，改用早期返回（Early Return）或抽取函式
4. 註解只寫「為什麼」，不寫「做什麼」

## 標準做法

### 早期返回取代巢狀判斷
```java
// Before
public BigDecimal calculateDiscount(Order order) {
    if (order != null) {
        if (order.getCustomer().isVip()) {
            if (order.getTotal().compareTo(THRESHOLD) > 0) {
                return order.getTotal().multiply(VIP_RATE);
            }
        }
    }
    return BigDecimal.ZERO;
}

// After
public BigDecimal calculateDiscount(Order order) {
    if (order == null || !order.getCustomer().isVip()) {
        return BigDecimal.ZERO;
    }
    if (order.getTotal().compareTo(THRESHOLD) <= 0) {
        return BigDecimal.ZERO;
    }
    return order.getTotal().multiply(VIP_RATE);
}
```

## 禁止事項
- 不得使用單字母變數名（迴圈索引 `i`/`j` 除外）
- 不得留下被註解掉的死程式碼
- 不得寫超過 50 行的單一函式（特殊情境需在 PR 說明理由）

## 驗收標準
- [ ] 圈複雜度（Cyclomatic Complexity）< 10
- [ ] 無被註解掉的死程式碼
- [ ] 命名通過「不看實作也能猜到用途」的檢驗

## 參考資料
- Robert C. Martin《Clean Code: A Handbook of Agile Software Craftsmanship》
