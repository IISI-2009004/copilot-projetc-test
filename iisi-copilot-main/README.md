# iisi-copilot

**iisi-copilot** 是一個以 **SSDLC（安全軟體開發生命週期）** 為核心的 AI 輔助開發工具庫，整合 GitHub Copilot、Claude Code 等主流 AI 編碼助手，提供可直接套用的 Agents、Instructions、Hooks、Skills、Prompts、Templates 與 Tutorials，讓開發團隊快速建立具備安全防護的 AI 驅動開發工作流。

---

## 為什麼需要這個專案？

AI 寫程式助手（Copilot、Claude Code）功能強大，但「開箱即用」的效果往往與企業需求有落差：

- 沒有符合組織規範的 Prompt 與 Instructions
- 缺乏安全檢查（注入攻擊、機敏資料外洩、危險指令執行）
- Agent 職責不清，多人協作時缺少標準角色定義
- 沒有對應 SSDLC 各階段的文件模板

本專案提供**可直接複製使用的素材包**，覆蓋從規劃到上線維運的完整 SSDLC 流程。

---

## 本repo結構

```
iisi-copilot/
├── agents/          # 自訂 AI Agent 角色定義（15 個 SSDLC 專責 Agent）
├── hooks/           # 生命週期自動化鉤子（安全防護、格式化、稽核）
├── instructions/    # GitHub Copilot / AI 自訂指令集
├── plugins/         # Copilot Plugin 配置（整合 MCP Server 與外部工具）
├── prompts/         # SSDLC 各階段 Prompt 範本庫
├── skills/          # Agent Skills 模組（可重複呼叫的專業能力單元）
├── templates/       # SSDLC 文件模板（需求、測試、部署、維運）
├── tutorials/       # 技術教學手冊（AI 工具、框架、方法論、程式語言、工具）
├── case-studies/    # 各專案導入後的案例回饋（心得、客製化檔案、驗證結果）
└── docs/            # 各元件的詳細說明文件
```

> 想貢獻新元件或回饋你的導入案例？請參閱 [CONTRIBUTING.md](CONTRIBUTING.md)。

---

## 快速上手

### Step 1：選擇你需要的元件

| 目標 | 前往 |
|------|------|
| 建立 AI Agent 團隊（規劃/開發/審查/安全/發布） | [agents/ssdlc/](agents/ssdlc/) |
| 強制執行安全政策與格式化（開發時防護） | [hooks/ssdlc/](hooks/ssdlc/) |
| 讓 Copilot 遵循專案技術規範 | [instructions/ssdlc/](instructions/ssdlc/) 或 [instructions/collection/](instructions/collection/) |
| 取得標準化 Prompt 範本 | [prompts/ssdlc/](prompts/ssdlc/) |
| 加入可重用的 Agent Skills | [skills/ssdlc/](skills/ssdlc/) |
| 取得 SSDLC 各階段文件模板 | [templates/ssdlc/](templates/ssdlc/) |
| 學習 AI 工具或技術框架 | [tutorials/](tutorials/) |
| 回饋你的專案導入案例與驗證結果 | [case-studies/](case-studies/) |

### Step 2：參考對應的說明文件

| 元件 | 說明文件 |
|------|---------|
| Agents | [docs/README.agents.md](docs/README.agents.md) |
| Hooks | [docs/README.hooks.md](docs/README.hooks.md) |
| Instructions | [docs/README.instructions.md](docs/README.instructions.md) |
| Plugins | [docs/README.plugins.md](docs/README.plugins.md) |
| Skills | [docs/README.skills.md](docs/README.skills.md) |
| Workflows | [docs/README.workflows.md](docs/README.workflows.md) |

---

## 核心元件介紹

### Agents — AI 開發團隊角色

SSDLC Agent 團隊由 15 個專責角色組成，覆蓋完整開發生命週期：

