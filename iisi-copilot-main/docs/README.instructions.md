# 📋 Custom Instructions 自訂指令

針對特定技術和編碼實踐，增強 GitHub Copilot 行為的團隊和專案特定指令。

### 如何使用自訂指令
  
**安裝方式:**
- 點擊您想使用的指令的 **VS Code** 或 **VS Code Insiders** 安裝按鈕
- 下載 `*.instructions.md` 檔案並手動將其添加到專案的指令集合中

**使用方式:**

- 將這些指令複製到工作區的 `.github/copilot-instructions.md` 檔案中
- 在工作區的 `.github/instructions/` 資料夾中建立任務特定的 `*.instructions.md` 檔案（例如，`.github/instructions/my-csharp-rules.instructions.md`）
- 指令安裝到工作區後會自動應用於 Copilot 行為

| 標題 | 描述 |
| ----- | ----------- |