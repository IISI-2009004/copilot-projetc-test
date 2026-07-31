---
description: '建立 GitHub Copilot 自訂代理檔案的指南。'
applyTo: '**/*.agent.md'
---

# Custom Agent File Guidelines

如何在 GitHub Copilot 中建立有效且易於維護的自訂代理文件，為特定開發任務提供專業知識。

## Project Context

- Target audience: 為 GitHub Copilot 建立自訂代理程式的開發者
- File format: Markdown，帶有 YAML frontmatter
- File naming convention: 小寫字母加連字號 (例如，`test-specialist.agent.md`)
- Location: `.github/agents/` 目錄（倉庫級別）或 `agents/` 目錄（組織/企業級別）
- Purpose: 定義具有專門知識、工具和指令的專用代理，用於特定任務
- Official documentation: https://docs.github.com/en/copilot/how-tos/use-copilot-agents/coding-agent/create-custom-agents

## Required Frontmatter

每個代理檔案都必須包含 YAML 前元數據，其中包含以下欄位：

```yaml
---
description: 'Brief description of the agent purpose and capabilities'
name: 'Agent Display Name'
tools: ['read', 'edit', 'search']
model: 'Claude Sonnet 4.5'
target: 'vscode'
---
```

### Core Frontmatter Properties

#### **description** (REQUIRED)
- 用單引號括起來的字串，清楚地說明了代理的目的和領域專業知識。
- 應簡潔明了（50-150個字），並具有行動意義。
- Example: `'Focuses on test coverage, quality, and testing best practices'`

#### **name** (OPTIONAL)
- 代理在使用者介面中的顯示名稱
- 如果省略，預設為檔案名稱（不含 `.md` 或 `.agent.md`）
- 使用標題大小寫並具有描述性
- 範例: `'Testing Specialist'`

#### **tools** (OPTIONAL)
- 代理可以使用的工具名稱或別名列表
- 支援逗號分隔的字串或 YAML 陣列格式
- 如果省略，代理可以使用所有可用工具
- 詳情請參見下方的 "工具配置" 部分

#### **model** (STRONGLY RECOMMENDED)
- 指定智能體應使用哪種人工智慧模型
- 支援 VS Code、JetBrains IDEs、Eclipse 和 Xcode
- 範例: `'Claude Sonnet 4.5'`, `'gpt-4'`, `'gpt-4o'`
- 根據代理的複雜性和所需功能選擇模型

#### **target** (OPTIONAL)
- 指定目標環境: `'vscode'` 或 `'github-copilot'`
- 如果省略，代理在兩個環境中都可用
- 當代理具有特定環境功能時使用

#### **user-invocable** (OPTIONAL)
- 布林值，控制該代理是否出現在聊天視窗的代理下拉式選單中。
- 預設值: `true` 如果省略
- 設定為 `false` 以建立僅能作為子代理或程式化存取的代理

#### **disable-model-invocation** (OPTIONAL)
- 布林值，控制該代理是否可以被其他代理呼叫為子代理。
- 預設值: `false` 如果省略
- 設定為 `true` 以防止子代理呼叫，同時保持在選擇器中可用

#### **metadata** (OPTIONAL, GitHub.com only)
- 物件，包含代理註解的名稱-值對
- 範例: `metadata: { category: 'testing', version: '1.0' }`
- 在 VS Code 中不支援

#### **mcp-servers** (OPTIONAL, Organization/Enterprise only)
- 配置僅此代理程式可用的 MCP 伺服器
- 僅支援組織/企業級代理
- 詳情請參見下方的 "MCP 伺服器配置" 部分

#### **handoffs** (OPTIONAL, VS Code only)
- 啟用引導式的順序工作流程，允許在代理之間過渡並提供建議的下一步
- 列出每個交接配置，指定目標代理和可選的提示
- 在聊天回應完成後，交接按鈕會出現，允許使用者移動到下一個代理
- 僅支援 VS Code（版本 1.106+）
- 詳情請參見下方的 "交接配置" 部分
## Handoffs Configuration

交接功能可讓您建立引導式順序工作流程，在自訂代理程式之間實現無縫過渡。這對於協調多步驟開發工作流程非常有用，使用者可以在進入下一步之前審核並批准每個步驟。

### Common Handoff Patterns

- **Planning → Implementation**: 在規劃代理中生成計劃，然後交接給實作代理開始編碼
- **Implementation → Review**: 完成實作後，切換到程式碼審查代理以檢查品質和安全問題
- **Write Failing Tests → Write Passing Tests**: 生成失敗的測試，然後交接給實作代理以編寫使這些測試通過的程式碼
- **Research → Documentation**: 研究一個主題，然後切換到文件代理撰寫指南

### Handoff Frontmatter Structure

在代理檔案的 YAML 前元資料中使用 handoffs 欄位定義交接：

