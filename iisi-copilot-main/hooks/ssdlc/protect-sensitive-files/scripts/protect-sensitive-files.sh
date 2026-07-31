#!/usr/bin/env bash
# protect-sensitive-files.sh
# PreToolUse Hook：保護敏感檔案，修改時要求確認或阻擋
set -euo pipefail

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
TOOL_INPUT=$(echo "$INPUT" | jq -r '.tool_input // empty')

# 僅檢查檔案操作工具
case "$TOOL_NAME" in
  create_file|replace_string_in_file|multi_replace_string_in_file|editFiles|Edit|Write|delete_file|run_in_terminal)
    ;;
  *)
    echo '{}'
    exit 0
    ;;
esac

# 取得操作的檔案路徑
FILE_PATH=$(echo "$TOOL_INPUT" | jq -r '.filePath // .file_path // empty')

# 若為 terminal 工具，從命令中提取目標檔案（僅處理 rm 命令）
if [ -z "$FILE_PATH" ] && [ "$TOOL_NAME" = "run_in_terminal" ]; then
  COMMAND=$(echo "$TOOL_INPUT" | jq -r '.command // empty')
  if echo "$COMMAND" | grep -qE "^rm "; then
    FILE_PATH=$(echo "$COMMAND" | grep -oE '[^ ]+$')
  fi
fi

if [ -z "$FILE_PATH" ]; then
  echo '{}'
  exit 0
fi

# 正規化路徑（取最後的相對路徑部分）
RELATIVE_PATH=$(echo "$FILE_PATH" | sed 's|.*[/\\]||; s|^\.\/||')
FULL_PATH="$FILE_PATH"

# 阻擋（deny）：這些檔案不允許 Agent 自動修改
DENY_PATTERNS=(
  "\.github/workflows/"
  "\.github/hooks/"
  "\.github/CODEOWNERS"
  "scripts/.*\.sh$"
  "scripts/.*\.ps1$"
)

for pattern in "${DENY_PATTERNS[@]}"; do
  if echo "$FULL_PATH" | grep -qE "$pattern"; then
    cat <<EOF
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "安全政策阻擋：'${FULL_PATH}' 屬於受保護的基礎設施檔案，不允許 Agent 自動修改。請由開發者手動編輯。"
  }
}
EOF
    exit 0
  fi
done

# 要求確認（ask）：這些檔案需要人工確認
ASK_PATTERNS=(
  "\.env$"
  "\.env\."
  "Dockerfile"
  "docker-compose"
  "\.tf$"
  "\.tfvars$"
  "package-lock\.json$"
  "yarn\.lock$"
  "pom\.xml$"
  "build\.gradle"
  "security"
  "auth.*config"
  "\.key$"
  "\.pem$"
  "\.cert$"
)

for pattern in "${ASK_PATTERNS[@]}"; do
  if echo "$FULL_PATH" | grep -iqE "$pattern"; then
    # 排除 .env.example
    if echo "$FULL_PATH" | grep -qE "\.example$"; then
      echo '{}'
      exit 0
    fi
    cat <<EOF
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "ask",
    "permissionDecisionReason": "此操作將修改敏感檔案 '${FULL_PATH}'。請確認是否允許 Agent 進行此變更。"
  }
}
EOF
    exit 0
  fi
done

echo '{}'
