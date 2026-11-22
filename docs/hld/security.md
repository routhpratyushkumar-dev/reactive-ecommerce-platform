# Security Architecture

## 1. Purpose

This document defines the security architecture for the Reactive E-Commerce Platform.

The objective is to protect customer information, authenticate users, authorize service operations, and secure communication between platform components.

## 2. Security Principles

The platform follows these principles:

- Authentication and authorization must be enforced.
- Apply least-privilege access.
- Do not expose internal services directly to external clients.
- Protect credentials and sensitive information.
- Validate all incoming requests.
- Use encrypted communication in deployed environments.
- Log security events without exposing secrets.

## 3. Authentication Architecture

The Auth Service is responsible for user authentication.

The initial implementation uses:

- Email and password authentication.
- BCrypt password hashing.
- JWT access tokens.
- Refresh tokens with expiry and revocation.
- Role-based authorization.

### Login flow

1. Customer submits email and password through API Gateway.
2. API Gateway forwards the request to Auth Service.
3. Auth Service validates the credentials.
4. Auth Service generates a signed JWT access token.
5. Auth Service issues a refresh token.
6. Client uses the access token for subsequent requests.

Access tokens should have a short configurable lifetime. Refresh tokens should be stored securely by clients and persisted as hashes by Auth Service.

## 4. Authorization

The platform initially supports role-based access control.

Proposed roles:

| Role | Permissions |
|---|---|
| CUSTOMER | Browse products, manage cart, place and view own orders |
| ADMIN | Manage products and perform administrative operations |
| SERVICE | Execute authorized internal service operations |

Services must enforce authorization for sensitive operations. Gateway authentication alone is not sufficient.

A customer must only be able to access their own cart, orders, and payment information.

## 5. API Gateway Security

Spring Cloud Gateway acts as the external entry point.

Responsibilities:

- Validate JWT access tokens for protected routes.
- Reject unauthenticated requests.
- Apply request-size limits.
- Apply rate limits.
- Forward authorized requests to internal services.
- Propagate correlation and trace identifiers.
- Avoid logging credentials or authorization headers.

Public routes, such as registration, login, and selected product browsing APIs, must be explicitly configured.

## 6. Service-to-Service Security

Internal service communication must be authenticated in deployed environments.

Initial implementation:

- Internal services run on a private Docker network locally.
- Service endpoints are not exposed directly to the public network.
- Internal requests carry appropriate service identity or authorization credentials.
- Each service validates the permissions required for sensitive operations.

For production deployment, stronger service identity and mutual TLS can be introduced.

## 7. Data Security

- Passwords are hashed using BCrypt.
- Refresh tokens are stored as hashes.
- Secrets are provided through environment variables or a secret manager.
- Database users follow least-privilege permissions.
- Sensitive customer information is not included in Kafka events unless required.
- Payment card details are not collected or stored in this mock-payment project.
- Production connections to databases and external systems must be encrypted.

## 8. Input Validation

All APIs must validate incoming data.

Examples:

- Validate email format.
- Reject invalid quantities.
- Validate positive monetary amounts.
- Enforce maximum request sizes.
- Validate identifiers and required fields.
- Reject unsupported or unexpected values.
- Prevent SQL injection through parameterized database operations.

Business validation must also be performed within the owning service.

## 9. Rate Limiting and Abuse Protection

Rate limiting should be applied to sensitive endpoints, especially:

- Login
- Registration
- Password-related operations
- Cart updates
- Checkout
- Payment initiation

The gateway can implement rate limiting using Redis-backed controls.

Additional protections such as account lockout or progressive login throttling can be introduced later.

## 10. Security Logging and Auditing

The platform should record important security events:

- Successful and failed login attempts.
- Token refresh and revocation.
- Unauthorized API access.
- Administrative product changes.
- Repeated validation failures.
- Suspicious checkout or payment activity.

Logs must not contain passwords, access tokens, refresh tokens, or sensitive payment details.

## 11. Security Testing

The implementation should include:

- Authentication unit tests.
- Authorization tests for customer and admin roles.
- Invalid and expired JWT tests.
- Input validation tests.
- Rate-limit tests.
- Dependency vulnerability scanning.
- API security testing using free tools such as OWASP ZAP.

## 12. Future Improvements

Potential improvements include:

- OAuth 2.0 and OpenID Connect.
- Multi-factor authentication.
- Mutual TLS for service-to-service communication.
- Centralized secrets management.
- Advanced fraud detection.
- Security information and event monitoring.
- Automated security testing in CI/CD.

## 13. Conclusion

The platform uses JWT-based authentication, role-based authorization, secure password hashing, API Gateway protection, and service-level access control.

Security controls will be implemented incrementally, with stronger production-grade controls added as the platform evolves.