---
name: "design-patterns"
description: '面對重複的條件分支、物件建立邏輯複雜、或需要在不修改既有程式碼前提下擴充行為時，評估並套用適當的設計模式'
skill: design-patterns
version: 1.0
tags: [design-patterns, architecture, java]
applicable-to: [Java]
last-updated: 2026-07-20
---

# 設計模式應用技能

## 適用情境
面對重複的條件分支、物件建立邏輯複雜、或需要在不修改既有程式碼前提下擴充行為時，評估並套用適當的設計模式。

## 核心原則
1. 模式服務於問題，不為了炫技而套用模式（YAGNI）
2. 優先使用組合（Composition）而非繼承
3. 每次套用模式都必須在 PR 中說明「為何選這個模式、放棄了哪些替代方案」
4. 依賴抽象（Interface），不依賴具體實作

## 標準做法

### Strategy Pattern 取代龐大 switch
```java
public interface FeeCalculator {
    BigDecimal calculate(Transaction tx);
}

@Component
public class WireTransferFeeCalculator implements FeeCalculator {
    public BigDecimal calculate(Transaction tx) {
        return tx.getAmount().multiply(new BigDecimal("0.001"));
    }
}

@Service
@RequiredArgsConstructor
public class FeeService {
    private final Map<TransactionType, FeeCalculator> calculators;

    public BigDecimal calculateFee(Transaction tx) {
        return calculators.get(tx.getType()).calculate(tx);
    }
}
```

## 禁止事項
- 不得為單一情境（只有一種實作）預先套用 Strategy/Factory 等模式
- 不得用繼承達成程式碼重用而忽略里氏替換原則（LSP）
- 不得引入模式後反而增加理解成本卻無實際彈性收益

## 驗收標準
- [ ] PR 描述中說明選用理由與放棄的替代方案
- [ ] 新增分支邏輯時無需修改既有類別（符合開放封閉原則）
- [ ] 單元測試可針對每個策略/實作獨立測試

## 參考資料
- Gang of Four《Design Patterns: Elements of Reusable Object-Oriented Software》
