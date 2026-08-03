---
description: "便攜式指南，用於編寫安全、快速、清晰的鉤子和可重複使用的鉤子範例。"
applyTo: ".github/hooks/**, hooks/**"
---

# Hook Authoring Guidelines

Hooks 鉤子是小型、確定性的命令或腳本， 會在特定的生命週期事件發生時執行。一個優秀的鉤子應該只做一件事，運行速度快，並且明確地展示其副作用。

## Folder Structure

GitHub Copilot hook 位於倉庫的 .github/hooks/ 目錄中：

```text
.github/
└── hooks/
    ├── block-dangerous-commands.json   ← hook config (which event, which script, options)
    └── scripts/
        ├── block-dangerous-commands.sh  ← Bash implementation
        └── block-dangerous-commands.ps1 ← PowerShell implementation (optional if Bash-only)
```

你可以擁有多個 .json 檔案－每個檔案都註冊一個或多個事件的鉤子。主機將加載所有這些文件。

## The Config File

每個 .json 檔案都將事件對應到一個 hook 條目陣列。

- **Command hooks** (`type: "command"`): 運行本地腳本。主機透過標準輸入傳遞事件 JSON，您的腳本透過退出程式碼和標準輸出做出回應。

### Config example

```json
{
  "version": 1,
  "hooks": {
    "preToolUse": [
      {
        "matcher": "bash",
        "type": "command",
        "bash": "./.github/hooks/scripts/block-dangerous-commands.sh",
        "powershell": "./.github/hooks/scripts/block-dangerous-commands.ps1",
        "cwd": ".",
        "timeoutSec": 5,
        "env": {
          "BLOCK_MODE": "deny"
        }
      }
    ]
  }
}
```

### Config fields

| Field        | Required    | What it does                                                                                                                                                                      |
| ------------ | ----------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `type`       | yes         | `"command"` for scripts                                                                                                                                                           |
| `matcher`    | no          | 主機級過濾器－僅當工具名稱與此值相符時（例如 "bash" 、 "powershell" 、 "edit" 、 "create" ），此鉤子才會觸發。已在本地 Copilot CLI v1.0.36 中驗證有效；尚未在倉庫鉤子範例中使用。 |
| `bash`       | one or both | 在支援 Unix/Bash 的主機上呼叫的命令列                                                                                                                                             |
| `powershell` | one or both | 在支援 Windows / PowerShell 的主機上呼叫的命令列                                                                                                                                  |
| `cwd`        | no          | 工作目錄，相對於倉庫根目錄                                                                                                                                                        |
| `timeoutSec` | no          | 主機在終止進程前的最大秒數（默認 30）                                                                                                                                             |
| `env`        | no          | 傳遞給腳本的額外進程環境變數                                                                                                                                                      |

### Why matchers matter

如果沒有matcher， 每次呼叫工具時都會觸發 preToolUse 鉤子。你的腳本開頭會包含類似這樣的樣板程式碼：

```bash
tool_name="$(printf '%s' "$payload" | jq -r '.toolName')"
[[ "$tool_name" != "bash" ]] && exit 0
```

有了匹配器，主機就會自動完成這種過濾——無需編寫樣板程式碼，也不會為無關工具啟動進程。一旦該功能穩定下來，這很可能會成為標準模式。

如果你的鉤子必須在 CLI 和雲端代理（或舊版本 CLI）上都能工作，即使使用匹配器，也應保留腳本內的過濾作為後備。

### `env` — static configuration for your script

`env` 是一個標準的主機欄位 。它裡面的鍵是作者定義的變數 －你可以選擇名稱和值。

它們作為 **進程環境變數** 傳遞，而不是在 stdin JSON 載荷中。將它們用於不應硬編碼的靜態配置：

| Pattern        | Example                                                                         |
| -------------- | ------------------------------------------------------------------------------- |
| Mode flag      | `"BLOCK_MODE": "deny"` — 同一個腳本在一個倉庫中記錄日誌，在另一個倉庫中阻止運行 |
| Threshold      | `"MAX_CHANGED_FILES": "20"`                                                     |
| Path           | `"AUDIT_LOG_PATH": ".github/logs/hooks.log"`                                    |
| Feature toggle | `"ENABLE_NOTIFICATIONS": "false"`                                               |

