# 3 人團隊用 Copilot CLI 跑 SSDLC：角色任務 × Git 分支實作範例

> 文件版本：v1.0　|　建立日期：2026-07-08　|　適用對象：小型開發團隊、Tech Lead
> 搭配文件：《用 Copilot CLI 搭配 iisi-copilot 從零建置一個專案（SSDLC 實作指南）》（`copilot_cli_ssdlc_guide_20260708.md`）
> 範例系統：**會議室預約 API**（Meeting Room Reservation，可被 2 位 PG 平行切分）
> **本文與原始指南的差異**：將所有 VS Code + Copilot Chat Agent 模式的操作，全部替換為 **Copilot CLI 終端機指令**。

---

## 0. 這份文件要示範什麼

前一份指南示範「一個人」如何用 CLI 走完 SSDLC。這份補上**團隊維度**：

> 「一個 **3 人小組**（1 位架構師兼 SA/SD、2 位 PG）怎麼分工？每個角色在每個階段做什麼？對應到 **git 分支**該怎麼開、怎麼合、怎麼處理衝突？」

三條主線交織：**角色任務 ↔ CLI 角色宣告指令 ↔ git 操作**。

---

## 1. 範例系統：會議室預約 API

| 功能 | 說明 |
|------|------|
| 會議室管理 | 新增/查詢/停用會議室（room） |
| 預約管理 | 預約/取消/查詢時段，且**不可時間重疊** |

技術棧：Java 21 + Spring Boot 3.3 + Spring Data JPA + H2/PostgreSQL。
兩個**低耦合模組**，讓兩位 PG 可在各自分支平行開發：

```
room-booking-api
├── room        （模組 A：會議室）  → PG-A 負責
└── reservation （模組 B：預約）    → PG-B 負責（依賴 room 的查詢介面）
```

---

## 2. 團隊組成、角色任務與 Agent 對應

| 成員 | 角色 | SSDLC 階段任務 | Copilot CLI 宣告角色 |
|------|------|----------------|---------------------|
| **Alice** | 架構師 / SA / SD（兼 Tech Lead、Reviewer、Release） | 需求分析、架構/API 設計、任務拆解、Code Review、安全審查、整合與發布 | `Planner`、`Architect`、`Code Reviewer`、`Security Reviewer`、`Release` |
| **Bob** | PG（後端） | 實作 room 模組、寫單元測試、修審查意見 | `Backend`、`Test Generator` |
| **Carol** | PG（後端） | 實作 reservation 模組（含時段衝突檢查）、寫單元測試、修審查意見 | `Backend`、`Test Generator` |

> **CLI 角色宣告說明**：表格中的 `Backend`、`Code Reviewer` 等是角色簡稱，對應 `.github/agents/backend.agent.md`、`.github/agents/code-reviewer.agent.md` 等檔案。在 CLI 對話開頭以「你是 SSDLC Backend（參考 `.github/agents/backend.agent.md`）」宣告即可，效果等同 VS Code 的下拉選單。

### RACI 速覽

| 活動 | Alice(架構師) | Bob(PG) | Carol(PG) |
|------|:---:|:---:|:---:|
| 需求 / 架構 / API 設計 | **R/A** | C | C |
| 專案骨架與分支保護 | **R/A** | I | I |
| room 模組開發+測試 | C/A | **R** | I |
| reservation 模組開發+測試 | C/A | I | **R** |
| Code Review / 安全審查 | **R/A** | C | C |
| 整合 develop / 發布 main | **R/A** | C | C |

R=執行 A=當責 C=諮詢 I=知會

---

## 3. Git 分支策略（簡化版 GitHub Flow + develop 整合）

```
main ─────●───────────────────────────────●(v1.0.0 tag)──►   只接來自 develop 的 PR，受保護
           \                              /
develop ────●──────●──────────●──────────●──────────────►   整合分支，PR 目標
             \      \        /          /
feature/room-crud    \      /(merge)   /
   (Bob) ─────●──●──●─┘     /          /
                           /          /
feature/reservation ─────●──●──●────┘
   (Carol)
```

### 分支命名與規則

