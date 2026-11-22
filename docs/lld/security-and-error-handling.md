# Security, Validation and Exception Handling

## 1. Purpose

This document defines the implementation-level security controls, request validation rules, and error-handling standards used by the microservices.

## 2. Authentication

Auth Service manages user authentication.

The implementation uses:

- Spring Security.
- BCrypt password hashing.
- Signed JWT access tokens.
- Opaque refresh tokens.
- Role-based access control.

JWT access tokens should contain:

- Subject/user ID.
- Issuer.
- Issued-at timestamp.
- Expiration timestamp.
- Roles or authorities.
- Token identifier where required.

Use a securely managed signing key. Do not hardcode signing keys in application configuration committed to Git.

## 3. Authorization

Roles:

- CUSTOMER
- ADMIN
- SERVICE

Examples:

| Operation | Required Role |
|---|---|
| Browse products | Public |
| Add cart item | CUSTOMER |
| View own orders | CUSTOMER |
| Create product | ADMIN |
| Update product | ADMIN |
| View analytics | ADMIN |
| Internal service operation | SERVICE or authorized service identity |

Customer ownership must be checked within the owning service.

A customer must not access another customer's order by changing an order ID in the request.

## 4. Password and Token Handling

- Hash passwords with BCrypt.
- Never store plaintext passwords.
- Generate refresh tokens using a cryptographically secure random generator.
- Store only refresh-token hashes.
- Revoke refresh tokens on logout.
- Validate token expiry and revocation.
- Rotate refresh tokens where supported.
- Never log access tokens or refresh tokens.

## 5. Request Validation

Use Jakarta Bean Validation annotations on request DTOs.

Example:

```java
public record AddCartItemRequest(
        @NotNull UUID productId,
        @Min(1) @Max(100) int quantity
) {}
```

Other validation examples:

| Field | Validation |
|---|---|
| Email | Not blank and valid email format |
| Password | Minimum length and configured strength |
| Product name | Not blank and maximum length |
| Price | Non-negative |
| Quantity | Positive and within configured limit |
| Currency | Supported three-character currency code |
| Order ID | Valid UUID |
| Address | Required fields present |

Business validation must also be implemented in the service layer.

Examples include product availability, stock quantity, order ownership, and valid order status transitions.

## 6. Common Exception Types

| Exception | Meaning |
|---|---|
| ResourceNotFoundException | Requested resource not found |
| DuplicateResourceException | Resource already exists |
| InvalidRequestException | Invalid request data |
| BusinessValidationException | Business rule violation |
| InsufficientInventoryException | Not enough stock |
| PaymentFailedException | Payment unsuccessful |
| DownstreamServiceException | Remote service failure |
| IdempotencyConflictException | Conflicting use of idempotency key |
| UnauthorizedOperationException | Operation not permitted |

Service-specific exceptions may extend common application exception types.

## 7. Global Error Handling

Each Spring WebFlux service should implement centralized exception handling using `@RestControllerAdvice`.

Example:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public Mono<ResponseEntity<ApiError>> handleNotFound(
            ResourceNotFoundException exception) {

        ApiError error = new ApiError(
                "RESOURCE_NOT_FOUND",
                exception.getMessage()
        );

        return Mono.just(
                ResponseEntity.status(HttpStatus.NOT_FOUND).body(error)
        );
    }
}
```

The actual implementation should also handle validation exceptions, downstream failures, and unexpected errors.

## 8. Standard Error Structure

```json
{
  "timestamp": "2026-11-22T10:30:00Z",
  "status": 400,
  "errorCode": "INVALID_REQUEST",
  "message": "Quantity must be greater than zero",
  "path": "/api/v1/cart/items",
  "correlationId": "corr-10001"
}
```

Do not return stack traces, database queries, SQL errors, internal hostnames, or credentials to clients.

## 9. Reactive Error Handling

Use Reactor operators where appropriate:

- `onErrorMap()` to translate exceptions.
- `onErrorResume()` for controlled recovery.
- `switchIfEmpty()` for missing results.
- `doOnError()` for diagnostic logging.
- `timeout()` for bounded remote operations.

Avoid swallowing errors and returning fake success values.

Do not use blocking operations in reactive request-processing paths.

## 10. Downstream Service Errors

External service clients must map remote errors into meaningful application exceptions.

Examples:

- Product not found → product validation failure.
- Inventory unavailable → insufficient inventory or service unavailable.
- Payment declined → payment failure.
- Downstream timeout → operation outcome may be unknown.

An unknown payment outcome must be reconciled using the payment operation identifier or idempotency key before retrying.

## 11. Idempotency

Use idempotency keys for operations that must not be duplicated.

Important operations include:

- Order creation.
- Payment initiation.
- Inventory reservation.
- Refund initiation.

A repeated request with the same key and same logical payload should return the previously recorded result.

Reusing a key with a different payload should return a conflict.

## 12. Security Logging

Log:

- Authentication success and failure.
- Authorization failures.
- Important administrative actions.
- Unexpected service errors.
- Repeated validation failures.

Never log:

- Passwords.
- JWT values.
- Refresh tokens.
- Card numbers or CVVs.
- Database passwords.
- Secret keys.

## 13. Conclusion

The platform uses Spring Security, JWT, role-based authorization, request validation, centralized exception handling, and idempotency controls.

These controls provide a consistent security and error-handling foundation across the microservices.