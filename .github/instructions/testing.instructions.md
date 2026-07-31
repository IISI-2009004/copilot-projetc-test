---
applyTo: "src/test/**/*.java"
---

# 測試程式碼規範

## 框架
- JUnit 5 + Mockito + AssertJ

## 結構
- 使用 AAA 模式：Arrange → Act → Assert
- 每個測試方法只測試一個行為
- Mock 外部依賴（資料庫、API、訊息佇列）

## 命名
- 測試類別：{被測類別}Test（如 UserServiceTest）
- 測試方法：should_{預期行為}_When_{條件}
- 使用 @DisplayName 提供中文描述

## 安全測試
- 每個 Controller 測試必須包含未認證存取測試
- 測試無效輸入（SQL Injection payload、XSS payload）
