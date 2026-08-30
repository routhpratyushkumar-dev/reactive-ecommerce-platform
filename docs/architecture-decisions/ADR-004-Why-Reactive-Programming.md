# ADR-004 — Why Reactive Programming?
## Status

Accepted

## Context

The platform contains many I/O-heavy workflows.

Example:

    Client
        |
        v
    API Gateway
        |
        v
    Order Service
        |
        +----> Inventory Service
        |
        +----> Payment Service
        |
        +----> Kafka
        |
        +----> Notification Service

## Decision

Use reactive programming for APIs and I/O pipelines where it provides measurable architectural value.

## Core Concepts

The project will use:

Mono<T>
Flux<T>

### Mono

Represents zero or one result.

    Mono<Product>

### Flux

Represents zero to many results.

    Flux<Product>

## Important Reactor Operators

The project will make use of:

* map
* flatMap
* flatMapMany
* concatMap
* filter
* zip
* merge
* switchIfEmpty
* onErrorResume
* doOnNext
* retry
* timeout
* buffer
* window

## Trade-offs

Reactive programming should not be introduced merely because it is popular.

It is most useful for:

* High concurrency
* I/O-heavy applications
* Streaming
* Event-driven systems