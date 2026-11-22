# Core Business Flows

## 1. Purpose

This document describes the main business workflows of the Reactive E-Commerce Platform.

It explains how customers interact with the platform and how microservices coordinate to complete business operations.

## 2. Customer Registration and Login

### Registration

1. Customer submits registration details through API Gateway.
2. API Gateway forwards the request to Auth Service.
3. Auth Service validates the request.
4. Auth Service checks whether the email already exists.
5. Auth Service hashes the password.
6. Auth Service saves the user record.
7. Auth Service returns a registration response.

### Login

1. Customer submits login credentials.
2. Auth Service validates the credentials.
3. Auth Service generates an access token and refresh token.
4. Client receives the tokens.
5. Client includes the access token in subsequent protected requests.

## 3. Product Browsing

1. Customer requests the product catalog.
2. API Gateway routes the request to Product Service.
3. Product Service checks Redis for cached product data.
4. If the cache contains the requested data, Product Service returns it.
5. Otherwise, Product Service retrieves data from PostgreSQL.
6. Product Service updates the cache where appropriate.
7. Product information is returned to the customer.

PostgreSQL remains the source of truth for product information.

## 4. Add Product to Cart

1. Customer selects a product and quantity.
2. API Gateway forwards the request to Cart Service.
3. Cart Service validates the customer identity and request.
4. Cart Service retrieves product information from Product Service.
5. Cart Service checks that the product is active.
6. Cart Service stores or updates the cart in Redis.
7. Cart Service returns the updated cart.

Cart data is temporary. Price and stock must be revalidated during checkout.

## 5. Checkout and Order Creation

Checkout is the main business workflow.

### Initial checkout process

1. Customer submits checkout request.
2. API Gateway forwards the request to Order Service.
3. Order Service validates the customer and request.
4. Order Service retrieves current product information and prices from Product Service.
5. Order Service calculates the order total.
6. Order Service creates an order in a pending state.
7. Order Service requests stock reservation from Inventory Service.
8. Inventory Service checks available stock and creates reservations.
9. Order Service requests payment processing from Payment Service.
10. Payment Service processes the mock payment request.
11. Payment Service returns the result to Order Service.
12. If payment succeeds, Order Service confirms the order.
13. Order Service records an `OrderConfirmed` event in its outbox as part of the order transaction.
14. The outbox publisher publishes the event to Kafka.
15. Notification Service consumes the event and processes a customer notification.
16. Analytics Service consumes the event and updates reporting data.

### Checkout failure

If inventory reservation fails, Order Service must not proceed with payment.

If payment fails after inventory has been reserved:

1. Order Service marks the order as failed or payment-failed.
2. Order Service requests Inventory Service to release the reservation.
3. Inventory Service releases the reserved stock.
4. A payment failure event may be published.
5. Notification Service can process the failure notification.

If a downstream service times out, the outcome may be unknown. The system must query the relevant operation using its idempotency key or business identifier before repeating a mutation.

Detailed retry and compensation rules are defined in the Failure and Resilience document.

## 6. Payment Processing

1. Order Service sends a payment request to Payment Service.
2. Payment Service validates the request.
3. Payment Service checks the idempotency key.
4. Payment Service invokes the mock payment provider.
5. Payment Service records the payment result.
6. Payment Service returns the result to Order Service.
7. Payment Service publishes a payment event where appropriate.

The mock provider simulates successful and failed payments. The platform does not process real card transactions.

## 7. Inventory Reservation

1. Order Service requests stock reservation.
2. Inventory Service checks available quantity.
3. Inventory Service atomically updates stock and creates a reservation.
4. Inventory Service returns the reservation result.
5. When the order succeeds, the reservation is confirmed.
6. When checkout fails, the reservation is released.
7. Expired reservations are released by a scheduled cleanup process.

The Inventory Service owns stock calculations and must prevent overselling.

## 8. Order Status Lifecycle

Proposed order statuses:

- CREATED
- INVENTORY_RESERVED
- PAYMENT_PENDING
- CONFIRMED
- PAYMENT_FAILED
- CANCELLED

Only valid state transitions should be allowed.

For example:

- CREATED → INVENTORY_RESERVED
- INVENTORY_RESERVED → PAYMENT_PENDING
- PAYMENT_PENDING → CONFIRMED
- PAYMENT_PENDING → PAYMENT_FAILED
- PAYMENT_FAILED → CANCELLED

Additional transitions can be introduced for timeouts, refunds, and post-confirmation cancellation.

## 9. Order Notification

1. Order Service publishes an order event to Kafka.
2. Notification Service consumes the event.
3. Notification Service validates the event.
4. Notification Service checks whether the event has already been processed.
5. Notification Service records the notification.
6. The initial implementation logs the notification result.
7. Future implementations may integrate email or SMS providers.

## 10. Analytics Processing

1. Business services publish relevant events to Kafka.
2. Analytics Service consumes events.
3. Analytics Service validates and deduplicates events.
4. Analytics Service updates reporting tables.
5. Reports can provide daily sales, order counts, payment totals, and product performance.

Analytics processing is asynchronous and must not block checkout.

## 11. Cancellation and Refunds

The initial implementation may support cancellation before order fulfillment.

Proposed flow:

1. Customer requests order cancellation.
2. Order Service validates the order and its current status.
3. Order Service requests payment refund when payment was completed.
4. Order Service requests inventory release or stock adjustment according to fulfillment state.
5. Order Service updates the cancellation status.
6. An order cancellation event is published.
7. Notification and Analytics services consume the event.

Refunds and inventory adjustments must be idempotent.

## 12. Conclusion

The platform supports customer registration, product browsing, cart management, checkout, payment, inventory reservation, order notifications, analytics, and basic cancellation.

The checkout workflow uses service APIs for immediate business decisions and Kafka for asynchronous event processing.