# Data Architecture

## 1. Purpose

This document defines the database strategy, data ownership, storage technologies, and data relationships for the Reactive E-Commerce Platform.

The platform follows the Database per Service pattern. Each microservice owns its data and is responsible for managing its persistence.

Other services must not directly access another service's database.

## 2. Database Strategy

The platform uses the following storage technologies:

| Technology | Purpose |
|---|---|
| PostgreSQL | Persistent transactional data |
| Redis | Cart storage, caching, and temporary data |
| Apache Kafka | Asynchronous event transport |
| PostgreSQL Outbox Tables | Reliable business event publishing |

PostgreSQL is the primary relational database. Redis is used for data that requires fast access or temporary storage.

Kafka is used for communication between services, not as a replacement for the services' primary databases.

## 3. Database Ownership

| Service | Storage | Data Owned |
|---|---|---|
| Auth Service | PostgreSQL | Users, roles, refresh-token records |
| Product Service | PostgreSQL + Redis | Products, categories, cached product data |
| Cart Service | Redis | Shopping carts and cart items |
| Inventory Service | PostgreSQL | Stock levels and reservations |
| Order Service | PostgreSQL | Orders, order items, outbox events |
| Payment Service | PostgreSQL | Payment attempts and transaction references |
| Notification Service | PostgreSQL (optional initially) | Notification history and delivery status |
| Analytics Service | PostgreSQL (initial implementation) | Aggregated reporting data |

Each service must access its own database through its repository or persistence layer.

## 4. Logical Data Model

### 4.1 Auth Service

**users**

| Column | Description |
|---|---|
| id | Primary key |
| email | Unique user email |
| password_hash | Securely hashed password |
| full_name | Customer name |
| status | Account status |
| created_at | Creation timestamp |
| updated_at | Last update timestamp |

**roles**

| Column | Description |
|---|---|
| id | Primary key |
| name | Role name |

**user_roles**

| Column | Description |
|---|---|
| user_id | User identifier |
| role_id | Role identifier |

**refresh_tokens**

| Column | Description |
|---|---|
| id | Primary key |
| user_id | User identifier |
| token_hash | Hashed refresh token |
| expires_at | Expiry timestamp |
| revoked_at | Revocation timestamp |

Passwords and refresh tokens must never be stored as plaintext.

### 4.2 Product Service

**categories**

| Column | Description |
|---|---|
| id | Primary key |
| name | Category name |
| description | Category description |

**products**

| Column | Description |
|---|---|
| id | Primary key |
| sku | Unique product identifier |
| name | Product name |
| description | Product description |
| category_id | Category identifier |
| price | Current selling price |
| status | ACTIVE or INACTIVE |
| created_at | Creation timestamp |
| updated_at | Last update timestamp |

Redis may cache frequently requested product details. PostgreSQL remains the source of truth.

### 4.3 Cart Service

Cart data is stored in Redis.

Logical cart structure:

- Cart ID
- Customer ID
- Product IDs
- Quantities
- Created timestamp
- Updated timestamp
- Expiration time

The cart stores product identifiers and quantities. Product descriptions and prices must be revalidated during checkout.

Cart data is temporary and may expire after a configurable period.

### 4.4 Inventory Service

**inventory**

| Column | Description |
|---|---|
| id | Primary key |
| product_id | Product identifier |
| available_quantity | Available stock |
| reserved_quantity | Reserved stock |
| updated_at | Last update timestamp |

**inventory_reservations**

| Column | Description |
|---|---|
| id | Primary key |
| order_id | Associated order |
| product_id | Product identifier |
| quantity | Reserved quantity |
| status | Reservation status |
| expires_at | Reservation expiry |
| created_at | Creation timestamp |

Reservation status may include:

- RESERVED
- CONFIRMED
- RELEASED
- EXPIRED

Inventory Service owns all stock calculations and reservation operations.

### 4.5 Order Service

**orders**

