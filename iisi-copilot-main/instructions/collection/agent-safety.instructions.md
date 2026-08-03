---
description: "建構安全、受控的人工智慧代理系統的指導原則。在編寫使用代理框架、工具呼叫層級管理（LLM）或多代理程式編排的程式碼時，應遵循這些原則，以確保適當的安全邊界、策略執行和可審計性。"
applyTo: "**"
---

# Agent Safety & Governance

## Core Principles

- **Fail closed**: 如果治理檢查出錯或存在歧義，則拒絕該操作，而不是允許該操作。
- **Policy as configuration**: 將治理規則定義在 YAML/JSON 文件中，而不是硬編碼在應用程式邏輯中
- **Least privilege**: 代理應僅擁有完成任務所需的最小工具訪問權限
- **Append-only audit**: 永遠不要修改或刪除審計記錄條目 — 不可變性可確保合規性

## Tool Access Controls

- 始終明確定義代理可以使用的工具清單－切勿授予其不受限制的工具存取權限。
- 將工具註冊與工具授權分離——框架知道有哪些工具存在，策略控制哪些工具被允許使用
- 對已知危險操作使用封鎖列表（如 shell 執行、文件刪除、資料庫 DDL）
- 對高影響工具（如發送電子郵件、部署、刪除記錄）要求人工審核
- 對每個請求的工具調用實施速率限制，以防止無限循環和資源耗盡

## Content Safety

- 在將用戶輸入傳遞給代理之前，掃描所有用戶輸入以檢測威脅信號（數據外洩、提示注入、權限提升）
- 過濾代理參數中的敏感模式：API 密鑰、憑證、個人識別信息、SQL 注入
- 使用可以在不更改代碼的情況下更新的正則表達式模式列表
- 檢查用戶的原始提示和代理生成的工具參數

## Multi-Agent Safety

- 每個多代理系統中的代理應該有自己的治理策略
- 當代理委派給其他代理時，應採用最嚴格的策略
- 跟蹤代理委派的信任分數——在失敗時降低信任，要求持續良好行為
- 永遠不要允許內部代理擁有比調用它的外部代理更廣泛的權限

## Audit & Observability

- 記錄每次工具調用，包括：時間戳記、代理 ID、工具名稱、允許/拒絕決定、策略名稱
- 記錄每次治理違規，包括匹配的規則和證據
- 將審計日誌導出為 JSON Lines 格式，以便與日誌聚合系統集成
- 在審計日誌中包含會話邊界（開始/結束）以便關聯

## Code Patterns

編寫代理工具函數時：

```python
# Good: Governed tool with explicit policy
@govern(policy)
async def search(query: str) -> str:
    ...

# Bad: Unprotected tool with no governance
async def search(query: str) -> str:
    ...
```

When defining policies:

```yaml
# Good: Explicit allowlist, content filters, rate limit
name: my-agent
allowed_tools: [search, summarize]
blocked_patterns: ["(?i)(api_key|password)\\s*[:=]"]
max_calls_per_request: 25

# Bad: No restrictions
name: my-agent
allowed_tools: ["*"]
```

When composing multi-agent policies:

```python
# Good: Most-restrictive-wins composition
final_policy = compose_policies(org_policy, team_policy, agent_policy)

# Bad: Only using agent-level policy, ignoring org constraints
final_policy = agent_policy
```

## Framework-Specific Notes

- **PydanticAI**: 使用帶有治理裝飾器包裝器的 @agent.tool 即將推出的 Traits 功能正是為此模式而設計的。
- **CrewAI**: 在 Crew 級別應用治理以涵蓋所有代理。使用 `before_kickoff` 回調進行策略驗證。
- **OpenAI Agents SDK**: 使用治理包裝 `@function_tool`。對多代理信任使用交接守衛。
- **LangChain/LangGraph**: 使用 `RunnableBinding` 或工具包裝器進行治理。在圖邊緣級別應用以控制流程。
- **AutoGen**: 在 `ConversableAgent.register_for_execution` 鉤子中實現治理。

## Common Mistakes

- 僅依賴輸出防護措施（生成後）而非執行前治理
- 將策略規則硬編碼而不是從配置中加載
- 允許代理自我修改其治理策略
- 忘記對工具 _參數_ 進行治理檢查，而不僅僅是工具 _名稱_
- 不隨時間衰減信任分數——過時的信任是危險的
- 在審計日誌中記錄提示——記錄決策和元數據，而不是用戶內容