```yaml
---
description: 'Brief description of the agent'
name: 'Agent Name'
tools: ['search', 'read']
handoffs:
  - label: Start Implementation
    agent: implementation
    prompt: 'Now implement the plan outlined above.'
    send: false
  - label: Code Review
    agent: code-review
    prompt: 'Please review the implementation for quality and security issues.'
    send: false
---
```

### Handoff Properties

清單中的每次交接都必須包含以下屬性：

| Property | Type | Required | Description |
|----------|------|----------|-------------|
| `label` | string | Yes | 在聊天介面中顯示在交接按鈕上的文字 |
| `agent` | string | Yes | 要切換到的目標代理識別符（名稱或不含 `.agent.md` 的檔案名稱） |
| `prompt` | string | No | 用於預先填充目標代理聊天輸入的提示文本 |
| `send` | boolean | No | 如果為 `true`，則自動將提示提交給目標代理（預設值: `false`） |

### Handoff Behavior

- **Button Display**:聊天回覆完成後，切換按鈕會以互動式建議的形式出現。
- **Context Preservation**: 當使用者選擇交接按鈕時，他們會切換到目標代理，同時保持對話上下文
- **Pre-filled Prompt**: 如果指定了 `prompt`，它會預先填充在目標代理的聊天輸入中
- **Manual vs Auto**: 當 `send: false` 時，使用者必須審核並手動發送預填充的提示；當 `send: true` 時，提示會自動提交

### Handoff Configuration Guidelines

#### When to Use Handoffs

- **Multi-step workflows**: 將複雜任務分解到專門的代理中
- **Quality gates**: 確保在實作階段之間進行審查步驟
- **Guided processes**: 引導使用者完成結構化的開發流程
- **Skill transitions**: 從規劃/設計轉移到實作/測試專家
#### Best Practices

- **Clear Labels**: 使用行動導向的標籤，清楚指示下一步。
  - ✅ Good: "開始實施"、"安全審查"、"編寫測試"
  - ❌ Avoid: "下一步"、"去找代理人"、"做點什麼"

- **Relevant Prompts**: 提供與上下文相關的提示，參考已完成的工作
  - ✅ Good: `'現在實施上述計劃。'`
  - ❌ Avoid: 通用提示，沒有上下文
- **Selective Use**: 不要為每個可能的代理人建立交接；專注於合乎邏輯的工作流程轉換。
  - 每個代理限制為 2-3 個最相關的下一步
  - 只為自然跟隨工作流程的代理添加交接

- **Agent Dependencies**: 確保目標代理在創建交接之前存在
  - 對不存在的代理的交接將被靜默忽略
  - 測試交接以驗證其是否按預期工作

- **Prompt Content**: 保持提示簡潔且可操作
  - 參考當前代理的工作而不重複內容
  - 提供目標代理可能需要的任何必要上下文

### Example: Complete Workflow

以下是一個由三位代理人透過交接班組成完整工作流程的範例：

**Planning Agent** (`planner.agent.md`):
```yaml
---
description: '為新功能或重構制定實施計劃'
name: 'Planner'
tools: ['search', 'read']
handoffs:
  - label: Implement Plan
    agent: implementer
    prompt: '實施上述計劃。'
    send: false
---
# Planner Agent

您是規劃專員。您的任務是：
1. 分析需求
2. 將工作分解為邏輯步驟
3. 制定詳細的實施計劃
4. 確定測試需求

不要撰寫任何程式碼 - 僅專注於規劃。
```

**Implementation Agent** (`implementer.agent.md`):
```yaml
---
description: '根據計劃或規範實現代碼。'
name: 'Implementer'
tools: ['read', 'edit', 'search', 'execute']
handoffs:
  - label: Review Implementation
    agent: reviewer
    prompt: '請審查此實作的程式碼品質、安全性及最佳實踐遵循情況。'
    send: false
---
# Implementer Agent
您是實作專員。您的任務是：
1. 根據提供的計劃或規範進行實作
2. 撰寫乾淨、可維護的程式碼
3. 包含適當的註解和文件
4. 遵循專案的程式碼標準

完整且徹底地實作解決方案。
```

**Review Agent** (`reviewer.agent.md`):
```yaml
---
description: '審查程式碼，確保其品質、安全性和符合最佳實務。'
name: 'Reviewer'
tools: ['read', 'search']
handoffs:
  - label: Back to Planning
    agent: planner
    prompt: '請查看以上回饋意見，並確定是否需要製定新計劃。'
    send: false
---
# Code Review Agent
您是程式碼審查專員。您的任務是：
1. 檢查程式碼品質和可維護性
2. 識別安全問題和漏洞
3. 驗證是否遵循專案標準
4. 提出改進建議

提供對實作的建設性回饋。
```

