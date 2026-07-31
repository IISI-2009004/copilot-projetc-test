#!/usr/bin/env bash
# 通用腳本：設定目標分支所需的 PR approval 數量
#
# 情境：develop/main 分支保護規則要求至少 1 個 approval，但 repo 擁有者/
#       唯一開發者無法核准自己的 PR。合併前需暫時把 required_approving_review_count
#       降為 0，合併後再還原為 1，避免分支保護規則被永久弱化。
#
# 本腳本只負責「設定 approval 數量」這一件事，合併請改用 scripts/merge-pr.sh。
#
# 使用前需先於 shell 設定：GH_TOKEN, GH_OWNER, GH_REPO
#
# 用法：
#   bash scripts/set-branch-approvals.sh <approval數量> [目標分支，預設 develop]
#
# 範例：
#   bash scripts/set-branch-approvals.sh 0 develop   # 合併前，暫時降為 0
#   bash scripts/set-branch-approvals.sh 1 develop   # 合併後，還原為 1

: "${GH_TOKEN:?請先 export GH_TOKEN}"
: "${GH_OWNER:?請先 export GH_OWNER}"
: "${GH_REPO:?請先 export GH_REPO}"

COUNT="${1:?請提供 approval 數量，例如：bash scripts/set-branch-approvals.sh 0 develop}"
BRANCH="${2:-develop}"

API="https://api.github.com/repos/${GH_OWNER}/${GH_REPO}"

echo "== 設定分支 ${BRANCH} 所需 approval 數量為 ${COUNT} =="

response="$(curl -sS -X PUT \
  -H "Authorization: Bearer ${GH_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "${API}/branches/${BRANCH}/protection" \
  -d "{
    \"required_status_checks\": null,
    \"enforce_admins\": true,
    \"required_pull_request_reviews\": {
      \"required_approving_review_count\": ${COUNT},
      \"dismiss_stale_reviews\": true
    },
    \"restrictions\": null,
    \"allow_force_pushes\": false,
    \"allow_deletions\": false
  }")"

if echo "${response}" | grep -q '"message"'; then
  echo "  失敗：${response}"
  exit 1
fi

echo "  成功：分支 ${BRANCH} 所需 approval 數量已設為 ${COUNT}"
