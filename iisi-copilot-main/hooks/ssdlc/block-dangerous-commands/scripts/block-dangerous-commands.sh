#!/usr/bin/env bash
# block-dangerous-commands.sh
# PreToolUse Hook：阻擋危險命令模式
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

# 危險命令模式（可自訂擴充）
DANGEROUS_PATTERNS=(
  "rm -rf /"
  "rm -rf ~"
  "rm -rf \.\."
  "DROP TABLE"
  "DROP DATABASE"
  "TRUNCATE TABLE"
  "DELETE FROM .* WHERE 1"
  "git push --force"
  "git push -f "
  "git reset --hard"
  "chmod 777"
  "chmod -R 777"
  "curl.*\| *bash"
  "curl.*\| *sh"
  "wget.*\| *bash"
  "wget.*\| *sh"
  "> /dev/sda"
  "mkfs\."
  "dd if=.* of=/dev/"
  ":(){ :|:& };:"
  "shutdown"
  "reboot"
  "init 0"
  "init 6"
)

for pattern in "${DANGEROUS_PATTERNS[@]}"; do
  if echo "$COMMAND" | grep -iqE "$pattern"; then
    cat <<EOF
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "安全政策阻擋：偵測到危險命令模式 '${pattern}'。此操作已被自動阻止。"
  }
}
EOF
    exit 0
  fi
done

echo '{}'
