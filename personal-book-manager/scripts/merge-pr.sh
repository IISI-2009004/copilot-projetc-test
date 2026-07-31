#!/usr/bin/env bash
# 通用腳本：純粹合併指定 PR（不處理分支保護規則）
#
# 若目標分支有 approval 保護規則且尚未核准，請先執行：
#   bash scripts/set-branch-approvals.sh 0 <目標分支>
# 合併完成後，記得還原：
#   bash scripts/set-branch-approvals.sh 1 <目標分支>
#
# 使用前需先於 shell 設定：GH_TOKEN, GH_OWNER, GH_REPO
#
# 用法：
#   bash scripts/merge-pr.sh <PR編號> [合併方式，預設 merge]
#
# 範例：
#   bash scripts/merge-pr.sh 3
#   bash scripts/merge-pr.sh 3 squash

: "${GH_TOKEN:?請先 export GH_TOKEN}"
: "${GH_OWNER:?請先 export GH_OWNER}"
: "${GH_REPO:?請先 export GH_REPO}"

PR_NUMBER="${1:?請提供 PR 編號，例如：bash scripts/merge-pr.sh 3}"
MERGE_METHOD="${2:-merge}"

API="https://api.github.com/repos/${GH_OWNER}/${GH_REPO}"

echo "== 合併 PR #${PR_NUMBER}（方式：${MERGE_METHOD}） =="

response="$(curl -sS -X PUT \
  -H "Authorization: Bearer ${GH_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "${API}/pulls/${PR_NUMBER}/merge" \
  -d "{\"merge_method\": \"${MERGE_METHOD}\"}")"

if echo "${response}" | grep -q '"merged":true'; then
  echo "  成功：PR #${PR_NUMBER} 已合併"
  exit 0
fi

echo "  失敗：${response}"
exit 1