此工作流程允許開發人員執行以下操作：
1. 首先使用規劃代理建立詳細計劃
2. 將計劃交給實施代理，由其根據計劃編寫程式碼
3. 將計劃交給審核代理，由其檢查實現情形
4. 如果發現重大問題，可選擇返回規劃階段

### Version Compatibility

- **VS Code**: VS Code 1.106 及更高版本支援交接功能
- **GitHub.com**: 目前不支援；代理轉換工作流程使用不同的機制
- **Other IDEs**: 支援有限或不支援；請專注於 VS Code 實作以獲得最佳相容性。

## Tool Configuration

### Tool Specification Strategies

**Enable all tools** (default):
```yaml
# Omit tools property entirely, or use:
tools: ['*']
```

**Enable specific tools**:
```yaml
tools: ['read', 'edit', 'search', 'execute']
```

**Enable MCP server tools**:
```yaml
tools: ['read', 'edit', 'github/*', 'playwright/navigate']
```

**Disable all tools**:
```yaml
tools: []
```

### Standard Tool Aliases

所有別名均不區分大小寫：

| Alias | Alternative Names | Category | Description |
|-------|------------------|----------|-------------|
| `execute` | shell, Bash, powershell | Shell execution | 在對應的 shell 中執行指令 |
| `read` | Read, NotebookRead, view | File reading | 讀取檔案內容 |
| `edit` | Edit, MultiEdit, Write, NotebookEdit | File editing | 編輯和修改檔案 |
| `search` | Grep, Glob, search | Code search | 搜尋檔案或檔案中的文字 |
| `agent` | custom-agent, Task | Agent invocation | 調用其他自訂代理 |
| `web` | WebSearch, WebFetch | Web access | 擷取網頁內容並進行搜尋 |
| `todo` | TodoWrite | Task management | 建立和管理任務清單（僅限 VS Code） |

### Built-in MCP Server Tools

**GitHub MCP Server**:
```yaml
tools: ['github/*']  # All GitHub tools
tools: ['github/get_file_contents', 'github/search_repositories']  # Specific tools
```
- 預設提供所有唯讀工具。
- 令牌作用域限定於來源儲存庫

**Playwright MCP Server**:
```yaml
tools: ['playwright/*']  # All Playwright tools
tools: ['playwright/navigate', 'playwright/screenshot']  # Specific tools
```
- 僅配置為訪問本地主機
- 適用於瀏覽器自動化和測試

### Tool Selection Best Practices

- **Principle of Least Privilege**:僅啟用代理實現其目標所需的工具
- **Security**: 除非明確需要，否則限制 `execute` 訪問
- **Focus**: 工具越少 = 代理目的越清晰，性能越好
- **Documentation**: 註解說明為何特定工具在複雜配置中是必要的

## Sub-Agent Invocation (Agent Orchestration)

代理可以使用代理呼叫工具 （ agent 工具）呼叫其他代理，以協調多步驟工作流程。

建議的方法是 **基於提示的編排**：
- 編排者以自然語言定義逐步工作流程。
- 每個步驟都委派給專門的代理。
- 編排者僅傳遞必要的上下文（例如，基礎路徑、標識符），並要求每個子代理閱讀其自己的 `.agent.md` 規範以獲取工具/約束。

### How It Works

1) 將 agent 新增至編排器的工具清單中，即可啟用代理呼叫：

```yaml
tools: ['read', 'edit', 'search', 'agent']
```

2) 對於每個步驟，透過提供以下資訊來呼叫子代理：
- **Agent name** (使用者選擇/呼叫的識別符)
- **Agent spec path** (要閱讀和遵循的 `.agent.md` 檔案)
- **Minimal shared context** (例如，`basePath`、`projectName`、`logFile`)

### Prompt Pattern (Recommended)

在每個步驟中使用一致的"包裝提示"，以便子代理的行為具有可預測性：

```text
此階段必須以「<AGENT_SPEC_PATH>」中定義的代理「<AGENT_NAME>」的身份執行。

重要提示：
- 閱讀並套用完整的 .agent.md 規格（工具、約束、品質標準）。
- 使用基本路徑「<BASE_PATH>」處理「<WORK_UNIT_NAME>」。
- 在此基本路徑下執行必要的讀取/寫入操作。
- 返回清晰的摘要（已執行的操作 + 已產生/修改的檔案 + 存在的問題）。
```

可選：如果您需要一個輕量級、結構化的包裝器來進行追溯，請在提示符中嵌入一個小的 JSON 區塊（仍然易於閱讀且與工具無關）：

```text
{
  "step": "<STEP_ID>",
  "agent": "<AGENT_NAME>",
  "spec": "<AGENT_SPEC_PATH>",
  "basePath": "<BASE_PATH>"
}
```

### Orchestrator Structure (Keep It Generic)

為了方便維護，請記錄以下結構元素：

