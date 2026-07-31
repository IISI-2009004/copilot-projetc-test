---
name: "pr-checker"
description: '檢查 PR 是否符合團隊規範，包含 commit message、變更範圍、測試覆蓋'
---

# PR Checker Skill

## 能力
此 Skill 可以：
1. 驗證 commit message 格式（Conventional Commits）
2. 檢查 PR 描述完整性
3. 驗證測試覆蓋率
4. 檢查是否有敏感資訊洩漏
5. 確認安全審查已完成

## Commit Message 格式

```
<type>(<scope>): <description>

type: feat|fix|refactor|test|docs|chore|security
scope: 可選，模組名稱
description: 簡短描述（≤ 72 字元）
```

## PR 檢查清單
- [ ] PR 標題符合 Conventional Commits 格式
- [ ] PR 描述包含變更摘要
- [ ] 所有測試通過
- [ ] 測試覆蓋率 ≥ 80%
- [ ] 無敏感資訊（密碼、Token、API Key）
- [ ] 安全審查已完成（若涉及安全變更）
- [ ] 文件已更新（若涉及 API 變更）
- [ ] Breaking Change 已標註
