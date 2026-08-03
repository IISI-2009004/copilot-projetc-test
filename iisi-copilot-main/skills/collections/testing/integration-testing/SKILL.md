---
name: "integration-testing"
description: "驗證多個元件（Controller + Service + 真實資料庫）協同運作是否正確時使用"
skill: integration-testing
version: 1.0
tags: [integration-testing, testcontainers, spring-boot-test]
applicable-to: [Spring Boot, Testcontainers]
last-updated: 2026-07-20
---

# 整合測試技能

## 適用情境

驗證多個元件（Controller + Service + 真實資料庫）協同運作是否正確時使用。

## 核心原則

1. 使用 Testcontainers 啟動真實資料庫，禁止用 H2 模擬 PostgreSQL/Oracle 行為
2. 每個測試前重置資料狀態，測試之間互不影響
3. 只在關鍵流程（如跨表交易、Migration）使用整合測試，其餘用單元測試涵蓋
4. 整合測試需能在 CI 中穩定執行，避免依賴外部網路服務

## 標準做法

### Testcontainers + SpringBootTest

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AccountIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16").withDatabaseName("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
    }

    @Test
    void should_persist_transfer_across_two_accounts() {
        // 呼叫真實 API，驗證真實資料庫狀態
    }
}
```

## 禁止事項

- 不得用記憶體資料庫（H2）取代真實資料庫種類做整合測試
- 不得讓測試相依於固定的外部服務（改用 WireMock/Testcontainers 模擬）
- 不得省略測試後的資料清理，造成測試間互相污染

## 驗收標準

- [ ] 使用與生產環境相同種類的資料庫（透過 Testcontainers）
- [ ] 測試可在 CI 環境穩定重複執行，無 Flaky 現象
- [ ] 涵蓋跨元件的關鍵交易路徑（非僅單一類別）

## 參考資料

- Testcontainers 官方文件（https://testcontainers.com）
