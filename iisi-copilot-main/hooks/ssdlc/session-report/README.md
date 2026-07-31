---
name: 'Session Report'
description: 'Stop Hook，在 Agent 會話結束時自動產生變更摘要報告，包含 Git diff 統計、修改檔案清單與會話 metadata，便於事後稽核與追蹤。'
tags:
  - ssdlc
  - audit
  - reporting
  - stop
---

# Session Report

Stop Hook，在 Agent 會話結束時自動產生結構化的變更摘要報告。

## 報告內容

| 區段 | 說明 |
|------|------|
| 會話 metadata | 會話 ID、時間戳記 |
| 變更統計 | `git diff --stat` 摘要 |
| 已修改檔案 | 未暫存的已變更檔案清單 |
| 已暫存檔案 | 已加入暫存區的檔案清單 |
| 新增檔案 | 未追蹤的新建檔案 |

## 報告位置

報告產生在 `.copilot/reports/` 目錄下，檔名格式為：
```
session-{sessionId}-{timestamp}.md
```

## 使用方式

```bash
cp hooks.json .github/hooks/session-report.json
cp scripts/session-report.sh ./scripts/
chmod +x ./scripts/session-report.sh
```

## 注意事項

- 報告腳本會檢查 `stop_hook_active` 旗標以防止無限迴圈
- 建議將 `.copilot/reports/` 加入 `.gitignore`
