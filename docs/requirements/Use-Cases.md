# UC-001 - Register User

## Goal

Allow a new customer to create an account.

## Primary Actor

Customer

## Supporting Actors

Authentication Service

Email Notification Service

## Preconditions

- Customer is not already registered.
- Email is valid.

## Trigger

Customer clicks "Register".

## Main Flow

1. Customer opens registration page.
2. Customer enters personal information.
3. Customer submits the form.
4. System validates input.
5. System checks if email already exists.
6. Password is encrypted.
7. Customer record is stored.
8. Verification email is sent.
9. Success response returned.

## Alternative Flow

4A. Invalid email format.

- Display validation error.

5A. Email already exists.

- Return HTTP 409 Conflict.

## Exception Flow

Database unavailable.

- Return HTTP 503.

## Postconditions

Customer account successfully created.

# UC-002 – Login

## Goal

Authenticate customer.

## Primary Actor

Customer

## Preconditions

Customer already registered.

## Main Flow

1. Enter email.
2. Enter password.
3. Validate credentials.
4. Generate JWT.
5. Generate Refresh Token.
6. Return tokens.

## Alternative Flow

Incorrect password. 

Return HTTP 401.

# UC-003 – Search Products
## Goal

Search products.

## Primary Actor

Customer

## Main Flow

1. Customer enters search keyword.
2. Product Service receives request.
3. Search database.
4. Return matching products.
5. Display results.

## Alternative Flow

No products found.

Return empty list.

# UC-004 – Place Order

Customer
│
▼
Order Service
│
▼
Inventory Service
│
▼
Payment Service
│
▼
Kafka
│
▼
Notification Service

## Goal

Purchase products.

## Primary Actor

Customer

## Supporting Actors

Order Service

Inventory Service

Payment Service

Notification Service

Kafka

## Preconditions

- Customer logged in.
- Cart contains items.
- Inventory available.

## Main Flow

1. Customer clicks Checkout.
2. Order Service validates cart.
3. Inventory reserves stock.
4. Payment initiated.
5. Payment succeeds.
6. Order created.
7. Kafka publishes OrderCreated event.
8. Notification Service sends confirmation email.
9. Customer receives order confirmation.

## Alternative Flow 1

Inventory unavailable.

Display Out of Stock.

Alternative Flow

Payment failed.

Release reserved inventory.

Cancel order.

## Alternative Flow 2

Kafka unavailable.

Retry event publication.

Store event in Outbox (advanced implementation).

Postconditions

Order successfully created.

# UC-005 – Cancel Order

## Goal

Cancel an order.

## Primary Actor

Customer

## Preconditions

- Order exists.
- Order is not shipped.

## Main Flow

1. Customer requests cancellation.
2. Order status is validated.
3. Inventory is restored.
4. Refund is initiated (if payment completed).
5. Notification is sent.

## Alternative Flow

Order already shipped.

Cancellation request rejected.