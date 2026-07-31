---
name: "event-driven"
description: '服務間需要解耦、或有多個下游需要對同一業務事實做出反應（通知、報表、稽核）時採用'
skill: event-driven
version: 1.0
tags: [event-driven, kafka, messaging]
applicable-to: [Kafka, Spring Boot]
last-updated: 2026-07-20
---

# 事件驅動架構技能

## 適用情境
服務間需要解耦、或有多個下游需要對同一業務事實做出反應（通知、報表、稽核）時採用。

## 核心原則
1. 事件是「已發生的事實」，用過去式命名（`OrderPlaced` 而非 `PlaceOrder`）
2. 事件內容應自足（Self-contained），下游不需回查來源服務即可處理
3. 消費端必須具備冪等性，因為訊息佇列多為 At-least-once 語意
4. 事件 Schema 需版本化管理，變更需向下相容

## 標準做法

### 發布事件
```java
@Service
@RequiredArgsConstructor
public class OrderService {
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;

    @Transactional
    public void placeOrder(Order order) {
        orderRepository.save(order);
        kafkaTemplate.send("order-events",
            new OrderPlacedEvent(order.getId(), order.getTotal(), Instant.now()));
    }
}
```

### 冪等消費
```java
@KafkaListener(topics = "order-events")
public void handle(OrderPlacedEvent event) {
    if (processedEventRepository.existsById(event.eventId())) {
        return; // 已處理過，避免重複扣款
    }
    billingService.charge(event.orderId(), event.total());
    processedEventRepository.save(new ProcessedEvent(event.eventId()));
}
```

## 禁止事項
- 不得假設訊息只會被消費一次（必須設計冪等）
- 不得讓事件 Schema 變更直接破壞既有消費者（需向下相容或雙版本並行）
- 不得用事件取代所有同步呼叫（強一致性需求場景仍應使用同步 API）

## 驗收標準
- [ ] 事件消費端具備冪等處理（去重機制）
- [ ] 事件 Schema 已版本化並有相容性測試
- [ ] 關鍵事件皆有對應的死信佇列（DLQ）與告警

## 參考資料
- Confluent — Event-Driven Architecture 設計指南