### `bash` and `powershell` —何時提供其中一種或兩種

主機選擇與目前環境相符的項目。它不會同時運行兩個條目，也不會在兩者之間來回切換。

| Situation                              | Provide                    |
| -------------------------------------- | -------------------------- |
| 私人鉤子，一個已知的平台               | 只有該平台的條目           |
| 發佈的鉤子聲稱支持跨平台               | 兩個條目都提供             |
| 單一跨平台運行時（Python、Node、pwsh） | 通過兩個條目暴露相同的腳本 |
| 僅限 Bash 的依賴                       | 僅 `bash`                  |
| 僅限 Windows 的依賴                    | 僅 `powershell`            |

透過兩個條目使用 Python 實現跨平台範例：

```json
{
  "type": "command",
  "bash": "python3 ./.github/hooks/scripts/check.py",
  "powershell": "python .\\.github\\hooks\\scripts\\check.py"
}
```

## The Script Contract

每個 hook 腳本都遵循相同的基本約定：從 stdin 讀取 JSON，執行工作，並透過退出程式碼、stdout 和 stderr 來回應。

**重要**: `toolArgs` 是一個 **JSON 字串**，而不是嵌套的物件。你必須再次解析它以訪問其欄位。

### Reading stdin and responding — Bash and PowerShell

**Bash**:

```bash
#!/usr/bin/env bash
set -euo pipefail
payload="$(cat)"
tool_name="$(printf '%s' "$payload" | jq -r '.toolName')"
tool_args="$(printf '%s' "$payload" | jq -r '.toolArgs')"
command="$(printf '%s' "$tool_args" | jq -r '.command // ""')"
```

**PowerShell**:

```powershell
Set-StrictMode -Version Latest
$payload = [Console]::In.ReadToEnd() | ConvertFrom-Json
$toolArgs = $payload.toolArgs | ConvertFrom-Json
$command = $toolArgs.command
```

To deny in `preToolUse` (PowerShell):

```powershell
@{ permissionDecision = 'deny'; permissionDecisionReason = 'Blocked by policy' } |
    ConvertTo-Json -Compress
exit 0
```

### What the script receives

| Input               | What it carries                                       |
| ------------------- | ----------------------------------------------------- |
| `stdin`             | 一個描述當前事件的 JSON 有效負載                      |
| process environment | 正常的環境變數，加上你在配置中 `env` 下定義的任何變數 |
| working directory   | 配置中的 `cwd`，或主機的默認值                        |

### How the script responds

| Channel       | Purpose                                                                 |
| ------------- | ----------------------------------------------------------------------- |
| exit `0`      | 腳本執行成功－除非標準輸出包含結構化的拒絕指令，否則主機將繼續運作。    |
| non-zero exit | **阻止觸發的操作** 並表示鉤子失敗                                       |
| `stdout`      | 結構化的機器可讀輸出——僅適用於記錄 stdout 架構的事件（如 `preToolUse`） |
| `stderr`      | 用於日誌的人類可讀診斷信息                                              |

### Exit codes and deny: the full picture

拒絕機制取決於事件 ：

| Event type                                                                  | How to allow                                                          | How to deny / block                                                                                                                                                                                   |
| --------------------------------------------------------------------------- | --------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `preToolUse`                                                                | 退出代碼為 0 ，空輸出或返回 {"permissionDecision":"allow"} 到標準輸出 | 首選方法 ：在標準輸出中傳回 exit 0 + {"permissionDecision":"deny","permissionDecisionReason":"..."} 可以給主機一個顯示原因的提示。 另一種方法是：非零退出代碼會阻塞工具調用，但沒有提供結構化的原因。 |
| `userPromptSubmitted`                                                       | exit `0`                                                              | 非零退出代碼會阻塞提示符號（此事件會忽略標準輸出）。                                                                                                                                                  |
| `agentStop`                                                                 | exit `0`                                                              | 非零退出代碼會阻塞操作                                                                                                                                                                                |
| Other events (`sessionStart`, `sessionEnd`, `postToolUse`, `errorOccurred`) | exit `0`                                                              | 非零退出代碼表示失敗；主機可能會跳過該事件的後續鉤子                                                                                                                                                  |

