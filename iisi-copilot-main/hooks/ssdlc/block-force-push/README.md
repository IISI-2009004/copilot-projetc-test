---
name: "Block Force Push"
description: "PreToolUse Hook，阻擋 Agent 執行 git push --force、git reset --hard 等破壞性 Git 操作，保護版本控制歷史完整性。"
tags:
  - ssdlc
  - security
  - git
  - pre-tool-use
---

# Block Force Push

PreToolUse Hook，防止 Agent 執行破壞性的 Git 操作。

## 阻擋的操作

| 操作                               | 風險                                           |
| ---------------------------------- | ---------------------------------------------- |
| `git push --force` / `git push -f` | 覆蓋遠端歷史，可能導致其他人的 commit 遺失     |
| `git reset --hard`                 | 丟棄未提交的工作區變更                         |
| `git clean -fdx`                   | 刪除所有未追蹤檔案（含 .gitignore 排除的檔案） |
| `git checkout -- .`                | 丟棄所有工作區變更                             |
| `git rebase` on shared branch      | 可能破壞共享分支歷史                           |

## 使用方式

```bash
cp hooks.json .github/hooks/block-force-push.json
cp scripts/block-force-push.sh ./scripts/
chmod +x ./scripts/block-force-push.sh
```
