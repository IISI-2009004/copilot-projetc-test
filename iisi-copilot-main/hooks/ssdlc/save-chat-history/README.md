---
name: 'Save Chat History'
description: 'Stop Hook，在 Agent 會話結束時自動將 GitHub Copilot 對話逐字稿附加到工作區的 chat_history.md（純問答）與 chat_detail_history.md（含工具呼叫），提供可追溯的稽核紀錄。'
tags:
  - ssdlc
  - audit
  - logging
  - transcript
  - stop
---

# Save Chat History

Stop Hook，在每次 GitHub Copilot Agent 會話結束時，自動解析當次會話的逐字稿（transcript），並附加到工作區根目錄的兩個 Markdown 檔案，方便事後檢視、稽核與知識留存。

## 產出檔案

| 檔案 | 內容 |
|------|------|
| `chat_history.md` | **純問答**：使用者訊息 + Assistant 的最終文字回覆 |
| `chat_detail_history.md` | **完整明細**：上述內容再加上每次工具呼叫（`_[Tool: ...]_`） |
| `chat_history.debug.json` | 僅在無法解析逐字稿時產生，保存原始 Stop 事件 JSON 供除錯 |

每次會話以 `## Session {時間戳記}` 區段附加，並記錄 Session ID 與 Stop reason。

## 運作原理

1. Hook 於 `Stop` 事件觸發，從 stdin 讀取事件 JSON。
2. 依 `session_id` 於 VS Code 的 workspaceStorage 尋找對應的逐字稿：
   ```
   Windows : %APPDATA%\Code\User\workspaceStorage\*\GitHub.copilot-chat\transcripts\{session_id}.jsonl
   Linux   : ~/.config/Code/User/workspaceStorage/*/GitHub.copilot-chat/transcripts/{session_id}.jsonl
   macOS   : ~/Library/Application Support/Code/User/workspaceStorage/*/GitHub.copilot-chat/transcripts/{session_id}.jsonl
   ```
   若找不到，退而使用事件中的 `transcript_path`。
3. 逐行解析 JSONL，將 `user.message` 與 `assistant.message` 寫入對應檔案。
4. 解析失敗時保存原始事件到 `chat_history.debug.json`，確保不遺失資料。

## 安裝方式

將整個 hook 資料夾複製到你的倉庫，使腳本位於 `.github/hooks/scripts/`（`.ps1` 的工作區根目錄推算依賴此路徑）：

```bash
# 於目標倉庫根目錄執行
mkdir -p .github/hooks/scripts
cp hooks.json               .github/hooks/save-chat-history.json
cp scripts/save-chat-history.ps1  .github/hooks/scripts/
cp scripts/save-chat-history.sh   .github/hooks/scripts/
chmod +x .github/hooks/scripts/save-chat-history.sh
```

`hooks.json` 內容：

```json
{
  "hooks": {
    "Stop": [
      {
        "type": "command",
        "windows": "powershell -ExecutionPolicy Bypass -File .github/hooks/scripts/save-chat-history.ps1",
        "command": "bash .github/hooks/scripts/save-chat-history.sh",
        "timeout": 30
      }
    ]
  }
}
```

- **Windows / VS Code**：使用 `save-chat-history.ps1`（`windows` 覆寫命令）。
- **Linux / macOS / Cloud Agent / CLI**：使用 `save-chat-history.sh`（預設 `command`，需安裝 `jq`）。

## 注意事項

- 腳本會檢查 `stop_hook_active` 旗標以防止無限迴圈。
- `chat_history.md`、`chat_detail_history.md`、`chat_history.debug.json` 為每次會話的產出，建議依團隊政策決定是否納入版控或加入 `.gitignore`。
- 逐字稿內容可能包含敏感資訊，分享或提交前請自行檢閱。
- bash 版本需要 `jq`；請確認執行環境已安裝。
