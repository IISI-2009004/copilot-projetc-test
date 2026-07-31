# ⚡ Agentic Workflows 智能體工作流程

[Agentic Workflows](https://github.github.com/gh-aw) 是人工智慧驅動的程式碼倉庫自動化工具，它在 GitHub Actions 中運行編碼代理程式。這些代理程式使用 Markdown 語言和自然語言指令進行定義，支援事件觸發和定時自動化，並內建安全防護機制和安全優先設計。

## 如何使用 Agentic 工作流程

**包含內容：**

- 每個工作流程都是一個單獨的 .md 文件，其中包含 YAML 前元資料和自然語言指令。
- 工作流程透過 gh aw compile 編譯成 .lock.yml GitHub Actions 檔案。
- 工作流程遵循 [GitHub Agentic Workflows specification](https://github.github.com/gh-aw)

**安裝步驟：**

- 安裝 gh aw CLI 擴充： gh extension install github/gh-aw
- 將工作流程 .md 檔案複製到您的倉庫的 .github/workflows/ 目錄中。
- 使用 gh aw compile 編譯產生 .lock.yml 文件
- Commit 提交 .md 和 .lock.yml 文件

**啟動/使用方法：**

- 工作流程會根據其配置的觸發器（計畫、事件、斜線命令）自動運行。
- 使用 `gh aw run <workflow>` 來觸發手動運行
- 使用 `gh aw status` 和 `gh aw logs` 指令監控運作狀況

**何時使用：**

- Automate issue triage and labeling 自動進行問題分類和標記
- Generate daily status reports 產生每日狀態報告
- Maintain documentation automatically 自動維護文檔
- Run scheduled code quality checks 運行計劃的程式碼品質檢查
- Respond to slash commands in issues and PRs 在問題和 PR 中回复斜杠命令
- Orchestrate multi-step repository automation 協調多步驟儲存庫自動化

| Name 姓名 | Description 描述 | Triggers  觸發器 |
| ---- | ----------- | -------- |
