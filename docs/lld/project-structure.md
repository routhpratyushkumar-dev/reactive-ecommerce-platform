# Project Structure and Package Design

## 1. Repository Structure

The project will use a multi-module Maven repository.

Each microservice will be an independent Spring Boot application with its own build configuration, application entry point, and tests.

```text
reactive-ecommerce-platform/
│
├── api-gateway/
├── auth-service/
├── product-service/
├── cart-service/
├── inventory-service/
├── order-service/
├── payment-service/
├── notification-service/
├── analytics-service/
│
├── infrastructure/
│   ├── docker/
│   ├── kafka/
│   ├── postgres/
│   └── monitoring/
│
├── docs/
│   ├── hld/
│   └── lld/
│
├── scripts/
├── .github/
│   └── workflows/
│
├── docker-compose.yml
├── pom.xml
├── .gitignore
└── README.md
```

## 2. Common Service Package Structure

Each business microservice will follow a similar structure.

Example: Product Service.

```text
product-service/
│
├── src/
│   ├── main/
│   │   ├── java/com/example/product/
│   │   │   ├── ProductServiceApplication.java
│   │   │   │
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── entity/
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   └── response/
│   │   │   ├── mapper/
│   │   │   ├── exception/
│   │   │   ├── config/
│   │   │   └── validation/
│   │   │
│   │   └── resources/
│   │       ├── application.yml
│   │       └── db/migration/
│   │
│   └── test/
│       └── java/com/example/product/
│           ├── controller/
│           ├── service/
│           ├── repository/
│           └── integration/
│
└── pom.xml
```

The base package `com.example` is a placeholder. A single consistent organization namespace will be selected when creating the actual repository.

## 3. Package Responsibilities

### controller

Responsibilities:

- Define REST endpoints.
- Accept request DTOs.
- Validate incoming requests.
- Invoke service methods.
- Return HTTP responses.

Controllers must not contain business logic or direct database queries.

### service

Responsibilities:

- Implement business rules.
- Coordinate repository operations.
- Call other services through dedicated clients.
- Publish or process business events where appropriate.
- Handle business-level validation.

### repository

Responsibilities:

- Interact with the service-owned database.
- Perform database queries.
- Save, update, and delete records.

Use reactive repository interfaces where supported.

### entity

Responsibilities:

- Define database persistence models.
- Map Java fields to database columns.
- Represent data owned by the service.

### dto

Contains request and response objects.

```text
dto/
├── request/
└── response/
```

API DTOs should not expose persistence entities directly.

### mapper

Converts between entities, domain objects, and DTOs.

### exception

Contains custom exceptions and service-specific error definitions.

### config

Contains application configuration such as:

- Security configuration.
- Database configuration.
- Redis configuration.
- Kafka configuration.
- WebClient configuration.
- Resilience configuration.

### validation

Contains reusable business and request validation logic where appropriate.

## 4. Service-Specific Packages

The following packages are recommended.

| Service | Additional Packages |
|---|---|
| API Gateway | filter, route, config |
| Auth Service | security, token, user |
| Product Service | category, cache |
| Cart Service | cart, client |
| Inventory Service | reservation, scheduler |
| Order Service | checkout, saga, client, outbox |
| Payment Service | provider, transaction |
| Notification Service | consumer, handler, delivery |
| Analytics Service | consumer, aggregation, reporting |

## 5. External Service Clients

Services should use dedicated client components for remote REST calls.

Examples:

- ProductClient
- InventoryClient
- PaymentClient

These components encapsulate WebClient configuration and downstream HTTP communication.

Business services should not construct raw HTTP requests throughout their methods.

## 6. Configuration Management

Each service should maintain its own application configuration.

Example:

```yaml
server:
  port: 8082

spring:
  application:
    name: product-service

  r2dbc:
    url: r2dbc:postgresql://localhost:5432/product_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  flyway:
    enabled: true
    url: jdbc:postgresql://localhost:5432/product_db
    user: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

Credentials must be supplied through environment variables or a secret manager, not committed to Git.

The port and database names above are illustrative and will be finalized in the deployment configuration.

## 7. Maven Structure

The root Maven project should use packaging `pom` and define the service modules.

Each service should have its own `pom.xml` with only the dependencies it needs.

The root project may manage common dependency versions through a parent POM or Spring Boot dependency management.

Avoid creating a shared Java library for business entities across microservices. Shared libraries, if introduced, should contain only stable technical utilities or event-contract definitions that do not compromise service independence.

## 8. Testing Structure

Tests should mirror the main application packages.

- Controller tests: API behavior and validation.
- Service tests: Business rules and reactive behavior.
- Repository tests: Database operations.
- Integration tests: Real infrastructure interactions.
- End-to-end tests: Complete business workflows.

## 9. Conclusion

The multi-module Maven structure provides a clear separation between microservices while maintaining one repository for development and portfolio presentation.

Each service remains independently buildable and deployable.