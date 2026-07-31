---
name: "sast-analysis"
description: '在 CI/CD 中對程式碼進行靜態安全與品質掃描，或分析掃描報告並提出修復方案時使用'
skill: sast-analysis
version: 1.0
tags: [sast, static-analysis, sonarqube]
applicable-to: [Java, SonarQube]
last-updated: 2026-07-20
---

# 靜態分析技能（SAST）

## 適用情境
在 CI/CD 中對程式碼進行靜態安全與品質掃描，或分析掃描報告並提出修復方案時使用。

## 核心原則
1. Quality Gate 是品質門禁，未通過不得合併
2. 修復需驗證掃描工具的誤判（False Positive），但不得以「誤判」為由忽略真實弱點
3. 新增的 Critical/High 問題優先於既有的技術債務處理
4. 掃描規則集需版本化，避免不同環境掃描結果不一致

## 標準做法

### Quality Gate 檢查（CI 中自動阻擋）
```bash
status=$(curl -s "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=$PROJECT_KEY" \
  -H "Authorization: Bearer $SONAR_TOKEN" | jq -r '.projectStatus.status')
if [ "$status" != "OK" ]; then
  echo "Quality Gate 未通過: $status"
  exit 1
fi
```

### 誤判標記需附理由（而非直接忽略）
```java
@SuppressWarnings("java:S3776") // 複雜度來自狀態機的窮舉分支，已拆分為子函式，經團隊審查接受
public void handleStateTransition(State from, Event event) { ... }
```

## 禁止事項
- 不得在未經審查的情況下大量 `@SuppressWarnings` 掩蓋掃描結果
- 不得只掃描新增程式碼而放任既有的 Critical 問題累積
- 不得在 CI 中跳過 Quality Gate 檢查（除非有明確的例外審批流程）

## 驗收標準
- [ ] CI 中 Quality Gate 檢查為強制關卡，無法繞過
- [ ] 所有誤判標記皆附有審查理由與簽核紀錄
- [ ] Critical/High 等級問題的平均修復時間（MTTR）有追蹤指標

## 參考資料
- SonarQube 官方文件 — Quality Gates
