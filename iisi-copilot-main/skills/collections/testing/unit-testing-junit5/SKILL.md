---
name: "unit-testing-junit5"
description: '為新開發或既有的 Java 類別補充單元測試'
skill: unit-testing-junit5
version: 1.0
tags: [junit5, mockito, unit-testing]
applicable-to: [Java, JUnit 5, Mockito]
last-updated: 2026-07-20
---

# JUnit 5 單元測試技能

## 適用情境
為新開發或既有的 Java 類別補充單元測試。

## 核心原則
1. 測試命名描述行為，不描述實作（`should_throw_when_balance_insufficient`）
2. 一個測試只驗證一件事，Assertion 聚焦在單一行為
3. 使用 Mockito 隔離外部依賴，但不 Mock 被測物件本身
4. 涵蓋 Happy Path、邊界值、例外情境三類案例

## 標準做法

### 標準測試結構（Given-When-Then）
```java
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock AccountRepository repository;
    @InjectMocks AccountService service;

    @Test
    @DisplayName("餘額不足時應拋出 InsufficientBalanceException")
    void should_throw_when_balance_insufficient() {
        // Given
        Account account = new Account("A1", Money.of(100));
        given(repository.findById("A1")).willReturn(Optional.of(account));

        // When / Then
        assertThatThrownBy(() -> service.withdraw("A1", Money.of(200)))
            .isInstanceOf(InsufficientBalanceException.class);
    }
}
```

### 參數化測試減少重複
```java
@ParameterizedTest
@CsvSource({"0, false", "-1, false", "100, true"})
void should_validate_amount(int amount, boolean expectedValid) {
    assertThat(AmountValidator.isValid(amount)).isEqualTo(expectedValid);
}
```

## 禁止事項
- 不得 Mock 靜態方法（代表設計問題，應先重構為可注入的依賴）
- 不得讓測試依賴執行順序（每個測試需獨立可重複執行）
- 不得只測 Happy Path 而忽略邊界與例外情境

## 驗收標準
- [ ] 行覆蓋率 ≥ 80%、重要業務邏輯覆蓋率 100%
- [ ] 每個測試方法名稱可讀出測試意圖，不需看內容
- [ ] 測試可獨立執行、可重複執行，無殘留狀態互相影響

## 參考資料
- JUnit 5 使用手冊（User Guide）
- 本手冊 14.3 節 Prompt-007（JUnit 5 測試產生 Prompt）