- **Dynamic parameters**: 從使用者提取的值（例如，`projectName`、`fileName`、`basePath`）。
- **Sub-agent registry**: 將每個步驟映射到 `agentName` + `agentSpecPath` 的列表/表格。
- **Step ordering**: 明確的順序（步驟 1 → 步驟 N）。
- **Trigger conditions** (可選但建議): 定義何時執行步驟與跳過步驟。
- **Logging strategy** (可選但建議): 在每個步驟後更新單一的日誌/報告檔案。

避免在編排器提示中嵌入編排"程式碼"（JavaScript、Python 等）；建議使用確定性、工具驅動的協調方式。

### Basic Pattern

每個步驟調用都應包含以下內容：

1. **Step description**: 清晰的一行描述（用於日誌和可追溯性）
2. **Agent identity**: `agentName` + `agentSpecPath`
3. **Context**: 一小組明確的變數（路徑、ID、環境名稱）
4. **Expected outputs**: 要創建/更新的檔案及其寫入位置
5. **Return summary**: 請子代理返回一個簡短的結構化摘要

### Example: Multi-Step Processing

```text
Step 1: Transform raw input data
Agent: data-processor
Spec: .github/agents/data-processor.agent.md
Context: projectName=${projectName}, basePath=${basePath}
Input: ${basePath}/raw/
Output: ${basePath}/processed/
Expected: write ${basePath}/processed/summary.md

Step 2: Analyze processed data (depends on Step 1 output)
Agent: data-analyst
Spec: .github/agents/data-analyst.agent.md
Context: projectName=${projectName}, basePath=${basePath}
Input: ${basePath}/processed/
Output: ${basePath}/analysis/
Expected: write ${basePath}/analysis/report.md
```

### Key Points

- **Pass variables in prompts**: 使用 `${variableName}` 來表示所有動態值
- **Keep prompts focused**: 為每個子代理提供清晰、具體的任務
- **Return summaries**: 每個子代理應報告其完成的工作
- **Sequential execution**: 當輸出/輸入之間存在依賴關係時，按順序執行步驟
- **Error handling**: 在進行依賴步驟之前檢查結果

### ⚠️ Tool Availability Requirement

**關鍵點** ：如果子代理程式需要特定工具（例如， edit 、 execute 、 search ），則編排器必須將這些工具包含在其自身的 tools 清單中。子代理程式無法存取其父編排器不可用的工具。

**Example**:
```yaml
# 如果您的子代理程式需要編輯檔案、執行命令或搜尋程式碼，則必須以代理身分執行此階段。
tools: ['read', 'edit', 'search', 'execute', 'agent']
```

編排器的工具權限限制了所有被呼叫子代理程式的功能。請仔細規劃工具列表，確保所有子代理程式都擁有所需的工具。

### ⚠️ Important Limitation

**子代理編排不適用於大規模資料處理。** 避免在以下情況下使用多步驟子代理管道：
- 處理數百或數千個檔案
- 處理大型資料集
- 對大型程式碼庫進行批量轉換
- 編排超過 5-10 個順序步驟

每次子代理調用都會增加延遲和上下文開銷。對於高容量處理，請直接在單個代理中實現邏輯。僅在協調專注且可管理的資料集上的專門任務時使用編排。

## Agent Prompt Structure

前言下方的 Markdown 內容定義了代理人的行為、專業知識和指示。結構良好的提示通常包括：

1. **Agent Identity and Role**: 代理人的身份和主要角色
2. **Core Responsibilities**: 代理人執行的具體任務
3. **Approach and Methodology**: 代理人完成任務的方法和流程
4. **Guidelines and Constraints**: 代理人應遵循的指導方針和限制
5. **Output Expectations**: 代理人應產生的輸出格式和品質

### Prompt Writing Best Practices

- **Be Specific and Direct**: 使用命令式語氣（例如 "分析"、"生成"）；避免模糊的術語
- **Define Boundaries**: 清楚地說明範圍限制和約束
- **Include Context**: 解釋領域專業知識並參考相關框架
- **Focus on Behavior**: 描述代理人應如何思考和工作
- **Use Structured Format**: 使用標題、項目符號和列表使提示易於掃描

## Variable Definition and Extraction

代理人可以定義動態參數以從使用者輸入中提取值，並在代理人的行為和子代理通訊中使用這些值。這使得代理人能夠靈活地適應使用者提供的資料。

### When to Use Variables

**Use variables when**:
- 代理行為取決於使用者輸入
- 需要將動態值傳遞給子代理
- 希望在不同情境中重複使用代理
- 需要參數化的工作流程
- 需要追蹤或引用使用者提供的上下文

**Examples**:
- 從使用者提示中提取專案名稱
- 捕捉管道處理的認證名稱
- 識別檔案路徑或目錄
- 提取配置選項
- 解析功能名稱或模組標識符

### Variable Declaration Pattern

