#!/usr/bin/env bash
# block-force-push.sh
# PreToolUse Hook：阻擋破壞性 Git 操作
set -euo pipefail

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
TOOL_INPUT=$(echo "$INPUT" | jq -r '.tool_input // empty')

# 僅檢查 Shell/Terminal 相關工具
case "$TOOL_NAME" in
  run_in_terminal|run_command|bash|shell|terminal)
    ;;
  *)
    echo '{}'
    exit 0
    ;;
esac

COMMAND=$(echo "$TOOL_INPUT" | jq -r '.command // .cmd // empty')

if [ -z "$COMMAND" ]; then
  echo '{}'
  exit 0
fi

# 破壞性 Git 操作模式
BLOCKED_PATTERNS=(
  "git push --force"
  "git push -f "
  "git push -f$"
  "git push [^ ]+ -f"
  "git push [^ ]+ --force"
  "git reset --hard"
  "git clean -fd"
  "git clean -fx"
  "git clean -fdx"
  "git checkout -- \."
  "git stash drop"
  "git branch -D"
)

for pattern in "${BLOCKED_PATTERNS[@]}"; do
  if echo "$COMMAND" | grep -iqE "$pattern"; then
    cat <<EOF
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "ask",
    "permissionDecisionReason": "偵測到破壞性 Git 操作：此命令可能導致不可逆的版本控制變更。請確認是否要繼續。",
    "additionalContext": "命令 '${COMMAND}' 包含破壞性 Git 操作。建議：使用 git push --force-with-lease 替代 --force，或先備份再執行。"
  }
}
EOF
    exit 0
  fi
done

echo '{}'
