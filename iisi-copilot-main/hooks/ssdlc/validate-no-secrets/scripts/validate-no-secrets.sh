#!/usr/bin/env bash
# validate-no-secrets.sh
# PreToolUse Hook：檢查 Agent 寫入的程式碼是否包含硬編碼敏感資訊
set -euo pipefail

INPUT=$(cat)
TOOL_NAME=$(echo "$INPUT" | jq -r '.tool_name // empty')
TOOL_INPUT=$(echo "$INPUT" | jq -r '.tool_input // empty')

# 僅檢查檔案建立或編輯工具
case "$TOOL_NAME" in
  create_file|replace_string_in_file|multi_replace_string_in_file|editFiles|Edit|Write)
    ;;
  *)
    echo '{}'
    exit 0
    ;;
esac

# 取得要寫入的內容
CONTENT=$(echo "$TOOL_INPUT" | jq -r '.content // .newString // .new_str // empty')
FILE_PATH=$(echo "$TOOL_INPUT" | jq -r '.filePath // .file_path // empty')

if [ -z "$CONTENT" ]; then
  echo '{}'
  exit 0
fi

# 排除已知安全的檔案路徑（如 .env.example、文件檔）
case "$FILE_PATH" in
  *.example|*.md|*.txt|*.rst|*README*|*CHANGELOG*|*LICENSE*)
    echo '{}'
    exit 0
    ;;
esac

# 敏感資訊模式（正則表達式）
SECRET_PATTERNS=(
  "AKIA[0-9A-Z]{16}"                          # AWS Access Key ID
  "BEGIN (RSA |EC |DSA |OPENSSH )?PRIVATE KEY"  # 私鑰
  "password\s*[:=]\s*['\"][^'\"]{4,}"           # 硬編碼密碼
  "passwd\s*[:=]\s*['\"][^'\"]{4,}"             # 硬編碼密碼
  "secret\s*[:=]\s*['\"][^'\"]{4,}"             # 硬編碼 secret
  "api[_-]?key\s*[:=]\s*['\"][^'\"]{8,}"        # API Key
  "token\s*[:=]\s*['\"][^'\"]{8,}"              # Token
  "bearer\s+[a-zA-Z0-9._-]{20,}"               # Bearer Token
  "ghp_[a-zA-Z0-9]{36}"                        # GitHub Personal Access Token
  "gho_[a-zA-Z0-9]{36}"                        # GitHub OAuth Token
  "sk-[a-zA-Z0-9]{32,}"                        # OpenAI API Key
  "mongodb\+srv://[^:]+:[^@]+@"                 # MongoDB 連線字串含密碼
  "jdbc:[^\"]*password=[^\"&]+"                 # JDBC 連線字串含密碼
)

for pattern in "${SECRET_PATTERNS[@]}"; do
  if echo "$CONTENT" | grep -iqE "$pattern"; then
    MATCHED=$(echo "$CONTENT" | grep -ioE "$pattern" | head -1 | cut -c1-20)
    cat <<EOF
{
  "hookSpecificOutput": {
    "hookEventName": "PreToolUse",
    "permissionDecision": "deny",
    "permissionDecisionReason": "安全政策阻擋：偵測到疑似硬編碼敏感資訊（匹配：${MATCHED}...）。請使用環境變數或 Secret Manager 替代。",
    "additionalContext": "請勿在程式碼中硬編碼密碼、API Key 或 Token。建議使用 process.env.API_KEY 或 System.getenv(\"API_KEY\") 等方式存取敏感資訊。"
  }
}
EOF
    exit 0
  fi
done

echo '{}'
