# Reactive E-Commerce Platform — Low-Level Design

## 1. Introduction

This document describes the Low-Level Design (LLD) of the Reactive E-Commerce Platform.

The LLD translates the High-Level Design into implementation-level details, including package structure, classes, APIs, database schemas, event contracts, validation, exception handling, and testing.

The project uses Java 17, Spring Boot, Spring WebFlux, Project Reactor, PostgreSQL, Redis, and Apache Kafka.

## 2. Design Objectives

- Define clear responsibilities for every class and component.
- Maintain separation between controllers, services, repositories, and domain models.
- Use reactive programming for non-blocking I/O.
- Keep microservice boundaries independent.
- Standardize REST API contracts.
- Define database ownership and persistence models.
- Establish consistent event contracts.
- Implement centralized error handling within each service.
- Support unit, integration, and end-to-end testing.

## 3. Microservices

| Service | Responsibility |
|---|---|
| API Gateway | Routing, authentication filters, rate limiting |
| Auth Service | Registration, login, JWT, refresh tokens |
| Product Service | Product and category management |
| Cart Service | Shopping cart operations |
| Inventory Service | Stock management and reservations |
| Order Service | Checkout and order lifecycle |
| Payment Service | Mock payment processing |
| Notification Service | Notification event processing |
| Analytics Service | Business event aggregation and reporting |

Each service is a separate Spring Boot application.

## 4. Common Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x |
| Web | Spring WebFlux |
| Reactive library | Project Reactor |
| HTTP client | WebClient |
| Database | PostgreSQL |
| Database access | Spring Data R2DBC |
| Migration | Flyway |
| Cache | Redis |
| Messaging | Apache Kafka |
| Security | Spring Security and JWT |
| Resilience | Resilience4j |
| Testing | JUnit 5, Mockito, Testcontainers |
| Containerization | Docker |

## 5. Design Principles

### Separation of concerns

Each application follows clear layers:

- Controller: HTTP request and response handling.
- Service: Business logic and orchestration.
- Repository: Database access.
- Domain: Business models and rules.
- DTO: API request and response models.
- Mapper: Conversion between API and domain models.
- Configuration: Framework and infrastructure configuration.

### Reactive programming

- Use Mono for zero-or-one asynchronous results.
- Use Flux for zero-to-many asynchronous results.
- Use WebClient for non-blocking HTTP calls.
- Use R2DBC for reactive PostgreSQL operations.
- Avoid blocking calls such as block() in reactive request-processing paths.

### Microservice independence

Each service owns its business data and database.

Services must not directly access another service's database.

### API consistency

All REST APIs should use consistent response structures, validation, status codes, and error formats.

## 6. Naming Conventions

| Element | Convention | Example |
|---|---|---|
| Package | Lowercase | com.example.product |
| Class | PascalCase | ProductService |
| Method | camelCase | findProductById |
| Variable | camelCase | productId |
| Constant | Uppercase | MAX_CART_ITEMS |
| Database table | snake_case | order_items |
| Database column | snake_case | created_at |
| Kafka event | PascalCase | OrderConfirmed |
| REST endpoint | Plural nouns | /api/v1/products |

## 7. Reactive Return Types

| Operation | Return Type |
|---|---|
| Find one record | Mono<T> |
| Find optional record | Mono<T> |
| Save one record | Mono<T> |
| Delete one record | Mono<Void> |
| Retrieve multiple records | Flux<T> |
| Stream multiple records | Flux<T> |

Empty results should be handled explicitly where business rules require them.

## 8. Implementation Order

The initial implementation will follow this order:

1. Project setup and common dependencies.
2. API Gateway.
3. Auth Service.
4. Product Service.
5. Cart Service.
6. Inventory Service.
7. Payment Service.
8. Order Service.
9. Notification Service.
10. Analytics Service.
11. Integration and end-to-end testing.
12. Docker Compose and CI/CD.

Order Service integration will be completed after its dependent services are available.

## 9. Scope

The first version will support:

- Customer registration and login.
- Product listing and product management.
- Cart creation and updates.
- Inventory reservation.
- Order creation and confirmation.
- Simulated payment.
- Kafka-based notifications.
- Basic analytics.

Real payment gateway integration and production-scale cloud deployment are future enhancements.

## 10. Conclusion

This LLD defines the implementation blueprint for the Reactive E-Commerce Platform. It establishes common coding conventions and detailed service responsibilities before application development begins.