| 分支 | 用途 | 誰可推送 | 合併方式 |
|------|------|---------|---------|
| `main` | 正式發布版 | 無人直接推（受保護） | 只接 `develop` 的 PR，需 Alice 核准 |
| `develop` | 整合測試 | 透過 PR | feature → develop 的 PR，需 1 人 review |
| `feature/room-crud` | Bob 的工作 | Bob | 完成後對 `develop` 開 PR |
| `feature/reservation` | Carol 的工作 | Carol | 完成後對 `develop` 開 PR |

命名慣例：`feature/<模組>-<簡述>`、`fix/<問題>`、`chore/<雜項>`。

### 分支保護（Alice 在 GitHub 設定一次）

- `main`、`develop`：禁止直接 push、禁止 force push、PR 需至少 1 個核准、需通過 CI。
- 保護 `.github/` 目錄變更需走 PR（避免有人亂改 Agent/規範）。

---

## 4. 完整流程逐步（角色任務 + git 操作）

> **CLI 核心操作模式**（取代 VS Code Agent 下拉選單與斜線指令）：
>
> | VS Code Chat | Copilot CLI 對應做法 |
> |-------------|---------------------|
> | 下拉選單切換到 `SSDLC Backend` | 對話開頭宣告：`你是 SSDLC Backend（參考 .github/agents/backend.agent.md）` |
> | `/implement-feature` 斜線指令 | 宣告：`請依照 .github/prompts/implement-feature.prompt.md 的格式` |
> | Skills 自動載入 | 明確宣告：`請參考 .github/skills/junit-generator/SKILL.md 的規則` |
>
> **三位成員開工前的環境確認**（CLI 比 VS Code 更簡單）：
> 1. 確認 Copilot CLI 已安裝並登入。
> 2. 確認工作目錄在專案根目錄（`cd room-booking-api`）。
> 3. 無需任何 settings.json 設定——`.github/copilot-instructions.md` 與 instructions 自動生效。
>
> Alice 應把以上寫進專案 README 的「環境設定」段落。

### Sprint 0｜架構師建立地基（Alice）

**任務**：建專案、裝 iisi-copilot 素材、做需求/設計/拆解、建立分支與保護。

#### 步驟 0-1：建立專案骨架目錄

```powershell
New-Item -ItemType Directory room-booking-api
Set-Location room-booking-api
git init

'.github/agents','.github/instructions','.github/skills','.github/prompts','.github/hooks',
'docs/requirements','docs/design','docs/architecture/adr','docs/security',
'src','tests' | ForEach-Object { New-Item -ItemType Directory -Force $_ }
```

#### 步驟 0-2：從 iisi-copilot 複製需要的素材

```powershell
$SRC = "..\iisi-copilot"

# (1) Agents（CLI 下做為角色宣告參考文件）
'orchestrator','planner','architect','backend','test-generator',
'code-reviewer','security-reviewer','doc-writer','release' |
  ForEach-Object { Copy-Item "$SRC\agents\ssdlc\$_.agent.md" .github\agents\ }

# (2) Instructions（自動套用的規範）
'backend-java','security','testing','code-review','pr-description' |
  ForEach-Object { Copy-Item "$SRC\instructions\ssdlc\$_.instructions.md" .github\instructions\ }
Copy-Item "$SRC\instructions\collection\spec-driven-workflow-v1.instructions.md" .github\instructions\

# (3) 全域 Copilot Instructions（步驟 0-3 改寫）
Copy-Item "$SRC\instructions\ssdlc\copilot-instructions.md" .github\copilot-instructions.md

# (4) Skills（CLI 下做為技能引用參考文件）
'junit-generator','security-review','pr-checker','doc-generator','api-reviewer' |
  ForEach-Object { Copy-Item "$SRC\skills\ssdlc\$_" .github\skills\ -Recurse }

# (5) Prompts（CLI 下做為格式引用範本）
Copy-Item "$SRC\prompts\ssdlc\requirements\analyze-user-story.prompt.md" .github\prompts\
Copy-Item "$SRC\prompts\ssdlc\design\api-design.prompt.md"               .github\prompts\
Copy-Item "$SRC\prompts\ssdlc\coding\implement-feature.prompt.md"        .github\prompts\
Copy-Item "$SRC\prompts\ssdlc\testing\generate-unit-tests.prompt.md"     .github\prompts\
Copy-Item "$SRC\prompts\ssdlc\security\threat-model.prompt.md"           .github\prompts\
Copy-Item "$SRC\prompts\ssdlc\review\code-review.prompt.md"              .github\prompts\

# (6) Hooks（CLI 原生支援，自動載入）
Copy-Item "$SRC\hooks\ssdlc\ssdlc-guardrails\hooks.json" .github\hooks\ssdlc-guardrails.json

# (7) 文件模板
Copy-Item "$SRC\templates\ssdlc\requirements\FRD_Template.md"     docs\requirements\
Copy-Item "$SRC\templates\ssdlc\requirements\UseCase_Template.md" docs\requirements\
Copy-Item "$SRC\templates\ssdlc\design\SDD_Template.md"           docs\design\
Copy-Item "$SRC\templates\ssdlc\design\API_Spec_Template.md"      docs\design\
Copy-Item "$SRC\templates\ssdlc\testing\TestPlan_Template.md"     docs\
```

