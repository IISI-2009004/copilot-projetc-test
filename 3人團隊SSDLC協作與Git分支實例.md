# 3 人團隊用 iisi-copilot 跑 SSDLC：角色任務 × Git 分支實作範例

> 文件版本：v1.0　|　建立日期：2026-05-12　|　適用對象：小型開發團隊、Tech Lead
> 搭配文件：《用 VS Code + GitHub Copilot 搭配 iisi-copilot 從零建置一個專案（SSDLC 實作指南）》
> 範例系統：**個人圖書管理系統**（Personal Book Manager，刻意做小，可被 2 位 PG 平行切分）

---

## 0. 這份文件要示範什麼

前一份指南示範「一個人」如何用各 Agent 走完 SSDLC。這份補上**團隊維度**：

> 「一個 **3 人小組**（1 位架構師兼 SA/SD、2 位 PG）怎麼分工？每個角色在每個階段做什麼？對應到 **git 分支**該怎麼開、怎麼合、怎麼處理衝突？」

重點有三條主線交織：**角色任務 ↔ 使用哪個 Copilot Agent ↔ 對應的 git 操作**。

---

## 1. 範例系統：個人圖書管理系統

| 功能 | 說明 |
|------|------|
| 書本管理 | 新增/編輯/刪除書本資訊（實體紙本或電子書），防止重複購買 |
| 閱讀記錄 | 記錄閱讀進度與時長、閱讀日曆視覺化 |
| Tag 與分類 | 使用者自訂 Tag 和分類目錄，方便整理藏書 |

技術棧沿用前一份：Java 21 + Spring Boot 3.3 + Spring Data JPA + H2/PostgreSQL。
刻意設計成兩個**低耦合模組**，讓兩位 PG 可在各自分支平行開發：

```
personal-book-manager
├── book    （模組 A：書本管理）  → PG-A 負責
└── reading （模組 B：閱讀記錄）  → PG-B 負責（依賴 book 的查詢介面）
```

---

## 2. 團隊組成、角色任務與 Agent 對應

| 成員 | 角色 | SSDLC 階段任務 | 主要驅動的 Copilot Agent |
|------|------|----------------|--------------------------|
| **Alice** | 架構師 / SA / SD（兼 Tech Lead、Reviewer、Release） | 需求分析、架構與 API 設計、任務拆解、Code Review、安全審查、整合與發布 | `Planner`、`Architect`、`Code Reviewer`、`Security Reviewer`、`Release` |
| **Bob** | PG（後端） | 實作 book 模組、寫單元測試、修審查意見 | `Backend`、`Test Generator` |
| **Carol** | PG（後端） | 實作 reading 模組（含閱讀時長統計與日曆）、寫單元測試、修審查意見 | `Backend`、`Test Generator` |

> 一人可驅動多個 Agent——Agent 是「能力角色」，不是「人」。Alice 一個人就同時扮演規劃、架構、審查、發布多種 Agent。

> **Agent 名稱對照**：本文為精簡，表格中的 `Backend`、`Code Reviewer` 等是**簡稱**；在 Copilot Chat 下拉選單裡它們的完整名稱都帶 `SSDLC` 前綴——例如 `Backend` → **`SSDLC Backend`**、`Test Generator` → **`SSDLC Test Generator`**、`Doc Writer` → **`SSDLC Doc Writer`**。選 Agent 時認 `SSDLC ...` 開頭即可。

### RACI 速覽

| 活動 | Alice(架構師) | Bob(PG) | Carol(PG) |
|------|:---:|:---:|:---:|
| 需求 / 架構 / API 設計 | **R/A** | C | C |
| 專案骨架與分支保護 | **R/A** | I | I |
| book 模組開發+測試 | C/A | **R** | I |
| reading 模組開發+測試 | C/A | I | **R** |
| Code Review / 安全審查 | **R/A** | C | C |
| 整合 develop / 發布 main | **R/A** | C | C |

R=執行 A=當責 C=諮詢 I=知會

