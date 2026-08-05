#!/usr/bin/env bash
# 整合腳本：以 repo 擁有者/唯一開發者身分合併 PR
#
# 本腳本是 scripts/set-branch-approvals.sh 與 scripts/merge-pr.sh 的整合封裝，
# 依序執行：
#   1. bash scripts/set-branch-approvals.sh 0 <目標分支>   # 暫時降為 0
#   2. bash scripts/merge-pr.sh <PR編號>                   # 合併 PR
#   3. bash scripts/set-branch-approvals.sh 1 <目標分支>   # 還原為 1
# 無論步驟 2 是否成功，都會執行步驟 3，確保分支保護規則不被永久弱化。
#
# 若想分開手動控制每個步驟（例如只想先降低保護、稍後再合併），
# 請直接使用 set-branch-approvals.sh 與 merge-pr.sh 兩支腳本。
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

set -u

: "${GH_TOKEN:?請先 export GH_TOKEN}"
: "${GH_OWNER:?請先 export GH_OWNER}"
: "${GH_REPO:?請先 export GH_REPO}"

PR_NUMBER="${1:?請提供 PR 編號，例如：bash scripts/merge-pr-as-owner.sh 2}"
BRANCH="${2:-develop}"
MERGE_METHOD="${3:-merge}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
EXIT_CODE=0

if ! bash "${SCRIPT_DIR}/set-branch-approvals.sh" 0 "${BRANCH}"; then
  echo "無法降低審核需求，中止（未變更任何合併狀態）"
  exit 1
fi

if ! bash "${SCRIPT_DIR}/merge-pr.sh" "${PR_NUMBER}" "${MERGE_METHOD}"; then
  echo "合併失敗，將還原審核需求為 1"
  EXIT_CODE=1
fi

# 無論合併成功與否，一律還原保護規則，確保規則不被永久弱化
if ! bash "${SCRIPT_DIR}/set-branch-approvals.sh" 1 "${BRANCH}"; then
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
