---
description: "為 GitHub Copilot 創建高品質代理技能的指南"
applyTo: "**/skills/**/SKILL.md"
---

# Agent Skills File Guidelines

創建有效且可移植的代理技能的說明，這些技能可透過專門的功能、工作流程和捆綁資源來增強 GitHub Copilot。

## What Are Agent Skills?

Agent Skills 是自包含的資料夾，內含指令和捆綁資源，用於教導 AI 代理專門的能力。與定義編碼標準的自訂指令不同，技能啟用特定任務的工作流程，這些工作流程可以包括腳本、範例、範本和參考資料。
主要特徵：

- **可移植**: 可在 VS Code、Copilot CLI 和 Copilot 編碼代理中使用
- **漸進加載**: 僅在與用戶請求相關時加載
- **資源捆綁**: 可以包括腳本、範本、範例與指令一起
- **按需啟用**: 根據提示相關性自動啟用

## Directory Structure

技能儲存在特定位置：

| Location                         | Scope                | Recommendation     |
| -------------------------------- | -------------------- | ------------------ |
| `.github/skills/<skill-name>/`   | Project/repository   | 推薦用於專案技能   |
| `.claude/skills/<skill-name>/`   | Project/repository   | 舊版，用於向後相容 |
| `~/.github/skills/<skill-name>/` | Personal (user-wide) | 推薦用於個人技能   |
| `~/.claude/skills/<skill-name>/` | Personal (user-wide) | 舊版，用於向後相容 |

每個技能**都必須**有自己的子目錄，其中至少包含一個 SKILL.md 檔案。

## Required SKILL.md Format

### Frontmatter (Required)

```yaml
---
name: webapp-testing
description: "用於使用 Playwright 測試本機 Web 應用程式的工具包。可用於驗證前端功能、偵錯 UI 行為、擷取瀏覽器螢幕截圖、檢查視覺回歸或檢視瀏覽器控制台日誌。支援 Chrome、Firefox 和 WebKit 核心瀏覽器。"
license: Complete terms in LICENSE.txt
---
```

| Field         | Required | Constraints                                                             |
| ------------- | -------- | ----------------------------------------------------------------------- |
| `name`        | Yes      | 全部小寫，空格以連字表示，最多 64 個字元（例如， webapp-testing ）      |
| `description` | Yes      | 10-1024 個字元，清楚描述功能和使用案例，使用單引號包裹                  |
| `license`     | No       | 參考 LICENSE.txt（例如，`Complete terms in LICENSE.txt`）或 SPDX 標識符 |

### Description Best Practices

**重要提示 ：** description 欄位是自動發現技能的主要機制。 Copilot 僅讀取技能 name 和 description 來決定是否載入該技能。如果您的描述含糊不清，該技能將永遠不會被啟動。

**What to include in description:**

1. **WHAT** 技能的功能（能力）
2. **WHEN** 使用時機（特定觸發器、場景、檔案類型或使用者請求）
3. **Keywords** 使用者可能在提示中提到的關鍵字

**Good description:**

```yaml
description: "用於使用 Playwright 測試本機 Web 應用程式的工具包。可用於驗證前端功能、偵錯 UI 行為、擷取瀏覽器螢幕截圖、檢查視覺回歸或檢視瀏覽器控制台日誌。支援 Chrome、Firefox 和 WebKit 核心瀏覽器。"
```

**Poor description:**

```yaml
description: "Web testing helpers"
```

描述不完善，原因如下：

- 沒有特定的觸發條件（Copilot 何時載入此內容？）
- 沒有關鍵字（哪些使用者提示會與之匹配？）
- 沒有功能說明（它實際上能做什麼？）

### Body Content

T主體部分包含詳細的指令，Copilot 會在技能啟動後載入這些指令。推薦章節：

| Section                     | Purpose                                                |
| --------------------------- | ------------------------------------------------------ |
| `# Title`                   | 簡要概述這項技能的功能                                 |
| `## When to Use This Skill` | 使用此技能的情境列表（強化描述觸發條件）               |
| `## Prerequisites`          | 所需工具、依賴項、環境設置（如適用）                   |
| `## Step-by-Step Workflows` | 可重複操作的步驟（構建、部署、設置）                   |
| `## Gotchas`                | 關於非顯而易見行為的主動警告（"永遠不要做 X，因為 Y"） |
| `## Troubleshooting`        | 已知問題的反應性修復（"如果看到 X，請嘗試 Y"）         |
| `## References`             | 連結到捆綁的文件或外部資源                             |

並非每項技能都需要包含所有章節。如果沒有外部依賴項，請跳過 ## Prerequisites 。如果技能僅提供建議，請跳過 ## Step-by-Step Workflows 」。如果技能涉及外部工具、API 或平台特定行為，請包含 ## Gotchas 。

