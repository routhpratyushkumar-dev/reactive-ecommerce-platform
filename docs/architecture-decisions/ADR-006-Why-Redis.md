# ADR-006— Why Redis?
## Status

Accepted

## Decision

Use Redis for low-latency distributed data access.

## Initial Use Cases
    Redis
    |
    +---- Product Cache
    |
    +---- Session Data
    |
    +---- Rate Limiting
    |
    +---- Distributed Locks

## Cache-Aside Pattern
    Product Request
          |
          v
         Redis
        /     \
     HIT       MISS
      |          |
      v          v
    Response   PostgreSQL
                    |
                    v
                  Redis
                    |
                    v
                 Response
## Why Redis?
* Very low latency
* In-memory operations
* TTL support
* Distributed deployment support
* Useful for caching and rate limiting

Concepts to Learn
* Cache-aside
* TTL
* Eviction
* Cache invalidation
* Write-through caching
* Write-behind caching
* Distributed locks