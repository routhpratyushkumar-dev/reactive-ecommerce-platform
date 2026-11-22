# Testing Strategy

## 1. Purpose

This document defines the testing approach for the Reactive E-Commerce Platform.

The objective is to validate business logic, reactive behavior, database operations, service communication, Kafka event processing, security, and complete business workflows.

## 2. Testing Tools

| Testing Area | Tool |
|---|---|
| Unit Testing | JUnit 5 |
| Mocking | Mockito |
| Reactive Testing | Reactor Test |
| Web Layer Testing | WebTestClient |
| Database Integration | Testcontainers |
| Kafka Integration | Testcontainers |
| API Testing | Bruno or Postman |
| Load Testing | k6 or Apache JMeter |
| Code Quality | Checkstyle or SpotBugs |
| CI/CD | GitHub Actions |

Use free and open-source tools wherever possible.

## 3. Unit Testing

Unit tests validate individual classes and business rules.

Examples:

### Auth Service

- Register a new user.
- Reject duplicate email.
- Reject invalid credentials.
- Generate valid access tokens.
- Reject expired refresh tokens.

### Product Service

- Create a product.
- Retrieve an existing product.
- Return an error for a missing product.
- Reject invalid price.
- Invalidate cache after product update.

### Cart Service

- Add product to cart.
- Update item quantity.
- Remove item.
- Reject invalid quantity.
- Reject inactive product.

### Inventory Service

- Reserve available stock.
- Reject insufficient stock.
- Release a reservation.
- Reject duplicate reservation.
- Prevent stock from becoming negative.

### Order Service

- Create an order.
- Calculate order total correctly.
- Reject invalid checkout.
- Handle inventory failure.
- Handle payment failure.
- Confirm a successful order.
- Prevent duplicate order creation.

### Payment Service

- Process successful payment.
- Simulate declined payment.
- Return existing result for repeated idempotency key.
- Reject invalid amount.
- Process refund safely.

## 4. Reactive Testing

Use Reactor Test's `StepVerifier` to verify Mono and Flux behavior.

Example:

```java
StepVerifier.create(productService.getProductById(productId))
        .expectNextMatches(product ->
                product.id().equals(productId))
        .verifyComplete();
```

Test cases should include:

- Successful emission.
- Empty publisher.
- Expected exception.
- Multiple Flux elements.
- Timeout behavior.
- Cancellation where relevant.

## 5. Controller Testing

Use WebTestClient to test REST APIs.

Validate:

- HTTP status.
- Response body.
- Request validation.
- Authentication.
- Authorization.
- Error response format.
- Pagination.
- Invalid request handling.

Controller tests should not require every downstream infrastructure dependency.

## 6. Repository Testing

Use Testcontainers with PostgreSQL and Redis where appropriate.

Validate:

- Insert and retrieval.
- Update and deletion.
- Unique constraints.
- Transaction behavior.
- Query correctness.
- Pagination.
- Redis expiration.
- Reservation concurrency.

Repository tests must use database behavior representative of the actual application rather than relying only on mocked repositories.

## 7. Kafka Testing

Test producer and consumer behavior using Kafka Testcontainers.

Scenarios:

- Publish OrderConfirmed.
- Consume OrderConfirmed.
- Consume PaymentCompleted.
- Ignore duplicate events safely.
- Handle invalid payload.
- Handle consumer processing failure.
- Route repeated failures to a Dead Letter Topic.
- Preserve event correlation information.

## 8. Integration Testing

Integration tests should verify interactions between application components.

Examples:

- Auth Service with PostgreSQL.
- Product Service with PostgreSQL and Redis.
- Cart Service with Redis and Product Service.
- Inventory Service with PostgreSQL.
- Order Service with Product, Inventory, and Payment clients.
- Order Outbox publisher with Kafka.
- Notification consumer with Kafka and PostgreSQL.

## 9. End-to-End Testing

The following business workflows should be tested:

### Successful checkout

1. Register customer.
2. Log in.
3. Browse products.
4. Add product to cart.
5. Ensure inventory is available.
6. Submit checkout.
7. Reserve inventory.
8. Process mock payment.
9. Confirm order.
10. Publish order event.
11. Process notification.

Expected result: order is confirmed, payment succeeds, and inventory is reserved or committed according to the inventory lifecycle.

### Failed checkout

1. Submit checkout with insufficient inventory.
2. Verify order is not confirmed.
3. Verify payment is not initiated.

Expected result: checkout fails without charging the customer.

### Payment failure

1. Submit checkout with available inventory.
2. Reserve inventory.
3. Simulate payment failure.
4. Release inventory.
5. Update order status.

Expected result: order is not confirmed and inventory is released.

### Duplicate request

1. Submit checkout.
2. Repeat the same request using the same idempotency key.
3. Verify only one logical order is created.

Expected result: the original operation result is returned without duplicate business side effects.

## 10. Resilience Testing

Test:

- Product Service unavailable.
- Inventory Service timeout.
- Payment Service timeout.
- Kafka broker unavailable.
- Database connection failure.
- Duplicate Kafka delivery.
- Outbox publisher restart.
- Expired inventory reservation.
- Concurrent stock reservation.

The system must not report false success when a business operation fails or its outcome is unknown.

## 11. Security Testing

Test:

- Invalid JWT.
- Expired JWT.
- Missing JWT.
- Customer accessing another customer's order.
- Customer accessing admin endpoints.
- Invalid request payload.
- Rate limiting.
- Sensitive information not appearing in logs.

## 12. Load Testing

Use k6 or JMeter to test:

- Product listing.
- Product detail retrieval.
- Cart updates.
- Concurrent checkout.
- Inventory reservation contention.
- Kafka event throughput.

Capture:

- Requests per second.
- Response time percentiles.
- Error percentage.
- CPU and memory.
- Database connection usage.
- Kafka consumer lag.

## 13. Code Coverage

Use JaCoCo to measure test coverage.

Initial project targets:

- Core business logic: aim for at least 80% line coverage.
- Critical checkout, inventory, and payment rules: comprehensive scenario coverage.
- Controllers and repositories: test important success and failure paths.

Coverage percentages are indicators, not substitutes for meaningful assertions.

## 14. CI Pipeline

GitHub Actions should execute:

1. Maven validation.
2. Compilation.
3. Unit tests.
4. Integration tests.
5. Code quality checks.
6. Package creation.

Docker image publishing and deployment can be added later.

## 15. Conclusion

The testing strategy combines unit, reactive, integration, API, security, resilience, and end-to-end testing.

The most critical test coverage will focus on checkout, payment, inventory consistency, and duplicate event handling.