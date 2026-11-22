# Monitoring and Observability Architecture

## 1. Purpose

This document defines how the Reactive E-Commerce Platform will collect logs, metrics, and distributed traces.

Observability helps developers understand application behavior, identify failures, and troubleshoot issues across microservices.

## 2. Observability Pillars

The platform uses three primary observability pillars:

| Pillar | Purpose | Example |
|---|---|---|
| Logs | Record application events | Payment processing failed |
| Metrics | Measure application health | API response time |
| Traces | Follow requests across services | Checkout request across Order, Inventory, and Payment |

## 3. Logging

Each service should produce structured application logs.

Recommended log fields:

- Timestamp
- Log level
- Service name
- Environment
- Correlation ID
- Trace ID
- Request or event identifier
- Message
- Error details where appropriate

Log levels:

- ERROR: Unexpected failures.
- WARN: Recoverable or suspicious conditions.
- INFO: Important business and application events.
- DEBUG: Detailed diagnostic information for development.

Passwords, access tokens, refresh tokens, and sensitive payment information must never be logged.

## 4. Metrics

The platform should collect metrics for each service.

### Application metrics

- Request count.
- Request latency.
- HTTP error rate.
- JVM heap usage.
- Garbage collection activity.
- Thread and connection pool usage.
- Reactive application resource utilization.

### Business metrics

- Orders created.
- Orders confirmed.
- Orders failed.
- Payment success and failure count.
- Inventory reservation failures.
- Notifications processed.

### Infrastructure metrics

- CPU usage.
- Memory usage.
- Disk usage.
- Database connections.
- Redis memory and cache hit ratio.
- Kafka broker availability.
- Kafka consumer lag.

## 5. Distributed Tracing

A checkout operation may involve several services.

Distributed tracing helps identify where time is spent and where failures occur.

Example trace:

1. Customer submits checkout.
2. API Gateway receives the request.
3. Order Service starts processing.
4. Product Service validates prices.
5. Inventory Service reserves stock.
6. Payment Service processes payment.
7. Order Service confirms the order.

Trace context should be propagated across HTTP requests.

Kafka event headers should carry trace context so asynchronous processing can be correlated with the originating transaction where supported.

## 6. Technology Choices

| Area | Technology |
|---|---|
| Application Metrics | Micrometer |
| Metrics Endpoint | Spring Boot Actuator |
| Metrics Collection | Prometheus |
| Dashboards | Grafana |
| Distributed Tracing | OpenTelemetry |
| Trace Backend | Jaeger |
| Application Logs | Structured logging |
| Local Infrastructure | Docker Compose |

The initial implementation can start with Actuator and structured logs, then add Prometheus, Grafana, and Jaeger.

All selected tools have free or open-source options.

## 7. Health Checks

Each service should expose health information through Spring Boot Actuator.

Health checks should distinguish:

- Liveness: Whether the application process is running.
- Readiness: Whether the service is ready to accept requests.

Dependency health indicators can include PostgreSQL, Redis, and Kafka where relevant.

A temporary dependency outage should not necessarily cause an application process to restart continuously. Liveness and readiness checks must be configured carefully.

## 8. Alerting

Potential alerts include:

- High API error rate.
- Increased response latency.
- Service unavailable.
- Database connection failures.
- High JVM memory usage.
- Kafka consumer lag above a configured threshold.
- Repeated payment failures.
- Outbox events remaining unpublished.
- Excessive inventory reservation failures.

Thresholds should be based on measured application behavior and service-level objectives.

## 9. Correlation IDs

Every incoming request should have a correlation identifier.

The identifier should be:

- Created when absent.
- Validated or safely handled when supplied by a client.
- Propagated to downstream HTTP calls.
- Included in relevant Kafka event headers or envelopes.
- Included in application logs.

This allows developers to follow a business operation across multiple services.

## 10. Monitoring Checkout

Checkout is a critical business workflow.

The platform should monitor:

- Checkout request count.
- Checkout completion rate.
- Average and percentile checkout duration.
- Inventory reservation failures.
- Payment failures.
- Orders stuck in intermediate states.
- Outbox publication delay.

Business monitoring should identify incomplete transactions rather than relying only on HTTP status codes.

## 11. Local Development Setup

Docker Compose can run:

- Prometheus.
- Grafana.
- Jaeger.
- Application services.
- Kafka and its dependencies.

The initial development phase may use logs and Actuator endpoints before adding the full observability stack.

## 12. Conclusion

The platform uses structured logging, application metrics, health checks, and distributed tracing to provide visibility across microservices.

Observability is especially important for checkout, payment, inventory, and asynchronous Kafka workflows.