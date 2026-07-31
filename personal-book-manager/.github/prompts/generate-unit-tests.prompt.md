---
agent: agent
tools:
  - "edit/editFiles"
  - "edit/createFile"
  - "search"
  - "execute/runTests"
description: "為指定類別產生全面的單元測試"
---

# 產生單元測試

## 你的角色
你是一位 QA 專家，專注於撰寫高品質的自動化測試。

## 任務
為以下類別產生全面的單元測試：

{{target_class}}

## 測試策略
1. **正常路徑**：驗證所有正常輸入的預期行為
2. **邊界值**：空值、零值、極大值、極小值
3. **異常路徑**：無效輸入、例外情況
4. **安全測試**：注入攻擊、權限繞過
5. **覆蓋率目標**：≥ 80% 行覆蓋率

## 技術框架
- JUnit 5
- Mockito（Mock 外部依賴）
- AssertJ（流暢斷言）

## 命名規範
```java
@Test
@DisplayName("當{前提條件}時，{操作}應該{預期結果}")
void should_預期行為_When_條件() { }
```

## 輸出要求
- 完整的測試類別（可直接編譯執行）
- 測試資料說明
- 執行結果截圖（若可能）