---

## 3. Git 分支策略（簡化版 GitHub Flow + develop 整合）

3 人小組不需要複雜的 Git Flow。採用：**`main`（正式，受保護）＋ `develop`（整合）＋ `feature/*`（每人每任務一條）**。

```
main ─────●───────────────────────────────●(v1.0.0 tag)──►   只接受來自 develop 的 PR，受保護
           \                              /
develop ────●──────●──────────●──────────●──────────────►   整合分支，PR 目標
             \      \        /          /
feature/book-management    \      /(merge)   /
   (Bob) ─────●──●──●─┘     /          /
                          /          /
feature/reading-record ───●──●──●────┘
   (Carol)
```

### 分支命名與規則

| 分支 | 用途 | 誰可推送 | 合併方式 |
|------|------|---------|---------|
| `main` | 正式發布版 | 無人直接推（受保護） | 只接 `develop` 的 PR，需 Alice 核准 |
| `develop` | 整合測試 | 透過 PR | feature → develop 的 PR，需 1 人 review |
| `feature/book-management` | Bob 的工作 | Bob | 完成後對 `develop` 開 PR |
| `feature/reading-record` | Carol 的工作 | Carol | 完成後對 `develop` 開 PR |

命名慣例：`feature/<模組>-<簡述>`、`fix/<問題>`、`chore/<雜項>`。

### 分支保護（Alice 在 GitHub 設定一次）

- `main`、`develop`：禁止直接 push、禁止 force push、PR 需至少 1 個核准、需通過 CI。
- 保護 `.github/` 目錄變更需走 PR（避免有人亂改 Agent/規範）。

---

## 4. 完整流程逐步（角色任務 + git 操作）

> 以下 git 指令在 PowerShell 直接可用（git 跨 shell 一致）。每段標明**誰做、做什麼、用哪個 Agent**。

> **三位成員開工前都要先做一次環境啟用**（否則 Copilot Agent 無法直接建檔 / 跑指令）——詳見前一份指南《SSDLC 實作指南》第 2.1 節：
> 1. settings.json 加上 `github.copilot.chat.agent.runTasks`、`terminal.integrated.shellIntegration.enabled`、`terminal.integrated.defaultProfile.windows` 等 6 個鍵（團隊版已放在專案 `.vscode/settings.json`，clone 後自動生效）。
> 2. 在 VS Code 終端機各自執行一次：`Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser`（個人機器設定，無法版控分發）。
> 3. Chat 切到 **Agent 模式**並允許工具執行。
>
> Alice 應把第 2 步寫進專案 README 的「環境設定」段落，讓 Bob、Carol clone 後照做。

### Sprint 0｜架構師建立地基（Alice）

**任務**：建專案、裝 iisi-copilot 素材、做需求/設計/拆解、建立分支與保護。
以下**完整不省略**，Alice 從零把整個專案地基立起來。

> **多行指令複製貼上**：以下多行指令用 here-string ＋ `Invoke-Expression` 包起來（`@'` … `'@ | Invoke-Expression`），**整段一次複製、貼進 PowerShell 就會執行**，不會因換行或管線接續而斷掉。用單引號版 `@'`（字面值），內部 `$SRC` 會在執行時才解析；若用雙引號版 `@"` 需在每個 `$` 前加反引號 `` ` ``。

#### 步驟 0-1：建立專案骨架目錄

```powershell
@'
# 在工作目錄下建立專案（假設與 iisi-copilot-main 同層）
New-Item -ItemType Directory personal-book-manager
Set-Location personal-book-manager
git init

# 標準目錄（-Force 等同 mkdir -p，一次建好多層）
'.github/agents','.github/instructions','.github/skills','.github/prompts','.github/hooks',
'docs/requirements','docs/design','docs/architecture/adr','docs/security',
'.vscode','src','tests' | ForEach-Object { New-Item -ItemType Directory -Force $_ }
'@ | Invoke-Expression
```

#### 步驟 0-2：從 iisi-copilot 複製需要的素材

```powershell
@'
# 來源路徑（依實際位置調整）
$SRC = "..\iisi-copilot-main"

