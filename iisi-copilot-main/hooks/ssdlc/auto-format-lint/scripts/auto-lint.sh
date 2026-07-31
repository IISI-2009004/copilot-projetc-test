#!/usr/bin/env bash
# auto-lint.sh
# PostToolUse Hook：檔案編輯後自動執行 Lint 檢查
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
LINT_OUTPUT=""

case "$EXTENSION" in
  js|jsx|ts|tsx)
    if command -v npx &>/dev/null && [ -f "node_modules/.bin/eslint" ]; then
      LINT_OUTPUT=$(npx eslint --no-error-on-unmatched-pattern --format compact "$FILE_PATH" 2>&1 || true)
    fi
    ;;
  py)
    if command -v ruff &>/dev/null; then
      LINT_OUTPUT=$(ruff check --output-format concise "$FILE_PATH" 2>&1 || true)
    elif command -v flake8 &>/dev/null; then
      LINT_OUTPUT=$(flake8 "$FILE_PATH" 2>&1 || true)
    fi
    ;;
  java)
    if [ -f "pom.xml" ] && command -v mvn &>/dev/null; then
      LINT_OUTPUT=$(mvn checkstyle:check -q 2>&1 || true)
    fi
    ;;
esac

# 若有 Lint 問題，回傳警告訊息
if [ -n "$LINT_OUTPUT" ] && [ "$LINT_OUTPUT" != "0" ]; then
  # 截斷過長的輸出
  TRUNCATED=$(echo "$LINT_OUTPUT" | head -10)
  cat <<EOF
{
  "systemMessage": "Lint 檢查結果（${FILE_PATH}）：\n${TRUNCATED}"
}
EOF
else
  echo '{}'
fi
