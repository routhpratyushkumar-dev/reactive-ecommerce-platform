# ADR-007 — Why Apache Kafka?
## Status

Accepted

## Context

Multiple services need to react to business events without becoming tightly coupled.

Example:

    Order Service
     |
     | OrderCreated
     v
    Kafka
     |
    +------------+-------------+
    |            |             |
    v            v             v
    Inventory    Notification    Analytics
    Service        Service        Service

## Decision

Use Apache Kafka for event-driven asynchronous communication.

## Benefits
* High throughput
* Durable events
* Partitioning
* Consumer groups
* Event replay
* Service decoupling
* Independent scaling

## Concepts to Learn
* Topics
* Partitions
* Offsets
* Consumer groups
* Producers
* Consumers
* Replication
* Ordering
* Delivery semantics
* Retry
* Dead Letter Topics
* Idempotency