# (1) Agents：3 人團隊這個後端專案會用到的角色
'orchestrator','planner','architect','backend','test-generator',
'code-reviewer','security-reviewer','doc-writer','release' |
  ForEach-Object { Copy-Item "$SRC\agents\ssdlc\$_.agent.md" .github\agents\ }

# (2) Instructions：後端 Java、安全、測試、Code Review、PR 規範
'backend-java','security','testing','code-review','pr-description' |
  ForEach-Object { Copy-Item "$SRC\instructions\ssdlc\$_.instructions.md" .github\instructions\ }
Copy-Item "$SRC\instructions\collection\spec-driven-workflow-v1.instructions.md" .github\instructions\

# (3) 全域 Copilot Instructions（步驟 0-3 會改成本專案內容）
Copy-Item "$SRC\instructions\ssdlc\copilot-instructions.md" .github\copilot-instructions.md

# (4) Skills：產測試、安全掃描、PR 檢核、文件產生、API 審查
'junit-generator','security-review','pr-checker','doc-generator','api-reviewer' |
  ForEach-Object { Copy-Item "$SRC\skills\ssdlc\$_" .github\skills\ -Recurse }

# (5) Prompts：各階段任務範本
Copy-Item "$SRC\prompts\ssdlc\requirements\analyze-user-story.prompt.md" .github\prompts\
Copy-Item "$SRC\prompts\ssdlc\design\api-design.prompt.md"               .github\prompts\
Copy-Item "$SRC\prompts\ssdlc\coding\implement-feature.prompt.md"        .github\prompts\
Copy-Item "$SRC\prompts\ssdlc\testing\generate-unit-tests.prompt.md"     .github\prompts\
Copy-Item "$SRC\prompts\ssdlc\security\threat-model.prompt.md"           .github\prompts\
Copy-Item "$SRC\prompts\ssdlc\review\code-review.prompt.md"              .github\prompts\

# (6) Hooks：安全護欄（CLI / Cloud Agent 生效）
Copy-Item "$SRC\hooks\ssdlc\ssdlc-guardrails\hooks.json" .github\hooks\ssdlc-guardrails.json

# (7) 文件模板，放 docs 供 Agent 引用
Copy-Item "$SRC\templates\ssdlc\requirements\FRD_Template.md"     docs\requirements\
Copy-Item "$SRC\templates\ssdlc\requirements\UseCase_Template.md" docs\requirements\
Copy-Item "$SRC\templates\ssdlc\design\SDD_Template.md"           docs\design\
Copy-Item "$SRC\templates\ssdlc\design\API_Spec_Template.md"      docs\design\
Copy-Item "$SRC\templates\ssdlc\testing\TestPlan_Template.md"     docs\
'@ | Invoke-Expression
```

#### 步驟 0-2b：啟用「產出落檔」，讓 Agent 執行完自動建文件

預設 Agent 只把結果回在聊天視窗、不會建檔。Alice 做下面兩件事，讓全隊的 Agent 跑完**自動把產出寫成檔案**（細節與原理見《SSDLC 實作指南》第 3.6 節）：

```powershell
# (a) 建立全域落檔規則（applyTo:'**' 自動套用到所有對話）
@'
---
description: 規定所有 SSDLC Agent 產出必須直接寫入檔案，而非僅顯示於聊天視窗
applyTo: "**"
---
# 產出落檔規則（Auto-Persist Output）

## 強制規則
- 任何階段性產出完成後，必須用 createFile/editFiles 直接寫入對應路徑，不可只回聊天視窗。
- 目標檔已存在則用 editFiles 更新並保留章節結構；寫檔後回覆已建立/更新的路徑。
- 檔名含日期時用 YYYYMMDD。