在代理提示符的開頭部分定義變量，以記錄預期參數：

```markdown
# Agent Name

## Dynamic Parameters

- **Parameter Name**: 描述和使用方式
- **Another Parameter**: 如何提取和使用

## Your Mission

Process [PARAMETER_NAME] to accomplish [task].
```

### Variable Extraction Methods

#### 1. **顯式使用者輸入**
如果提示中未偵測到變量，則要求使用者提供該變數：

```markdown
## Your Mission

透過分析程式碼庫來推進專案。

### Step 1: Identify Project
如果未提供項目名稱，請**詢問使用者**以下資訊：
- 項目名稱或識別符
- 基本路徑或目錄位置
- 配置類型（如適用）

利用這些資訊來理解後續所有任務的背景。
```

#### 2. **從提示中隱式擷取變量**
自動從使用者的自然語言輸入中提取變量：

```javascript
// 範例：從使用者輸入中提取認證名稱
const userInput = "Process My Certification";

// Extract key information
const certificationName = extractCertificationName(userInput);
// Result: "My Certification"

const basePath = `certifications/${certificationName}`;
// Result: "certifications/My Certification"
```

#### 3. **上下文變數解析**
使用檔案上下文或工作區資訊來推導變量：

```markdown
## Variable Resolution Strategy

1. **從使用者提示中尋找**：首先，尋找使用者輸入中的明確提示
2. **從檔案上下文中尋找**：檢查目前檔案名稱或路徑
3. **從工作區尋找**：使用工作區資料夾或活動項目
4. **從設定中尋找**：參考設定文件
5. **詢問使用者**：如果以上方法都無效，則請求使用者提供缺失的信息
```

### Using Variables in Agent Prompts

#### Variable Substitution in Instructions

在代理提示中使用模板變量，使其具有動態性：

```markdown
# Agent Name

## Dynamic Parameters
- **Project Name**: ${projectName}
- **Base Path**: ${basePath}
- **Output Directory**: ${outputDir}

## Your Mission

處理位於 `${basePath}` 的 **${projectName}** 專案。

## Process Steps

1. 從 `${basePath}/input/` 讀取輸入
2. 根據專案配置處理文件
3. 將結果寫入 `${outputDir}/`
4. 產生匯總報告

## Quality Standards

- 維護 **${projectName}** 的專案特定程式碼標準
- 遵循目錄結構：`${basePath}/[structure]`
```

#### Passing Variables to Sub-Agents

呼叫子代理程式時，請透過提示符號中的替換變數傳遞所有上下文資訊。建議傳遞路徑和標識符 ，而不是整個文件內容。

Example (prompt template):

```text

此階段必須使用".github/agents/documentation-writer.agent.md"中定義的代理程式"documentation-writer"執行。

重要提示：

- 閱讀並套用完整的「.agent.md」規格。
- 專案："${projectName}"
- 基本路徑："projects/${projectName}"
- 輸入："projects/${projectName}/src/"
- 輸出："projects/${projectName}/docs/"

任務：

1. 讀取輸入路徑下的來源檔案。
2. 產生文檔。
3. 將輸出寫入輸出路徑。
4. 傳回簡要摘要（建立/更新的文件、關鍵決策、問題）。
```

子代理程式接收嵌入在提示中的所有必要上下文。在發送提示之前，變量已解析，因此子代理程式使用具體的路徑和值，而不是變量佔位符。

### Real-World Example: Code Review Orchestrator

一個簡單的程式碼協調器範例，它透過多個專門的代理程式來驗證程式碼：

1) 確定共享上下文：
- `repositoryName`、`prNumber`
- `basePath`（例如，`projects/${repositoryName}/pr-${prNumber}`）
2) 依序呼叫專用代理程式（每個代理程式讀取其自身的 `.agent.md` 規格）：

```text
Step 1: Security Review
Agent: security-reviewer
Spec: .github/agents/security-reviewer.agent.md
Context: repositoryName=${repositoryName}, prNumber=${prNumber}, basePath=projects/${repositoryName}/pr-${prNumber}
Output: projects/${repositoryName}/pr-${prNumber}/security-review.md

Step 2: Test Coverage
Agent: test-coverage
Spec: .github/agents/test-coverage.agent.md
Context: repositoryName=${repositoryName}, prNumber=${prNumber}, basePath=projects/${repositoryName}/pr-${prNumber}
Output: projects/${repositoryName}/pr-${prNumber}/coverage-report.md

Step 3: Aggregate
Agent: review-aggregator
Spec: .github/agents/review-aggregator.agent.md
Context: repositoryName=${repositoryName}, prNumber=${prNumber}, basePath=projects/${repositoryName}/pr-${prNumber}
Output: projects/${repositoryName}/pr-${prNumber}/final-review.md
```

#### Example: 條件步驟編排（程式碼審查）

