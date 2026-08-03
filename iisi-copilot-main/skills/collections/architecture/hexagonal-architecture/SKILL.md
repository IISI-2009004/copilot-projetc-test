---
name: "hexagonal-architecture"
description: "希望核心商業邏輯不被框架、資料庫或外部服務綁死，以利長期維護與測試時採用"
skill: hexagonal-architecture
version: 1.0
tags: [hexagonal-architecture, ports-and-adapters, clean-architecture]
applicable-to: [Java, Spring Boot]
last-updated: 2026-07-20
---

# 六角形架構技能

## 適用情境

希望核心商業邏輯不被框架、資料庫或外部服務綁死，以利長期維護與測試時採用。

## 核心原則

1. Domain 層不得依賴 Spring/JPA 等框架註解
2. 對外的輸入輸出一律透過 Port（介面）定義，Adapter 實作細節
3. 依賴方向永遠指向內（Adapter → Domain，Domain 不知道 Adapter 的存在）
4. 每個 Adapter 可被替換（例如 REST Adapter 換成 gRPC Adapter）而不動 Domain

## 標準做法

### Port（介面）定義於 Domain

```java
// domain 層，不依賴任何框架
public interface AccountRepositoryPort {
    Optional<Account> findById(AccountId id);
    void save(Account account);
}
```

### Adapter 實作（infrastructure 層）

```java
@Repository
@RequiredArgsConstructor
public class JpaAccountRepositoryAdapter implements AccountRepositoryPort {
    private final AccountJpaRepository jpaRepository;

    public Optional<Account> findById(AccountId id) {
        return jpaRepository.findById(id.value()).map(AccountMapper::toDomain);
    }

    public void save(Account account) {
        jpaRepository.save(AccountMapper.toEntity(account));
    }
}
```

## 禁止事項

- 不得在 Domain 層 import `jakarta.persistence.*` 或 `org.springframework.*`
- 不得讓 Application Service 直接依賴 Adapter 的具體類別
- 不得為求方便讓 Domain 物件與 JPA Entity 共用同一個類別

## 驗收標準

- [ ] Domain 層無框架依賴，可獨立編譯測試
- [ ] 所有外部互動（DB、外部 API、訊息佇列）皆透過 Port 定義
- [ ] 更換 Adapter 實作不需修改 Domain 或 Application 層程式碼

## 參考資料

- Alistair Cockburn — Hexagonal Architecture 原始論文