## 階段 → 產出路徑（涵蓋本專案會用到的所有 Agent，無遺漏）
| 階段 / Agent | 產出檔案 |
|--------------|---------|
| Orchestrator | docs/orchestrator-plan.md |
| Planner | docs/requirements/requirements.md |
| Architect（設計） | docs/design/design.md |
| Architect（拆解） | docs/design/tasks.md |
| Architect（威脅建模） | docs/security/threat-model.md |
| Backend | src/**（程式碼）＋ docs/design/implementation-notes.md |
| Test Generator | src/test/** ＋ docs/TestReport.md |
| Code Reviewer | docs/review/code-review-YYYYMMDD.md |
| Security Reviewer | docs/security/security-review-YYYYMMDD.md |
| Doc Writer | README.md、docs/api/*.md |
| Release | CHANGELOG.md、docs/ReleaseNote.md |
'@ | Set-Content -Encoding utf8 .github\instructions\auto-persist.instructions.md

# (b) 給每個 Agent 補上寫檔工具（在 tools: 區塊加入 edit/createFile、edit/editFiles）
#     逐檔編輯 .github\agents\*.agent.md，於 tools: 下新增兩行：
#       - "edit/createFile"
#       - "edit/editFiles"
#     ★ 檢查「還沒加」的 agent（輸出為空＝全部都有、沒有遺漏）：
Get-ChildItem .github\agents\*.agent.md | Where-Object {
  -not (Select-String -Path $_.FullName -Pattern "createFile" -Quiet)
} | Select-Object Name
```

同時在每個 `*.prompt.md` 結尾加「## 產出落檔」指定確切檔名（對照上表），Orchestrator 的 agent 檔也加上落檔規則寫入 `docs/orchestrator-plan.md`。完成後三位成員在 **Agent 模式**執行任何 Prompt，文件都會自動出現在檔案總管。

> 因為這些修改都在 `.github/` 內、隨 Git 分發，**Bob、Carol clone 後立即享有相同行為**，不需各自重設。

#### 步驟 0-3：改寫全域指令 `.github/copilot-instructions.md`（個人圖書管理系統版）

複製來的範本是「電商平台」內容，**必須改成本專案的真實設定**。用編輯器把內容換成：

```markdown
# 專案 Copilot Instructions

## 專案概述
個人圖書管理系統（Personal Book Manager），提供個人藏書管理與閱讀記錄。
模組：book（書本管理，含 Tag 與分類）、reading（閱讀記錄與日曆）。
技術棧：Java 21 + Spring Boot 3.3 + Spring Data JPA + H2(開發)/PostgreSQL(正式)，Maven 建置。

## 程式碼規範
- 分層架構：Controller → Service → Repository
- 命名：類別 PascalCase、方法/變數 camelCase、常數 UPPER_SNAKE_CASE
- 所有 public 方法需 JavaDoc
- 使用 Bean Validation 驗證輸入；自訂 Exception 處理業務例外
- 日誌用 SLF4J，禁止輸出個資/密碼/Token

## 模組邊界（重要）
- book 模組對外只暴露介面 `BookQueryPort.existsBook(bookId): boolean`
- reading 記錄新增前必須呼叫該介面確認書本存在
- 兩模組各自獨立資料夾，禁止互相直接存取對方 Repository

## 安全規範
- 所有外部輸入必須驗證；JPA 參數化查詢避免注入
- 書本 ISBN 去重檢查，避免使用者重複購買實體書
- 錯誤訊息不可洩漏堆疊細節給前端

## 測試規範
- JUnit 5 + Mockito + AssertJ，覆蓋率 ≥ 80%
- 測試命名：should_預期行為_When_條件
- reading 必含閱讀時長計算、日期邊界測試

## 工作流程
- 遵循 spec-driven-workflow：維護 requirements.md / design.md / tasks.md 三件工件
- 所有變更走 feature 分支 → PR → 審查 → develop → main
```

#### 步驟 0-4：建立團隊共用 VS Code 設定

把可分發的設定放進 `.vscode/settings.json`（讓 Bob、Carol clone 後自動具備 Agent 建檔能力）：

> ⚠️ 這是 **JSON 設定內容，存成專案的 `.vscode/settings.json` 檔案**（不是貼進 PowerShell 終端機）。

```json
{
  "github.copilot.chat.codeGeneration.useInstructionFiles": true,
  "chat.promptFiles": true,
  "files.eol": "\n",
  "github.copilot.chat.agent.runTasks": true,
  "terminal.integrated.shellIntegration.enabled": true,
  "terminal.integrated.defaultProfile.windows": "PowerShell"
}
```

> 提醒：`Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser` 是每台機器的個人設定，無法版控分發——Alice 要寫進 README，Bob、Carol clone 後各自執行一次（見本章開頭的環境啟用提示）。

#### 步驟 0-5：建立 .gitignore 並首次提交

```powershell
# 產生 Java/Maven 用的 .gitignore（避免把 target/、IDE 暫存檔簽入）
@'
target/
*.class
.idea/
*.iml
.vscode/*.log
!/.vscode/settings.json
'@ | Set-Content -Encoding utf8 .gitignore

git add .
git commit -m "chore: init project + install iisi-copilot SSDLC assets"
```

#### 步驟 0-6：推到 GitHub 並建立分支

```powershell
# 推 main
git branch -M main
git remote add origin https://github.com/<org>/personal-book-manager.git
git push -u origin main

# 從 main 開出 develop 整合分支
git switch -c develop
git push -u origin develop
```

#### 步驟 0-7：設定分支保護（GitHub 網頁，Alice 操作一次）

到 GitHub repo → Settings → Branches → Add rule，對 `main` 與 `develop`：

- ☑ Require a pull request before merging（至少 1 個 approval）
- ☑ Require status checks to pass（CI 綠燈才可合）
- ☑ Do not allow force pushes、Do not allow deletions
- （選用）對 `.github/**` 套用 CODEOWNERS，變更需 Alice 核准

**接著在 VS Code + Copilot Chat 做設計（產出文件，commit 到 develop）：**

| Agent | Alice 的動作 | 產出 |
|-------|-------------|------|
| `Planner` | `/analyze-user-story` 分析個人圖書管理需求 | `docs/requirements/requirements.md` |
| `Architect` | `/api-design` 設計 book / reading 兩模組 API、STRIDE | `docs/design/design.md` |
| `Architect` | 拆任務、標記負責人 | `docs/design/tasks.md` |

`tasks.md` 範例（明確切給兩位 PG）：

```markdown
# tasks.md
## 模組 A：book（負責人 Bob，分支 feature/book-management）
- [ ] A1 Book Entity + Repository（含 ISBN、書名、作者、類型、實體/電子書別）
- [ ] A2 BookService（新增/編輯/刪除 + ISBN 重複購買檢查）
- [ ] A3 BookController + Bean Validation
- [ ] A4 TagService / CategoryService（自訂 Tag 與分類目錄的 CRUD）
- [ ] A5 BookService/Controller/TagService 單元測試 ≥ 80%

## 模組 B：reading（負責人 Carol，分支 feature/reading-record）
- [ ] B1 ReadingRecord Entity + Repository（含書本ID、開始日期、閱讀時長、進度）
- [ ] B2 ReadingService（新增/更新閱讀記錄 + 閱讀時長累計）
- [ ] B3 ReadingCalendarService（依日期彙整閱讀時長，供日曆視覺化）
- [ ] B4 ReadingController + Bean Validation
- [ ] B5 閱讀時長計算、日期邊界測試 ≥ 80%

## 介面契約（Alice 先定，避免兩人卡住）
- book 模組提供 `BookQueryPort.existsBook(bookId): boolean`
  → reading 新增記錄前呼叫，確認書本存在
```

```powershell
git add docs/
git commit -m "docs: requirements, design, tasks for book & reading"
git push origin develop
```

> 關鍵：Alice **先把介面契約（`BookQueryPort`）定下來**，兩位 PG 才能各自對著契約開發、不互相阻塞。

---

### Sprint 1｜兩位 PG 平行開發

> 兩位 PG 做法相同，差別只在模組與 prompt 內容。每人流程都是 **①開分支 → ②用 Backend Agent 實作 → ③用 Test Generator Agent 補測試 → ④本機驗證 → ⑤commit/push**。

#### Bob（book 模組）

**① 從最新 develop 開自己的 feature 分支**

```powershell
git switch develop
git pull origin develop
git switch -c feature/book-management
```

**② 用 `SSDLC Backend` Agent 實作 book 模組**

> book 模組重點（供理解，**不必抄進 prompt**）：書本 CRUD（實體書/電子書）、ISBN 重複購買檢查、自訂 Tag 與分類目錄，並實作 reading 會用到的對外契約 `BookQueryPort`。這些細節都已寫在 `design.md`／`tasks.md`——所以 prompt 只要叫 Agent 去讀、以文件為準即可，不要把欄位/端點寫死（每次 plan 產出可能不同）。

在 VS Code Chat 切到 **Agent 模式** → 選 **`SSDLC Backend`** → 引用 `implement-feature`：

```
/implement-feature

讀取 docs/design/tasks.md 與 docs/design/design.md，實作 book 模組中所有「尚未勾選（[ ]）」的任務。
以 design.md 為單一真實來源：實體欄位、端點、以及對外契約（如 BookQueryPort）都以文件定義為準，不要自行假設。
遵守 copilot-instructions 與 backend-java.instructions（分層、JavaDoc、自訂 Exception、SLF4J、JPA 參數化查詢）。
程式建立於 src/main/java/.../book/，完成後把 tasks.md 對應項目改為 [x]。
```

Backend Agent 會依 `design.md` 內容**自動建立對應的分層程式**（實體、Repository、Service、Controller、契約實作、全域例外處理等）——**實際檔名與數量由文件決定**，並寫 `docs/design/implementation-notes.md`。Bob 逐項核對 `tasks.md` 打勾。

**③ 用 `SSDLC Test Generator` Agent 補測試**

Chat 換 **`SSDLC Test Generator`**（自動載入 `junit-generator` Skill）：

```
/generate-unit-tests

為 src/ 下 book 模組這次實作的 Service 與 Controller 類別產生 JUnit 5 測試
（依 tasks.md 的測試任務與實際存在的類別，不要假設類別名稱）。
每個 public 方法至少 3 個案例（Happy / Boundary / Error），Mockito mock 相依、
AssertJ 斷言，命名 should_xxx_When_yyy，覆蓋率 ≥ 80%。
```

測試碼自動落檔 `src/test/`。

**④⑤ 本機驗證後 commit / push**

```powershell
mvn test                       # 本機綠燈
git add src/
git commit -m "feat(book): Book entity, service, BookQueryPort, controller + Tag/Category"
git add src/test/
git commit -m "test(book): unit tests for BookService/Controller/TagService (cov 85%)"
git push -u origin feature/book-management   # 小步推送，建議每天
```

#### Carol（reading 模組）— 同時進行

**① 開分支**

```powershell
git switch develop
git pull origin develop
git switch -c feature/reading-record
```

**② 用 `SSDLC Backend` Agent 實作 reading 模組**（Agent 模式 → `SSDLC Backend`）

> reading 模組重點（供理解，**不必抄進 prompt**）：新增/更新閱讀記錄（進度與時長）、閱讀時長累計、依日期彙整的閱讀日曆。新增記錄前透過 `BookQueryPort` 確認書本存在，**核心是閱讀時長的正確計算與累計**。這些規則都定義在 `design.md`，prompt 只要叫 Agent 讀文件、以文件為準。

```
/implement-feature

讀取 docs/design/tasks.md 與 docs/design/design.md，實作 reading 模組中所有「尚未勾選（[ ]）」的任務。
以 design.md 為單一真實來源（實體、端點、閱讀時長規則、以及要呼叫的 BookQueryPort 契約都以文件為準）。
只透過 design.md 定義的契約與 book 模組互動，禁止直接存取 book 的 Repository。
遵守 copilot-instructions 與 backend-java.instructions。程式建立於 src/main/java/.../reading/，完成後更新 tasks.md。
```

> Carol 這裡的重點是**對著 `design.md` 定義的 `BookQueryPort` 契約開發**——即使 Bob 的 book 模組還沒合併，Carol 也能先以介面／mock 開發，不被卡住。

**③ 用 `SSDLC Test Generator` Agent 補測試**

```
/generate-unit-tests

為 src/ 下 reading 模組的 Service 類別產生測試，依 design.md 的閱讀時長規則涵蓋邊界情境
（如跨日閱讀、時長為零、書本不存在、累計計算等，以文件定義為準）。
Mockito mock 相依（含 BookQueryPort）、AssertJ，覆蓋率 ≥ 80%。
```

**④⑤ 驗證後 commit / push**

```powershell
mvn test
git add src/
git commit -m "feat(reading): reading record via BookQueryPort + duration accumulation + calendar"
git add src/test/
git commit -m "test(reading): duration calc/cross-day/no-book/calendar boundary cases (cov 88%)"
git push -u origin feature/reading-record
```

> 兩條 feature 分支互不干擾，各自獨立 commit/push；因為 book / reading 在不同資料夾、且只透過 `BookQueryPort` 契約互動，平行開發幾乎不會踩到對方的檔案。

---

### Sprint 1 收尾｜開 PR、Review、整合到 develop

#### Bob 先完成，對 develop 開 PR

```powershell
# 開 PR 前先把 develop 的新進度併回自己分支，本機先解掉衝突
git switch feature/book-management
git fetch origin
git merge origin/develop          # 或 git rebase origin/develop
mvn test
git push origin feature/book-management

# 開 PR（gh CLI）
gh pr create --base develop --head feature/book-management `
  --title "feat(book): book management module" --fill
```

PR 觸發 **`pr-checker` Skill** 自動檢核，並套用 `pr-description.instructions.md` 格式。

**Alice 做 Code Review + 安全審查**（Reviewer 角色）：

| Agent | 動作 |
|-------|------|
| `Code Reviewer` | `/code-review` 對照分層、命名、JavaDoc、例外處理 |
| `Security Reviewer` | `/threat-model` 跑 OWASP / `security-review` Skill |

審查有意見 → Bob 在**同一條 feature 分支**修正、再 push，PR 自動更新：

```powershell
git switch feature/book-management
# ...依審查意見用 Backend Agent 修正...
git commit -am "fix(book): address review - ISBN validation & error mapping"
git push origin feature/book-management
```

審查通過 → Alice 核准並合併：

```powershell
gh pr merge feature/book-management --merge --delete-branch   # 合進 develop 並刪遠端分支
```

#### Carol 接著開 PR（此時 develop 已含 Bob 的 book 模組）

```powershell
git switch feature/reading-record
git fetch origin
git merge origin/develop          # 把 Bob 的 book 併進來，整合真正的 BookQueryPort 實作
# 若有衝突 → 見第 5 章解衝突流程
mvn test                          # 整合後再跑一次，確認 reading 串得起 book
git push origin feature/reading-record
gh pr create --base develop --head feature/reading-record --title "feat(reading): reading record module" --fill
```

Alice 同樣審查 → 通過 → 合併。**此時 develop 已是 book + reading 整合完成的可運行版本。**

---

### Sprint 1 發布｜整合測試 → main → tag（Alice）

```powershell
git switch develop
git pull origin develop
mvn verify                        # 整合/驗收測試全綠

# 對 main 開發布 PR
gh pr create --base main --head develop --title "release: v1.0.0" --fill
```

用 **`Release` Agent** 產 CHANGELOG / Release Note、檢查發布安全閘門（測試通過、無高風險弱點、文件齊）。
用 **`SSDLC Doc Writer` Agent** 補 README / API 文件。

```powershell
gh pr merge develop --merge                # 合進 main
git switch main
git pull origin main
git tag -a v1.0.0 -m "Personal Book Manager v1.0.0"
git push origin v1.0.0
```

---

## 5. 衝突處理範例（兩人改到同一檔）

情境：Bob 與 Carol 都動了 `application.yml` 或共用設定，Carol merge develop 時出現衝突。

```powershell
git switch feature/reading-record
git fetch origin
git merge origin/develop
# CONFLICT (content): Merge conflict in src/main/resources/application.yml
```

處理步驟：

1. 在 VS Code 開衝突檔，會看到 `<<<<<<<`、`=======`、`>>>>>>>` 標記。
2. **可請 Copilot Chat 協助**：選一般 Chat，貼上衝突段落問「兩邊都要保留，幫我合併這段 yaml」。
3. 確認保留正確內容、刪掉衝突標記。

```powershell
git add src/main/resources/application.yml
mvn test                          # 確認合併後仍綠燈
git commit                        # 完成 merge commit（沿用預設訊息即可）
git push origin feature/reading-record
```

> 預防勝於治療：**每天 `git pull origin develop` 併回自己分支**，小步整合，衝突就小。模組邊界由 Alice 在 Sprint 0 切乾淨（book / reading 各自資料夾），是減少衝突的根本。

---

## 6. 角色 × Agent × Git 全流程對照表

| 階段 | 誰 | Copilot Agent | Git 操作 |
|------|----|----|----|
| 專案初始化 | Alice | — | `git init` → push `main` → 開 `develop` |
| 需求分析 | Alice | `Planner` | commit `requirements.md` 到 develop |
| 架構/API 設計 | Alice | `Architect` | commit `design.md` / `tasks.md` |
| 分支保護設定 | Alice | — | GitHub 設定 `main`/`develop` 保護 |
| book 開發 | Bob | `Backend`,`Test Generator` | `feature/book-management` 上 commit/push |
| reading 開發 | Carol | `Backend`,`Test Generator` | `feature/reading-record` 上 commit/push |
| 開 PR | Bob/Carol | （`pr-checker`） | `gh pr create --base develop` |
| Code/安全審查 | Alice | `Code Reviewer`,`Security Reviewer` | PR 上留言；PG 修正後再 push |
| 合併整合 | Alice | — | `gh pr merge --delete-branch` 進 develop |
| 發布 | Alice | `Release`,`Doc Writer` | develop→main PR、`git tag v1.0.0` |

---

## 7. 給小團隊的最佳實務

1. **介面契約先行**：Alice 在 Sprint 0 把模組邊界與介面（如 `BookQueryPort`）定死，兩位 PG 才能真正平行、不互卡。
2. **一條 feature 分支只做一個任務**，commit 訊息用 Conventional Commits（`feat:`/`fix:`/`test:`/`docs:`）。
3. **每天併回 develop**，小步整合，把衝突壓到最小。
4. **PR 不繞過審查**：`main`/`develop` 受保護，所有變更走 PR，Alice 至少核准 1 次。
5. **安全審查是閘門**：`Security Reviewer` 沒過，不准合併。
6. **修審查意見回原 feature 分支**再 push，PR 會自動更新，不要另開分支。
7. `.github/`（Agent/規範/Hook）視為團隊資產，變更同樣走 PR 審核。
8. **Agent 是角色不是人**：一個人（Alice）可依當下任務切換多個 Agent；兩位 PG 用同一組 `Backend`/`Test Generator` Agent，因為規範都寫在共用的 instructions 裡，產出風格自然一致。

---

> 把「個人圖書管理系統」換成你真正的系統、把模組數量對應到團隊人數，這套「**契約先行 → feature 分支平行開發 → PR + AI 審查 → develop 整合 → main 發布**」的節奏完全可複製。
