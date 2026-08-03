---
name: "dependency-scanning"
description: "定期或於 CI 中掃描第三方套件已知漏洞（CVE），並規劃升級或緩解策略"
skill: dependency-scanning
version: 1.0
tags: [dependency-scanning, sca, cve]
applicable-to: [Maven, npm]
last-updated: 2026-07-20
---

# 相依套件掃描技能

## 適用情境

定期或於 CI 中掃描第三方套件已知漏洞（CVE），並規劃升級或緩解策略。

## 核心原則

1. 依 CVSS 分數分級處理：CVSS ≥ 9 需在 SLA 內立即處理，中低風險排入例行維護
2. 升級套件版本前需確認相容性（Semantic Versioning、Breaking Change）
3. 無法立即升級時需記錄暫時緩解措施與追蹤升級排程，而非永久抑制
4. 掃描範圍需涵蓋直接依賴與傳遞依賴（Transitive Dependency）

## 標準做法

### CI 中設定失敗門檻

```bash
mvn dependency-check:check \
  -DfailBuildOnCVSS=7 \
  -DsuppressionFile=.github/security/suppressions.xml
```

### 暫時抑制需附到期日與理由

```xml
<suppress until="2026-09-30Z">
  <notes>等待上游函式庫發布修復版本，已建立追蹤 Ticket SEC-2201</notes>
  <cve>CVE-2026-12345</cve>
</suppress>
```

## 禁止事項

- 不得永久抑制（Suppress）已知漏洞而不設到期日與追蹤
- 不得只掃描直接依賴而忽略傳遞依賴中的漏洞
- 不得在 CVSS ≥ 9 的漏洞上僅以「暫時抑制」處理超過 SLA 時限

## 驗收標準

- [ ] CI 掃描涵蓋直接與傳遞依賴
- [ ] 所有抑制規則皆有到期日、理由與對應追蹤 Ticket
- [ ] Critical 漏洞的修復時效符合企業 SLA

## 參考資料

- OWASP Dependency-Check 官方文件
