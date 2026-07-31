#!/usr/bin/env bash
# auto-format.sh
# PostToolUse Hook：檔案編輯後自動執行 Prettier 格式化
set -euo pipefail

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.filePath // .tool_input.file_path // empty')

# 僅在檔案編輯工具觸發時執行
case "$TOOL_NAME" in
  create_file|replace_string_in_file|multi_replace_string_in_file|editFiles|Edit|Write)
    ;;
  *)
    echo '{}'
    exit 0
    ;;
esac

if [ -z "$FILE_PATH" ] || [ ! -f "$FILE_PATH" ]; then
  echo '{}'
  exit 0
fi

EXTENSION="${FILE_PATH##*.}"

# 僅格式化 Prettier 支援的檔案類型
case "$EXTENSION" in
  js|jsx|ts|tsx|json|css|scss|less|html|md|yaml|yml|graphql)
    if command -v npx &>/dev/null; then
      npx prettier --write "$FILE_PATH" 2>/dev/null || true
    fi
    ;;
esac

echo '{}'
