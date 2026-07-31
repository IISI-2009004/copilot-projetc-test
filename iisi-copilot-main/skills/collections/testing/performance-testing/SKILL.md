---
name: "performance-testing"
description: '上線前驗證 API 在預期流量下的延遲與吞吐量，或定位效能回歸問題時使用'
skill: performance-testing
version: 1.0
tags: [performance-testing, load-testing, k6]
applicable-to: [REST API, k6]
last-updated: 2026-07-20
---

# 效能測試技能

## 適用情境
上線前驗證 API 在預期流量下的延遲與吞吐量，或定位效能回歸問題時使用。

## 核心原則
1. 先定義明確的效能目標（P95 延遲、QPS），再設計測試腳本
2. 測試環境規格需與生產環境相近，結果才有參考價值
3. 區分負載測試（Load）、壓力測試（Stress）、尖峰測試（Spike）三種情境
4. 每次效能測試結果需與基準線（Baseline）比較，而非只看單次數字

## 標準做法

### k6 負載測試腳本
```javascript
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  stages: [
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'], // P95 < 300ms
  },
};

export default function () {
  const res = http.get('https://staging.example.com/api/v1/accounts/1');
  check(res, { 'status is 200': (r) => r.status === 200 });
}
```

## 禁止事項
- 不得在沒有預先定義效能目標的情況下就開始測試（無法判斷通過與否）
- 不得直接對生產環境進行壓力測試
- 不得忽略資料庫連線池、快取等下游資源在高壓下的行為

## 驗收標準
- [ ] P95/P99 延遲與目標 QPS 皆有明確門檻並自動判定通過/失敗
- [ ] 測試結果已與前次基準線比較，無明顯回歸
- [ ] 測試涵蓋負載、壓力、尖峰三種情境中與本次變更相關的至少一種

## 參考資料
- Grafana k6 官方文件（https://k6.io/docs）
