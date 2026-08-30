# Sprint 0.3 — Technology Research & Architecture Decisions

Project: Reactive E-Commerce Platform
Sprint: 0.3
Status: In Progress
Objective: Evaluate and document the technologies, architectural patterns, trade-offs, and engineering decisions that will form the foundation of the platform.

## 1. Sprint Objective

The objective of Sprint 0.3 is to answer the following question:

What technologies will we use, why will we use them, what alternatives did we consider, and what trade-offs are we accepting?

The Reactive E-Commerce Platform is intended to demonstrate modern enterprise Java development, reactive programming, event-driven architecture, microservices, cloud-native deployment, observability, and DevOps practices.

Technology decisions should therefore be based on engineering requirements rather than simply choosing popular technologies.

## 2. Proposed Technology Stack

            Layer	                                Technology
       2. Programming Language	            Java 21
       3. Backend Framework	                    Spring Boot
       4. Reactive Framework	            Spring WebFlux
       5. Reactive Library	                    Project Reactor
       6. API Style	                            REST
       7. API Gateway	                    Spring Cloud Gateway
       8. Primary Database	                    PostgreSQL
       9. Reactive Database Access	            R2DBC
       10. Optional NoSQL Database	            MongoDB
       11. Cache	                            Redis
       12. Messaging	                    Apache Kafka
       13. Security	                            Spring Security + JWT
       14. Build Tool	                    Maven
       15. Unit Testing	                    JUnit 5 + Mockito
       16. API Testing	                    WebTestClient
       17. Integration Testing	            Testcontainers
       18. API Client	                    Bruno
       19. Containerization	                    Docker
       20. Local Kubernetes	                    Kind
       21. Orchestration	                    Kubernetes
       22. CI/CD	                            GitHub Actions
       23. Metrics	                            Prometheus
       24. Dashboard	                    Grafana
       25. Logging	                            Loki
       26. Distributed Tracing	            OpenTelemetry + Jaeger
       27. Architecture Diagrams	            Mermaid / PlantUML / Draw.io
       28. Source Control	                    Git + GitHub

The project will use free and open-source tools wherever possible.

## 3. Initial Architecture Vision

The proposed architecture is:

                         +-------------------+
                         |      CLIENT       |
                         +---------+---------+
                                   |
                                   v
                         +-------------------+
                         |    API GATEWAY    |
                         +---------+---------+
                                   |
             +---------------------+---------------------+
             |           |            |           |      |
             v           v            v           v      v
          +------+   +---------+   +------+   +-------+  ...
          | Auth |   | Product |   | Cart |   | Order |
          | Svc  |   |  Svc    |   | Svc  |   | Svc   |
          +------+   +---------+   +------+   +-------+
                          |             |          |
                          v             v          v
                     PostgreSQL      Redis     PostgreSQL
                                                |
                                                v
                                         +-------------+
                                         |    Kafka    |
                                         +------+------+
                                                |
                              +-----------------+----------------+
                              |                 |                |
                              v                 v                v
                       +-------------+   +-------------+   +-------------+
                       | Notification|   |  Analytics  |   | Other       |
                       |   Service   |   |   Service   |   | Consumers   |
                       +-------------+   +-------------+   +-------------+

This architecture will be validated and refined during Sprint 0.4 — High-Level Design.

# Observability Strategy

A production-grade distributed system requires observability.

We will implement the three major pillars:

                 Observability
                      |
       +--------------+--------------+
       |              |              |
       v              v              v
     Logs          Metrics         Traces
       |              |              |
      Loki        Prometheus    OpenTelemetry
                                      |
                                      v
                                    Jaeger
                      |
                      v
                   Grafana
## Logs

Use Loki.

Capture:
* Application logs
* Error logs
* Correlation IDs
* Request information

## Metrics

Use Prometheus.

Examples:

* Request count
* Response time
* Error rate
* JVM metrics
* CPU usage
* Memory usage
* Kafka metrics

## Dashboards

Use Grafana.

## Distributed Tracing

Use:

* OpenTelemetry
* Jaeger

Example:

    Client
     |
     v
    Gateway
     |
     v
    Order Service
     |
     +----> Inventory Service
     |
     +----> Payment Service
     |
     +----> Kafka

A distributed trace should allow us to follow one request across these components.


# Testing Strategy

The project will follow a testing pyramid.

                 +-----------+
                 |   E2E     |
                 +-----------+
                       ^
                       |
              +----------------+
              | Integration    |
              +----------------+
                       ^
                       |
                 +-----------+
                 |   Unit    |
                 +-----------+
## Unit Testing

Tools:

* JUnit 5
* Mockito

Test:

* Business logic
* Validation
* Error handling
* Individual components

## API Testing

Use:

WebTestClient

Example:

    WebTestClient
        |
        v
    WebFlux Endpoint
        |
        v
    Expected Response

## Integration Testing

Use Testcontainers.

Example:

    Integration Test
        |
        +---- PostgreSQL Container
        |
        +---- Kafka Container
        |
        +---- Redis Container

This provides more realistic tests than mocking every external dependency.

# CI/CD Strategy

Use GitHub Actions.

## Pipeline
    Developer
     |
     v
    Git Push
     |
     v
    GitHub
     |
     v
    GitHub Actions
     |
     +---- Build
     |
     +---- Unit Tests
     |
     +---- Integration Tests
     |
     +---- Static Analysis
     |
     +---- Docker Build
     |
     +---- Security Scan
     |
     v
    Deployment

## Initial Pipeline

The first version should perform:

* Checkout code
* Setup Java
* Build with Maven
* Run unit tests
* Run integration tests
* Generate test reports

Later we will add:

* Docker image build
* Image scanning
* Deployment
* Kubernetes deployment

# MongoDB Decision
## Status

To Be Evaluated

MongoDB will not be added simply to demonstrate another database.

A potential use case is a flexible product catalog.

Example:
    
    {
    "productId": "P1001",
    "name": "Laptop",
    "category": "Electronics",
    "attributes": {
    "ram": "16GB",
    "storage": "1TB",
    "processor": "Intel i7"
        }
    }
    

Different categories can contain different attributes.

However, PostgreSQL may still be sufficient.

The final decision will be made during HLD based on actual domain requirements.