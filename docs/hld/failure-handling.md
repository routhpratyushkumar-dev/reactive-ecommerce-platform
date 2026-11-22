# Failure and Resilience Architecture

## 1. Purpose

This document defines how the Reactive E-Commerce Platform handles service failures, network problems, database errors, and Kafka processing failures.

The objective is to prevent isolated failures from causing widespread platform outages and to maintain consistent business data.

## 2. Resilience Principles

The platform follows these principles:

- Expect failures in distributed systems.
- Apply timeouts to remote calls.
- Retry only operations that are safe to retry.
- Use circuit breakers to prevent repeated calls to unhealthy services.
- Make event consumers idempotent.
- Avoid losing important business events.
- Use compensating actions for distributed business transactions.
- Provide sufficient logs and metrics to diagnose failures.

## 3. REST Communication Failures

A service calling another service may encounter:

- Connection timeout.
- Response timeout.
- Service unavailable.
- Invalid response.
- Network interruption.
- Database failure in the target service.

### Handling strategy

- Configure connection and response timeouts.
- Return meaningful error responses.
- Apply bounded retries for eligible transient failures.
- Use exponential backoff with jitter.
- Apply circuit breakers to protect downstream services.
- Propagate correlation IDs for troubleshooting.

Retries must not be applied blindly to payment, order creation, or inventory mutations.

## 4. Retry Strategy

Retries may be used for temporary failures such as:

- Connection interruptions.
- HTTP 502, 503, and 504 responses.
- Temporary broker or database availability problems.

Retries should not be used for:

- Invalid requests.
- Authentication failures.
- Authorization failures.
- Insufficient inventory.
- Invalid payment details.
- Other permanent business validation errors.

Use a small, configurable retry count with backoff and jitter.

Every retried mutation must have an idempotency mechanism or a way to determine its previous outcome.

## 5. Circuit Breaker

Circuit breakers prevent repeated calls to a failing downstream service.

The platform can use Resilience4j.

Circuit breaker states:

- CLOSED: Requests are allowed normally.
- OPEN: Requests are rejected temporarily.
- HALF_OPEN: A limited number of requests are allowed to test recovery.

Circuit breakers are useful for calls to Product, Inventory, and Payment services.

Fallback responses are acceptable for some read operations, such as serving cached product information when safe.

Fallback must not falsely report success for checkout, payment, or inventory mutations.

## 6. Kafka Failure Handling

Kafka-related failures include:

- Broker unavailable.
- Producer send failure.
- Consumer processing failure.
- Duplicate message delivery.
- Invalid event payload.
- Consumer lag.

### Handling strategy

- Configure producer acknowledgements and appropriate durability settings.
- Use bounded producer retries and delivery timeouts.
- Make consumers idempotent.
- Use retry topics or controlled retry handling where appropriate.
- Route repeatedly failing messages to a Dead Letter Topic.
- Monitor consumer lag and failed processing.
- Define retention and replay procedures.

The system must not assume exactly-once processing across all services.

## 7. Idempotency

Idempotency prevents repeated requests or messages from causing duplicate business operations.

Examples:

- Payment initiation uses a unique idempotency key.
- Order creation uses a client request identifier where appropriate.
- Inventory reservation uses an order or reservation identifier.
- Kafka consumers record processed event IDs.

When the same operation is received again, the service should return or preserve the original result rather than perform the business operation again.

## 8. Transactional Outbox

The Transactional Outbox Pattern protects important event publication.

Process:

1. Service begins a database transaction.
2. Service updates its business data.
3. Service inserts an event record into its outbox table.
4. Service commits the transaction.
5. An outbox publisher reads unpublished records.
6. Publisher sends events to Kafka.
7. Publisher marks successfully published records.

If Kafka is unavailable, unpublished events remain in the outbox and can be retried.

Duplicate publication is possible, so consumers must be idempotent.

## 9. Saga and Compensation

Checkout spans multiple independent services. A single database transaction cannot safely cover all operations.

The initial design uses Order Service as the Saga orchestrator.

Example:

| Step | Operation | Compensation |
|---|---|---|
| 1 | Create pending order | Cancel pending order |
| 2 | Reserve inventory | Release reservation |
| 3 | Initiate payment | Refund if payment succeeded |
| 4 | Confirm order | Handle cancellation through a separate workflow |

A compensation is a new business operation, not a rollback of another service's database transaction.

The order must retain enough state to determine which operations have completed.

## 10. Database Failure Handling

If a service database becomes unavailable:

- The service should fail safely.
- New mutations must not be reported as successful.
- Database connections should have timeouts and bounded resource usage.
- Health checks should reflect database connectivity.
- Recovery should use backups and migration procedures.
- Pending outbox records must be preserved.

## 11. Inventory Reservation Expiry

Inventory reservations should have an expiry time.

A scheduled process should identify expired reservations and release stock safely.

The release operation must be idempotent to avoid increasing available stock more than once.

## 12. Dead Letter Handling

Messages that cannot be processed after the configured retry policy should be sent to a Dead Letter Topic.

The platform should record:

- Original topic.
- Event ID.
- Failure reason.
- Processing timestamp.
- Retry count.
- Correlation ID.

Messages should be reviewed and replayed only after the root cause is addressed.

## 13. Graceful Degradation

The platform may continue limited operations when non-critical services are unavailable.

Examples:

- Product browsing may use safe cached data.
- Analytics outages must not block checkout.
- Notification outages must not prevent order confirmation.

The platform must not degrade by pretending that payment or inventory operations succeeded.

## 14. Failure Testing

The following scenarios should be tested:

- Product Service unavailable.
- Inventory Service timeout.
- Payment Service timeout.
- Kafka broker unavailable.
- Duplicate Kafka event.
- Consumer processing exception.
- Database connection failure.
- Outbox publisher interruption.
- Inventory reservation expiry.
- Repeated checkout request.

## 15. Conclusion

The platform uses timeouts, bounded retries, circuit breakers, idempotency, the Transactional Outbox Pattern, and Saga compensation to improve reliability.

These patterns help the platform recover from failures while protecting order, payment, and inventory consistency.