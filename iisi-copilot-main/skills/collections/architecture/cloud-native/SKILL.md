---
name: "cloud-native"
description: '將應用設計為適合容器化、可水平擴展、可在 Kubernetes 上穩定運行的服務時使用'
skill: cloud-native
version: 1.0
tags: [cloud-native, kubernetes, twelve-factor]
applicable-to: [Kubernetes, Spring Boot]
last-updated: 2026-07-20
---

# Cloud Native 技能

## 適用情境
將應用設計為適合容器化、可水平擴展、可在 Kubernetes 上穩定運行的服務時使用。

## 核心原則
1. 遵循 Twelve-Factor App：設定外部化（環境變數）、無狀態、Log 輸出到 stdout
2. 提供標準的 Liveness/Readiness Probe，讓編排系統知道服務健康狀態
3. 優雅關閉（Graceful Shutdown）：收到 SIGTERM 後完成進行中的請求再結束
4. 資源請求與限制（requests/limits）必須明確設定，避免資源搶佔

## 標準做法

### Health Probe 設定
```yaml
livenessProbe:
  httpGet:
    path: /actuator/health/liveness
    port: 8080
  initialDelaySeconds: 20
readinessProbe:
  httpGet:
    path: /actuator/health/readiness
    port: 8080
  periodSeconds: 5
```

### 優雅關閉設定
```yaml
# application.yml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

## 禁止事項
- 不得把設定值寫死在程式碼或 Docker Image 中（應使用 ConfigMap/Secret）
- 不得省略 resources.requests/limits，導致單一 Pod 可能耗盡節點資源
- 不得將任何本地狀態（Session、暫存檔）存在容器內部而不外部化

## 驗收標準
- [ ] Liveness/Readiness Probe 皆已設定且能正確反映服務狀態
- [ ] 設定全部外部化，Image 可在任何環境重複部署
- [ ] 收到 SIGTERM 時可在時限內完成進行中請求後才結束

## 參考資料
- The Twelve-Factor App（https://12factor.net）
