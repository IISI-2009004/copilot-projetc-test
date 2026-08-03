# 1. 建立 Agent Skills

## 1.1 Skills 概念

Agent Skills 是可被 Agent 自動載入的**專業能力模組**，基於開放標準（[agentskills.io](https://agentskills.io)）。每個 Skill 定義了特定的能力，包含工具、腳本、範本與指引。

### Skills 儲存位置

Skills 支援多種儲存路徑（專案層級與個人層級）：

**專案層級**（納入版本控管）：

| 路徑                                   | 格式        | 說明                           |
| -------------------------------------- | ----------- | ------------------------------ |
| `.github/skills/<skill-name>/SKILL.md` | GitHub 原生 | 主要推薦路徑                   |
| `.claude/skills/<skill-name>/SKILL.md` | Claude 相容 | 跨 VS Code 與 Claude Code 共用 |
| `.agents/skills/<skill-name>/SKILL.md` | 通用格式    | 開放標準格式                   |

**個人層級**（跨工作區）：

| 路徑                                      | 說明                       |
| ----------------------------------------- | -------------------------- |
| `~/.copilot/skills/<skill-name>/SKILL.md` | GitHub Copilot 個人 Skills |
| `~/.claude/skills/<skill-name>/SKILL.md`  | Claude Code 個人 Skills    |
| `~/.agents/skills/<skill-name>/SKILL.md`  | 通用個人 Skills            |

### `gh skill` CLI 命令

使用 GitHub CLI 探索與安裝社群 Skills：

```bash
# 探索可用的 Skills
gh skill search "security review"

# 安裝 Skill 到目前專案
gh skill install <skill-name>

# 列出已安裝的 Skills
gh skill list
```

> 💡 **組織/企業層級 Skills**：目前為「Coming Soon」狀態，未來將支援在 `.github-private` repo 中定義組織級 Skills。

### Skills vs Instructions vs Prompts

| 特性                   | Skills                           | Instructions            | Prompts            |
| ---------------------- | -------------------------------- | ----------------------- | ------------------ |
| **用途**               | 可執行的專業能力                 | 靜態規則與限制          | 任務範本           |
| **包含腳本**           | ✓ 可包含可執行腳本               | ✗                       | ✗                  |
| **包含範本**           | ✓ 可包含檔案範本                 | ✗                       | ✗                  |
| **觸發方式**           | 相關時自動載入                   | 自動套用                | 手動選用           |
| **支援 Slash Command** | ✓（透過 `name` frontmatter）     | ✗                       | ✓（`/`）           |
| **版本控管**           | ✓                                | ✓                       | ✓                  |
| **放置位置**           | `.github/skills/{name}/SKILL.md` | `.github/instructions/` | `.github/prompts/` |

### SKILL.md Frontmatter

| 欄位          | 類型   | 必要 | 說明                               |
| ------------- | ------ | ---- | ---------------------------------- |
| `name`        | string | ✓    | Skill 名稱（可作為 Slash Command） |
| `description` | string | ✓    | Skill 描述（AI 用來判斷是否相關）  |

## 1.2 Skill 1 — Security Review

**目錄結構**：

```
.github/skills/security-review/
├── SKILL.md
└── scripts/
    └── owasp-check.sh
```

**檔案**：`.github/skills/security-review/SKILL.md`

```markdown
---
name: "security-review"
description: "執行 OWASP Top 10 安全審查，識別程式碼中的安全漏洞"
---

# Security Review Skill

## 能力

此 Skill 可以：

1. 基於 OWASP Top 10 審查程式碼安全性
2. 識別常見的安全漏洞模式
3. 執行依賴安全掃描
4. 產出結構化的安全審查報告

## 使用方式

當需要進行安全審查時，請：

1. **識別審查範圍**：確定要審查的檔案或模組
2. **執行靜態分析**：掃描程式碼中的安全漏洞模式
3. **依賴掃描**：如果存在 `pom.xml`，執行：

   ./scripts/owasp-check.sh

4. **產出報告**：使用以下格式

## 安全漏洞檢查清單

### A01 - Broken Access Control

- [ ] 是否有缺少授權檢查的端點
- [ ] 是否有 IDOR（Insecure Direct Object Reference）
- [ ] CORS 配置是否正確

### A02 - Cryptographic Failures

- [ ] 是否使用了過時的加密算法（MD5、SHA-1、DES）
- [ ] 密碼是否使用 BCrypt/Argon2 雜湊
- [ ] 敏感資料是否加密傳輸

### A03 - Injection

- [ ] SQL 查詢是否使用參數化
- [ ] 是否有 OS Command Injection 風險
- [ ] 是否有 LDAP/XPath Injection

### A04 - Insecure Design

- [ ] 是否實作 Rate Limiting
- [ ] 是否有適當的輸入驗證

### A05 - Security Misconfiguration

- [ ] 是否暴露了 debug/stack trace 資訊
- [ ] 預設帳號/密碼是否已移除

## 報告格式

## 安全審查報告

**掃描日期**：{date}
**掃描範圍**：{scope}

| #   | OWASP 類別 | 風險等級 | 檔案 | 行號 | 說明 | 修正建議 |
| --- | ---------- | -------- | ---- | ---- | ---- | -------- |
```

**檔案**：`.github/skills/security-review/scripts/owasp-check.sh`

```bash
#!/bin/bash
# OWASP Dependency Check 執行腳本
echo "=== OWASP Dependency Check ==="
if [ -f "pom.xml" ]; then
    mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7
elif [ -f "package.json" ]; then
    npm audit --audit-level=high
elif [ -f "requirements.txt" ]; then
    pip-audit -r requirements.txt
else
    echo "未找到支援的依賴管理檔案"
fi
```

## 1.3 Skill 2 — JUnit Test Generator

**目錄結構**：

```
.github/skills/junit-generator/
├── SKILL.md
└── templates/
    └── test-template.java
```

**檔案**：`.github/skills/junit-generator/SKILL.md`

```markdown
---
name: "junit-generator"
description: "產生 JUnit 5 單元測試，遵循 AAA 模式與團隊命名規範"
---

# JUnit Test Generator Skill

## 能力

此 Skill 可以：

1. 分析 Java 類別結構
2. 為每個 public 方法產生測試
3. 產生邊界值與異常路徑測試
4. 使用 Mockito Mock 外部依賴

## 測試範本

使用 `templates/test-template.java` 作為基礎範本。

## 產生規則

1. **測試類別命名**：`{被測類別}Test`
2. **測試方法命名**：`should_{預期行為}_When_{條件}`
3. **每個方法至少 3 個測試**：
   - 正常路徑（Happy Path）
   - 邊界值（Boundary）
   - 異常路徑（Error Path）
4. **使用 @DisplayName** 提供可讀的測試描述
5. **使用 AssertJ** 進行斷言

## 覆蓋率目標

- 行覆蓋率 ≥ 80%
- 分支覆蓋率 ≥ 70%
```

## 1.4 Skill 3 — PR Checker

**檔案**：`.github/skills/pr-checker/SKILL.md`

```markdown
---
name: "pr-checker"
description: "檢查 PR 是否符合團隊規範，包含 commit message、變更範圍、測試覆蓋"
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

<type>(<scope>): <description>

type: feat|fix|refactor|test|docs|chore|security
scope: 可選，模組名稱
description: 簡短描述（≤ 72 字元）

## PR 檢查清單

- [ ] PR 標題符合 Conventional Commits 格式
- [ ] PR 描述包含變更摘要
- [ ] 所有測試通過
- [ ] 測試覆蓋率 ≥ 80%
- [ ] 無敏感資訊（密碼、Token、API Key）
- [ ] 安全審查已完成（若涉及安全變更）
- [ ] 文件已更新（若涉及 API 變更）
- [ ] Breaking Change 已標註
```

## 1.5 Skill 4 — API Reviewer

**檔案**：`.github/skills/api-reviewer/SKILL.md`

```markdown
---
name: "api-reviewer"
description: "審查 RESTful API 設計，確保符合企業 API 標準"
---

# API Reviewer Skill

## 能力

審查 API 端點設計是否符合以下標準：

## API 設計規範

### URL 設計

- 使用名詞複數（`/users`，不用 `/user`）
- 使用 kebab-case（`/user-profiles`，不用 `/userProfiles`）
- 版本號在路徑中（`/api/v1/users`）
- 巢狀資源最多 2 層（`/users/{id}/orders`）

### HTTP Method

| Method | 用途     | 冪等 | 安全 |
| ------ | -------- | ---- | ---- |
| GET    | 查詢資源 | ✓    | ✓    |
| POST   | 建立資源 | ✗    | ✗    |
| PUT    | 完整更新 | ✓    | ✗    |
| PATCH  | 部分更新 | ✗    | ✗    |
| DELETE | 刪除資源 | ✓    | ✗    |

### 回應狀態碼

| 狀態碼 | 用途                |
| ------ | ------------------- |
| 200    | 成功（含回應 body） |
| 201    | 建立成功            |
| 204    | 成功（無 body）     |
| 400    | 請求格式錯誤        |
| 401    | 未認證              |
| 403    | 未授權              |
| 404    | 資源不存在          |
| 409    | 衝突                |
| 422    | 驗證失敗            |
| 500    | 伺服器錯誤          |

### 安全要求

- 所有端點必須定義認證方式
- 敏感操作需要額外的授權檢查
- 回應中不可包含內部實作細節
```

## 1.6 Skill 5 — Reverse Analysis

**檔案**：`.github/skills/reverse-analysis/SKILL.md`

```markdown
---
name: "reverse-analysis"
description: "分析遺留系統模組，產出架構文件與依賴圖"
---

# Reverse Analysis Skill

## 能力

此 Skill 可以：

1. 掃描模組目錄結構
2. 分析程式碼依賴關係
3. 提取業務邏輯規則
4. 產出 Mermaid 架構圖
5. 評估技術債務

## 分析模板

### 模組分析報告模板

使用 `templates/module-report.md` 格式。

### 依賴關係圖模板

使用 `templates/dependency-map.md` 格式。

## 分析流程

1. 列出模組內所有檔案及行數
2. 識別進入點（Controller、Main、Scheduler）
3. 追蹤核心流程的呼叫鏈
4. 建立依賴關係圖
5. 識別外部系統整合點
6. 評估程式碼品質與技術債務
7. 提出現代化建議
```

## 1.7 Skill 6 — Doc Generator

**檔案**：`.github/skills/doc-generator/SKILL.md`

````markdown
---
name: "doc-generator"
description: "根據原始碼自動產生技術文件，包含 API 文件、架構說明、使用者指南"
---

# Doc Generator Skill

## 能力

此 Skill 可以：

1. 掃描原始碼產生 API 文件
2. 根據架構產生架構說明文件
3. 根據功能產生使用者指南
4. 產生 Mermaid 圖表

## 文件模板

### API 文件格式

使用 `templates/api-doc-template.md`：

```markdown
# API 文件：{API 名稱}

## 概述

{API 用途說明}

## 端點

### {Method} {Path}

**描述**：{端點說明}

**認證**：{認證方式}

**Request**：

| 參數 | 類型 | 必要 | 說明 |
| ---- | ---- | ---- | ---- |

**Response**：
json
{
"範例回應"
}

**錯誤碼**：

| 狀態碼 | 說明 |
| ------ | ---- |

## 撰寫原則

1. 使用繁體中文
2. 技術名詞保留英文
3. 每個概念配合程式碼範例
4. 使用 Mermaid 繪製流程圖
```
````

## 1.9 Skill Library Collections

除了上述以任務流程為主的 Skills 之外，`skills/collections/` 收錄了依技術主題分類的企業級 Skill Library，依教學手冊 4.3.1 節目錄結構與第十五章內容落地，共 5 大分類、26 個 Skill：

| 分類         | 路徑                               | 涵蓋技能                                                         |
| ------------ | ---------------------------------- | ---------------------------------------------------------------- |
| Coding       | `skills/collections/coding/`       | Spring Boot、Vue3+TS、Clean Code、設計模式、API 設計、資料庫優化 |
| Architecture | `skills/collections/architecture/` | 微服務、六角形架構、DDD、事件驅動、Cloud Native                  |
| Testing      | `skills/collections/testing/`      | JUnit 5、整合測試、E2E（Playwright）、效能測試、安全測試         |
| Security     | `skills/collections/security/`     | OWASP Top 10、SAST、相依套件掃描、秘密管理、威脅建模             |
| Migration    | `skills/collections/migration/`    | Java 升級、Struts 遷移、JSP 遷移、Spring Boot 升級、單體轉微服務 |

每個 Skill 皆為 `skills/collections/<category>/<skill-name>/SKILL.md`，frontmatter 符合本節「SKILL.md Frontmatter」規範（`name` + `description`），完整清單見 [skills/collections/README.md](collections/README.md)。

## 1.8 Skills 管理策略

| 策略              | 說明                           |
| ----------------- | ------------------------------ |
| **版本控管**      | 所有 Skills 納入 Git           |
| **獨立目錄**      | 每個 Skill 一個獨立目錄        |
| **SKILL.md 必要** | 每個 Skill 目錄必須有 SKILL.md |
| **腳本可執行**    | 確保腳本有正確的執行權限       |
| **定期更新**      | 隨專案需求演進更新 Skill 內容  |
| **團隊審核**      | 新 Skill 需經 PR 審核          |

# 2. skills 參考

- Pm Skills教學手冊 https://chihhung.github.io/Blog/posts/%E6%95%99%E5%AD%B8/ai%E9%96%8B%E7%99%BC/pm-skills%E6%95%99%E5%AD%B8%E6%89%8B%E5%86%8A/

- Addyosmani Agent Skills 教學手冊 https://chihhung.github.io/Blog/posts/%E6%95%99%E5%AD%B8/ai%E9%96%8B%E7%99%BC/addyosmani-agent-skills-%E6%95%99%E5%AD%B8%E6%89%8B%E5%86%8A/

- Mattpocock Skills 教學手冊 https://chihhung.github.io/Blog/posts/%E6%95%99%E5%AD%B8/ai%E9%96%8B%E7%99%BC/mattpocock-skills-%E6%95%99%E5%AD%B8%E6%89%8B%E5%86%8A/

- Skill_Seekers教學手冊 https://chihhung.github.io/Blog/posts/%E6%95%99%E5%AD%B8/ai%E9%96%8B%E7%99%BC/skill_seekers%E6%95%99%E5%AD%B8%E6%89%8B%E5%86%8A/

- Agent Skills教學手冊 https://chihhung.github.io/Blog/posts/%E6%95%99%E5%AD%B8/ai%E9%96%8B%E7%99%BC/agent-skills%E6%95%99%E5%AD%B8%E6%89%8B%E5%86%8A/

- Claude Agent Skills教學手冊 https://chihhung.github.io/Blog/posts/%E6%95%99%E5%AD%B8/ai%E9%96%8B%E7%99%BC/claude-agent-skills%E6%95%99%E5%AD%B8%E6%89%8B%E5%86%8A/

- Anthropic Cybersecurity Skills 教學手冊 https://chihhung.github.io/Blog/posts/%E6%95%99%E5%AD%B8/ai%E9%96%8B%E7%99%BC/anthropic-cybersecurity-skills-%E6%95%99%E5%AD%B8%E6%89%8B%E5%86%8A/

- Eve 教學手冊 https://chihhung.github.io/Blog/posts/%E6%95%99%E5%AD%B8/ai%E9%96%8B%E7%99%BC/eve-%E6%95%99%E5%AD%B8%E6%89%8B%E5%86%8A/

---
