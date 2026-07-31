---
applyTo: "src/**/*.{java,ts,tsx}"
---

# 安全程式碼規範

## 輸入驗證
- 所有外部輸入（HTTP 參數、Header、Body）必須驗證
- 使用白名單驗證，不使用黑名單
- 數值輸入檢查範圍
- 字串輸入檢查長度與格式

## 注入防護
- SQL：使用 JPA/Hibernate 參數化查詢，禁止字串拼接 SQL
- XSS：輸出時進行 HTML 編碼
- Command Injection：禁止直接執行使用者輸入的系統指令
- Path Traversal：驗證檔案路徑，禁止 ../ 

## 認證與授權
- 使用 Spring Security
- API 端點使用 @PreAuthorize 或 @Secured
- 實作最小權限原則

## 敏感資料
- 密碼使用 BCrypt 雜湊
- 敏感配置使用環境變數或 Vault
- 日誌不可包含密碼、Token、個資
- API 回應不可包含內部錯誤堆疊
