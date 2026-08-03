---
name: "api-design"
description: "設計新的 REST API 端點，或審查既有 API 的一致性與向後相容性"
skill: api-design
version: 1.0
tags: [api-design, rest, openapi]
applicable-to: [REST API, OpenAPI]
last-updated: 2026-07-20
---

# RESTful API 設計技能

## 適用情境

設計新的 REST API 端點，或審查既有 API 的一致性與向後相容性。

## 核心原則

1. 資源用名詞複數（`/accounts`），動作用 HTTP 動詞表達，不用動詞當路徑
2. 狀態碼語意正確：200/201/204 成功、400/404/409 用戶端錯誤、500 伺服端錯誤
3. 分頁、排序、過濾使用一致的 Query 參數慣例（`page`, `size`, `sort`, `filter`）
4. 每個端點都要有對應的 OpenAPI 描述

## 標準做法

### 資源與版本命名

```
GET    /api/v1/accounts?page=0&size=20&sort=createdAt,desc
POST   /api/v1/accounts
GET    /api/v1/accounts/{id}
PATCH  /api/v1/accounts/{id}
DELETE /api/v1/accounts/{id}
POST   /api/v1/accounts/{id}/transfer   # 動作型端點才允許動詞
```

### 統一錯誤回應格式

```json
{
  "timestamp": "2026-07-20T10:00:00Z",
  "status": 409,
  "error": "INSUFFICIENT_BALANCE",
  "message": "帳戶餘額不足以完成此交易",
  "traceId": "a1b2c3d4"
}
```

## 禁止事項

- 不得在 URL 中放動詞（如 `/getAccountById`）
- 不得讓同一個資源在不同端點回傳不一致的欄位命名風格
- 不得省略版本號（`/v1/`），造成未來無法平滑升級

## 驗收標準

- [ ] 所有端點皆有 OpenAPI/Swagger 文件
- [ ] 錯誤回應格式全站一致
- [ ] 破壞性變更（Breaking Change）皆透過新版本號釋出，不直接修改既有端點

## 參考資料

- Microsoft REST API Guidelines
- OpenAPI 3.x 規範（https://spec.openapis.org/oas/latest.html）
