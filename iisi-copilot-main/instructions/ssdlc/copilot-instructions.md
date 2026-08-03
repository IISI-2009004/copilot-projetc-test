# 專案 Copilot Instructions

## 專案概述

本專案為企業級電商平台後端服務，使用 Java 21 + Spring Boot 3.3 技術棧。

## 程式碼規範

### 語言與框架

- Java 21（使用 Record、Pattern Matching、Virtual Thread 等新特性）
- Spring Boot 3.3.x
- Maven 建置

### 命名慣例

- 類別名稱：PascalCase（`UserService`）
- 方法與變數：camelCase（`findByEmail`）
- 常數：UPPER_SNAKE_CASE（`MAX_RETRY_COUNT`）
- 套件名稱：全小寫（`com.company.project.service`）

### 架構規範

- 分層架構：Controller → Service → Repository
- Controller 只做參數驗證與回應組裝
- Service 處理業務邏輯
- Repository 負責資料存取

### 日誌規範

- 使用 SLF4J + Log4j2
- ERROR：系統錯誤，需要立即處理
- WARN：可恢復的問題
- INFO：重要業務事件
- DEBUG：開發除錯資訊
- 禁止在日誌中輸出：密碼、Token、個資

### 安全規範

- 所有外部輸入必須驗證
- SQL 查詢使用參數化查詢
- 敏感資料傳輸使用 HTTPS
- 密碼儲存使用 BCrypt
- API 必須有認證與授權

### 測試規範

- 使用 JUnit 5 + Mockito + AssertJ
- 測試覆蓋率 ≥ 80%
- 測試命名：should_預期行為_When_條件

### 文件規範

- 所有 public 方法必須有 JavaDoc
- README 保持更新
- API 變更需更新 OpenAPI 規格
