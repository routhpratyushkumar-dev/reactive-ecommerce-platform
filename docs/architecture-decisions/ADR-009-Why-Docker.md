# ADR-009 — Why Docker?
## Status

Accepted

## Decision

Use Docker for application packaging and local development.

## Proposed Local Environment
Docker Compose

    +----------------+
    | PostgreSQL     |
    +----------------+
    
    +----------------+
    | Redis          |
    +----------------+
    
    +----------------+
    | Kafka          |
    +----------------+
    
    +----------------+
    | Auth Service   |
    +----------------+
    
    +----------------+
    | Product Service|
    +----------------+
    
    +----------------+
    | Order Service  |
    +----------------+

## Benefits
* Reproducible environments
* Consistent dependencies
* Easy local setup
* Isolation
* Portable application packaging

The long-term goal is to allow developers to start the infrastructure with:

**docker compose up**