此範例展示了更完整的編排，包括預檢 、 條件步驟以及必需行為與可選行為。

**Dynamic parameters (inputs):**
- `repositoryName`, `prNumber`
- `basePath` (e.g., `projects/${repositoryName}/pr-${prNumber}`)
- `logFile` (e.g., `${basePath}/.review-log.md`)

**Pre-flight checks (recommended):**
- 驗證預期資料夾/檔案是否存在（例如， ${basePath}/changes/ ， ${basePath}/reports/ ）。
- 偵測影響步驟觸發的高階特徵（例如，倉庫語言、是否存在 `package.json`、`pom.xml`、`requirements.txt`、測試資料夾）。
- 在開始時記錄發現結果。

**Step trigger conditions:**

| Step | Status | Trigger Condition | On Failure |
|------|--------|-------------------|-----------|
| 1: Security Review | **Required** | Always run | Stop pipeline |
| 2: Dependency Audit | Optional | If a dependency manifest exists (`package.json`, `pom.xml`, etc.) | Continue |
| 3: Test Coverage Check | Optional | If test projects/files are present | Continue |
| 4: Performance Checks | Optional | If perf-sensitive code changed OR a perf config exists | Continue |
| 5: Aggregate & Verdict | **Required** | Always run if Step 1 completed | Stop pipeline |

**Execution flow (natural language):**
1. 初始化 basePath 並建立/更新 logFile 。
2. 執行預檢並記錄結果。
3. 依序執行步驟 1 → N。
4. 對每個步驟：
  - 如果觸發條件為假：標記為 **SKIPPED** 並繼續。
  - 否則：使用包裝提示呼叫子代理程式並捕捉其摘要。
  - 標記為 **SUCCESS** 或 **FAILED**。
  - 如果步驟為 **Required** 且失敗：停止管道並寫入失敗摘要。
5. 以最終摘要部分結束（整體狀態、產物、後續行動）。

**Sub-agent invocation prompt (example):**

```text
此階段必須以「.github/agents/security-reviewer.agent.md」中定義的代理程式「security-reviewer」身分執行。

重要提示：
- 閱讀並套用完整的「.agent.md」規格。
- 在倉庫「${repositoryName}」的 PR「${prNumber}」上進行操作。
- 基本路徑：「${basePath}」。

任務：
1. 檢視「${basePath}/changes/」下的變更。
2. 將審查結果寫入「${basePath}/reports/security-review.md」。
3. 傳回一份簡短的摘要，內容包括：關鍵發現、建議的修復方案以及建立/修改的文件。
```

**Logging format (example):**

```markdown
## Step 2: Dependency Audit
**Status:** ✅ SUCCESS / ⚠️ SKIPPED / ❌ FAILED
**Trigger:** package.json present
**Started:** 2026-01-16T10:30:15Z
**Completed:** 2026-01-16T10:31:05Z
**Duration:** 00:00:50
**Artifacts:** reports/dependency-audit.md
**Summary:** [brief agent summary]
```

此模式適用於任何編排場景：提取變量，使用清晰的上下文呼叫子代理，等待結果。


### Variable Best Practices

#### 1. **Clear Documentation**
務必記錄預期使用的變數：

```markdown
## Required Variables
- **projectName**: 項目名稱（字串，必填）
- **basePath**: 專案檔案的根目錄（路徑，必填）

## Optional Variables
- **mode**: 處理模式 - quick/standard/detailed（列舉，預設：standard）
- **outputFormat**: 輸出格式 - markdown/json/html（列舉，預設：markdown）

## Derived Variables
- **outputDir**: 自動設置為 ${basePath}/output
- **logFile**: 自動設置為 ${basePath}/.log.md
```

#### 2. **Consistent Naming**
使用一致的變數命名規範：

```javascript
// Good: Clear, descriptive naming
const variables = {
  projectName,          // 項目名稱
  basePath,            // 專案檔案的根目錄
  outputDirectory,     // 結果保存位置
  processingMode,      // 處理模式（詳細程度）
  configurationPath    // 配置文件位置
};

// Avoid: 含糊不清或前後矛盾
const bad_variables = {
  name,     // 太過通用
  path,     // 不清楚是哪個路徑
  mode,     // 太短
  config    // 太模糊
};
```

#### 3. **Validation and Constraints**
記錄有效值和約束條件：

```markdown
## Variable Constraints

**projectName**:
- Type: string (alphanumeric, hyphens, underscores allowed)
- Length: 1-100 characters
- Required: yes
- Pattern: `/^[a-zA-Z0-9_-]+$/`

**processingMode**:
- Type: enum
- Valid values: "quick" (< 5min), "standard" (5-15min), "detailed" (15+ min)
- Default: "standard"
- Required: no
```

## MCP Server Configuration (Organization/Enterprise Only)

MCP 伺服器透過附加工具擴充代理功能。僅支援組織級和企業級代理程式。

