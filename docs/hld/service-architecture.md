# Service Architecture

## 1. Overview
By the end of this step, we'll have clearly defined:

* Each microservice
* Its business responsibility
* Its APIs
* Its data ownership
* Its dependencies
* What it should not do
* How services communicate

Our target is:

                        API Gateway
                           |
        +----------+-----------+-----------+----------+
        |          |           |           |          |
        v          v           v           v          v
      Auth      Product       Cart      Inventory    Order
      Service   Service       Service   Service      Service
                                                       |
                                                       v
                                                    Payment
                                                    Service
                                                       |
                                                       v
                                                     Kafka
                                                       |
                                    +------------------+----------------+
                                    |                                   |
                                    v                                   v
                                Notification                         Analytics
                                Service                              Service

## 2. Service Boundary Principles
We're going to use **Domain-Driven Design (DDD)** concepts at a high level.

The key question is:

"What business capability does this service own?"

Not:

"Which database table should become a service?"

For example, don't create:

    User Service
    Product Service
    Order Service
    OrderItem Service
    Payment Service

just because there are five tables.

Instead, group related business capabilities.

## 3. Auth Service
### Responsibility

Authentication and identity-related operations.

### Owns

    User registration
    Login
    Password management
    JWT generation
    Refresh tokens
    Role management
    Account status

### Example APIs

    POST /auth/register
    POST /auth/login
    POST /auth/refresh
    POST /auth/logout
    
    GET  /users/me
    PUT  /users/me

### Data

    Auth Database
    
    users
    roles
    user_roles
    refresh_tokens

### It should **NOT** own

    ❌ Products
    ❌ Orders
    ❌ Cart
    ❌ Inventory
    ❌ Payment information

## 4. Product Service

### Responsibility

Product catalog.

### Owns

    Products
    Categories
    Product descriptions
    Product pricing
    Product status
    Product attributes
    Product images metadata

### APIs

    GET    /products
    GET    /products/{id}
    POST   /products
    PUT    /products/{id}
    DELETE /products/{id}
    
    GET    /categories

### Data

    Product Database
    
    products
    categories
    product_categories
    product_attributes

### Redis

Product Service can use Redis:

    Product Service
            |
            +---- Redis
            |
            +---- PostgreSQL

### Important boundary

Product Service owns product information.

It does not own:

    Stock quantity
    Orders
    Shopping carts
    Payments

Inventory owns stock.

## 5. Cart Service

### Responsibility

Managing the customer's shopping cart.

### Owns

    Cart
    Cart Items
    Quantities
    Cart expiration

Example:

    Customer
      |
      v
    Cart
      |
      +---- Product A × 2
      |
      +---- Product B × 1

### APIs

    GET    /cart
    POST   /cart/items
    PUT    /cart/items/{productId}
    DELETE /cart/items/{productId}
    DELETE /cart

### Storage

For our project, we'll use Redis as the primary cart store.

Why?

Cart data is:

Frequently accessed
Frequently modified
Short-lived
Naturally key-value oriented

Example:

    cart:{customerId}

could contain the customer's current cart.

### Important

Cart Service should not own product information.

It stores something like:

not the complete product.

## 6. Inventory Service

This is one of the most important services.

### Responsibility

Managing product availability and stock.

### Owns
    Stock quantity
    Reserved quantity
    Available quantity
    Warehouse information
    Inventory reservations

Example

    Product: P1001
    
    Total Stock       = 100
    Reserved          = 20
    Available         = 80

### APIs
    GET  /inventory/{productId}
    
    POST /inventory/reservations
    POST /inventory/reservations/{id}/confirm
    POST /inventory/reservations/{id}/release

### Data
    Inventory Database
    
    inventory
    inventory_reservations
    warehouses

### Critical rule

Product Service does NOT own inventory.

Why?

Because:

    Product = What are we selling?
    
    Inventory = How many are available?

These are different business responsibilities.

## 7. Order Service
This is our core business service.

### Responsibility

Order lifecycle.

### Owns
    Orders
    Order Items
    Order status
    Order totals
    Order history
### APIs
    POST /orders
    GET  /orders/{id}
    GET  /orders
    POST /orders/{id}/cancel
### Order states
    CREATED
        |
        v
    PENDING_PAYMENT
        |
        +------> PAYMENT_FAILED
        |
        v
    CONFIRMED
        |
        v
    SHIPPED
        |
        v
    DELIVERED
### Data
    Order Database
    
    orders
    order_items
    order_status_history
### Critical boundary

Order Service owns:

    The order

It doesn't own:

    Product catalog
    Inventory
    Payment processing
    Notifications

## 8. Payment Service
### Responsibility

Payment processing.

### Owns
    Payment transactions
    Payment status
    Payment reference
    Payment attempts
    Refund information
### APIs

Conceptually:

    POST /payments
    GET  /payments/{id}
    POST /payments/{id}/refund