#### 步驟 0-2b：啟用「產出落檔」，讓 CLI 對話完成後自動建文件

> **CLI 差異**：原始指南的「產出落檔」是透過 VS Code 的 `edit/createFile` 工具與 Agent 模式觸發。在 CLI 下，落檔是**天生支援**的——只要指令結尾加上「完成後寫入 [路徑]」，CLI 就會直接建立檔案，不需要額外設定。
>
> 以下的 `auto-persist.instructions.md` 做為**全域提醒規則**（`applyTo: '**'`），確保任何成員在 CLI 對話中都自動收到落檔指示。

```powershell
@'
---
description: 規定所有 SSDLC 產出必須直接寫入檔案，而非僅顯示於對話視窗
applyTo: "**"
---
# 產出落檔規則（Auto-Persist Output）

## 強制規則
- 任何階段性產出完成後，必須直接寫入對應路徑，不可只回在對話視窗。
- 目標檔已存在則更新並保留章節結構；寫檔後回覆已建立/更新的路徑。
- 檔名含日期時用 YYYYMMDD。

## 階段 → 產出路徑（涵蓋本專案所有 Agent）
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
```

> 因為這些修改都在 `.github/` 內、隨 Git 分發，**Bob、Carol clone 後立即享有相同行為**，不需各自重設。

#### 步驟 0-3：改寫全域指令 `.github/copilot-instructions.md`（會議室預約版）

```powershell
@'
# 專案 Copilot Instructions

## 專案概述
會議室預約 API（Meeting Room Reservation），提供會議室管理與時段預約。
模組：room（會議室）、reservation（預約，需防時段重疊）。
技術棧：Java 21 + Spring Boot 3.3 + Spring Data JPA + H2(開發)/PostgreSQL(正式)，Maven 建置。

## 程式碼規範
- 分層架構：Controller → Service → Repository
- 命名：類別 PascalCase、方法/變數 camelCase、常數 UPPER_SNAKE_CASE
- 所有 public 方法需 JavaDoc
- 使用 Bean Validation 驗證輸入；自訂 Exception 處理業務例外
- 日誌用 SLF4J，禁止輸出個資/密碼/Token

## 模組邊界（重要）
- room 模組對外只暴露介面 RoomQueryPort.existsActiveRoom(roomId): boolean
- reservation 預約前必須呼叫該介面確認會議室存在且啟用
- 兩模組各自獨立資料夾，禁止互相直接存取對方 Repository

## 安全規範
- 所有外部輸入必須驗證；JPA 參數化查詢避免注入
- 時段預約需檢查重疊，避免重複佔用（業務完整性）
- 錯誤訊息不可洩漏堆疊細節給前端

## 測試規範
- JUnit 5 + Mockito + AssertJ，覆蓋率 ≥ 80%
- 測試命名：should_預期行為_When_條件
- reservation 必含重疊/相鄰/跨日邊界測試

## 工作流程
- 遵循 spec-driven-workflow：維護 requirements.md / design.md / tasks.md 三件工件
- 所有變更走 feature 分支 → PR → 審查 → develop → main
'@ | Set-Content -Encoding utf8 .github\copilot-instructions.md
```

