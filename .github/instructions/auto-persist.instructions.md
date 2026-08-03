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

| 階段 / Agent          | 產出檔案                                               |
| --------------------- | ------------------------------------------------------ |
| Orchestrator          | docs/orchestrator-plan.md                              |
| Planner               | docs/requirements/requirements.md                      |
| Architect（設計）     | docs/design/design.md                                  |
| Architect（拆解）     | docs/design/tasks.md                                   |
| Architect（威脅建模） | docs/security/threat-model.md                          |
| Backend               | src/**（程式碼）＋ docs/design/implementation-notes.md |
| Test Generator        | src/test/** ＋ docs/TestReport.md                      |
| Code Reviewer         | docs/review/code-review-YYYYMMDD.md                    |
| Security Reviewer     | docs/security/security-review-YYYYMMDD.md              |
| Doc Writer            | README.md、docs/api/*.md                               |
| Release               | CHANGELOG.md、docs/ReleaseNote.md                      |
