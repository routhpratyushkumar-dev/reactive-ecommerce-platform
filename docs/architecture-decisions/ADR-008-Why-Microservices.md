# ADR-008 — Why Microservices?
## Status

Accepted

## Initial Service Boundaries
* API Gateway

* Auth Service

* Product Service

* Cart Service

* Inventory Service

* Order Service

* Payment Service

* Notification Service

## Why Microservices?

Potential benefits:

* Independent deployment
* Independent scaling
* Service ownership
* Fault isolation
* Clear domain boundaries

## Challenges

Microservices introduce significant complexity:

* Network failures
* Distributed transactions
* Data consistency
* Service discovery
* Observability
* Deployment complexity
* Debugging complexity

## Decision Rationale

Microservices are being selected because the project is intended to demonstrate distributed enterprise architecture and the e-commerce domain contains reasonably separable business capabilities.

The project will avoid creating services purely for technology demonstration.