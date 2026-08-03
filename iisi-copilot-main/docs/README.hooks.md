# 🪝 Hooks

鉤子功能支援在 GitHub Copilot 編碼代理程式會話期間由特定事件觸發的自動化工作流程，例如會話開始、會話結束、使用者提示和工具使用。

## 如何使用 Hooks

**包含內容：**

- 每個鉤子Hooks都是一個資料夾，其中包含一個 README.md 檔案和一個 hooks.json 配置。
- Hooks 可能包含輔助腳本、實用程式或其他捆綁資源
- Hooks 鉤子遵循 [GitHub Copilot hooks 規格](https://docs.github.com/en/copilot/how-tos/use-copilot-agents/coding-agent/use-hooks)

**安裝步驟：**

- C將 hook 資料夾複製到您的倉庫的 .github/hooks/ 目錄中。
- 確保所有捆綁的腳本都是可執行的（ chmod +x script.sh ）
- 將hook提交到倉庫的預設分支

**啟動/使用方法：**

- Hooks 鉤子在 Copilot 編碼代理會話期間自動執行
- 在 hooks.json 檔案中配置鉤子事件
- 可用事件： sessionStart 、 sessionEnd 、 userPromptSubmitted 、 preToolUse 、 postToolUse 、 errorOccurred

**何時使用：**

- Automate session logging and audit trails 自動記錄會話日誌和審計跟踪
- Auto-commit changes at session end 會話結束時自動提交更改
- Track usage analytics 追蹤使用情況分析
- Integrate with external tools and services 外部工具和服務集成
- Custom session workflows 自訂會話工作流程

| Name 姓名 | Description 描述 | Events 事件 | Bundled Assets 捆綁資產 |
| --------- | ---------------- | ----------- | ----------------------- |
