#!/usr/bin/env bash
# inject-project-context.sh
# SessionStart Hook：注入專案上下文資訊供 Agent 參考
set -euo pipefail

BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
LAST_COMMIT=$(git log -1 --format="%h %s" 2>/dev/null || echo "unknown")
NODE_VERSION=$(node --version 2>/dev/null || echo "not installed")
JAVA_VERSION=$(java -version 2>&1 | head -1 || echo "not installed")

cat <<EOF
{
  "systemMessage": "專案上下文：分支=${BRANCH}，最後 commit=${LAST_COMMIT}，Node=${NODE_VERSION}，Java=${JAVA_VERSION}"
}
EOF