### Configuration Format

```yaml
---
name: my-custom-agent
description: 'Agent with MCP integration'
tools: ['read', 'edit', 'custom-mcp/tool-1']
mcp-servers:
  custom-mcp:
    type: 'local'
    command: 'some-command'
    args: ['--arg1', '--arg2']
    tools: ["*"]
    env:
      ENV_VAR_NAME: ${{ secrets.API_KEY }}
---
```

### MCP Server Properties

- **type**: 伺服器類型（ 'local' 或 'stdio' ）
- **command**: 啟動 MCP 伺服器的命令
- **args**: 命令參數陣列
- **tools**: 從此伺服器啟用的工具（`["*"]` 表示全部）
- **env**: 環境變數（支援 secrets）

### Environment Variables and Secrets

Secrets 必須在倉庫設定中的 "copilot" 環境下配置。

**支援的語法**:
```yaml
env:
  # Environment variable only
  VAR_NAME: COPILOT_MCP_ENV_VAR_VALUE

  # Variable with header
  VAR_NAME: $COPILOT_MCP_ENV_VAR_VALUE
  VAR_NAME: ${COPILOT_MCP_ENV_VAR_VALUE}

  # GitHub Actions-style (YAML only)
  VAR_NAME: ${{ secrets.COPILOT_MCP_ENV_VAR_VALUE }}
  VAR_NAME: ${{ var.COPILOT_MCP_ENV_VAR_VALUE }}
```

## File Organization and Naming

### Repository-Level Agents
- Location: `.github/agents/`
- Scope: Available only in the specific repository
- Access: Uses repository-configured MCP servers

### Organization/Enterprise-Level Agents
- Location: `.github-private/agents/` (then move to `agents/` root)
- Scope: 僅在特定儲存庫中可用
- Access: 可以配置專用的 MCP 伺服器

### Naming Conventions
- 使用小寫字母和連字號: `test-specialist.agent.md`
- 名稱應反映代理的用途
- 文件名成為默認代理名稱（如果未指定 `name`）
- 允許的字符: `.`, `-`, `_`, `a-z`, `A-Z`, `0-9`

## Agent Processing and Behavior

### Versioning
- 基於代理文件的 Git 提交 SHA
- 為不同的代理版本建立分支/標籤
- 使用最新版本實例化特定儲存庫/分支的代理
- PR 互動使用相同的代理版本以保持一致性

### Name Conflicts
優先權（從高到低）：
1. 儲存庫級代理
2. 組織級代理
3. 企業級代理

低層級的配置會覆蓋高層級中具有相同名稱的配置。

### Tool Processing
- `tools` 清單篩選器可用工具（內建工具和 MCP 工具）
- 未指定工具 = 啟用所有工具
- 空列表 (`[]`) = 禁用所有工具
- 指定列表 = 僅啟用那些工具
- 未識別的工具名稱將被忽略（允許環境特定工具）
### MCP Server Processing Order
1. 開箱即用的 MCP 伺服器（例如，GitHub MCP）
2. 自訂代理 MCP 配置（僅限組織/企業）
3. 儲存庫級 MCP 配置

每個層級都可以覆蓋前一層的設置。

## Agent Creation Checklist

### Frontmatter
- [ ] `description` 欄位存在且內容詳實（50-150 個字元）
- [ ] `description` 使用單引號包裹
- [ ] `name` 已指定（可選但建議）
- [ ] `tools` 已適當配置（或故意省略）
- [ ] `model` 已指定以獲得最佳性能
- [ ] `target` 已設置（如果特定於環境）
- [ ] 使用 `user-invocable: false` 隱藏於選擇器，同時允許子代理調用
- [ ] 使用 `disable-model-invocation: true` 防止子代理調用，同時保持選擇器可見


### Prompt Content
- [ ] 清晰定義代理身份和角色
- [ ] 明確列出核心職責
- [ ] 說明方法和流程
- [ ] 指定指南和約束條件
- [ ] 記錄輸出期望
- [ ] 提供範例（如有幫助）
- [ ] 指令具體且可操作
- [ ] 範圍和邊界明確定義
- [ ] 總內容不超過 30,000 個字元

### File Structure
- [ ] 檔案名稱遵循小寫字母加連字符的命名規則
- [ ] 檔案放置在正確的目錄中（`.github/agents/` 或 `agents/`）
- [ ] 檔案名稱僅使用允許的字符
- [ ] 檔案擴展名為 `.agent.md`

### Quality Assurance
- [ ] 代理目的唯一且不重複
- [ ] 工具最小化且必要
- [ ] 指令清晰且無歧義
- [ ] 代理已使用代表性任務進行測試
- [ ] 文件參考資料是最新的
- [ ] 已考慮安全性（如適用）

## Common Agent Patterns