> **CLI 差異**：不需 `.vscode/settings.json`。這份 `copilot-instructions.md` 在 CLI 對話中自動生效。若團隊成員同時使用 VS Code，可另外建立 `.vscode/settings.json`（內容見原始指南 3.4），但不是 CLI 必要條件。

#### 步驟 0-4：建立 .gitignore 並首次提交

```powershell
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

#### 步驟 0-5：推到 GitHub 並建立分支

```powershell
git branch -M main
git remote add origin https://github.com/<org>/room-booking-api.git
git push -u origin main

git switch -c develop
git push -u origin develop
```

#### 步驟 0-6：設定分支保護（GitHub 網頁，Alice 操作一次）

到 GitHub repo → Settings → Branches → Add rule，對 `main` 與 `develop`：

- ☑ Require a pull request before merging（至少 1 個 approval）
- ☑ Require status checks to pass（CI 綠燈才可合）
- ☑ Do not allow force pushes、Do not allow deletions

#### 步驟 0-7：需求分析與架構設計（Alice 在 CLI 操作）

**需求分析**（CLI 指令）：

```
你是 SSDLC Planner（參考 .github/agents/planner.agent.md）。
請依照 .github/prompts/analyze-user-story.prompt.md 的格式，分析以下需求：

會議室預約系統：管理員可新增/查詢/停用會議室，使用者可預約/取消/查詢時段，
時段不可重疊（同一會議室同時段只能有一筆有效預約）。
請輸出需求摘要、User Story（EARS/INVEST）、任務清單與 OWASP 安全考量。
完成後寫入 docs/requirements/requirements.md。
```

**架構與 API 設計**（CLI 指令）：

```
你是 SSDLC Architect（參考 .github/agents/architect.agent.md）。
請依照 .github/prompts/api-design.prompt.md 的格式，
根據 docs/requirements/requirements.md，設計 room / reservation 兩模組 API：
- 資源、端點、HTTP 方法、請求/回應 schema、狀態碼
- 分層架構與資料模型
- 定義 RoomQueryPort 介面契約（reservation 要呼叫的對外介面）
- 用 STRIDE 做威脅建模
請依 docs/design/SDD_Template.md 與 docs/design/API_Spec_Template.md 的結構輸出。
完成後設計寫入 docs/design/design.md，威脅建模寫入 docs/security/threat-model.md。
```

**任務拆解**（CLI 指令）：

```
你是 SSDLC Architect。
根據 docs/design/design.md，把實作步驟拆成可追蹤的任務清單，
明確標記每個任務的負責人（Bob→room、Carol→reservation）與相依順序，
寫入 docs/design/tasks.md（Markdown checkbox 格式）。
```

`tasks.md` 範例：

```markdown
# tasks.md
## 模組 A：room（負責人 Bob，分支 feature/room-crud）
- [ ] A1 Room Entity + Repository
- [ ] A2 RoomService（新增/查詢/停用）
- [ ] A3 RoomController + Bean Validation
- [ ] A4 RoomService/Controller 單元測試 ≥ 80%

## 模組 B：reservation（負責人 Carol，分支 feature/reservation）
- [ ] B1 Reservation Entity + Repository
- [ ] B2 ReservationService（預約/取消/查詢 + 時段衝突檢查）
- [ ] B3 ReservationController + Bean Validation
- [ ] B4 衝突情境測試（重疊/相鄰/跨日）≥ 80%

## 介面契約（Alice 先定，避免兩人卡住）
- room 模組提供 RoomQueryPort.existsActiveRoom(roomId): boolean
  → reservation 預約前呼叫，確認會議室存在且啟用
