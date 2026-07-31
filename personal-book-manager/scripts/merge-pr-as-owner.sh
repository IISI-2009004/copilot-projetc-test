#!/usr/bin/env bash
# 通用腳本：以 repo 擁有者/唯一開發者身分合併 PR
#
# 情境：develop 分支已設定「至少 1 個 approval」的保護規則，但 GitHub 不允許
#       PR 作者核准自己的 PR。此腳本會：
#         1. 暫時把目標分支的 required_approving_review_count 降為 0
#         2. 合併指定的 PR
#         3. 無論成功或失敗，都會把 required_approving_review_count 還原為 1
#       確保分支保護規則不會被永久弱化。
#
# 使用前需先於 shell 設定：GH_TOKEN, GH_OWNER, GH_REPO
#
# 用法：
#   bash scripts/merge-pr-as-owner.sh <PR編號> [目標分支，預設 develop] [合併方式，預設 merge]
#
# 範例：
#   bash scripts/merge-pr-as-owner.sh 2
#   bash scripts/merge-pr-as-owner.sh 2 develop squash
#
# 注意：本腳本執行完畢會正常結束並回到 shell 提示字元。

: "${GH_TOKEN:?請先 export GH_TOKEN}"
: "${GH_OWNER:?請先 export GH_OWNER}"
: "${GH_REPO:?請先 export GH_REPO}"

PR_NUMBER="${1:?請提供 PR 編號，例如：bash scripts/merge-pr-as-owner.sh 2}"
BRANCH="${2:-develop}"
MERGE_METHOD="${3:-merge}"

API="https://api.github.com/repos/${GH_OWNER}/${GH_REPO}"
EXIT_CODE=0

set_required_approvals() {
  local count="$1"
  echo "== 設定分支 ${BRANCH} 所需 approval 數量為 ${count} =="

  local response
  response="$(curl -sS -X PUT \
    -H "Authorization: Bearer ${GH_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    "${API}/branches/${BRANCH}/protection" \
    -d "{
      \"required_status_checks\": null,
      \"enforce_admins\": true,
      \"required_pull_request_reviews\": {
        \"required_approving_review_count\": ${count},
        \"dismiss_stale_reviews\": true
      },
      \"restrictions\": null,
      \"allow_force_pushes\": false,
      \"allow_deletions\": false
    }")"

  if echo "${response}" | grep -q '"message"'; then
    echo "  失敗：${response}"
    return 1
  fi
  echo "  成功"
  return 0
}

merge_pr() {
  echo "== 合併 PR #${PR_NUMBER}（方式：${MERGE_METHOD}） =="

  local response
  response="$(curl -sS -X PUT \
    -H "Authorization: Bearer ${GH_TOKEN}" \
    -H "Accept: application/vnd.github+json" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    "${API}/pulls/${PR_NUMBER}/merge" \
    -d "{\"merge_method\": \"${MERGE_METHOD}\"}")"

  if echo "${response}" | grep -q '"merged":true'; then
    echo "  成功：PR #${PR_NUMBER} 已合併至 ${BRANCH}"
    return 0
  fi
  echo "  失敗：${response}"
  return 1
}

# --- 主流程 ---

if ! set_required_approvals 0; then
  echo "無法降低審核需求，中止（未變更任何合併狀態）"
  exit 1
fi

if ! merge_pr; then
  echo "合併失敗，將還原審核需求為 1"
  EXIT_CODE=1
fi

# 無論合併成功與否，一律還原保護規則，確保規則不被永久弱化
if ! set_required_approvals 1; then
  echo "警告：還原審核需求為 1 失敗，請手動確認 ${BRANCH} 的分支保護設定！"
  EXIT_CODE=1
fi

echo
if [ "${EXIT_CODE}" -eq 0 ]; then
  echo "全部完成：PR #${PR_NUMBER} 已合併，${BRANCH} 分支保護（1 個 approval）已還原。"
else
  echo "執行過程中有步驟失敗，請檢查上方訊息並視需要手動處理。"
fi

exit "${EXIT_CODE}"
