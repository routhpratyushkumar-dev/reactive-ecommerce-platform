# Kafka Event Design

## 1. Purpose

This document defines the Kafka topics, event structures, producers, consumers, and processing rules for the Reactive E-Commerce Platform.

Kafka enables asynchronous communication between independently deployed microservices.

## 2. Topic Design

| Topic | Producer | Consumers |
|---|---|---|
| order-events | Order Service | Notification, Analytics |
| payment-events | Payment Service | Order, Notification, Analytics |
| inventory-events | Inventory Service | Order, Analytics |

The initial implementation uses three primary business topics. Additional topics can be introduced when operational or scaling requirements justify them.

## 3. Event Envelope

Every event should use a common envelope.

```json
{
  "eventId": "evt-10001",
  "eventType": "OrderConfirmed",
  "eventVersion": 1,
  "occurredAt": "2026-11-22T10:30:00Z",
  "aggregateId": "order-10001",
  "correlationId": "corr-20001",
  "payload": {}
}
```

Field definitions:

| Field | Description |
|---|---|
| eventId | Unique event identifier |
| eventType | Event name |
| eventVersion | Event schema version |
| occurredAt | Event creation timestamp |
| aggregateId | Related business entity |
| correlationId | Cross-service transaction identifier |
| payload | Event-specific data |

## 4. Order Events

Proposed event types:

- OrderCreated
- OrderConfirmed
- OrderCancelled
- OrderPaymentFailed

### OrderConfirmed example

```json
{
  "eventId": "evt-10001",
  "eventType": "OrderConfirmed",
  "eventVersion": 1,
  "occurredAt": "2026-11-22T10:30:00Z",
  "aggregateId": "order-10001",
  "correlationId": "corr-20001",
  "payload": {
    "orderId": "order-10001",
    "customerId": "customer-101",
    "totalAmount": 4998.00,
    "currency": "INR"
  }
}
```

## 5. Payment Events

Proposed event types:

- PaymentInitiated
- PaymentCompleted
- PaymentFailed
- PaymentRefunded

### PaymentCompleted example

```json
{
  "eventId": "evt-10002",
  "eventType": "PaymentCompleted",
  "eventVersion": 1,
  "occurredAt": "2026-11-22T10:31:00Z",
  "aggregateId": "order-10001",
  "correlationId": "corr-20001",
  "payload": {
    "paymentId": "payment-10001",
    "orderId": "order-10001",
    "amount": 4998.00,
    "currency": "INR",
    "status": "COMPLETED"
  }
}
```

Payment events must not include card numbers, CVVs, access tokens, or other sensitive credentials.

## 6. Inventory Events

Proposed event types:

- InventoryReserved
- InventoryReleased
- InventoryReservationExpired

### InventoryReserved example

```json
{
  "eventId": "evt-10003",
  "eventType": "InventoryReserved",
  "eventVersion": 1,
  "occurredAt": "2026-11-22T10:29:00Z",
  "aggregateId": "order-10001",
  "correlationId": "corr-20001",
  "payload": {
    "reservationId": "reservation-10001",
    "orderId": "order-10001",
    "status": "RESERVED"
  }
}
```

## 7. Producer Responsibilities

| Producer | Events |
|---|---|
| Order Service | OrderCreated, OrderConfirmed, OrderCancelled, OrderPaymentFailed |
| Payment Service | PaymentInitiated, PaymentCompleted, PaymentFailed, PaymentRefunded |
| Inventory Service | InventoryReserved, InventoryReleased, InventoryReservationExpired |

Critical business events from Order Service should use the Transactional Outbox Pattern.

Other producers should use an appropriate reliable publication approach, introducing an outbox where losing an event would cause business inconsistency.

## 8. Consumer Responsibilities

| Consumer | Events | Action |
|---|---|---|
| Order Service | Payment events | Update payment and order workflow state |
| Notification Service | Order and payment events | Process customer notifications |
| Analytics Service | Order, payment and inventory events | Update reporting data |

Notification and Analytics consumers must not change another service's database.

## 9. Message Key and Ordering

Use the order ID as the Kafka message key for events associated with an order.

This helps preserve ordering for a given order within a Kafka partition.

Kafka guarantees ordering within a partition, not globally across all partitions or topics.

## 10. Delivery and Idempotency

The system should assume at-least-once message processing.

Consumers must:

- Validate the event structure.
- Check whether the event has already been processed.
- Apply the business operation safely.
- Persist processing results.
- Avoid duplicate side effects.

Where event processing writes to a database, the processed-event record and business update should be committed in the same local transaction when practical.

## 11. Error Handling

- Retry eligible transient failures.
- Use bounded retries.
- Route repeatedly failing messages to a Dead Letter Topic.
- Record event ID and failure details.
- Monitor consumer lag.
- Support controlled message replay.

Do not retry permanently invalid messages indefinitely.

## 12. Event Versioning

Every event contains an event version.

Event changes should be backward compatible where practical.

Avoid removing or changing the meaning of existing fields without a migration plan.

Consumers should ignore unknown optional fields and validate required fields.

## 13. Kafka Configuration

Initial configuration should define:

- Bootstrap servers.
- Consumer group IDs.
- Topic names.
- Serialization format.
- Producer acknowledgement settings.
- Retry and delivery settings.
- Consumer offset handling.
- Dead Letter Topic strategy.

Kafka credentials and broker addresses must be provided through environment configuration.

## 14. Conclusion

Kafka provides asynchronous event communication between the platform's microservices.

The event envelope, message keys, idempotent consumers, versioning, and failure handling rules provide a consistent foundation for event-driven development.