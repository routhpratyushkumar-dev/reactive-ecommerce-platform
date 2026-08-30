# ADR-001: Use Java 21

## Status

Accepted

## Context

The Reactive E-Commerce Platform requires a modern, stable, long-term-supported Java version capable of supporting enterprise application development.

The application will use Spring Boot, Spring WebFlux, Kafka, Redis and other modern Java ecosystem technologies.

## Decision

We will use Java 21 as the primary development language and runtime.

## Why Java 21?

Java 21 provides:

- Long-Term Support
- Virtual Threads
- Pattern Matching
- Record Patterns
- Sequenced Collections
- Modern garbage collection improvements
- Improved developer productivity
- Strong ecosystem support

## Alternatives Considered

### Java 17

Java 17 is also an LTS release and is widely used in enterprise applications.

However, Java 21 provides newer language and JVM capabilities.

### Java 8

Java 8 remains common in legacy enterprise applications but lacks many modern language and JVM improvements.

## Trade-offs

Using Java 21 means:

- Developers need a compatible JDK.
- Some legacy libraries may require upgrades.
- Production environments must support Java 21.

## Consequences

The project will use Java 21 features where they provide clear value while avoiding unnecessary language complexity.