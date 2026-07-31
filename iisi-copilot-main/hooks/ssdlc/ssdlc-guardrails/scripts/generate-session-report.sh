#!/usr/bin/env bash
# generate-session-report.sh
# Stop Hook：Agent 會話結束時產生變更摘要報告
set -euo pipefail

INPUT=$(cat)
STOP_HOOK_ACTIVE=$(echo "$INPUT" | jq -r '.stop_hook_active // false')

# 防止無限迴圈：若 Stop Hook 已在執行中則跳過
if [ "$STOP_HOOK_ACTIVE" = "true" ]; then
  echo '{}'
  exit 0
fi

SESSION_ID=$(echo "$INPUT" | jq -r '.sessionId // "unknown"')
TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%SZ")

# 收集 Git 變更摘要
CHANGED_FILES=$(git diff --name-only 2>/dev/null | head -20 || echo "無法取得變更清單")
STAGED_FILES=$(git diff --cached --name-only 2>/dev/null | head -20 || echo "無已暫存檔案")
DIFF_STAT=$(git diff --stat 2>/dev/null | tail -1 || echo "無變更統計")

REPORT_DIR=".copilot/reports"
mkdir -p "$REPORT_DIR"

REPORT_FILE="${REPORT_DIR}/session-${SESSION_ID}-$(date +%Y%m%d%H%M%S).md"

cat > "$REPORT_FILE" <<EOF
# Agent 會話報告

- **會話 ID**：${SESSION_ID}
- **時間**：${TIMESTAMP}

## 變更摘要

### 變更統計
\`\`\`
${DIFF_STAT}
\`\`\`

### 已修改檔案
\`\`\`
${CHANGED_FILES}
\`\`\`

### 已暫存檔案
\`\`\`
${STAGED_FILES}
\`\`\`
EOF

cat <<EOF
{
  "systemMessage": "會話報告已產生：${REPORT_FILE}"
}
EOF
