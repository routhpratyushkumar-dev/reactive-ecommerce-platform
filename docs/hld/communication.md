# Communication Architecture

## 1. Purpose

This document defines how services within the Reactive E-Commerce Platform communicate with one another.

The platform uses a combination of synchronous REST communication and asynchronous event-driven communication through Apache Kafka.

The architecture aims to provide:

- Loose coupling between microservices.
- Non-blocking communication for I/O operations.
- Reliable event processing.
- Fault isolation and resilience.
- Scalability as transaction volume increases.

## 2. Communication Patterns

The platform uses two primary communication patterns.

### 2.1 Synchronous Communication — REST

Synchronous REST communication is used when a service requires an immediate response from another service.

**Technology:**
- Spring WebFlux
- Spring WebClient
- Spring Cloud Gateway
- JSON over HTTP

**Examples:**
- Cart Service retrieves product information from Product Service.
- Order Service validates product prices through Product Service.
- Order Service requests stock reservation from Inventory Service.
- Order Service requests payment processing from Payment Service.

WebClient will be used for outbound HTTP calls to support non-blocking I/O.

### 2.2 Asynchronous Communication — Kafka

Asynchronous communication is used when a service needs to notify other services about a business event without waiting for their responses.

**Technology:**
- Apache Kafka
- Spring Kafka for event publishing and consumption
- JSON event payloads

**Examples:**
- Order Service publishes `OrderCreated`.
- Order Service publishes `OrderConfirmed`.
- Payment Service publishes `PaymentCompleted` or `PaymentFailed`.
- Inventory Service publishes `InventoryReserved` or `InventoryReleased`.
- Notification Service consumes order and payment events.
- Analytics Service consumes business events.

Kafka consumers must be idempotent because messages may be delivered more than once.

## 3. Service Communication Matrix

| Calling Service | Target Service | Communication | Purpose |
|---|---|---|---|
| API Gateway | Auth Service | REST | Authentication and token-related requests |
| API Gateway | Product Service | REST | Product catalog APIs |
| API Gateway | Cart Service | REST | Cart operations |
| API Gateway | Order Service | REST | Order operations |
| Cart Service | Product Service | REST | Retrieve and validate product information |
| Order Service | Product Service | REST | Validate products and current prices |
| Order Service | Inventory Service | REST | Reserve or release stock |
| Order Service | Payment Service | REST | Initiate payment |
| Order Service | Kafka | Event publishing | Publish order events |
| Payment Service | Kafka | Event publishing | Publish payment events |
| Inventory Service | Kafka | Event publishing | Publish inventory events |
| Notification Service | Kafka | Event consumption | Process notification events |
| Analytics Service | Kafka | Event consumption | Process business events |

Internal service APIs will not be exposed directly to external clients. External requests will enter through the API Gateway.

## 4. Kafka Topics

The following topics are proposed for the initial implementation.

| Kafka Topic | Publisher | Consumers | Purpose |
|---|---|---|---|
| order-events | Order Service | Notification, Analytics | Order lifecycle events |
| payment-events | Payment Service | Order, Notification, Analytics | Payment status events |
| inventory-events | Inventory Service | Order, Analytics | Stock reservation and release events |
| notification-events | Internal event producers | Notification Service | Notification processing |

The initial implementation may use fewer topics if that keeps development manageable. Topics can be separated further as the platform grows.

## 5. Event Structure

All Kafka messages should follow a common event envelope.

Example:

```json
{
  "eventId": "evt-10001",
  "eventType": "OrderCreated",
  "eventVersion": 1,
  "occurredAt": "2026-11-22T10:30:00Z",
  "aggregateId": "order-10001",
  "correlationId": "corr-20001",
  "payload": {
    "orderId": "order-10001",
    "customerId": "customer-101",
    "totalAmount": 2499.00,
    "currency": "INR"
  }
}
```

### Event field definitions

- `eventId`: Unique identifier for the event.
- `eventType`: Business event name.
- `eventVersion`: Version of the event schema.
- `occurredAt`: Timestamp when the event occurred.
- `aggregateId`: Business entity identifier, such as order ID.
- `correlationId`: Identifier used to trace a business transaction across services.
- `payload`: Event-specific business data.

Kafka messages related to the same order should use the order ID as the message key where appropriate. This helps preserve ordering for that order within a Kafka partition.

## 6. Order Checkout Communication Flow

The initial checkout process uses synchronous service calls for operations that require immediate results.

