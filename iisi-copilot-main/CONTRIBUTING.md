# 貢獻指南 (Contributing to iisi-copilot)

感謝你願意為 **iisi-copilot** 貢獻！本專案是以 SSDLC（安全軟體開發生命週期）為核心的 AI 輔助開發素材庫，提供 Agents、Instructions、Hooks、Skills、Prompts、Templates 等可直接套用的範本，讓各專案能快速建立自己的 AI Agent Team。

我們特別歡迎**各專案在導入本 repo 範本後的實務回饋**——包含使用心得、依專案需求客製化後的檔案、以及驗證測試結果——透過 [案例回饋（Case Studies）](#案例回饋-case-studies) 機制回饋到本 repo，讓範本持續進化、嘉惠後續採用的團隊。

## 目錄

- [可接受的貢獻類型](#可接受的貢獻類型)
- [不接受的貢獻](#不接受的貢獻)
- [品質準則](#品質準則)
- [如何貢獻各類型元件](#如何貢獻各類型元件)
- [案例回饋 (Case Studies)](#案例回饋-case-studies)
- [提交 Pull Request 流程](#提交-pull-request-流程)
- [審查者責任](#審查者責任)
- [進一步考量與限制](#進一步考量與限制)

---

## 可接受的貢獻類型

本 repo 目前包含以下元件類型，歡迎針對任一類型提出新增或修正：

| 類型               | 資料夾                                                                                           | 說明文件                                                   |
| ------------------ | ------------------------------------------------------------------------------------------------ | ---------------------------------------------------------- |
| Agents             | [agents/ssdlc/](agents/ssdlc/)                                                                   | [agents/README.md](agents/README.md)                       |
| Hooks              | [hooks/ssdlc/](hooks/ssdlc/)                                                                     | [docs/README.hooks.md](docs/README.hooks.md)               |
| Instructions       | [instructions/ssdlc/](instructions/ssdlc/)、[instructions/collection/](instructions/collection/) | [docs/README.instructions.md](docs/README.instructions.md) |
| Plugins            | [plugins/ssdlc/](plugins/ssdlc/)                                                                 | [docs/README.plugins.md](docs/README.plugins.md)           |
| Prompts            | [prompts/ssdlc/](prompts/ssdlc/)                                                                 | [prompts/README.md](prompts/README.md)                     |
| Skills             | [skills/ssdlc/](skills/ssdlc/)                                                                   | [docs/README.skills.md](docs/README.skills.md)             |
| Templates          | [templates/ssdlc/](templates/ssdlc/)                                                             | —                                                          |
| Tutorials          | [tutorials/](tutorials/)                                                                         | —                                                          |
| **案例回饋（新）** | [case-studies/](case-studies/)                                                                   | [case-studies/README.md](case-studies/README.md)           |

## 不接受的貢獻

為維護本 repo 的安全與品質，以下內容**不會被接受**：

- 包含真實客戶名稱、內部網域、金鑰、密碼、Token 等**機敏資訊**的檔案
- 意圖繞過安全檢查、弱化 Hooks 防護機制、或教導執行危險指令的內容
- 與現有元件高度重複、未提供額外價值的內容
- 未遵循本 repo 既有命名規則與目錄結構的檔案
- 違反 Responsible AI 原則或可能被用於惡意用途的內容

## 品質準則

- **具體明確**：避免空泛描述，提供可直接套用的具體指引或範例
- **遵循命名慣例**：檔名一律小寫、以連字號分隔（如 `code-reviewer.agent.md`）
- **聚焦單一主題**：一個檔案只涵蓋一個技術、角色或情境
- **已驗證**：Agent、Instructions、Hooks、Skills 等內容應實際測試過，確保能與 GitHub Copilot / Claude Code 正常搭配運作
- **文件同步**：新增元件後，同步更新對應的 `docs/README.X.md` 或該資料夾自身的索引表

## 如何貢獻各類型元件

各元件類型的檔案格式、frontmatter 規範與撰寫細節，請參閱上方表格對應的說明文件（例如新增 Agent 請參考 [agents/README.md](agents/README.md)）。一般流程為：

1. 在對應資料夾新增檔案（依既有命名規則）
2. 依該類型的格式規範撰寫內容（frontmatter、章節結構等）
3. 更新相關的索引表（根目錄 [README.md](README.md) 或 `docs/README.X.md`）
4. 依 [提交 Pull Request 流程](#提交-pull-request-流程) 送出貢獻

---

## 案例回饋 (Case Studies)

本 repo 的核心目標之一，是讓各專案在導入 SSDLC Agent Team 後，將實務經驗回饋回來，形成正向循環。

### 可回饋的內容

| 類型           | 說明                                                                                                             |
| -------------- | ---------------------------------------------------------------------------------------------------------------- |
| 使用心得       | 導入的背景、採用的 Agent/Instructions/Hooks/Plugins 組合、遇到的問題與解法                                       |
| 客製化後的檔案 | 依專案需求調整過的 `.agent.md`、`.instructions.md`、`.prompt.md`、Hooks 設定/腳本、Plugin 配置等                 |
| 驗證測試結果   | 使用該 Agent Team 產出的測試報告、驗收報告（可對應 [templates/ssdlc/testing/](templates/ssdlc/testing/) 的格式） |
| 成效數據       | 導入前後的量化比較（如審查耗時、缺陷密度、安全掃描發現數等），非必要但十分歡迎                                   |

### 回饋前必做檢查

⚠️ **送出前務必移除以下機敏資訊**：

- 公司 / 客戶名稱、內部網域、IP、伺服器位址
- API 金鑰、密碼、Token、憑證
- 內部專案代號、未公開的商業資訊
- 個資或任何可識別特定人員的資訊

### 如何提交案例

1. 複製 [case-studies/TEMPLATE/](case-studies/TEMPLATE/) 資料夾，重新命名為你的專案代號（小寫、連字號分隔，例如 `case-studies/ecommerce-backend-revamp/`）
2. 依範本填寫 `README.md`（專案背景、採用元件、心得）
3. 將客製化後的檔案放入對應子目錄（`agents/`、`instructions/`、`prompts/`、`hooks/`、`plugins/`），驗證報告放入 `reports/`；沒有的子目錄可以整個省略
4. 完成 [回饋前必做檢查](#回饋前必做檢查) 後，依 [提交 Pull Request 流程](#提交-pull-request-流程) 送出

詳細規範請參閱 [case-studies/README.md](case-studies/README.md)。

> 💡 若還不確定內容是否適合回饋，也可以先開一個 Issue（使用 `case-study-submission` 表單）與維護者討論，再正式送出 PR。

---

## 提交 Pull Request 流程

1. **Fork 或建立分支**：從 `main` 分支建立新分支（例如 `feat/add-xxx-agent`、`case-study/ecommerce-backend-revamp`）
2. **新增／修改檔案**：依上述規範新增元件或案例回饋
3. **自我檢查**：對照 [.github/PULL_REQUEST_TEMPLATE.md](.github/PULL_REQUEST_TEMPLATE.md) 的檢核清單逐項確認
4. **開啟 Pull Request**：目標分支為 `main`，PR 內容需包含：
   - 清楚的標題（說明新增/修改的內容）
   - 貢獻類型（元件新增 / 案例回饋 / 文件修正等）
   - 簡要說明動機與內容
5. **審查與修正**：Reviewer 會確認格式、命名、是否含機敏資訊等項目，如有問題會於 PR 中留言，請依回饋修正
6. **合併**：審查通過後，由具權限的維護者將 PR 合併回 `main`

## 審查者責任

Reviewer 於審查 PR 時應確認：

- [ ] 內容不含機敏資訊（公司/客戶名稱、金鑰、內部網域等）
- [ ] 檔案命名與位置符合本 repo 既有規則
- [ ] 格式符合該類型的規範（frontmatter、章節結構等）
- [ ] 相關索引文件（README、docs/README.X.md）已同步更新
- [ ] 案例回饋已依 [回饋前必做檢查](#回饋前必做檢查) 確認過

## 進一步考量與限制

1. **目前無 CI 自動化腳本**：不像 [github/awesome-copilot](https://github.com/github/awesome-copilot) 有 `npm run build` 可自動更新 README 索引表，本 repo 目前採**人工維護**索引（新增元件時需手動同步 README / docs/README.X.md）。未來如有需求，可再擴充 GitHub Action 進行連結有效性、命名規則等自動檢查。
2. **Issue 表單採用 classic markdown 範本**：[.github/ISSUE_TEMPLATE/case-study-submission.md](.github/ISSUE_TEMPLATE/case-study-submission.md) 使用傳統 Markdown 格式，而非 GitHub YAML issue forms，因本 repo 尚無其他表單慣例可依循，維護與修改上較為簡單直接。
