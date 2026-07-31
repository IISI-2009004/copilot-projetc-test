---
name: "microservices"
description: '評估單體應用是否該拆分、或設計新微服務的邊界與通訊方式時使用'
skill: microservices
version: 1.0
tags: [microservices, architecture, ddd]
applicable-to: [Java, Spring Boot, Microservices]
last-updated: 2026-07-20
---

# 微服務架構技能

## 適用情境
評估單體應用是否該拆分、或設計新微服務的邊界與通訊方式時使用。

## 核心原則
1. 服務邊界依 Bounded Context 劃分，而非依技術層（不要拆成 UI 服務、DB 服務）
2. 每個服務擁有自己的資料庫，禁止跨服務直接存取他人資料表
3. 服務間同步呼叫用 REST/gRPC，非同步事件用訊息佇列，依一致性需求選擇
4. 拆分前先確認組織與團隊結構能對應到服務邊界（Conway's Law）

## 標準做法

### 服務邊界識別（以 Context Map 表達）
```text
Order Context ──(同步 REST)──> Inventory Context
     │
     └──(非同步 Event: OrderPlaced)──> Notification Context
                                   └──> Billing Context
```

### 服務間資料一致性：Saga 模式
```java
@Component
public class OrderSagaOrchestrator {
    public void handle(OrderPlacedEvent event) {
        try {
            inventoryClient.reserve(event.orderId(), event.items());
            billingClient.charge(event.orderId(), event.amount());
        } catch (ReservationFailedException e) {
            billingClient.refund(event.orderId());
        }
    }
}
```

## 禁止事項
- 不得讓兩個服務共用同一張資料表
- 不得在服務間用分散式交易（2PC）取代 Saga，除非已充分評估效能代價
- 不得在尚未有明確 Bounded Context 前就以「未來可能要拆」為由過早拆分

## 驗收標準
- [ ] 每個服務可獨立部署、獨立資料庫
- [ ] 跨服務資料一致性策略（Saga/Event）已文件化
- [ ] Context Map 已繪製並經團隊審閱

## 參考資料
- Sam Newman《Building Microservices》
- 本手冊 3.2 節 Multi-Agent Architecture
