# 專案 Copilot Instructions

## 專案概述

個人圖書管理系統（Personal Book Manager），提供個人藏書管理與閱讀記錄。
模組：book（書本管理，含 Tag 與分類）、reading（閱讀記錄與日曆）。
技術棧：Java 21 + Spring Boot 3.3 + Spring Data JPA + H2(開發)/PostgreSQL(正式)，Maven 建置。

## 程式碼規範

- 分層架構：Controller → Service → Repository
- 命名：類別 PascalCase、方法/變數 camelCase、常數 UPPER_SNAKE_CASE
- 所有 public 方法需 JavaDoc
- 使用 Bean Validation 驗證輸入；自訂 Exception 處理業務例外
- 日誌用 SLF4J，禁止輸出個資/密碼/Token

## 模組邊界（重要）

- book 模組對外只暴露介面 `BookQueryPort.existsBook(bookId): boolean`
- reading 記錄新增前必須呼叫該介面確認書本存在
- 兩模組各自獨立資料夾，禁止互相直接存取對方 Repository

## 安全規範

- 所有外部輸入必須驗證；JPA 參數化查詢避免注入
- 書本 ISBN 去重檢查，避免使用者重複購買實體書
- 錯誤訊息不可洩漏堆疊細節給前端

## 測試規範

- JUnit 5 + Mockito + AssertJ，覆蓋率 ≥ 80%
- 測試命名：should_預期行為_When_條件
- reading 必含閱讀時長計算、日期邊界測試

## 工作流程

- 遵循 spec-driven-workflow：維護 requirements.md / design.md / tasks.md 三件工件
- 所有變更走 feature 分支 → PR → 審查 → develop → main