```

```powershell
git add docs/
git commit -m "docs: requirements, design, tasks for room & reservation"
git push origin develop
```

> **關鍵**：Alice **先把介面契約（`RoomQueryPort`）定下來**，兩位 PG 才能各自對著契約開發、不互相阻塞。

---

### Sprint 1｜兩位 PG 平行開發

> 兩位 PG 做法相同，差別只在模組與 CLI 指令內容。每人流程都是 **①開分支 → ②CLI 角色宣告實作 → ③CLI 角色宣告補測試 → ④本機驗證 → ⑤commit/push**。

#### Bob（room 模組）

**① 從最新 develop 開自己的 feature 分支**

```powershell
git switch develop
git pull origin develop
git switch -c feature/room-crud
```

**② 用 CLI 以 Backend 角色實作 room 模組**

> room 模組重點（供理解，**不必抄進 CLI 指令**）：會議室 CRUD + 停用，並實作 reservation 會用到的對外契約 `RoomQueryPort`。這些細節都已寫在 `design.md`——CLI 指令只要叫 AI 讀文件、以文件為準即可。

```
你是 SSDLC Backend（參考 .github/agents/backend.agent.md）。
請依照 .github/prompts/implement-feature.prompt.md 的格式，
讀取 docs/design/tasks.md 與 docs/design/design.md，
實作 room 模組中所有「尚未勾選（[ ]）」的任務。
以 design.md 為單一真實來源：實體欄位、端點、以及對外契約（如 RoomQueryPort）都以文件定義為準，不要自行假設。
遵守 .github/instructions/backend-java.instructions.md 的規範（分層、JavaDoc、自訂 Exception、SLF4J、JPA 參數化查詢）。
程式建立於 src/main/java/.../room/，完成後把 tasks.md 對應項目改為 [x]，變更摘要寫入 docs/design/implementation-notes.md。
```

**③ 用 CLI 以 Test Generator 角色補測試**

```
你是 SSDLC Test Generator（參考 .github/agents/test-generator.agent.md）。
請參考 .github/skills/junit-generator/SKILL.md 的規則，
依照 .github/prompts/generate-unit-tests.prompt.md 的格式，
為 src/ 下 room 模組這次實作的 Service 與 Controller 類別產生 JUnit 5 測試
（依 tasks.md 的測試任務與實際存在的類別，不要假設類別名稱）。
每個 public 方法至少 3 個案例（Happy / Boundary / Error），Mockito mock 相依、
AssertJ 斷言，命名 should_xxx_When_yyy，覆蓋率 ≥ 80%。
測試碼建立於 src/test/，測試摘要寫入 docs/TestReport.md。
```

**④⑤ 本機驗證後 commit / push**

```powershell
mvn test
git add src/
git commit -m "feat(room): Room entity, service, RoomQueryPort, controller + validation"
git add src/test/
git commit -m "test(room): unit tests for RoomService/Controller (cov 85%)"
git push -u origin feature/room-crud
```

#### Carol（reservation 模組）— 同時進行

**① 開分支**

```powershell
git switch develop
git pull origin develop
git switch -c feature/reservation
```

**② 用 CLI 以 Backend 角色實作 reservation 模組**

> reservation 模組重點（供理解，**不必抄進 CLI 指令**）：預約/取消/查詢，預約前透過 `RoomQueryPort` 確認會議室存在且啟用，**核心是時段衝突檢查**。這些規則都定義在 `design.md`。

```
你是 SSDLC Backend（參考 .github/agents/backend.agent.md）。
請依照 .github/prompts/implement-feature.prompt.md 的格式，
讀取 docs/design/tasks.md 與 docs/design/design.md，
實作 reservation 模組中所有「尚未勾選（[ ]）」的任務。
以 design.md 為單一真實來源（實體、端點、時段衝突規則、以及要呼叫的 RoomQueryPort 契約都以文件為準）。
只透過 design.md 定義的契約與 room 模組互動，禁止直接存取 room 的 Repository。
遵守 .github/instructions/backend-java.instructions.md 的規範。
程式建立於 src/main/java/.../reservation/，完成後更新 tasks.md，變更摘要寫入 docs/design/implementation-notes.md。
```

> Carol 對著 `design.md` 定義的 `RoomQueryPort` 契約開發——即使 Bob 的 room 模組還沒合併，Carol 也能先以介面／mock 開發，不被卡住。

**③ 用 CLI 以 Test Generator 角色補測試**

```
你是 SSDLC Test Generator（參考 .github/agents/test-generator.agent.md）。
請參考 .github/skills/junit-generator/SKILL.md 的規則，
依照 .github/prompts/generate-unit-tests.prompt.md 的格式，
為 src/ 下 reservation 模組的 Service 類別產生測試，依 design.md 的時段規則涵蓋邊界情境
（如完全重疊、部分重疊、相鄰不重疊、跨日、會議室不存在等，以文件定義為準）。
Mockito mock 相依（含 RoomQueryPort）、AssertJ，覆蓋率 ≥ 80%。
測試碼建立於 src/test/，測試摘要附加到 docs/TestReport.md。
```

**④⑤ 驗證後 commit / push**

```powershell
mvn test
git add src/
git commit -m "feat(reservation): booking via RoomQueryPort + time-overlap check"
git add src/test/
git commit -m "test(reservation): overlap/adjacent/cross-day/no-room cases (cov 88%)"
git push -u origin feature/reservation
```

> 兩條 feature 分支互不干擾；room / reservation 在不同資料夾、且只透過 `RoomQueryPort` 契約互動，平行開發幾乎不會踩到對方的檔案。

---

### Sprint 1 收尾｜開 PR、Review、整合到 develop

#### Bob 先完成，對 develop 開 PR

```powershell
git switch feature/room-crud
git fetch origin
git merge origin/develop
mvn test
git push origin feature/room-crud

