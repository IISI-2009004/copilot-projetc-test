---
name: "database-optimization"
description: 'API 回應時間異常、資料庫 CPU 過高、或 JPA 出現 N+1 查詢問題時的分析與優化'
skill: database-optimization
version: 1.0
tags: [database, performance, jpa, sql]
applicable-to: [JPA, PostgreSQL, MySQL]
last-updated: 2026-07-20
---

# 資料庫優化技能

## 適用情境
API 回應時間異常、資料庫 CPU 過高、或 JPA 出現 N+1 查詢問題時的分析與優化。

## 核心原則
1. 先量測再優化：以 Slow Query Log／APM 找出真正的瓶頸，不憑感覺猜測
2. 索引服務於查詢模式，而非每個欄位都建索引
3. 讀寫分離：高頻讀取場景優先考慮快取，而非一味加索引
4. 避免過早引入 CQRS／讀寫分離等重量級方案，先確認負載真的需要

## 標準做法

### 解決 N+1 查詢
```java
// Before：每筆 Order 各自查詢一次 Customer（N+1）
List<Order> orders = orderRepository.findAll();

// After：使用 @EntityGraph 一次查完
@EntityGraph(attributePaths = {"customer"})
List<Order> findAllWithCustomer();
```

### 索引設計原則
```sql
-- 依實際查詢條件建立複合索引，欄位順序 = 等值查詢在前、範圍查詢在後
CREATE INDEX idx_transaction_account_created
    ON transactions (account_id, created_at DESC);
```

## 禁止事項
- 不得在沒有量測資料的情況下大量新增索引（索引也有寫入成本）
- 不得用 `SELECT *`，尤其在大表格上
- 不得對高基數（High Cardinality）以外的欄位建立單欄索引卻期待顯著效益

## 驗收標準
- [ ] 優化前後皆有效能量測數據對比
- [ ] N+1 查詢問題已透過 `@EntityGraph`/`JOIN FETCH`/`@BatchSize` 解決
- [ ] 新增索引已確認對應的查詢計畫（EXPLAIN）確實走索引

## 參考資料
- 本手冊 14.2 節 Prompt-006（資料庫查詢優化 Prompt）