| Column | Description |
|---|---|
| id | Primary key |
| customer_id | Customer identifier |
| total_amount | Order total |
| currency | Currency code |
| status | Order status |
| created_at | Creation timestamp |
| updated_at | Last update timestamp |

**order_items**

| Column | Description |
|---|---|
| id | Primary key |
| order_id | Associated order |
| product_id | Product identifier |
| product_name | Product name snapshot |
| unit_price | Price at purchase time |
| quantity | Purchased quantity |
| subtotal | Item subtotal |

Order items retain a snapshot of product name and price so that historical orders do not change when the product catalog is updated.

**outbox_events**

| Column | Description |
|---|---|
| id | Event identifier |
| aggregate_id | Order identifier |
| event_type | Business event type |
| payload | Event payload |
| created_at | Creation timestamp |
| published_at | Publication timestamp |
| status | Publication status |

The outbox table supports reliable event publication.

### 4.6 Payment Service

**payments**

| Column | Description |
|---|---|
| id | Primary key |
| order_id | Associated order |
| amount | Payment amount |
| currency | Currency code |
| status | Payment status |
| provider_reference | Mock provider reference |
| idempotency_key | Unique request key |
| created_at | Creation timestamp |
| updated_at | Last update timestamp |

The initial project uses a mock payment provider. Real card numbers, CVVs, and other sensitive payment credentials will not be stored.

### 4.7 Notification Service

**notifications**

| Column | Description |
|---|---|
| id | Primary key |
| customer_id | Customer identifier |
| event_id | Source event identifier |
| channel | Notification channel |
| status | Delivery status |
| created_at | Creation timestamp |
| delivered_at | Delivery timestamp |

The initial implementation may log notifications instead of integrating an external email or SMS provider.

### 4.8 Analytics Service

Analytics Service maintains reporting-oriented data derived from Kafka events.

Possible initial entities:

- Order summary
- Daily sales summary
- Payment summary
- Product sales summary

Analytics data is derived and can be rebuilt from retained events or source data exports where available.

## 5. Data Relationships

Logical relationships include:

- One user can have multiple orders.
- One order can contain multiple order items.
- One order can have multiple payment attempts.
- One product can have one inventory record.
- One order can have multiple inventory reservations.
- One user can have one active cart at a time in the initial implementation.

Cross-service identifiers are stored as references. They are not implemented as database foreign keys across service databases.

## 6. Data Consistency

The platform uses different consistency approaches depending on the business operation.

### Strong consistency within a service

A service uses database transactions to maintain consistency for its own data.

Examples:

- Creating an order and its order items.
- Updating inventory and creating a reservation.
- Recording a payment attempt.

### Eventual consistency across services

Cross-service updates are coordinated through REST calls and Kafka events.

Examples:

- Notification history is updated after an order event is consumed.
- Analytics summaries are updated asynchronously.
- Order status may be updated after receiving a payment result event.

The platform does not assume a single ACID transaction across multiple microservices.

## 7. Data Retention and Cleanup

- Expired carts should be removed through Redis expiration.
- Expired inventory reservations should be released.
- Refresh tokens should expire or be revoked.
- Application logs should follow a configurable retention policy.
- Notification and analytics data should have configurable retention.
- Outbox records should be archived or cleaned up after successful publication and the required retention period.

## 8. Data Security

- Database credentials must be stored outside source code.
- Services should use separate database users and least-privilege permissions.
- Sensitive fields must not appear in logs.
- Passwords must be stored using a secure password-hashing algorithm.
- Production database connections must use encryption.
- Backups should be protected and access-controlled.

## 9. Initial Implementation Strategy

For local development, Docker Compose will run PostgreSQL, Redis, and Kafka.

Each service will have its own logical database or isolated schema and credentials. Production deployment can use separately managed database instances as required.

Database migrations will be version-controlled and applied through a migration tool such as Flyway.

## 10. Conclusion

The platform follows the Database per Service pattern, with PostgreSQL for transactional persistence, Redis for temporary and cached data, and Kafka for asynchronous communication.

This design reduces coupling between services and provides a foundation for independent development, deployment, and scaling.