gh pr create --base develop --head feature/room-crud \
  --title "feat(room): room management module" --fill
```

**Alice 做 Code Review + 安全審查**（CLI 操作）：

```
你是 SSDLC Code Reviewer（參考 .github/agents/code-reviewer.agent.md）。
請依照 .github/prompts/code-review.prompt.md 的格式，
審查 src/main/java/.../room/ 下這次新增的程式碼，
對照 .github/instructions/code-review.instructions.md：
分層是否正確、命名、JavaDoc、例外處理、是否有重複/壞味道。
列出問題清單（嚴重度分級）與修正建議，結果寫入 docs/review/code-review-20260708.md。
```

```
你是 SSDLC Security Reviewer（參考 .github/agents/security-reviewer.agent.md）。
請參考 .github/skills/security-review/SKILL.md，
依照 .github/prompts/threat-model.prompt.md 的格式，
對 room 模組做 OWASP Top 10 檢查，
結果寫入 docs/security/security-review-20260708.md。
```

審查有意見 → Bob 在**同一條 feature 分支**修正、再 push，PR 自動更新：

```powershell
git switch feature/room-crud
# 依審查意見用 CLI Backend 角色修正...
git commit -am "fix(room): address review - null check & error mapping"
git push origin feature/room-crud
```

審查通過 → Alice 核准並合併：

```powershell
gh pr merge feature/room-crud --merge --delete-branch
```

#### Carol 接著開 PR

```powershell
git switch feature/reservation
git fetch origin
git merge origin/develop   # 把 Bob 的 room 併進來，整合真正的 RoomQueryPort 實作
mvn test                   # 整合後再跑一次，確認 reservation 串得起 room
git push origin feature/reservation
gh pr create --base develop --head feature/reservation \
  --title "feat(reservation): booking module" --fill
```

Alice 同樣審查 → 通過 → 合併。**此時 develop 已是 room + reservation 整合完成的可運行版本。**

---

### Sprint 1 發布｜整合測試 → main → tag（Alice）

```powershell
git switch develop
git pull origin develop
mvn verify