**經驗法則** ：如果事件具有結構化的 stdout 模式（例如 preToolUse ），則應使用它——它能提供清晰的拒絕理由，並且是官方文件中記錄的拒絕路徑。對於沒有結構化 stdout 的事件，非零退出值是實際可行的阻塞機制——這一點已在程式碼庫範例和學習中心文件中得到證實，儘管 GitHub 官方文件並未明確將「非零退出值 = 阻塞」作為一項約定保證。

### Example 1: Commit gate — block commits until lint, types, and tests pass

**這種模式之所以重要** ：拒絕原因包含了實際的錯誤訊息，因此代理可以發現問題所在並在再次嘗試之前進行修復。這創造了一個自我修正的回饋循環——這是鉤子函數最強大的功能。

**Event**: `preToolUse` — 在代理程式執行 git commit 之前觸發

**Config** — `.github/hooks/commit-gate.json`:

```json
{
  "version": 1,
  "hooks": {
    "preToolUse": [
      {
        "type": "command",
        "bash": "./.github/hooks/scripts/commit-gate.sh",
        "cwd": ".",
        "timeoutSec": 120
      }
    ]
  }
}
```

**Script** — `.github/hooks/scripts/commit-gate.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

payload="$(cat)"
tool_name="$(printf '%s' "$payload" | jq -r '.toolName')"

# Only gate bash commands that are git commits
if [[ "$tool_name" != "bash" ]]; then exit 0; fi
command="$(printf '%s' "$payload" | jq -r '.toolArgs' | jq -r '.command // ""')"
if ! printf '%s' "$command" | grep -q "git commit"; then exit 0; fi

CWD="$(printf '%s' "$payload" | jq -r '.cwd')"
ERRORS=""

# 1. TypeScript type check
if [[ -f "$CWD/tsconfig.json" ]]; then
  TSC_OUT=$(cd "$CWD" && npx tsc --noEmit 2>&1) || ERRORS="${ERRORS}
=== TypeScript Errors ===
$(echo "$TSC_OUT" | head -30)"
fi

# 2. Lint
if [[ -f "$CWD/package.json" ]]; then
  HAS_LINT=$(jq -r '.scripts.lint // empty' "$CWD/package.json" 2>/dev/null)
  if [[ -n "$HAS_LINT" ]]; then
    LINT_OUT=$(cd "$CWD" && npm run lint --silent 2>&1) || ERRORS="${ERRORS}
=== Lint Errors ===
$(echo "$LINT_OUT" | tail -30)"
  fi

  # 3. Tests
  HAS_TEST=$(jq -r '.scripts.test // empty' "$CWD/package.json" 2>/dev/null)
  if [[ -n "$HAS_TEST" ]]; then
    TEST_OUT=$(cd "$CWD" && CI=true npm test -- --watchAll=false 2>&1) || ERRORS="${ERRORS}
=== Test Failures ===
$(echo "$TEST_OUT" | tail -30)"
  fi
fi

if [[ -n "$ERRORS" ]]; then
  jq -nc --arg reason "Cannot commit — fix these issues first:
$ERRORS" \
    '{permissionDecision:"deny",permissionDecisionReason:$reason}'
fi
exit 0
```

**What happens at runtime:**

| Scenario        | stdout                                                                                                                         | exit     | Host action                                         |
| --------------- | ------------------------------------------------------------------------------------------------------------------------------ | -------- | --------------------------------------------------- |
| All checks pass | empty                                                                                                                          | `0`      | Commit proceeds                                     |
| Lint fails      | `{"permissionDecision":"deny","permissionDecisionReason":"Cannot commit — fix these issues first:\n=== Lint Errors ===\n..."}` | `0`      | Blocks commit; agent sees the errors and fixes them |
| jq missing      | empty                                                                                                                          | non-zero | Hook failure                                        |

### Example 2: Auto-format after file edits

**這種模式之所以重要** ：代理程式會編寫程式碼，格式化程式隨後立即運行——無需任何手動操作。代理程式下次讀取該檔案時，看到的將是格式化後的版本。

**Event**: `postToolUse` — 在 `edit` 或 `create` 工具調用後觸發

**Config** — `.github/hooks/format-on-save.json`:

```json
{
  "version": 1,
  "hooks": {
    "postToolUse": [
      {
        "type": "command",
        "bash": "./.github/hooks/scripts/format-on-save.sh",
        "cwd": ".",
        "timeoutSec": 15
      }
    ]
  }
}
```