1. Customer submits checkout request through API Gateway.
2. API Gateway forwards the request to Order Service.
3. Order Service validates product details and prices through Product Service.
4. Order Service requests stock reservation from Inventory Service.
5. Order Service requests payment processing from Payment Service.
6. Payment Service returns the payment result.
7. If payment succeeds, Order Service confirms the order.
8. Order Service publishes an `OrderConfirmed` event.
9. Notification Service consumes the event and processes the customer notification.
10. Analytics Service consumes the event for reporting.

If payment fails, Order Service marks the order as failed or pending according to the checkout state and requests inventory release.

This is a simplified orchestration-based Saga approach. Detailed transaction states, compensation actions, and failure scenarios will be documented in later HLD steps.

## 7. Reactive Communication Design

The platform uses Spring WebFlux for reactive REST APIs.

### Key implementation choices

- Use `Mono` for asynchronous operations returning zero or one result.
- Use `Flux` for asynchronous streams returning multiple results.
- Use WebClient for non-blocking HTTP communication between services.
- Avoid blocking calls such as `block()` in reactive request-processing paths.
- Use reactive database drivers such as R2DBC where supported.
- Use reactive Redis access for Redis operations.

Kafka publishing and consumption will be implemented using Spring Kafka initially. Kafka listener processing must be designed carefully; using Spring Kafka does not automatically make a listener reactive.

## 8. Reliability and Error Handling

Service communication must handle failures safely.

### REST communication

- Configure connection and response timeouts.
- Use bounded retries with exponential backoff and jitter for eligible transient failures.
- Apply circuit breakers to protect services from repeated downstream failures.
- Avoid automatic retries for non-idempotent operations unless an idempotency mechanism is in place.
- Do not return fake success responses when payment or inventory operations fail.

### Kafka communication

- Use unique event IDs.
- Make consumers idempotent.
- Handle failed message processing.
- Use a Dead Letter Topic (DLT) for messages that repeatedly fail processing.
- Monitor consumer lag and processing failures.
- Do not assume exactly-once processing across the entire microservice platform.

## 9. Event Publishing Reliability

A service must avoid losing business events when its database transaction succeeds but Kafka publishing fails.

For important business events, the platform will use the **Transactional Outbox Pattern**.

The service writes its business data and an event record into the same database transaction. A separate publisher process reads unpublished records and sends them to Kafka.

This approach will initially be implemented for critical order events.

Consumers must still be idempotent because an event may be published or delivered more than once.

## 10. Security

- All external client requests must pass through API Gateway.
- External communication must use HTTPS in deployed environments.
- Internal service communication must use authenticated service-to-service access in deployed environments.
- JWT tokens will be validated at the gateway and relevant services.
- Sensitive payment information must not be included in Kafka event payloads or application logs.
- Secrets and credentials must be stored outside source code.

## 11. Observability

Every request and event should be traceable across services.

The platform will use:

- Correlation IDs for business transaction tracking.
- Structured application logs.
- Distributed tracing with OpenTelemetry.
- Metrics for REST response times and error rates.
- Kafka consumer lag and processing metrics.

## 12. Initial Technology Stack

| Area | Technology |
|---|---|
| API Gateway | Spring Cloud Gateway |
| REST APIs | Spring WebFlux |
| HTTP Client | Spring WebClient |
| Event Broker | Apache Kafka |
| Kafka Integration | Spring Kafka |
| Reactive Database Access | Spring Data R2DBC |
| Cache | Redis |
| Distributed Tracing | OpenTelemetry |
| Local Environment | Docker Compose |

All tools and technologies selected for the initial implementation can be used with free or open-source options.

## 13. Future Improvements

The following improvements can be considered after the initial platform is working:

- More advanced Saga orchestration.
- Event schema management and compatibility checks.
- Separate retry and Dead Letter Topics.
- Service mesh for larger deployments.
- Advanced Kafka partitioning and consumer scaling.
- API versioning and backward compatibility.
- Automated contract testing between services.

## 14. Conclusion

The platform follows a hybrid communication architecture.

Synchronous REST communication handles operations that require immediate responses, while Kafka supports asynchronous event-driven workflows.

Spring WebFlux and WebClient provide non-blocking REST communication. Kafka enables loose coupling between services, while idempotent consumers, timeouts, retries, circuit breakers, and the Transactional Outbox Pattern improve reliability.

This architecture provides a foundation for building and scaling the Reactive E-Commerce Platform.