| 階段 | Agent | 職責 |
|------|-------|------|
| 調度 | [Orchestrator](agents/ssdlc/orchestrator.agent.md) | 全局調度與 Agent 編排 |
| 調度 | [Project Manager](agents/ssdlc/project-manager.agent.md) | 專案進度與資源管控 |
| 規劃 | [Planner](agents/ssdlc/planner.agent.md) | 需求分析與任務拆解 |
| 規劃 | [Architect](agents/ssdlc/architect.agent.md) | 架構設計與 STRIDE 威脅建模 |
| 開發 | [Backend](agents/ssdlc/backend.agent.md) | 後端開發與 API 設計 |
| 開發 | [Frontend](agents/ssdlc/frontend.agent.md) | 前端開發與 UI 實作 |
| 品質 | [Code Reviewer](agents/ssdlc/code-reviewer.agent.md) | 程式碼審查與品質把關 |
| 安全 | [Security Reviewer](agents/ssdlc/security-reviewer.agent.md) | SAST/SCA 掃描與安全審查 |
| 測試 | [Test Generator](agents/ssdlc/test-generator.agent.md) | 測試案例與安全測試產生 |
| 交付 | [Doc Writer](agents/ssdlc/doc-writer.agent.md) | 技術文件撰寫 |
| 交付 | [Release](agents/ssdlc/release.agent.md) | 版本發佈與安全閘門 |
| 交付 | [DevOps](agents/ssdlc/devops.agent.md) | CI/CD Pipeline 與 IaC |
| 維運 | [Incident Response](agents/ssdlc/incident-response.agent.md) | 事件回應與事後檢討 |
| 輔助 | [Reverse Engineer](agents/ssdlc/reverse-eng.agent.md) | 逆向工程與系統理解 |
| 輔助 | [Thinking Beast Mode](agents/ssdlc/Thinking-Beast-Mode.agent.md) | 深度推理與創意突破 |

詳細 Agent 設計規範、格式說明與協作流程圖，請參閱 [agents/README.md](agents/README.md)。

---

### Hooks — 開發生命週期安全防護

Hooks 在 AI 寫程式工具每次操作前後自動執行，提供確定性的安全強制機制：

| Hook | 功能 |
|------|------|
| [auto-format-lint](hooks/ssdlc/auto-format-lint/) | 自動格式化與 Lint 檢查 |
| [block-dangerous-commands](hooks/ssdlc/block-dangerous-commands/) | 攔截高風險終端指令 |
| [block-force-push](hooks/ssdlc/block-force-push/) | 防止 force push 至保護分支 |
| [protect-sensitive-files](hooks/ssdlc/protect-sensitive-files/) | 保護機敏檔案不被意外修改 |
| [save-chat-history](hooks/ssdlc/save-chat-history/) | 會話結束時保存 Copilot 對話逐字稿 |
| [session-report](hooks/ssdlc/session-report/) | 會話結束時產生稽核報告 |
| [ssdlc-guardrails](hooks/ssdlc/ssdlc-guardrails/) | SSDLC 流程合規強制執行 |
| [validate-no-secrets](hooks/ssdlc/validate-no-secrets/) | 偵測並阻止機密資料提交 |

詳細安裝與設定說明，請參閱 [docs/README.hooks.md](docs/README.hooks.md)。

---

### Instructions — AI 行為自訂指令

分為兩個子目錄：

- **[instructions/ssdlc/](instructions/ssdlc/)** — 對應 SSDLC 流程的專用指令（後端 Java 規範、Code Review 標準、安全規範、PR 描述格式、測試規範）
- **[instructions/collection/](instructions/collection/)** — 通用技術指令集（Spring Boot、Quarkus、Tailwind、Docker、OWASP、效能最佳化、無障礙設計、Java 升版等 30+ 個指令）

安裝方式：將 `.instructions.md` 檔案放入專案的 `.github/instructions/` 資料夾，Copilot 會自動套用。

詳細說明請參閱 [docs/README.instructions.md](docs/README.instructions.md)。

---

### Prompts — SSDLC 各階段 Prompt 範本

針對 SSDLC 七大階段提供標準化 Prompt：

| 階段 | 路徑 |
|------|------|
| 需求分析 | [prompts/ssdlc/requirements/](prompts/ssdlc/requirements/) |
| 設計 | [prompts/ssdlc/design/](prompts/ssdlc/design/) |
| 寫程式 | [prompts/ssdlc/coding/](prompts/ssdlc/coding/) |
| 測試 | [prompts/ssdlc/testing/](prompts/ssdlc/testing/) |
| 安全審查 | [prompts/ssdlc/security/](prompts/ssdlc/security/) |
| 程式碼審查 | [prompts/ssdlc/review/](prompts/ssdlc/review/) |
| 逆向工程 | [prompts/ssdlc/reverse-engineering/](prompts/ssdlc/reverse-engineering/) |

詳細設計原則請參閱 [prompts/README.md](prompts/README.md)。

---

### Skills — 可重用 Agent 能力模組

| Skill | 功能 |
|-------|------|
| [api-reviewer](skills/ssdlc/api-reviewer/) | API 設計審查與規範驗證 |
| [doc-generator](skills/ssdlc/doc-generator/) | 技術文件自動產生 |
| [junit-generator](skills/ssdlc/junit-generator/) | JUnit 測試案例產生 |
| [pr-checker](skills/ssdlc/pr-checker/) | PR 品質與安全檢核 |
| [reverse-analysis](skills/ssdlc/reverse-analysis/) | 現有程式碼逆向分析 |
| [security-review](skills/ssdlc/security-review/) | 安全弱點掃描與報告 |

