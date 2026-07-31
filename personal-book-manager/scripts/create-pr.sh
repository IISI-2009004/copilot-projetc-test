#!/usr/bin/env bash
# 通用腳本：透過 GitHub REST API 建立 Pull Request（取代網頁手動操作）
#
# 使用前需先於 shell 設定：GH_TOKEN, GH_OWNER, GH_REPO
#
# 用法：
#   bash scripts/create-pr.sh <head分支> <base分支> <標題> [說明內文]
#
# 範例：
#   bash scripts/create-pr.sh docs/add-ssdlc-collab-notes develop "docs: 新增協作文件"
#   bash scripts/create-pr.sh feature/x develop "feat: 新增 X" "詳細說明第一行\n第二行"
#
# 說明：
#   - <說明內文> 可省略；若包含換行請用 \n（會原樣寫入 JSON 的 body 欄位）
#   - 標題/內文含中文或特殊符號皆可，腳本會先寫入暫存 JSON 檔再以 --data @file 送出，
#     避免 shell 轉義造成的 curl/JSON 解析錯誤
#
# 注意：本腳本執行完畢會正常結束並回到 shell 提示字元。

: "${GH_TOKEN:?請先 export GH_TOKEN}"
: "${GH_OWNER:?請先 export GH_OWNER}"
: "${GH_REPO:?請先 export GH_REPO}"

HEAD_BRANCH="${1:?請提供 head 分支名稱，例如：bash scripts/create-pr.sh feature/x develop \"標題\"}"
BASE_BRANCH="${2:?請提供 base 分支名稱（合併目標），例如：develop}"
TITLE="${3:?請提供 PR 標題}"
BODY="${4:-}"

API="https://api.github.com/repos/${GH_OWNER}/${GH_REPO}"
TMP_JSON="$(mktemp)"
trap 'rm -f "${TMP_JSON}"' EXIT

# 用 python3（多數 git bash/Windows 環境已內建或可用）或簡易跳脫方式產生合法 JSON。
# 若沒有 python3，退回用基本的字串取代跳脫雙引號，避免依賴額外工具。
if command -v python3 >/dev/null 2>&1; then
  python3 - "$HEAD_BRANCH" "$BASE_BRANCH" "$TITLE" "$BODY" > "${TMP_JSON}" << 'PYEOF'
import json, sys
head, base, title, body = sys.argv[1:5]
print(json.dumps({"title": title, "head": head, "base": base, "body": body}))
PYEOF
else
  esc() { printf '%s' "$1" | sed 's/\\/\\\\/g; s/"/\\"/g'; }
  {
    printf '{'
    printf '"title": "%s", ' "$(esc "$TITLE")"
    printf '"head": "%s", ' "$(esc "$HEAD_BRANCH")"
    printf '"base": "%s", ' "$(esc "$BASE_BRANCH")"
    printf '"body": "%s"' "$(esc "$BODY")"
    printf '}'
  } > "${TMP_JSON}"
fi

echo "== 建立 PR：${HEAD_BRANCH} -> ${BASE_BRANCH} =="

RESPONSE="$(curl -sS -X POST \
  -H "Authorization: Bearer ${GH_TOKEN}" \
  -H "Accept: application/vnd.github+json" \
  -H "X-GitHub-Api-Version: 2022-11-28" \
  "${API}/pulls" \
  --data @"${TMP_JSON}")"

if echo "${RESPONSE}" | grep -q '"html_url"'; then
  PR_URL="$(echo "${RESPONSE}" | grep -o '"html_url": *"[^"]*"' | head -1 | sed 's/.*"\(https[^"]*\)"/\1/')"
  PR_NUMBER="$(echo "${RESPONSE}" | grep -o '"number": *[0-9]*' | head -1 | grep -o '[0-9]*')"
  echo "  成功：PR #${PR_NUMBER}"
  echo "  ${PR_URL}"
  echo
  echo "後續可用以下指令合併（若分支有保護且你是唯一審核者）："
  echo "  bash scripts/merge-pr-as-owner.sh ${PR_NUMBER} ${BASE_BRANCH}"
  exit 0
else
  echo "  失敗：${RESPONSE}"
  exit 1
fi
