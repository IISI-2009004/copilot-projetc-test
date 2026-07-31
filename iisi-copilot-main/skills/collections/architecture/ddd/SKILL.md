---
name: "ddd"
description: '商業邏輯複雜、規則多變的核心領域（例如帳務、清算、風控），需要建立貼近業務語言的領域模型時使用'
skill: ddd
version: 1.0
tags: [ddd, domain-model, ubiquitous-language]
applicable-to: [Java, Domain Model]
last-updated: 2026-07-20
---

# DDD 領域驅動設計技能

## 適用情境
商業邏輯複雜、規則多變的核心領域（例如帳務、清算、風控），需要建立貼近業務語言的領域模型時使用。

## 核心原則
1. 使用 Ubiquitous Language：程式碼中的類別/方法名稱必須與業務人員使用的詞彙一致
2. Aggregate 是交易一致性的邊界，一次交易只修改一個 Aggregate
3. 商業規則封裝在 Entity/Value Object 內，禁止外洩到 Service 層做「貧血模型」判斷
4. 使用 Domain Event 表達「已發生的業務事實」，供其他 Bounded Context 訂閱

## 標準做法

### Aggregate Root 封裝不變條件
```java
public class Account {
    private AccountId id;
    private Money balance;

    public void withdraw(Money amount) {
        if (balance.isLessThan(amount)) {
            throw new InsufficientBalanceException(id, amount);
        }
        this.balance = balance.subtract(amount);
        registerEvent(new MoneyWithdrawnEvent(id, amount));
    }
}
```

### Value Object 表達領域概念
```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        if (amount.scale() > 4) throw new IllegalArgumentException("金額精度過高");
    }
    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(amount.subtract(other.amount), currency);
    }
}
```

## 禁止事項
- 不得寫出「貧血模型」（Entity 只有 getter/setter，規則全部寫在 Service）
- 不得讓一次交易同時修改兩個以上的 Aggregate（應改用 Domain Event + 最終一致性）
- 不得讓 Value Object 有可變狀態（必須是 Immutable）

## 驗收標準
- [ ] 核心業務規則封裝於 Entity/Value Object，Service 只負責協調
- [ ] Aggregate 邊界清楚，跨 Aggregate 一致性透過 Domain Event 處理
- [ ] 程式碼命名與業務詞彙表（Ubiquitous Language）一致

## 參考資料
- Eric Evans《Domain-Driven Design: Tackling Complexity in the Heart of Software》
