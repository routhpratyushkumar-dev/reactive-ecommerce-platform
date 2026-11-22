# REST API Specification

## 1. API Standards

All external APIs will follow these conventions:

- Base path: `/api/v1`
- Data format: JSON
- Content type: `application/json`
- Authentication: Bearer JWT for protected endpoints
- Resource naming: plural nouns
- Identifiers: UUID
- Timestamps: ISO 8601 UTC
- Monetary values: decimal values with an explicit currency code

Example:

`GET /api/v1/products`

## 2. Standard HTTP Status Codes

| Status | Meaning |
|---|---|
| 200 | Request successful |
| 201 | Resource created |
| 202 | Request accepted for asynchronous processing |
| 204 | Successful request with no response body |
| 400 | Invalid request |
| 401 | Authentication required or invalid |
| 403 | Access forbidden |
| 404 | Resource not found |
| 409 | Business conflict |
| 422 | Business validation failure |
| 429 | Too many requests |
| 500 | Internal server error |
| 503 | Service unavailable |

## 3. Auth Service APIs

Base URL: `/api/v1/auth`

| Method | Endpoint | Description | Authentication |
|---|---|---|---|
| POST | `/register` | Register customer | Public |
| POST | `/login` | Authenticate customer | Public |
| POST | `/refresh` | Refresh access token | Refresh token |
| POST | `/logout` | Revoke refresh token | Authenticated |

### Registration request

```json
{
  "email": "customer@example.com",
  "password": "StrongPassword123",
  "fullName": "Test Customer"
}
```

### Login request

```json
{
  "email": "customer@example.com",
  "password": "StrongPassword123"
}
```

### Authentication response

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "opaque-refresh-token",
  "tokenType": "Bearer",
  "expiresIn": 900
}
```

The refresh token is an opaque, securely generated token. Only its hash should be persisted.

## 4. Product Service APIs

Base URL: `/api/v1/products`

| Method | Endpoint | Description | Role |
|---|---|---|---|
| GET | `/` | List products | Public |
| GET | `/{id}` | Get product details | Public |
| POST | `/` | Create product | ADMIN |
| PUT | `/{id}` | Update product | ADMIN |
| DELETE | `/{id}` | Deactivate product | ADMIN |

### Create product request

```json
{
  "sku": "SKU-1001",
  "name": "Wireless Headphones",
  "description": "Bluetooth headphones",
  "categoryId": "c5a3f8e1-1a4b-4d45-ae5d-123456789abc",
  "price": 2499.00,
  "currency": "INR"
}
```

### Product response

```json
{
  "id": "d4a3f8e1-1a4b-4d45-ae5d-123456789abc",
  "sku": "SKU-1001",
  "name": "Wireless Headphones",
  "price": 2499.00,
  "currency": "INR",
  "status": "ACTIVE"
}
```

Product listing should support pagination, filtering, and sorting.

## 5. Cart Service APIs

Base URL: `/api/v1/cart`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/` | Get current customer's cart |
| POST | `/items` | Add item |
| PUT | `/items/{productId}` | Update item quantity |
| DELETE | `/items/{productId}` | Remove item |
| DELETE | `/` | Clear cart |

The customer ID must come from the authenticated principal, not from an untrusted request field.

### Add item request

```json
{
  "productId": "d4a3f8e1-1a4b-4d45-ae5d-123456789abc",
  "quantity": 2
}
```

### Cart response

```json
{
  "customerId": "e2a3f8e1-1a4b-4d45-ae5d-123456789abc",
  "items": [
    {
      "productId": "d4a3f8e1-1a4b-4d45-ae5d-123456789abc",
      "quantity": 2
    }
  ]
}
```

Prices displayed in the cart may be retrieved from Product Service, but checkout must revalidate them.

## 6. Inventory Service APIs

Base URL: `/api/v1/inventory`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/{productId}` | Get stock |
| POST | `/reservations` | Reserve stock |
| POST | `/reservations/{id}/confirm` | Confirm reservation |
| POST | `/reservations/{id}/release` | Release reservation |

### Reserve stock request

```json
{
  "orderId": "a1a3f8e1-1a4b-4d45-ae5d-123456789abc",
  "items": [
    {
      "productId": "d4a3f8e1-1a4b-4d45-ae5d-123456789abc",
      "quantity": 2
    }
  ]
}
```

The reservation endpoint must support an idempotency key.

### Reservation response

```json
{
  "reservationId": "b1a3f8e1-1a4b-4d45-ae5d-123456789abc",
  "orderId": "a1a3f8e1-1a4b-4d45-ae5d-123456789abc",
  "status": "RESERVED"
}
```

## 7. Order Service APIs

Base URL: `/api/v1/orders`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/checkout` | Create order through checkout |
| GET | `/{orderId}` | Get order details |
| GET | `/` | Get customer's orders |
| POST | `/{orderId}/cancel` | Cancel eligible order |

### Checkout request

```json
{
  "items": [
    {
      "productId": "d4a3f8e1-1a4b-4d45-ae5d-123456789abc",
      "quantity": 2
    }
  ],
  "shippingAddress": {
    "line1": "Example Street",
    "city": "Hyderabad",
    "state": "Telangana",
    "postalCode": "500001",
    "country": "India"
  }
}
```

### Order response

```json
{
  "orderId": "a1a3f8e1-1a4b-4d45-ae5d-123456789abc",
  "status": "CONFIRMED",
  "totalAmount": 4998.00,
  "currency": "INR",
  "items": [
    {
      "productId": "d4a3f8e1-1a4b-4d45-ae5d-123456789abc",
      "productName": "Wireless Headphones",
      "unitPrice": 2499.00,
      "quantity": 2,
      "subtotal": 4998.00
    }
  ]
}
```

Checkout should return an appropriate pending or failure response if the final outcome has not yet been determined.

## 8. Payment Service APIs

Base URL: `/api/v1/payments`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/` | Initiate payment |
| GET | `/{paymentId}` | Get payment status |
| POST | `/{paymentId}/refund` | Refund payment |

### Payment request

```json
{
  "orderId": "a1a3f8e1-1a4b-4d45-ae5d-123456789abc",
  "amount": 4998.00,
  "currency": "INR"
}
```

Payment initiation must accept an idempotency key.

## 9. Analytics APIs

Base URL: `/api/v1/analytics`

| Method | Endpoint | Description | Role |
|---|---|---|---|
| GET | `/sales/daily` | Daily sales summary | ADMIN |
| GET | `/orders/summary` | Order statistics | ADMIN |
| GET | `/products/top-selling` | Top-selling products | ADMIN |

## 10. Common Error Response

All services should return a consistent error structure.

```json
{
  "timestamp": "2026-11-22T10:30:00Z",
  "status": 404,
  "errorCode": "PRODUCT_NOT_FOUND",
  "message": "Requested product was not found",
  "path": "/api/v1/products/123",
  "correlationId": "corr-10001"
}
```

Do not expose stack traces, database details, or internal credentials.

## 11. Pagination

List APIs should support:

- `page`
- `size`
- `sort`

Example:

`GET /api/v1/products?page=0&size=20&sort=name,asc`

Maximum page size should be configured to prevent excessive resource usage.

## 12. API Security

- Public endpoints must be explicitly configured.
- Protected endpoints require valid access tokens.
- ADMIN operations require the ADMIN role.
- Customers can access only their own resources.
- Internal APIs must use authenticated service-to-service communication.
- Sensitive operations should support idempotency keys.

## 13. Conclusion

This document defines the initial REST API contracts. Exact schemas and validation rules will be finalized during implementation and maintained alongside the service code.