**Script** — `.github/hooks/scripts/format-on-save.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

payload="$(cat)"
tool_name="$(printf '%s' "$payload" | jq -r '.toolName')"
result_type="$(printf '%s' "$payload" | jq -r '.toolResult.resultType // ""')"

# Only format after successful file writes
case "$tool_name" in
  edit|create) ;;
  *) exit 0 ;;
esac
[[ "$result_type" != "success" ]] && exit 0

file_path="$(printf '%s' "$payload" | jq -r '.toolArgs' | jq -r '.path // ""')"
[[ -z "$file_path" || ! -f "$file_path" ]] && exit 0

# Run the project's formatter — adapt to your stack
if command -v npx >/dev/null 2>&1 && [[ -f "package.json" ]]; then
  npx prettier --write "$file_path" 2>/dev/null || true
elif command -v dotnet >/dev/null 2>&1 && [[ "$file_path" == *.cs ]]; then
  dotnet format --include "$file_path" 2>/dev/null || true
fi
exit 0
```

**What happens at runtime:**

| Scenario                         | What the hook does                 | exit |
| -------------------------------- | ---------------------------------- | ---- |
| 代理程式成功編輯了 src/app.ts 。 | 運行 `prettier --write src/app.ts` | `0`  |
| 代理程式執行 `bash ls`           | Skips（不是檔案寫入工具）          | `0`  |
| Prettier 未安裝                  | 靜默跳過格式化                     | `0`  |

### Example 3: 使用結構化拒絕規則阻止危險命令

**這種模式之所以重要** ：最簡單的防護措施——在破壞性 shell 命令執行之前阻止它們，並提供代理可以讀取的明確原因。

**Event**: `preToolUse` — 在任何工具呼叫之前觸發

**Config** — `.github/hooks/block-dangerous.json`:

```json
{
  "version": 1,
  "hooks": {
    "preToolUse": [
      {
        "type": "command",
        "bash": "./.github/hooks/scripts/block-dangerous.sh",
        "cwd": ".",
        "timeoutSec": 5,
        "env": {
          "BLOCK_MODE": "deny"
        }
      }
    ]
  }
}
```

**Script** — `.github/hooks/scripts/block-dangerous.sh`:

```bash
#!/usr/bin/env bash
set -euo pipefail

payload="$(cat)"
block_mode="${BLOCK_MODE:-log}"
tool_name="$(printf '%s' "$payload" | jq -r '.toolName')"

[[ "$tool_name" != "bash" ]] && exit 0

command="$(printf '%s' "$payload" | jq -r '.toolArgs' | jq -r '.command // ""')"

if printf '%s' "$command" | grep -qE 'rm -rf /|git reset --hard|git clean -fd|git push.*--force'; then
  # Truncate command to avoid leaking secrets in deny reason or logs
  short_cmd="$(printf '%.80s' "$command")"
  if [[ "$block_mode" == "deny" ]]; then
    jq -cn --arg reason "Destructive command blocked: ${short_cmd}..." \
      '{permissionDecision:"deny",permissionDecisionReason:$reason}'
  else
    echo "Would block: ${short_cmd}..." >&2
  fi
fi
exit 0
```

**What happens at runtime:**

| Scenario           | BLOCK_MODE | stdout                              | exit | Host action         |
| ------------------ | ---------- | ----------------------------------- | ---- | ------------------- |
| Safe command       | any        | empty                               | `0`  | Proceeds            |
| `git push --force` | `deny`     | `{"permissionDecision":"deny",...}` | `0`  | Blocks with reason  |
| `git push --force` | `log`      | empty                               | `0`  | Proceeds (log only) |

## Event Types

完整的 hooks 參考文件具有權威性。在編寫 hook 之前， **請務必查看該文件以獲取最新的 payload 格式 ：**

