#!/usr/bin/env bash
# block-dangerous-commands.sh
# PreToolUse Hook：阻擋危險命令執行
set -euo pipefail

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
TOOL_INPUT=$(echo "$INPUT" | jq -r '.tool_input // empty')

# 僅檢查 Shell/Terminal 相關工具
case "$TOOL_NAME" in
  run_in_terminal|run_command|bash|shell|terminal)
    COMMAND=$(echo "$TOOL_INPUT" | jq -r '.command // .cmd // empty')

    # 危險命令模式清單
    DANGEROUS_PATTERNS=(
      "rm -rf /"
      "rm -rf ~"
      "rm -rf \.\."
      "DROP TABLE"
      "DROP DATABASE"
      "TRUNCATE TABLE"
      "git push --force"
      "git push -f"
      "git reset --hard"
      "chmod 777"
      "curl.*| bash"
      "wget.*| bash"
      "> /dev/sda"
      "mkfs\."
      ":(){ :|:& };:"
    )

    for pattern in "${DANGEROUS_PATTERNS[@]}"; do
      if echo "$COMMAND" | grep -iqE "$pattern"; then
        cat <<EOF
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "安全政策阻擋：偵測到危險命令模式 '${pattern}'"
  }
}
EOF
        exit 0
      fi
    done
    ;;
esac

# 未偵測到危險，允許執行
echo '{}'
