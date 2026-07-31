---
description: '透過更好的上下文管理，建立程式碼和專案結構以最大限度地提高 GitHub Copilot 的效用，並提供相關指南。'
applyTo: '**'
---

# Context Engineering

幫助 GitHub Copilot 理解你的代碼庫並提供更好建議的原則。

## Project Structure 專案結構

- **使用描述性文件路徑**：`src/auth/middleware.ts` > `src/utils/m.ts`。Copilot 使用路徑來推斷意圖。
- **將相關代碼放在一起**：將組件、測試、類型和鉤子放在一起。一個搜索模式應該能找到所有相關內容。
- **從 index 文件導出公共 API**：導出的內容是契約；未導出的內容是內部實現。這有助於 Copilot 理解邊界。

## Code Patterns  代碼模式

- **優先使用明確類型而非推斷**：類型註解是上下文。`function getUser(id: string): Promise<User>` 比 `function getUser(id)` 告訴 Copilot 更多信息。
- **使用語義化名稱**：`activeAdultUsers` > `x`。自我描述的代碼是 AI 可讀的代碼。
- **定義常量**：`MAX_RETRY_ATTEMPTS = 3` > 魔法數字 `3`。命名值承載意義。

## Working with Copilot 與 Copilot 合作

- **保持相關文件在標籤中打開**：Copilot 使用打開的標籤作為上下文信號。正在處理身份驗證？打開與身份驗證相關的文件。
- **有意地定位光標**：Copilot 優先考慮光標附近的代碼。將光標放在上下文重要的位置。
- **對於複雜任務使用 Copilot Chat**：內聯補全的上下文有限。聊天模式可以看到更多文件。

## Context Hints 上下文提示

- **添加 COPILOT.md 文件**：記錄架構決策、模式和 Copilot 應遵循的約定。
- **使用策略性註解**：在複雜模組的頂部，簡要描述流程或目的。
- **明確引用模式**："遵循與 `src/api/users.ts` 相同的模式" 為 Copilot 提供具體範例。

## Multi-File Changes 多文件更改

- **首先描述範圍**: 告訴 Copilot 所有涉及的文件，然後再請求更改。"我需要更新 User 模型、API 端點和測試。"
- **逐步進行**：一次處理一個文件，驗證每個更改。不要一次性要求所有更改。
- **檢查理解**：在複雜重構之前，詢問 "你需要查看哪些文件？"。

## When Copilot Struggles 當 Copilot 遇到困難時

- **缺少上下文**：打開相關文件標籤，或明確粘貼代碼片段。
- **建議過時**：Copilot 可能看不到最近的更改。重新打開文件或重啟會話。
- **答案過於通用**：更具體一些。添加約束，提及框架，引用現有代碼。
