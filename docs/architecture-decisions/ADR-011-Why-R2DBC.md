# ADR-011 — Why R2DBC?
## Status

Accepted

## Context

Traditional JDBC is blocking.

A reactive application using:

    WebFlux
     |
     v
    JPA
     |
     v
    JDBC
     |
     v
    PostgreSQL

can still perform blocking database operations.

## Decision

Use R2DBC for reactive relational database connectivity.

## Proposed Flow
    WebFlux
     |
     v
    Project Reactor
     |
     v
    Spring Data R2DBC
     |
     v
    R2DBC Driver
     |
     v
    PostgreSQL

## Why?

R2DBC allows database operations to participate in a reactive, non-blocking application model.

## Why not JPA?

JPA and traditional JDBC APIs are blocking.

If the goal is an end-to-end non-blocking flow, R2DBC is a better fit for relational database access.

## Trade-offs

R2DBC does not provide every feature and abstraction that developers may be accustomed to with JPA/Hibernate.

Therefore, database design and data-access code will need to be more deliberate.