gh pr create --base main --head develop --title "release: v1.0.0" --fill
```

**發布文件**（CLI 操作）：

```
你是 SSDLC Release（參考 .github/agents/release.agent.md）。
產生 v1.0.0 的 CHANGELOG 與 Release Note，列出本版功能與已知限制，
並檢查發布安全閘門（測試通過、無高風險弱點、文件齊備）。
CHANGELOG 寫入 CHANGELOG.md，Release Note 寫入 docs/ReleaseNote.md。
```

```
你是 SSDLC Doc Writer（參考 .github/agents/doc-writer.agent.md）。
請參考 .github/skills/doc-generator/SKILL.md，
為本專案產生 README（安裝/啟動/API 用法）與 API 文件。
README 寫入 README.md，API 文件寫入 docs/api/api-reference.md。
```

```powershell
gh pr merge develop --merge
git switch main
git pull origin main
git tag -a v1.0.0 -m "Meeting Room Reservation API v1.0.0"
git push origin v1.0.0
```

---

## 5. 衝突處理範例（兩人改到同一檔）

情境：Bob 與 Carol 都動了 `application.yml`，Carol merge develop 時出現衝突。

```powershell
git switch feature/reservation
git fetch origin
git merge origin/develop
# CONFLICT (content): Merge conflict in src/main/resources/application.yml
```

處理步驟：

1. 開啟衝突檔，會看到 `<<<<<<<`、`=======`、`>>>>>>>` 標記。
2. **在 Copilot CLI 貼上衝突段落**：

   ```
   以下是 application.yml 的 git 衝突段落，兩邊都要保留，幫我合併這段 yaml：
   [貼上衝突內容]
   ```

3. 確認保留正確內容、刪掉衝突標記。

```powershell
git add src/main/resources/application.yml
mvn test
git commit
git push origin feature/reservation
```

> **預防勝於治療**：每天 `git pull origin develop` 併回自己分支，小步整合，衝突就小。模組邊界由 Alice 在 Sprint 0 切乾淨（room / reservation 各自資料夾），是減少衝突的根本。

---

## 6. 角色 × CLI 指令 × Git 全流程對照表

| 階段 | 誰 | CLI 角色宣告 | Git 操作 |
|------|----|------------|---------|
| 專案初始化 | Alice | — | `git init` → push `main` → 開 `develop` |
| 需求分析 | Alice | `你是 SSDLC Planner` | commit `requirements.md` 到 develop |
| 架構/API 設計 | Alice | `你是 SSDLC Architect` | commit `design.md` / `tasks.md` |
| 分支保護設定 | Alice | — | GitHub 設定 `main`/`develop` 保護 |
| room 開發 | Bob | `你是 SSDLC Backend`、`你是 SSDLC Test Generator` | `feature/room-crud` 上 commit/push |
| reservation 開發 | Carol | `你是 SSDLC Backend`、`你是 SSDLC Test Generator` | `feature/reservation` 上 commit/push |
| 開 PR | Bob/Carol | — | `gh pr create --base develop` |
| Code/安全審查 | Alice | `你是 SSDLC Code Reviewer`、`你是 SSDLC Security Reviewer` | PR 上留言；PG 修正後再 push |
| 合併整合 | Alice | — | `gh pr merge --delete-branch` 進 develop |
| 發布 | Alice | `你是 SSDLC Release`、`你是 SSDLC Doc Writer` | develop→main PR、`git tag v1.0.0` |

---

## 7. Hooks 安全護欄（CLI 原生支援）

`.github/hooks/ssdlc-guardrails.json` 在 **Copilot CLI 下原生生效**（VS Code 仍是 Preview）：

- **block-dangerous-commands**：攔截高風險指令
- **validate-no-secrets**：偵測即將提交的 API key / 密碼並阻擋
- **protect-sensitive-files**：保護 `.env`、金鑰檔
- **session-report**：對話結束產生稽核報告

> ✅ **CLI 優勢**：Hooks 在 CLI 下為正式功能，護欄完整生效，團隊安全防護開箱即用。

---

## 8. 給小團隊的最佳實務

1. **介面契約先行**：Alice 在 Sprint 0 把 `RoomQueryPort` 定死，兩位 PG 才能真正平行、不互卡。
2. **CLI 角色宣告一句話**：每次對話開頭加「你是 SSDLC Backend（參考 .github/agents/backend.agent.md）」，AI 行為更聚焦。
3. **一條 feature 分支只做一個任務**，commit 訊息用 Conventional Commits（`feat:`/`fix:`/`test:`/`docs:`）。
4. **每天併回 develop**，小步整合，把衝突壓到最小。
5. **PR 不繞過審查**：`main`/`develop` 受保護，所有變更走 PR，Alice 至少核准 1 次。
6. **安全審查是閘門**：Security Reviewer CLI 指令沒過，不准合併。
7. **修審查意見回原 feature 分支**再 push，PR 會自動更新，不要另開分支。
8. **Agent 是角色不是人**：Alice 一人依當下任務切換多個 CLI 角色宣告；兩位 PG 共用 Backend/Test Generator 角色，因規範都在共用 instructions 裡，產出風格自然一致。

---

> 把「會議室預約」換成你真正的系統、把模組數量對應到團隊人數，這套「**契約先行 → feature 分支平行開發 → CLI 角色宣告審查 → develop 整合 → main 發布**」的節奏完全可複製。
