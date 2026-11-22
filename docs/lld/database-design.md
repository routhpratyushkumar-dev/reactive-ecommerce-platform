# Database Design

## 1. Database Strategy

The platform follows Database per Service.

Each microservice owns its database and controls its schema migrations.

| Database | Owner |
|---|---|
| auth_db | Auth Service |
| product_db | Product Service |
| inventory_db | Inventory Service |
| order_db | Order Service |
| payment_db | Payment Service |
| notification_db | Notification Service |
| analytics_db | Analytics Service |

Cart Service uses Redis for cart persistence.

For local development, the databases may run on one PostgreSQL instance while remaining logically isolated. Production can use separate instances.

## 2. Common Database Standards

- Primary keys use UUID.
- Table names use snake_case.
- Column names use snake_case.
- Timestamps use TIMESTAMPTZ.
- Monetary amounts use NUMERIC, not floating-point types.
- Foreign keys are allowed within a service database.
- Cross-service relationships use identifiers without cross-database foreign keys.
- Indexes should support common query patterns.
- Schema changes are managed through Flyway migrations.

## 3. Auth Database

### users

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| email | VARCHAR(255) | Unique, not null |
| password_hash | VARCHAR(255) | Not null |
| full_name | VARCHAR(150) | Not null |
| status | VARCHAR(30) | Not null |
| created_at | TIMESTAMPTZ | Not null |
| updated_at | TIMESTAMPTZ | Not null |

### roles

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| name | VARCHAR(50) | Unique, not null |

### user_roles

| Column | Type | Constraint |
|---|---|---|
| user_id | UUID | Foreign key to users |
| role_id | UUID | Foreign key to roles |

Composite primary key: `(user_id, role_id)`.

### refresh_tokens

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| user_id | UUID | Foreign key to users |
| token_hash | VARCHAR(255) | Not null |
| expires_at | TIMESTAMPTZ | Not null |
| revoked_at | TIMESTAMPTZ | Nullable |
| created_at | TIMESTAMPTZ | Not null |

Create an index on `user_id` and an appropriate unique index on `token_hash`.

## 4. Product Database

### categories

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| name | VARCHAR(100) | Not null |
| description | TEXT | Nullable |
| created_at | TIMESTAMPTZ | Not null |

### products

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| sku | VARCHAR(100) | Unique, not null |
| name | VARCHAR(200) | Not null |
| description | TEXT | Nullable |
| category_id | UUID | Foreign key to categories |
| price | NUMERIC(12,2) | Not null |
| currency | CHAR(3) | Not null |
| status | VARCHAR(30) | Not null |
| created_at | TIMESTAMPTZ | Not null |
| updated_at | TIMESTAMPTZ | Not null |

Indexes:

- `sku`
- `category_id`
- `status`
- `name` where product search requires it

Price must be non-negative. Product updates should invalidate Redis cache entries.

## 5. Cart Redis Data Model

Redis key:

`cart:{customerId}`

Example value:

```json
{
  "customerId": "e2a3f8e1-1a4b-4d45-ae5d-123456789abc",
  "items": [
    {
      "productId": "d4a3f8e1-1a4b-4d45-ae5d-123456789abc",
      "quantity": 2
    }
  ],
  "updatedAt": "2026-11-22T10:30:00Z"
}
```

Set a configurable TTL on cart data.

Redis is the source of truth for active carts in the initial implementation. If a cart expires, the customer may need to add items again.

## 6. Inventory Database

### inventory

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| product_id | UUID | Unique, not null |
| available_quantity | INTEGER | Not null |
| reserved_quantity | INTEGER | Not null |
| version | BIGINT | Not null |
| updated_at | TIMESTAMPTZ | Not null |

### inventory_reservations

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| order_id | UUID | Not null |
| status | VARCHAR(30) | Not null |
| expires_at | TIMESTAMPTZ | Not null |
| created_at | TIMESTAMPTZ | Not null |
| updated_at | TIMESTAMPTZ | Not null |

