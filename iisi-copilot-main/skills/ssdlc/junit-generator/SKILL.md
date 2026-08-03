---
name: "junit-generator"
description: "產生 JUnit 5 單元測試，遵循 AAA 模式與團隊命名規範"
---

# JUnit Test Generator Skill

## 能力

此 Skill 可以：

1. 分析 Java 類別結構
2. 為每個 public 方法產生測試
3. 產生邊界值與異常路徑測試
4. 使用 Mockito Mock 外部依賴

## 測試範本

使用 `templates/test-template.java` 作為基礎範本。

## 產生規則

1. **測試類別命名**：`{被測類別}Test`
2. **測試方法命名**：`should_{預期行為}_When_{條件}`
3. **每個方法至少 3 個測試**：
   - 正常路徑（Happy Path）
   - 邊界值（Boundary）
   - 異常路徑（Error Path）
4. **使用 @DisplayName** 提供可讀的測試描述
5. **使用 AssertJ** 進行斷言

## 覆蓋率目標

- 行覆蓋率 ≥ 80%
- 分支覆蓋率 ≥ 70%