- [Hooks configuration reference](https://docs.github.com/en/copilot/reference/hooks-configuration)
- [About hooks](https://docs.github.com/en/copilot/concepts/agents/cloud-agent/about-hooks)

| Event                 | stdout                                                                                | Typical use                      |
| --------------------- | ------------------------------------------------------------------------------------- | -------------------------------- |
| `sessionStart`        | **parsed** －stdout 中的 additionalContext 被注入到會話中                             | 設定、驗證、上下文注入、日誌記錄 |
| `sessionEnd`          | ignored                                                                               | 清理、摘要                       |
| `userPromptSubmitted` | ignored                                                                               | 審計、提示阻止                   |
| `preToolUse`          | **parsed** — `permissionDecision`, `modifiedArgs`/`updatedInput`, `additionalContext` | 防護措施、拒絕/阻止、參數修改    |
| `postToolUse`         | ignored                                                                               | 日誌記錄、格式化                 |
| `postToolUseFailure`  | —                                                                                     | 工具運行失敗後的恢復             |
| `agentStop`           | —                                                                                     | 最終驗證                         |
| `subagentStart`       | —                                                                                     | 子代理審計                       |
| `subagentStop`        | —                                                                                     | 子代理輸出驗證                   |
| `errorOccurred`       | ignored                                                                               | 診斷、警報                       |
| `preCompact`          | —                                                                                     | 壓縮前工作                       |
| `permissionRequest`   | —                                                                                     | 批准工作流程                     |

### Payload schemas for common events

這些是 hooks 參考文件中的有效載荷形狀。 請務必參考 [官方參考](https://docs.github.com/en/copilot/reference/hooks-configuration) 以獲取最新的欄位。

**`sessionStart`**

```json
{
  "timestamp": 1704614400000,
  "cwd": "/path/to/project",
  "source": "new",
  "initialPrompt": "Create a new feature"
}
```

source 為 "new" 、 "resume" 或 "startup" 。 initialPrompt 是使用者首次看到的提示訊息 initialPrompt (如果已提供)。

**`sessionStart` stdout output** — 主機解析標準輸出以獲得：

```json
{
  "additionalContext": "Current branch: main. Deploy target: staging."
}
```

`additionalContext` 會直接注入到會話對話中，讓 hooks 能夠動態提供特定於環境的上下文。

**`sessionEnd`**

```json
{
  "timestamp": 1704618000000,
  "cwd": "/path/to/project",
  "reason": "complete"
}
```

`reason` 為 "complete" 、 "error" 、 "abort" 、 "timeout" 或 "user_exit" 。

**`userPromptSubmitted`**

```json
{
  "timestamp": 1704614500000,
  "cwd": "/path/to/project",
  "prompt": "Fix the authentication bug"
}
```

此欄位為 prompt －即使用者提交的確切文字。

**`preToolUse`**

```json
{
  "timestamp": 1704614600000,
  "cwd": "/path/to/project",
  "toolName": "bash",
  "toolArgs": "{\"command\":\"rm -rf dist\",\"description\":\"Clean build directory\"}"
}
```

`toolArgs` 是一個 JSON 字串 －需要再次解析才能存取其欄位。

**`preToolUse` stdout output** — 主機解析標準輸出以獲得：

| Field                            | What it does                                                              |
| -------------------------------- | ------------------------------------------------------------------------- |
| `permissionDecision`             | "deny" 會阻止工具呼叫。 "allow" 和 "ask" 也可以接受；目前只處理 "deny" 。 |
| `permissionDecisionReason`       | 顯示給使用者的人類可讀原因                                                |
| `modifiedArgs` or `updatedInput` | 替換工具參數 — 用於取代原始參數                                           |
| `additionalContext`              | 注入到代理上下文中的文字，用於本次操作                                    |

**`postToolUse`**

```json
{
  "timestamp": 1704614700000,
  "cwd": "/path/to/project",
  "toolName": "bash",
  "toolArgs": "{\"command\":\"npm test\"}",
  "toolResult": {
    "resultType": "success",
    "textResultForLlm": "All tests passed (15/15)"
  }
}
```

`resultType` 為 "success" 、 "failure" 或 "denied" 。

**`errorOccurred`**

```json
{
  "timestamp": 1704614800000,
  "cwd": "/path/to/project",
  "error": {
    "message": "Network timeout",
    "name": "TimeoutError",
    "stack": "TimeoutError: Network timeout\n    at ..."
  }
}
```

**`agentStop`**

```json
{
  "timestamp": 1704618000000,
  "cwd": "/path/to/project"
}
```

最小有效載荷－用於觸發會話結束操作，例如運行 git diff --stat 或最終驗證。

## 當hooks鉤子是錯誤的工具時

| Avoid hooks for                            | Better fit                                  |
| ------------------------------------------ | ------------------------------------------- |
| 開放式推理或風格指導                       | Instructions, prompts, or agents            |
| 長多步驟工作流程，包含記憶、重試或分支     | Agents, scripts, or workflow engines        |
| 背景守護程序、監視器、去抖動循環或異步作業 | Dedicated automation, services, or CI       |
| 大規模倉庫範圍的驗證                       | CI, scheduled jobs, or dedicated automation |

## 通用設計規則

| Rule                                       | Why it matters             |
| ------------------------------------------ | -------------------------- |
| 一個鉤子，一份責任                         | 小型鉤子更容易信任和調試。 |
| 預設優先觀察                               | 阻塞或變更應該是明確的選擇 |
| 保持鉤子同步、有界且非互動式               | 鉤子運行在關鍵路徑上       |
| 使鉤子具有確定性和冪等性                   | 重新運行不應該產生漂移     |
| 預設不修改分支、索引或工作樹狀態           | Git破壞性行為風險高        |
| 將提示、工具參數和工具輸出視為不可信且敏感 | 輸入可能是敵對的或私密的   |
| 從日誌中刪除秘密、憑證、令牌和私密內容     | 日誌通常比鉤子運行時間長   |

## 腳本編寫規則

- 驗證您實際使用的 JSON 字段
- 引用 shell 變量，切勿從原始輸入建置指令
- 除非主機要求結構化輸出，否則請保持標準輸出乾淨。
- 使用嚴格模式：Bash `set -euo pipefail`，PowerShell `Set-StrictMode -Version Latest`
- 儘早檢查依賴項，如果缺少依賴項，則明確地報錯。
- 執行過程中避免出現提示、隱藏安裝或環境變更。
- 手動將代表性的 JSON 有效負載透過管道傳遞給測試腳本。

## 選擇最小可行實現

1. **PowerShell 7**, **Node.js**, 或 **Python** 用於廣泛可移植的鉤子
2. **Bash** 當 Bash 是明確要求或安全假設時
3. 當倉庫已經依賴**現有項目 CLI** 時，該 CLI 仍然存在。

**不要**為了實作一個普通的鉤子而引入一個新的編譯執行環境。

## 包裝一個可重複使用的掛鉤

- P將配置、腳本和文件打包在一起
- 記錄觸發事件、目的、副作用、依賴項和停用路徑
- 解釋一下這個鉤子讀取什麼、寫入什麼、阻塞什麼。

## Anti-Patterns

- 長時間運行的鉤子、監視器、背景守護程序或一次性異步工作
- 每個事件都進行大量掃描，而更窄的觸發器即可
- 關鍵路徑中的隱藏網路呼叫或上傳
- 默認情況下靜默修改 Git 狀態（checkout、reset、clean、stash、stage、commit、push 或歷史重寫）
- 互動式提示或隱式批准步驟
- 嘈雜的標準輸出、臨時輸出格式或混合的機器/人類輸出
- 記錄原始提示、秘密、憑證或大型工具輸出
- 混合不相關責任的單片鉤子

## 可移植性

### GitHub Copilot: CLI, VS Code, and Cloud Agent

相同的 .github/hooks/*.json 配置、相同的有效負載架構和相同的腳本契約可在 CLI、VS Code 和雲端代理中使用。事件名稱同時接受駝峰式命名（ preToolUse ）和帕斯卡式命名（ PreToolUse ）。工具參數的有效負載欄位為 toolArgs （JSON 字串）。

要注意的是：雲端代理只會從程式碼倉庫的預設分支載入 hooks。如果你的 hooks.json 檔案只位於特性分支上，雲端代理將無法辨識它。

### Claude Code

Claude Code 使用不同的鉤子系統：

- 設定在 `~/.claude/settings.json` 和 `.claude/settings.json`
- 不同的事件名稱和匹配器語法（正規表示式、 if 條件）
- Exit 2 = 阻塞，Exit 1 = 非阻塞錯誤（與 GitHub Copilot 不同）
- 5 種鉤子類型（command、http、mcp_tool、prompt、agent）
- 29+ 事件，包括 `FileChanged`、`CwdChanged`、`ConfigChange`

共享的最佳實踐是相同的：保持鉤子小型、確定性、明確的 I/O，並嚴格控制副作用。