### inventory_reservation_items

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| reservation_id | UUID | Foreign key to inventory_reservations |
| product_id | UUID | Not null |
| quantity | INTEGER | Not null |

The reservation operation must prevent overselling.

Use a database transaction and conditional stock update, or another safe concurrency-control strategy. Do not rely only on reading a quantity and updating it later without concurrency protection.

## 7. Order Database

### orders

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| customer_id | UUID | Not null |
| total_amount | NUMERIC(12,2) | Not null |
| currency | CHAR(3) | Not null |
| status | VARCHAR(40) | Not null |
| idempotency_key | VARCHAR(150) | Unique where provided |
| shipping_address | JSONB | Not null |
| created_at | TIMESTAMPTZ | Not null |
| updated_at | TIMESTAMPTZ | Not null |

### order_items

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| order_id | UUID | Foreign key to orders |
| product_id | UUID | Not null |
| product_name | VARCHAR(200) | Not null |
| unit_price | NUMERIC(12,2) | Not null |
| quantity | INTEGER | Not null |
| subtotal | NUMERIC(12,2) | Not null |

Order items retain a product name and price snapshot.

### outbox_events

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| aggregate_id | UUID | Not null |
| event_type | VARCHAR(100) | Not null |
| event_version | INTEGER | Not null |
| payload | JSONB | Not null |
| created_at | TIMESTAMPTZ | Not null |
| published_at | TIMESTAMPTZ | Nullable |
| status | VARCHAR(30) | Not null |

Indexes should support retrieval of unpublished events and order history.

## 8. Payment Database

### payments

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| order_id | UUID | Not null |
| amount | NUMERIC(12,2) | Not null |
| currency | CHAR(3) | Not null |
| status | VARCHAR(30) | Not null |
| provider_reference | VARCHAR(150) | Nullable |
| idempotency_key | VARCHAR(150) | Unique, not null |
| created_at | TIMESTAMPTZ | Not null |
| updated_at | TIMESTAMPTZ | Not null |

An order can have multiple payment attempts. Each attempt must have a unique idempotency key.

## 9. Notification Database

### notifications

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| customer_id | UUID | Not null |
| event_id | UUID | Unique, not null |
| channel | VARCHAR(30) | Not null |
| status | VARCHAR(30) | Not null |
| message | TEXT | Nullable |
| created_at | TIMESTAMPTZ | Not null |
| delivered_at | TIMESTAMPTZ | Nullable |

The unique event ID helps prevent duplicate notification processing.

## 10. Analytics Database

### daily_sales_summary

| Column | Type | Constraint |
|---|---|---|
| id | UUID | Primary key |
| sales_date | DATE | Unique, not null |
| order_count | BIGINT | Not null |
| successful_payment_count | BIGINT | Not null |
| total_sales | NUMERIC(14,2) | Not null |
| currency | CHAR(3) | Not null |
| updated_at | TIMESTAMPTZ | Not null |

Analytics data is derived from consumed business events. Reporting must distinguish currencies rather than adding unlike currencies together.

## 11. Database Migrations

Each service maintains its own Flyway migration directory.

Example:

```text
src/main/resources/db/migration/
├── V1__create_users.sql
├── V2__create_roles.sql
└── V3__create_refresh_tokens.sql
```

Migration scripts must be version-controlled and should not be edited after they have been applied in a shared environment. Create a new migration for schema changes.

## 12. Data Integrity

- Validate positive quantities.
- Validate non-negative prices.
- Use database constraints for uniqueness.
- Use transactions for related writes within a service.
- Use optimistic locking or conditional updates where concurrent updates are possible.
- Keep business state transitions in the owning service.
- Do not create cross-service foreign keys.

## 13. Conclusion

This database design separates ownership by microservice and uses PostgreSQL for transactional data, Redis for carts, and Kafka for event transport.

It supports service independence, reliable persistence, and future scalability.