### Data
    Payment Database
    
    payments
    payment_transactions
    refunds
### Important

The Payment Service should not store raw card information.

In a real production system, sensitive payment information should generally be handled through a compliant payment provider/tokenization mechanism.

For our project, we'll use a **mock payment provider**.

Example:
    
    Payment Service
        |
        v
    Mock Payment Provider
        |
        v
    SUCCESS / FAILURE

This lets us learn payment workflows without needing a real payment account.

## 9. Notification Service
### Responsibility

Sending notifications.

### Notifications
    Order confirmation
    Payment confirmation
    Order cancellation
    Shipping update
### Architecture
          Kafka
            |
            v
    Notification Service
        /            \
       v              v
      Email           SMS

For our free project, we'll initially implement email simulation/logging rather than paying for an external SMS/email provider.

### Important

Notification Service should not directly control orders.

It reacts to events.

For example:

    OrderConfirmed
      |
      v
    Kafka
      |
      v
    Notification Service
      |
      v
    Send confirmation

## 10. Analytics Service

This is an event consumer.

It can consume:
* OrderCreated
* OrderConfirmed
* PaymentCompleted
* ProductViewed
* ProductCreated

Architecture:

                    Kafka
                      |
          +-----------+-----------+
          |                       |
          v                       v
       Notification              Analytics
       Service                   Service

Analytics should not interfere with the order transaction.

If Analytics is down:

Orders should continue working.

That's an important microservice principle.

## 11. Responsibility Matrix

| Service      | Owns                     | Database           | Communication |
| ------------ | ------------------------ | ------------------ | ------------- |
| Auth         | Identity, authentication | PostgreSQL         | REST          |
| Product      | Catalog                  | PostgreSQL + Redis | REST          |
| Cart         | Shopping cart            | Redis              | REST          |
| Inventory    | Stock/reservation        | PostgreSQL         | REST + Kafka  |
| Order        | Order lifecycle          | PostgreSQL         | REST + Kafka  |
| Payment      | Payment lifecycle        | PostgreSQL         | REST + Kafka  |
| Notification | Notifications            | Optional           | Kafka         |
| Analytics    | Business analytics       | TBD                | Kafka         |

## 12. Service Communication Matrix
Now let's define who talks to whom.

| Caller    | Receiver     | Method | Reason               |
| --------- | ------------ | ------ | -------------------- |
| Client    | Gateway      | HTTPS  | External request     |
| Gateway   | Auth         | REST   | Authentication       |
| Gateway   | Product      | REST   | Product queries      |
| Gateway   | Cart         | REST   | Cart operations      |
| Gateway   | Order        | REST   | Order operations     |
| Order     | Inventory    | REST   | Reserve stock        |
| Order     | Payment      | REST   | Initiate payment     |
| Order     | Kafka        | Event  | Order events         |
| Inventory | Kafka        | Event  | Inventory events     |
| Payment   | Kafka        | Event  | Payment events       |
| Kafka     | Notification | Event  | Send notification    |
| Kafka     | Analytics    | Event  | Analytics processing |


## 13. Core Checkout Flow

Now let's combine everything.

Suppose:

    Customer wants to buy Laptop × 1

Flow:

    Customer
        |
        v
    API Gateway
        |
        v
    Order Service
        |
        | 1. Validate cart/order
        v
    Inventory Service
        |
        | 2. Reserve stock
        v
    Payment Service
        |
        | 3. Process payment
        v
    Order Service
        |
        | 4. Confirm order
        v
    Order DB
        |
        | 5. Publish OrderConfirmed
        v
    Kafka
        |
        +---------> Notification
        |
        +---------> Analytics

This is our first major business flow.

## 14. Failure Scenarios

Suppose:

    Inventory
      |
      v
    Stock Reserved
      |
      v
    Payment
      |
      X
    FAILED

We don't want inventory permanently reserved.

Therefore:

    Payment Failed
        |
        v
    Order Service
        |
        v
    Release Inventory
        |
        v
    Inventory Service

Then:

    Order Status = PAYMENT_FAILED

This introduces a distributed transaction problem.

We'll solve the details later using patterns such as:

    Saga
    Compensation
    Outbox

Don't implement these yet.

We're still in HLD.

## 15. Service Boundary Rules
These rules should go into the document.

### Rule 1

    A service owns its data.

Don't allow:

    Order Service
        |
        v
    Product Database

Instead:

    Order Service       Product Service
        |                      | 
        v                      v
    Order DB              Product DB

    
### Rule 2

    Don't share database tables between services.

Bad:

    Order Service   ──┐
                      ├──► Same DB Tables
    Product Service  ─┘

Good:

    Order ──► Order DB
    
    Product ──► Product DB
### Rule 3

    Services communicate through APIs or events, not direct database access.

### Rule 4

    Business logic belongs to the service that owns the business capability.

For example:

    Inventory reservation

belongs to Inventory Service.

Not Order Service.