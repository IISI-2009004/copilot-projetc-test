---
name: "security-testing"
description: '新功能上線前或既有 API 定期安全驗證，涵蓋認證、授權、輸入驗證等面向'
skill: security-testing
version: 1.0
tags: [security-testing, dast, penetration-testing]
applicable-to: [Web API]
last-updated: 2026-07-20
---

# 安全測試技能

## 適用情境
新功能上線前或既有 API 定期安全驗證，涵蓋認證、授權、輸入驗證等面向。

## 核心原則
1. 對照 OWASP Top 10（見 owasp-top10 技能）逐項驗證，而非隨機測試
2. 授權測試需涵蓋「水平權限提升」（存取他人資料）與「垂直權限提升」（低權限操作高權限功能）
3. 安全測試案例需納入自動化迴歸測試，避免修復後又回退
4. 高風險發現（如可繞過認證）需立即升級處理，不等排程

## 標準做法

### 水平權限提升測試
```java
@Test
@DisplayName("使用者 A 不應能查詢使用者 B 的帳戶")
void should_reject_access_to_other_users_account() {
    String tokenForUserA = login("userA");
    var response = given()
        .header("Authorization", "Bearer " + tokenForUserA)
        .when().get("/api/v1/accounts/{id}", accountIdOfUserB)
        .then().extract().response();

    assertThat(response.statusCode()).isEqualTo(403);
}
```

## 禁止事項
- 不得只測試「登入後可以做什麼」，忽略「登入後不該做什麼」
- 不得在生產環境執行滲透測試（DAST）除非已取得正式授權窗口
- 不得將發現的安全弱點只記錄不追蹤修復進度

## 驗收標準
- [ ] OWASP Top 10 對應項目皆有自動化測試案例覆蓋
- [ ] 水平/垂直權限提升測試已納入 CI 迴歸測試
- [ ] 高風險（CRITICAL/HIGH）發現皆有對應的修復 Ticket 與時限

## 參考資料
- OWASP Testing Guide
- `skills/collections/security/owasp-top10` OWASP Top 10 技能
