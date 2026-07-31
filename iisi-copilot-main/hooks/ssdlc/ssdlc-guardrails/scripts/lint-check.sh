#!/usr/bin/env bash
# lint-check.sh
# PostToolUse Hook：檔案編輯後執行 Lint 檢查
set -euo pipefail

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.filePath // .tool_input.file_path // empty')

# 僅在檔案編輯工具觸發時執行
case "$TOOL_NAME" in
  create_file|replace_string_in_file|editFiles|Edit|Write)
    ;;
  *)
    echo '{}'
    exit 0
    ;;
esac

if [ -z "$FILE_PATH" ]; then
  echo '{}'
  exit 0
fi

EXTENSION="${FILE_PATH##*.}"

case "$EXTENSION" in
  js|jsx|ts|tsx)
    if command -v npx &>/dev/null && [ -f "node_modules/.bin/eslint" ]; then
      npx eslint --no-error-on-unmatched-pattern "$FILE_PATH" 2>&1 || true
    fi
    ;;
  py)
    if command -v ruff &>/dev/null; then
      ruff check "$FILE_PATH" 2>&1 || true
    elif command -v flake8 &>/dev/null; then
      flake8 "$FILE_PATH" 2>&1 || true
    fi
    ;;
  java)
    if [ -f "pom.xml" ] && command -v mvn &>/dev/null; then
      mvn checkstyle:check -q 2>&1 || true
    fi
    ;;
esac

echo '{}'