詳細規範請參閱 [docs/README.skills.md](docs/README.skills.md)。

---

### Templates — SSDLC 文件模板

提供各 SSDLC 階段的標準文件範本：

| 類別 | 包含模板 |
|------|---------|
| [需求](templates/ssdlc/requirements/) | BRD、FRD、PRD、SRD、UseCase、SecurityRequirements、ChangeRequest |
| [設計](templates/ssdlc/design/) | 系統設計文件 |
| [測試](templates/ssdlc/testing/) | TestPlan、TestCase、TestReport、PerformanceTestReport、SecurityScanReport |
| [部署](templates/ssdlc/deployment/) | 部署相關文件 |
| [維運](templates/ssdlc/operations/) | 維運相關文件 |
| [專案](templates/ssdlc/project/) | README、CHANGELOG、Retrospective、UserManual |

---

### Tutorials — 技術教學手冊

涵蓋五大類別的完整中文教學手冊：

#### AI 工具
涵蓋 Claude Code、GitHub Copilot、SSDLC Agent Team 建立、MCP、Agent Skills 等 50+ 篇教學。
→ [tutorials/AI/](tutorials/AI/)

#### 框架
Spring Boot、Spring Framework、Quarkus、Angular、React、Vue3、Node.js、Jakarta EE、Tailwind CSS 等。
→ [tutorials/framework/](tutorials/framework/)

#### 方法論
Clean Architecture、Clean Code、DDD、TDD、BDD、Microservices、Design Patterns、Refactoring、UML 等。
→ [tutorials/methodology/](tutorials/methodology/)

#### 程式語言
Java、Python、TypeScript、JavaScript、C#、C++、Rust、SQL、Bash、PowerShell 等。
→ [tutorials/program language/](tutorials/program%20language/)

#### 工具
Git、GitHub、GitLab、Jenkins、Kubernetes、Podman、Redis、Kafka、Keycloak、VS Code、IntelliJ 等。
→ [tutorials/tools/](tutorials/tools/)

---

### Case Studies — 各專案案例回饋

