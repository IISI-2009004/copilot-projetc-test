---
name: "java-spring-boot"
description: "在 Java 21 + Spring Boot 3.x 專案中新增或修改後端功能，包含 Controller、Service、Repository 三層的實作"
skill: java-spring-boot
version: 1.0
tags: [java, spring-boot, backend]
applicable-to: [Java 21, Spring Boot 3.x]
last-updated: 2026-07-20
---

# Spring Boot 開發技能

## 適用情境

在 Java 21 + Spring Boot 3.x 專案中新增或修改後端功能，包含 Controller、Service、Repository 三層的實作。

## 核心原則

1. 遵循 Hexagonal Architecture：Controller 只負責協定轉換，商業邏輯放在 Service
2. 一律使用建構子注入（Constructor Injection），禁止 Field Injection
3. 使用 Java 21 Record 作為 DTO，搭配 Bean Validation 註解
4. 所有對外行為必須有對應的 JUnit 5 測試

## 標準做法

### Controller 層

```java
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/{id}/transfer")
    public ResponseEntity<TransferResult> transfer(
            @PathVariable Long id,
            @RequestBody @Valid TransferRequest request) {
        return ResponseEntity.ok(accountService.transfer(id, request));
    }
}
```

### Service 層（商業邏輯與交易邊界）

```java
@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {
    private final AccountRepository accountRepository;

    public TransferResult transfer(Long fromId, TransferRequest request) {
        Account from = accountRepository.findByIdForUpdate(fromId)
            .orElseThrow(() -> new AccountNotFoundException(fromId));
        from.withdraw(request.amount());
        accountRepository.save(from);
        return new TransferResult(from.getId(), from.getBalance());
    }
}
```

## 禁止事項

- 不得在 Controller 直接呼叫 Repository
- 不得使用 `@Autowired` Field Injection
- 不得在 Service 方法簽章外洩漏 JPA Entity（回傳需轉換為 DTO/Record）
- 不得忽略 `@Transactional` 的邊界設計（避免長交易鎖表）

## 驗收標準

- [ ] Controller / Service / Repository 職責邊界清楚，無跨層存取
- [ ] 所有輸入皆有 Bean Validation
- [ ] 對應的 JUnit 5 測試涵蓋 Happy Path 與至少一個例外情境
- [ ] 金額欄位使用 `BigDecimal` 而非 `double`

## 參考資料

- Spring Boot 官方文件－Building RESTful Web Services
- 本手冊 4.3.2 節 Skill 檔案格式範例