### Testing Specialist
**Purpose**: 專注於測試覆蓋率和質量
**Tools**: 所有工具（用於全面的測試創建）
**Approach**: 分析、識別漏洞、編寫測試，避免修改生產代碼

### Implementation Planner
**Purpose**: 創建詳細的技術計劃和規範
**Tools**: 僅限 `['read', 'search', 'edit']`
**Approach**: 分析需求，創建文檔，避免實施

### Code Reviewer
**Purpose**: 審查代碼質量並提供反饋
**Tools**: 僅限 `['read', 'search']`
**Approach**: 分析、提出改進建議，避免直接修改

### Refactoring Specialist
**Purpose**: 改善代碼結構和可維護性
**Tools**: `['read', 'search', 'edit']`
**Approach**: 分析模式，提出重構建議，安全地實施

### Security Auditor
**Purpose**: 識別安全問題和漏洞
**Tools**: `['read', 'search', 'web']`
**Approach**: 掃描代碼，檢查 OWASP，報告發現

## Common Mistakes to Avoid

### Frontmatter Errors
- ❌ 缺少 description 字段
- ❌ Description 未使用引號包裹
- ❌ 無效的工具名稱，未檢查文檔
- ❌ YAML 語法錯誤（縮進、引號）

### Tool Configuration Issues
- ❌ 過度授予工具訪問權限
- ❌ 缺少代理所需的工具
- ❌ 工具別名使用不一致
- ❌ 忘記 MCP 伺服器命名空間（`server-name/tool`）

### Prompt Content Problems
- ❌ 指令模糊、不明確
- ❌ 指南或約束條件相互矛盾
- ❌ 缺少明確的範圍定義
- ❌ 缺少輸出期望
- ❌ 指令過於冗長（超過字元限制）
- ❌ 缺少複雜任務的範例或上下文

### Organizational Issues
- ❌ 檔案名稱未反映代理目的
- ❌ 錯誤的目錄（混淆儲存庫級與組織級）
- ❌ 檔案名稱中使用空格或特殊字符
- ❌ 重複的代理名稱導致衝突

## Testing and Validation

### Manual Testing
1. 建立具有正確 frontmatter 的代理文件
2. 重新加載 VS Code 或刷新 GitHub.com
3. 從 Copilot Chat 的下拉選單中選擇代理
4. 使用代表性用戶查詢進行測試
5. 驗證工具訪問是否按預期工作
6. 確認輸出符合預期

### Integration Testing
- 使用不同範圍內的文件類型測試代理
- 驗證 MCP 伺服器連接（如果已配置）
- 檢查代理在缺少上下文時的行為
- 測試錯誤處理和邊界情況
- 驗證代理切換和交接

### Quality Checks
- 運行代理創建檢查清單
- 對照常見錯誤清單進行審查
- 與儲存庫中的範例代理進行比較
- 對複雜代理進行同儕審查
- 記錄任何特殊配置需求

## Additional Resources

### Official Documentation
- [Creating Custom Agents](https://docs.github.com/en/copilot/how-tos/use-copilot-agents/coding-agent/create-custom-agents)
- [Custom Agents Configuration](https://docs.github.com/en/copilot/reference/custom-agents-configuration)
- [Custom Agents in VS Code](https://code.visualstudio.com/docs/copilot/customization/custom-agents)
- [MCP Integration](https://docs.github.com/en/copilot/how-tos/use-copilot-agents/coding-agent/extend-coding-agent-with-mcp)

### Community Resources
- [Awesome Copilot Agents Collection](https://github.com/github/awesome-copilot/tree/main/agents)
- [Customization Library Examples](https://docs.github.com/en/copilot/tutorials/customization-library/custom-agents)
- [Your First Custom Agent Tutorial](https://docs.github.com/en/copilot/tutorials/customization-library/custom-agents/your-first-custom-agent)

### Related Files
- [Prompt Files Guidelines](./prompt.instructions.md) - 用於創建提示文件
- [Instructions Guidelines](./instructions.instructions.md) - 用於創建指令文件

## Version Compatibility Notes

### GitHub.com (Coding Agent)
- ✅ 完全支援所有標準 frontmatter 屬性
- ✅ 儲存庫和組織/企業級代理
- ✅ MCP 伺服器配置（組織/企業級）
- ❌ 不支援 `model`、`argument-hint`、`handoffs` 屬性

### VS Code / JetBrains / Eclipse / Xcode
- ✅ 支援 AI 模型選擇的 `model` 屬性
- ✅ 支援 `argument-hint` 和 `handoffs` 屬性
- ✅ 使用者配置檔和工作區級代理
- ❌ 無法在儲存庫級別配置 MCP 伺服器
- ⚠️ 某些屬性可能行為不同

在為多個環境建立代理時，應專注於通用屬性，並在所有目標環境中進行測試。必要時，可使用 `target` 屬性建立特定於環境的代理程式。
