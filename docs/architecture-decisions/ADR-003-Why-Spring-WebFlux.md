# ADR-003 — Why Spring WebFlux?
## Status

Accepted

## Context

The platform is expected to handle many concurrent I/O operations involving:

* Databases
* Redis
* Kafka
* External services
* HTTP APIs

A blocking request model can tie up application threads while waiting for I/O.

## Decision

Use Spring WebFlux for reactive HTTP APIs.

WebFlux uses a non-blocking programming model and is built around Project Reactor.

## Traditional Blocking Model
     Request 
        |
        v
     Thread
        |
        v
     Database
        |
        | WAIT
        | WAIT
        v
     Response

The thread can remain occupied while the database operation is executing.

## Reactive Model
    Request
        |
        v
    Event Loop
        |
        v
    Non-Blocking I/O
        |
        +----> Other Requests
        |
        +----> Other Requests
        |
        v
    Result Available
        |
        v
    Response

The objective is to avoid blocking application threads while waiting for I/O.

## Benefits
* Non-blocking I/O
* Efficient resource utilization
* High concurrency
* Backpressure support
* Streaming support

## Trade-offs

Reactive programming introduces additional complexity:

* Steeper learning curve
* More complex debugging
* Reactive error handling
* Difficulty integrating blocking libraries
* More complex stack traces

## Important Decision

WebFlux will not be considered automatically superior to Spring MVC.

For applications with:

* Low concurrency
* Mostly blocking operations
* CPU-heavy workloads
* Existing blocking dependencies

Spring MVC may be a better choice.