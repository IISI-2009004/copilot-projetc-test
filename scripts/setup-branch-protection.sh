#!/usr/bin/env bash
# 設定 main / develop 分支保護規則（透過 GitHub REST API，取代網頁手動操作）
# 使用前需先於 shell 設定：GH_TOKEN, GH_OWNER, GH_REPO
#
# 對應規則（與 3人團隊SSDLC協作文件 步驟0-7 一致）：
#   - 需至少 1 個 PR approval 才能合併
#   - 需 CI 狀態檢查通過（若尚無 CI workflow，先留空陣列，之後有了 workflow 再補上 check name）
#   - 禁止 force push
#   - 禁止刪除分支
#
# 用法：
#   export GH_TOKEN="ghp_xxx"
#   export GH_OWNER="your-org-or-username"
#   export GH_REPO="personal-book-manager"
#   bash scripts/setup-branch-protection.sh

set -euo pipefail

: "${GH_TOKEN:?請先 export GH_TOKEN}"
: "${GH_OWNER:?請先 export GH_OWNER}"
: "${GH_REPO:?請先 export GH_REPO}"

API="https://api.github.com/repos/${GH_OWNER}/${GH_REPO}"

protect_branch() {
  local branch="$1"
  echo "== 設定分支保護：${branch} =="

  curl -sS -X PUT \
    -H "Authorization: Bearer ${GH_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    "${API}/branches/${branch}/protection" \
    -d '{
      "required_status_checks": null,
      "enforce_admins": true,
      "required_pull_request_reviews": {
        "required_approving_review_count": 1,
        "dismiss_stale_reviews": true
      },
      "restrictions": null,
      "allow_force_pushes": false,
      "allow_deletions": false
    }' | grep -E '"url"|"message"' || true

  echo
}

protect_branch "main"
protect_branch "develop"

echo "完成。可用以下指令驗證："
echo "  curl -sS -H \"Authorization: Bearer \$GH_TOKEN\" ${API}/branches/main/protection | head -40"