有關內容品質原則（應包含哪些內容以及應省略哪些內容），請參閱下面的 [ 「撰寫高影響力技能」](#writing-high-impact-skills) below.

### Writing Each Section

**`# Title`** — 用一句話概括這項技能的功能。避免使用籠統的措詞；要具體說明其應用領域。

**`## When to Use This Skill`** — 列出具體場景，以強化技能描述中的觸發條件。這有助於 Copilot 確認已載入正確的技能。

```markdown
## When to Use This Skill

- 使用者要求在瀏覽器中測試 Web 應用程式
- 用戶需要截取螢幕截圖以進行視覺回歸測試
- 使用者希望使用瀏覽器控制台日誌來偵錯前端行為
```

**`## Prerequisites`** — 只有當該技能需要 Copilot 無法假定可用的工具、服務或設定時才列出。請列出確切的安裝命令。

```markdown
## Prerequisites

- [Playwright](https://playwright.dev/) installed: `npm install -D @playwright/test`
- At least one browser engine installed: `npx playwright install chromium`
```

**`## Step-by-Step Workflows`** — 針對順序至關重要的可重複流程（例如建置、部署、環境設定），使用編號步驟進行說明。描述每個階段要完成的任務，而不是硬編碼檔案路徑或行號－步驟應能適應不同的專案結構。對於複雜的工作流程（500 萬個步驟），請將其拆分為 references/ 文件，並提供連結。

```markdown
## Step-by-Step Workflows

### Deploy to Staging

1. 建置專案：`npm run build`
2. 執行部署前驗證：`npm run validate`
3. 部署到預發布環境：`npm run deploy -- --env staging`
4. 驗證健康端點是否回傳 200
```

**`## Gotchas`** — 主動警告以防止錯誤。記錄非顯而易見的預設值、API 特性、版本特定行為和常見陷阱。將關鍵限制加粗，然後解釋原因。

```markdown
## Gotchas

- **Never** call `billing.charge()` without checking `user.hasPaymentMethod` first —
  the SDK throws an unrecoverable error instead of returning a failure.
- The `currency` field expects ISO 4217 codes, not display names.
  Copilot often writes "dollars" instead of "USD".
```

**`## Troubleshooting`** — 針對已知問題的被動修復方案，以症狀→解決方案對的表格形式呈現。每一行都應包含完整資訊並可執行。

```markdown
## Troubleshooting

| Issue                    | Solution                                               |
| ------------------------ | ------------------------------------------------------ |
| Plugin won't connect     | Check servers are running (`npm run start:all`)        |
| Browser blocks localhost | Allow local network access, or try a different browser |
| Tool execution times out | Ensure the plugin UI is open and shows "Connected"     |
```

**`## References`** — 連結到捆綁的文件或外部資源。使用相對路徑引用捆綁的文件。

## Bundling Resources

技能可以包括 Copilot 按需存取的其他文件：

### Supported Resource Types

| Folder        | Purpose                                            | Loaded into Context? | Example Files                                        |
| ------------- | -------------------------------------------------- | -------------------- | ---------------------------------------------------- |
| `scripts/`    | 可執行的自動化操作                                 | 當執行時             | `helper.py`, `validate.sh`, `build.ts`               |
| `references/` | AI 代理用來做決策的文件                            | 是，當被引用時       | `api_reference.md`, `schema.md`, `workflow_guide.md` |
| `assets/`     | **靜態文件，原樣使用** 在輸出中（AI 代理不會修改） | 否                   | `logo.png`, `brand-template.pptx`, `custom-font.ttf` |
| `templates/`  | **起始代碼/模板，AI 代理會修改** 並基於此構建      | 是，當被引用時       | `viewer.html` (插入算法), `hello-world/` (擴展)      |

### Directory Structure Example

```
.github/skills/my-skill/
├── SKILL.md              # 必填：主要說明文件
├── LICENSE.txt           # 建議：授權條款（通常為 Apache 2.0）
├── scripts/              # 可選：可執行的自動化操作
│   ├── helper.py         # Python 腳本
│   └── helper.ps1        # PowerShell 腳本
├── references/           # 可選：載入到上下文中的文件
│   ├── api_reference.md
│   ├── workflow-setup.md     # 詳細工作流程（>5 步驟）
│   └── workflow-deployment.md
├── assets/               # 可選：原樣使用的靜態文件
│   ├── baseline.png      # 比對用的參考圖片
│   └── report-template.html
└── templates/            # 可選：AI 代理會修改的起始代碼/模板
    ├── scaffold.py       # AI 代理會自訂的代碼骨架
    └── config.template   # AI 代理會填寫的配置模板
```

> **LICENSE.txt**: 建立技能時，從 https://www.apache.org/licenses/LICENSE-2.0.txt 下載 Apache 2.0 授權條款，並保存為 `LICENSE.txt`。在附錄部分更新版權年份和所有者。

### Assets vs Templates: Key Distinction

**Assets** 是靜態資源，**在輸出中保持不變**：

- 一個嵌入到生成文檔中的 `logo.png`
- 一個作為輸出格式複製的 `report-template.html`
- 一個應用於文字渲染的 `custom-font.ttf`

**Templates** 是起始代碼/模板，**AI 代理會主動修改**：

- 一個 `scaffold.py`，AI 代理會在其中插入邏輯
- 一個 `config.template`，AI 代理會根據使用者需求填寫值
- 一個 `hello-world/` 專案目錄，AI 代理會擴展新功能

**經驗法則**：如果 AI 代理會讀取並基於文件內容構建 → `templates/`。如果文件在輸出中原樣使用 → `assets/`。

### Referencing Resources in SKILL.md

使用相對路徑引用技能目錄中的檔案：

```markdown
## Available Scripts

執行[輔助腳本](./scripts/helper.py)以自動執行常見任務。

有關詳細文檔，請參閱[API 參考](./references/api_reference.md)。

使用[鷹架](./templates/scaffold.py)作為起點。
```

## Progressive Loading Architecture

技能採用三級加載以提高效率：

| Level           | What Loads             | When                    |
| --------------- | ---------------------- | ----------------------- |
| 1. Discovery    | 僅 name 和 description | 始終(輕量級元資​​料)    |
| 2. Instructions | 完整的 `SKILL.md` 內容 | 當請求與描述匹配時      |
| 3. Resources    | 腳本、範例、文檔       | 僅當 Copilot 引用它們時 |

這意味著：

- 安裝許多技能而不消耗上下文
- 每個任務僅加載相關內容
- 資源僅在明確需要時加載

## Content Guidelines

### Writing Style

- 使用祈使語氣："Run", "Create", "Configure"（而不是 "You should run"）
- 具體且可操作
- 包含帶參數的精確命令
- 顯示預期輸出（如有幫助）
- 保持章節專注且易於掃描

### Script Requirements

引入腳本時，優先選擇跨平台語言：

| Language   | Use Case               |
| ---------- | ---------------------- |
| Python     | 複雜自動化、資料處理   |
| pwsh       | PowerShell Core 腳本   |
| Node.js    | 基於 JavaScript 的工具 |
| Bash/Shell | 簡單自動化任務         |

最佳實踐：

- 包含幫助/使用說明 (`--help` 標誌)
- 優雅地處理錯誤並提供清晰的訊息
- 避免存儲憑證或秘密
- 儘可能使用相對路徑

### When to Bundle Scripts

在以下情況下，請將腳本包含在您的技能中：

- 相同的代碼會被代理重複編寫
- 確定性可靠性至關重要（例如，文件操作、API 調用）
- 複雜邏輯受益於預先測試，而不是每次生成
- 操作具有自包含的目的，可以獨立演變
- 可測試性很重要 — 腳本可以進行單元測試和驗證
- 可預測的行為優於動態生成

腳本促進演進：即使是簡單的操作，當它們可能變得複雜、需要在多次調用中保持一致行為，或需要未來的可擴展性時，也受益於以腳本實現。

### 安全考量

- 腳本依賴現有的憑證助手（不存儲憑證）
- 僅對破壞性操作包含 `--force` 標誌
- 在不可逆操作前警告使用者
- 記錄任何網絡操作或外部調用

## Writing High-Impact Skills

### Focus on What Copilot Doesn't Know

不要包含 Copilot 從訓練資料中已知的資訊－例如標準語言語法、常用函式庫用法或文件齊全的 API 行為。技能中的每一行程式碼都應該包含 Copilot 可能出錯或完全忽略的內容。如果資訊已在官方文件的第一頁出現，則無需添加。重點關注內部約定、不明顯的預設值、版本特有的特性以及會改變 Copilot 行為的特定領域工作流程。

### Context Budget Awareness

在技​​能發現過程中，所有技能描述都共享有限的上下文視窗。您的描述會與其他所有已安裝的技能爭奪 Copilot 的注意力。請保持描述簡潔明了，並包含關鍵字——力求使用最短的文本，同時清晰地傳達「什麼」、「何時」以及相關的關鍵字。冗長的描述不僅浪費您的預算，還會降低系統中其他技能的可見度。

### Gotchas Are Your Highest-Signal Content

這 ## Gotchas 部分始終是任何技能中最有價值的部分——它提供主動警告，防患於未然。這與 ## Troubleshooting 部分不同，「故障排除」部分提供的是事後補救措施。將「## 注意事項」部分視為動態更新的部分：每次 Copilot 產生錯誤結果時，就加入一條注意事項。將關鍵限制加粗，然後解釋原因（例如，「 切勿在未先檢查 Y 情況下呼叫 X() ——SDK 會拋出無法恢復的錯誤」）。

### Prefer Flexible Guidelines Over Rigid Steps

僅對具體、可重複的操作（例如建置、部署、環境設定）使用編號步驟，因為這些操作的順序至關重要。對於開放式任務（例如偵錯、重構、程式碼審查），請提供決策標準和參考資訊－Copilot 需要靈活性來適應使用者的具體情況。

```markdown
# ❌ Too rigid

1. 開啟 src/api/handlers.ts 文件
2. 找出名為 processOrder 的函數
3. 在第 45-60 行附近新增 try-catch 區塊

# ✅ Flexible

修復 API 處理程序中的錯誤處理時：

- 確保所有資料庫操作都有適當的錯誤處理
- 使用專案的 ErrorHandler 工具（請參閱 ./references/error-handling.md）
- 記錄錯誤時提供足夠的上下文訊息，以便在生產環境中進行調試。
```

### Use Progressive Disclosure for Large Skills

如果您的 SKILL.md 檔案超過 200 行左右，請考慮將詳細內容拆分到子目錄中。這樣可以減少上下文資訊的消耗——Copilot 最初只會載入核心指令，並根據需要載入參考資料。

```markdown
## Reference Files

- `references/api.md` — 完整的函數簽名和返回類型
- `references/error-codes.md` — 每個此服務可能返回的錯誤代碼
- `scripts/validate.sh` — 在進行更改後運行此腳本以驗證正確性

根據當前任務的需要閱讀這些文件。不要一次性全部閱讀。
```

## Common Patterns

### Parameter Table Pattern

文檔參數清晰明確：

```markdown
| Parameter   | Required | Default | Description            |
| ----------- | -------- | ------- | ---------------------- |
| `--input`   | Yes      | -       | 要處理的輸入文件或 URL |
| `--action`  | Yes      | -       | 要執行的操作           |
| `--verbose` | No       | `false` | 啟用詳細輸出           |
```

### Workflow Execution Pattern

在執行多步工作流程時，創建一個 TODO 列表，其中每個步驟都引用相關的文檔：

```markdown
## TODO

- [ ] Step 1: Configure environment - see [workflow-setup.md](./references/workflow-setup.md#environment)
- [ ] Step 2: Build project - see [workflow-setup.md](./references/workflow-setup.md#build)
- [ ] Step 3: Deploy to staging - see [workflow-deployment.md](./references/workflow-deployment.md#staging)
- [ ] Step 4: Run validation - see [workflow-deployment.md](./references/workflow-deployment.md#validation)
- [ ] Step 5: Deploy to production - see [workflow-deployment.md](./references/workflow-deployment.md#production)
```

這樣可以確保可追溯性，並允許在工作流程中斷時恢復工作流程。

## Validation Checklist

發布技能之前：

- [ ] `SKILL.md` 包含有效的前元數據，其中包含 name 和 description
- [ ] `name` 為小寫並使用連字符，長度不超過 64 個字符
- [ ] `description` 清楚地說明 **WHAT** 它的功能，**WHEN** 使用它，以及相關的 **KEYWORDS**
- [ ] `description` 簡明且關鍵字密集（遵守上下文預算）
- [ ] 主體內容專注於 Copilot 無法從訓練數據中獲知的信息
- [ ] 主體內容包括使用時機、前置條件（如適用）和核心指令
- [ ] `## 注意事項` 部分存在，如果技能涉及非顯而易見的行為、API 特性或常見陷阱
- [ ] SKILL.md 主體內容少於 500 行（考慮在 ~200 行時拆分到 `references/`；500 行是硬性上限）
- [ ] 大型工作流程（>5 步）拆分到 `references/` 文件夾，並在 SKILL.md 中提供清晰的鏈接
- [ ] 腳本包含幫助文檔和錯誤處理
- [ ] 所有資源引用使用相對路徑
- [ ] 不包含硬編碼的憑證或秘密

## Related Resources

- [Agent Skills Specification](https://agentskills.io/)
- [VS Code Agent Skills Documentation](https://code.visualstudio.com/docs/copilot/customization/agent-skills)
- [Reference Skills Repository](https://github.com/anthropics/skills)
- [Awesome Copilot Skills](https://github.com/github/awesome-copilot/blob/main/docs/README.skills.md)
