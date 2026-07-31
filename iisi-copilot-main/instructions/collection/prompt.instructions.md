---
description: '建立高品質 GitHub Copilot 提示檔的指南'
applyTo: '**/*.prompt.md'
---

# Copilot 提示檔指南

I建立有效且易於維護的提示檔案的說明，這些提示檔案可指導 GitHub Copilot 在任何儲存庫中提供一致、高品質的結果。

## 範圍與原則
- 目標受眾: 為 Copilot Chat 編寫可重用提示的維護者和貢獻者。
- 目標: 可預測的行為、清晰的期望、最小的權限以及跨儲存庫的可移植性。
- 主要參考資料: VS Code 關於提示檔的文件和組織特定的慣例。

## Frontmatter Requirements 前言要求

每個提示符檔案都應該包含 YAML 前元數據，其中包含以下欄位：

### 必填/推薦欄位

| 欄位 | 必填 | 說明 |
|-------|----------|-------------|
| `description` | 推薦 | 提示的簡短描述（單句，可操作的結果） |
| `name` | 選填 | 在聊天中輸入 `/` 後顯示的名稱。如果未指定，默認為檔案名稱 |
| `agent` | 推薦 | 要使用的代理：`ask`、`edit`、`agent` 或自定義代理名稱。默認為當前代理 |
| `model` | 選填 | 要使用的語言模型。默認為當前選擇的模型 |
| `tools` | 選填 | 可用於此提示的工具/工具集名稱列表 |
| `argument-hint` | 選填 | 在聊天輸入中顯示的提示文字，以引導使用者互動 |

### 指南

- 使用一致的引號（建議使用單引號）並保持每行一個欄位，以提高可讀性和版本控制的清晰度
- 如果指定了 `tools` 並且當前代理是 `ask` 或 `edit`，則默認代理將變為 `agent`
- 保留組織要求的任何其他元數據（`language`、`tags`、`visibility` 等）
## File Naming and Placement 文件命名和放置
- 使用以 .prompt.md 結尾的 kebab-case 檔案名，並將其儲存在 .github/prompts/ 下，除非您的工作區標準指定了其他目錄。
- 提供一個簡短的檔案名，以傳達操作意圖（例如，`generate-readme.prompt.md` 而不是 `prompt1.prompt.md`）。

## Body Structure 正文結構
- 首先使用與提示意圖相符的 # 級標題，以便在快速選擇搜尋中能夠很好地顯示。
- 使用可預測的章節組織內容。建議的基線：`Mission` 或 `Primary Directive`、`Scope & Preconditions`、`Inputs`、`Workflow`（逐步）、`Output Expectations` 和 `Quality Assurance`。
- 根據領域調整章節名稱，但保留邏輯流程：為什麼 → 背景 → 輸入 → 操作 → 輸出 → 驗證。
- 使用相對鏈接引用相關提示或指令檔，以便於發現。

## Input and Context Handling 輸入和上下文處理
- 使用 `${input:variableName[:placeholder]}` 來表示必填值，並說明使用者何時需要提供它們。盡可能提供預設值或替代方案。
- 僅在必要時提及上下文變數，如 `${selection}`、`${file}`、`${workspaceFolder}`，並描述 Copilot 應如何解釋它們。
- 說明在缺少必要上下文時應如何處理（例如，"請求檔案路徑，如果仍未定義則停止"）。

## Tool and Permission Guidance 工具和權限指南
- 將 `tools` 限制為完成任務所需的最小集合。當順序重要時，按首選執行順序列出它們。
- 如果提示從聊天模式繼承工具，請提及該關係並說明任何關鍵工具行為或副作用。
- 警告破壞性操作（檔案創建、編輯、終端命令）並在工作流程中包含防護措施或確認步驟。

## Instruction Tone and Style 指令語氣與風格
- 使用直接、命令式的句子針對 Copilot（例如，"分析"、"生成"、"總結"）。
- 保持句子簡短且明確，遵循 Google 開發者文件翻譯最佳實踐以支持本地化。
- 避免使用成語、幽默或文化特定的參考；偏好中性、包容的語言。

## Output Definition 輸出定義
- 指定預期結果的格式、結構和位置（例如，"使用以下模板創建 `docs/adr/adr-XXXX.md`"）。
- 包含成功標準和失敗觸發條件，以便 Copilot 知道何時停止或重試。
- 提供驗證步驟——手動檢查、自動命令或接受標準清單——以便審查者在運行提示後執行。

## Examples and Reusable Assets 示例和可重用資產
- 嵌入提示應生成或遵循的良好/不良範例或腳手架（Markdown 模板、JSON 存根）。
- 保持參考表（功能、狀態碼、角色描述）內聯，以保持提示自包含。當上游資源更改時，更新這些表。
- 連結到權威文件，而不是複製冗長的指導。

## Quality Assurance Checklist 質量保證清單
- [ ] Frontmatter 欄位完整、準確且最小權限。
- [ ] 輸入包含佔位符、預設行為和備選方案。
- [ ] 工作流程涵蓋準備、執行和後處理，無遺漏。
- [ ] 輸出期望包括格式和存儲細節。
- [ ] 驗證步驟可操作（命令、差異檢查、審查提示）。
- [ ] 提示中引用的安全、合規和隱私政策是最新的。
- [ ] 提示在 VS Code 中成功執行（`Chat: Run Prompt`）並使用代表性場景。

## Maintenance Guidance 維護指南
- 將提示與其影響的代碼一起進行版本控制；當依賴、工具或審查流程變更時更新它們。
- 定期審查提示，以確保工具列表、模型要求和鏈接的文件保持有效。
- 與其他儲存庫協調：當提示被證明廣泛有用時，將通用指導提取到指令文件或共享提示包中。

## Additional Resources 附加資源
- [Prompt Files Documentation](https://code.visualstudio.com/docs/copilot/customization/prompt-files#_prompt-file-format)
- [Awesome Copilot Prompt Files](https://github.com/github/awesome-copilot/tree/main/prompts)
- [Tool Configuration](https://code.visualstudio.com/docs/copilot/chat/chat-agent-mode#_agent-mode-tools)