各專案導入 SSDLC Agent Team 後的實務回饋，包含使用心得、客製化後的檔案與驗證測試結果，詳見 [case-studies/](case-studies/)。歡迎將你的導入經驗回饋回本 repo，作法請參閱 [CONTRIBUTING.md](CONTRIBUTING.md#案例回饋-case-studies)。

---

## SSDLC Agent 協作流程

```
需求分析 → 架構設計 → 後端/前端開發 → Code Review → 安全審查 → 測試 → 文件 → 發布 → CI/CD → 維運
  Planner    Architect   Backend/Frontend   Reviewer    Security   Test    Doc    Release  DevOps  Incident
              (STRIDE)                      (品質把關)  (OWASP)   Generator Writer  (閘門)         Response
```

全局由 **Orchestrator** 調度、**Project Manager** 管控進度。

---

## 適用對象

- **開發團隊**：想讓 AI 助手遵循團隊規範、自動執行安全檢查
- **DevOps / SRE**：需要在 CI/CD 加入 AI 驅動的安全閘門
- **資安工程師**：想在 SSDLC 各階段嵌入 OWASP / SAST / SCA 防護
- **技術主管**：建立跨團隊一致的 AI 使用標準

## 貢獻

歡迎貢獻新的 Agents、Instructions、Hooks、Skills、Prompts，或回饋你的專案導入案例。詳細規範請參閱 [CONTRIBUTING.md](CONTRIBUTING.md)。

## Copilot 專案初始化與標準目錄設計
### your-project 標準目錄樹

```
your-project/
│
├── AGENTS.md                          # 全域 Agent 指令（所有 AI Agent 通用）
│
├── .github/
│   ├── copilot-instructions.md        # Copilot 全域指令（自動套用至所有對話）
│   │
│   ├── agents/                        # Custom Agent 定義
│   │   ├── planner.agent.md           # 規劃 Agent（VS Code 格式）
│   │   ├── architect.agent.md         # 架構 Agent
│   │   ├── backend.agent.md           # 後端 Agent
│   │   ├── frontend.agent.md          # 前端 Agent
│   │   ├── test-generator.agent.md    # 測試 Agent
│   │   ├── security-reviewer.agent.md # 安全 Agent
│   │   ├── code-reviewer.agent.md     # Code Review Agent
│   │   ├── release.agent.md           # Release Agent
│   │   ├── reverse-eng.agent.md       # 逆向工程 Agent
│   │   └── doc-writer.agent.md        # 文件 Agent
│   │
│   ├── instructions/                  # 檔案型 Instructions
│   │   ├── backend-java.instructions.md
│   │   ├── frontend.instructions.md
│   │   ├── security.instructions.md
│   │   ├── testing.instructions.md
│   │   ├── reverse-engineering.instructions.md
│   │   ├── code-review.instructions.md
│   │   └── pr-description.instructions.md
│   │
│   ├── skills/                        # Agent Skills
│   │   ├── security-review/
│   │   │   ├── SKILL.md
│   │   │   └── scripts/
│   │   │       └── owasp-check.sh
│   │   ├── junit-generator/
│   │   │   ├── SKILL.md
│   │   │   └── templates/
│   │   │       └── test-template.java
│   │   ├── pr-checker/
│   │   │   ├── SKILL.md
│   │   │   └── checklists/
│   │   │       └── pr-checklist.md
│   │   ├── api-reviewer/
│   │   │   └── SKILL.md
│   │   ├── reverse-analysis/
│   │   │   ├── SKILL.md
│   │   │   └── templates/
│   │   │       ├── module-report.md
│   │   │       └── dependency-map.md
│   │   └── doc-generator/
│   │       ├── SKILL.md
│   │       └── templates/
│   │           └── api-doc-template.md
│   │
│   ├── hooks/                         # Hooks 定義
│   │   └── ssdlc-guardrails.json
│   │
│   ├── prompts/                       # Prompt Library
│   │   ├── requirements/
│   │   │   └── analyze-user-story.prompt.md
│   │   ├── design/
│   │   │   └── api-design.prompt.md
│   │   ├── coding/
│   │   │   └── implement-feature.prompt.md
│   │   ├── testing/
│   │   │   └── generate-unit-tests.prompt.md
│   │   ├── security/
│   │   │   └── threat-model.prompt.md
│   │   ├── review/
│   │   │   └── code-review.prompt.md
│   │   └── reverse-engineering/
│   │       └── analyze-legacy-module.prompt.md
│   │
│   ├── PULL_REQUEST_TEMPLATE.md       # PR 模板
│   │
│   ├── ISSUE_TEMPLATE/                # Issue 模板
│   │   ├── feature-request.yml
│   │   ├── bug-report.yml
│   │   └── reverse-engineering-task.yml
│   │
│   └── 教學/
│       └── AI開發/
│           └── GitHub Copilot 建立 SSDLC Agent Team 教學手冊.md
│
├── docs/
│   ├── architecture/                  # 架構文件
│   │   └── adr/                       # Architecture Decision Records
│   │       └── 001-clean-architecture.md
│   ├── governance/                    # 治理文件
│   │   ├── agent-team-governance.md
│   │   └── model-selection-policy.md
│   ├── security/                      # 安全基線
│   │   └── security-baseline.md
│   └── reverse-engineering/           # 逆向工程基線
│       └── legacy-system-inventory.md
│
├── .claude/                           # Claude Code 相容格式（選用）
│   ├── agents/                        # Claude 格式 Agent（VS Code 也會偵測）
│   └── skills/                        # Claude 格式 Skills
│
├── .agents/                           # 通用 Agent 格式（選用）
│   └── skills/                        # 通用格式 Skills
│
├── .vscode/
│   └── settings.json                  # 團隊共用 VS Code 設定
│
├── src/                               # 原始碼
├── tests/                             # 測試
└── README.md
```

### 檔案用途說明

| 檔案 / 目錄 | 用途 | 自動套用 | 版本控管 |
|------------|------|---------|---------|
| `AGENTS.md` | 全域 Agent 指令，所有 AI Agent（VS Code、Claude Code 等）通用 | ✓ 始終套用 | ✓ |
| `.github/copilot-instructions.md` | Copilot 專用全域指令 | ✓ 始終套用 | ✓ |
| `.github/agents/*.agent.md` | Custom Agent 定義（VS Code 格式） | 選擇 Agent 時套用 | ✓ |
| `.github/agents/*.md` | Custom Agent 定義（GitHub.com / CLI 通用） | 選擇 Agent 時套用 | ✓ |
| `.github/instructions/*.instructions.md` | 檔案型指令，按 `applyTo` 模式套用 | ✓ 符合模式時自動套用 | ✓ |
| `.github/skills/*/SKILL.md` | Agent Skills，按需載入（也可放在 `.claude/skills/` 或 `.agents/skills/`） | 相關時自動載入 | ✓ |
| `.github/hooks/*.json` | Hooks 定義（VS Code Preview / Cloud Agent+CLI GA） | ✓ 觸發時自動執行 | ✓ |
| `.github/prompts/*.prompt.md` | Prompt 範本，手動引用 | ✗ 需手動選用 | ✓ |
| `docs/governance/` | 治理文件 | ✗ 供人類閱讀 | ✓ |
| `docs/architecture/adr/` | 架構決策紀錄 | ✗ 可被 Agent 引用 | ✓ |


### 實務建議

由於 VS Code 與 GitHub.com / CLI 會**共用** `.github/agents/` 目錄，建議：

1. **核心欄位使用通用格式**：`name`、`description`、`tools` 在兩個環境都支援
2. **VS Code 專有欄位會被 GitHub.com 忽略**，不會造成錯誤
3. **在同一檔案中同時定義兩端需要的內容**，減少維護負擔
4. **測試時在兩端都驗證**，確保 Agent 行為符合預期

## Project / User / Org 層級差異

| 層級 | 適用範圍 | 儲存位置 | 治理責任 |
|------|---------|---------|---------|
| **Project（專案）** | 單一 Repository | `.github/agents/`, `.github/instructions/`, `.github/skills/` | 專案團隊 |
| **User（個人）** | 個人所有工作區 | `~/.copilot/agents/`, `~/.copilot/instructions/`, `~/.copilot/skills/`（或 `~/.claude/skills/`, `~/.agents/skills/`） | 個人 |
| **Organization（組織）** | 組織內所有 Repository | `.github-private` repo 的 `agents/`, `instructions/` | 組織管理員 |
| **Enterprise（企業）** | 企業內所有組織 | `.github-private` repo（企業層級） | 企業管理員 |

### 優先順序

```
個人（User）> 專案（Project）> 組織（Organization）> 企業（Enterprise）
```

> 當多層級的指令衝突時，較高優先順序的指令會覆蓋較低的。

### 組織層級設定步驟

```
1. 在組織中建立名為 `.github-private` 的 Repository
2. 在該 Repository 中建立 `agents/` 目錄
3. 放入組織級 Agent Profile（例如 security-reviewer.md）
4. 組織成員的 VS Code 會自動探索這些 Agent
   （需設定 github.copilot.chat.organizationCustomAgents.enabled = true）
```

## 版本控管策略

| 項目 | 版本控管策略 |
|------|------------|
| **Agent Profiles** | 納入 Git，隨專案版本控管 |
| **Instructions** | 納入 Git，隨專案版本控管 |
| **Skills** | 納入 Git，隨專案版本控管 |
| **Hooks** | 納入 Git，隨專案版本控管 |
| **Prompt Files** | 納入 Git，隨專案版本控管 |
| **個人 Instructions/Agents** | 個人管理，可透過 Settings Sync 同步 |
| **組織層級設定** | 由 `.github-private` repo 管理，有獨立 PR 審核流程 |
| **VS Code settings.json** | 團隊共用部分納入 Git，個人偏好不納入 |
| **Memory** | ⚠️ **不可** 直接版本控管（由 Copilot 管理，28 天自動過期） |

### 命名規範

```
Agent:       {角色}.agent.md         → planner.agent.md
Instruction: {領域}.instructions.md  → backend-java.instructions.md
Skill:       {能力}/SKILL.md         → security-review/SKILL.md
Prompt:      {階段}/{動作}.prompt.md  → testing/generate-unit-tests.prompt.md
Hook:        {用途}.json             → ssdlc-guardrails.json
```

### 企業實務建議

1. **所有自訂檔案都應納入版本控管**（除 Memory 外）
2. **組織層級變更需經 PR 審核**，避免影響所有團隊
3. **建立 CHANGELOG**，追蹤 Agent Team 設定變更
4. **使用 Branch Protection Rules** 保護 `.github/` 目錄
5. **定期 Review** 組織層級 Instructions 與 Agents，確保仍然適用

### 手動複製以 Agents為例: prompt, insturctions, plugins, skills, hooks 做法相同

```bash
# 複製agents目錄下的 Agent 到 Claude Code
cp agents/ssdlc/*.md ~/.claude/agents/

# 複製agents目錄下的 Agent 到 Claude Code by 專案
cp agents/ssdlc/*.md  /your-project/.claude/agents/

# 複製到 GitHub Copilot 個人共用
cp agents/ssdlc/*.md ~/.github/agents/

# 複製到 GitHub Copilot 個人共用 by 專案
cp agents/ssdlc/*.md  /your-project/.github/agents/

```
