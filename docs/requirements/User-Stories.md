# Authentication

## US-AUTH-001 - User Registration

**Epic:** Authentication

**Priority:** High

### User Story

As a customer,

I want to register using my email address,

So that I can securely access the platform.

### Acceptance Criteria

- User must provide first name, last name, email, password and phone number.
- Email must be unique.
- Password must be encrypted before storing.
- User receives a verification email.
- Registration fails if email already exists.

### Business Value

Provides secure onboarding for new customers.

## US-AUTH-002 - User Login

Epic: Authentication

Priority: High

### User Story

As a registered customer,

I want to log into the application,

So that I can access my account.

### Acceptance Criteria

- Valid credentials generate JWT.
- Refresh token generated.
- Invalid credentials return HTTP 401.
- Failed login attempts logged.

Business Value

Secure access to customer accounts.

# Product

## US-PROD-001 – Browse Products
As a customer,

I want to browse products,

So that I can explore available items.

Acceptance Criteria

- Products displayed with image.
- Price visible.
- Category displayed.
- Pagination supported.
- Sorting available.

## US-PROD-002 – Search Products
As a customer,

I want to search products by keyword,

So that I can quickly find what I need.

Acceptance Criteria

- Search by name.
- Search by category.
- Partial match supported.
- Response time below 300ms.

# Cart
## US-CART-001
As a customer,

I want to add products to my shopping cart,

So that I can purchase multiple products together.

Acceptance Criteria

- Quantity configurable.
- Stock validated.
- Cart total recalculated.
- Duplicate products update quantity.

# Orders
## US-ORDER-001
As a customer,

I want to place an order,

So that I can purchase products.

Acceptance Criteria

- Validate stock.
- Validate payment.
- Generate order.
- Reserve inventory.
- Send notification.

# Inventory
As an inventory manager,

I want stock to reduce automatically,

So that inventory remains accurate.

Acceptance Criteria

- Stock updated after successful payment.
- Prevent negative inventory.
- Trigger low-stock alert.

# Notifications
As a customer,

I want to receive order updates,

So that I know the status of my purchase.

Acceptance Criteria

- Order confirmation email.
- Shipping notification.
- Delivery notification.