# System Context — Reactive E-Commerce Platform

## 1. Overview

The Reactive E-Commerce Platform is a cloud-native,
microservices-based e-commerce application designed to
demonstrate reactive Java development, event-driven
architecture and scalable distributed systems.

## 2. Primary Users

- Customer
- Seller
- Administrator
- Operations / Support

## 3. Core Capabilities

- User registration and authentication
- Product browsing
- Product search
- Shopping cart
- Inventory management
- Checkout
- Payment processing
- Order management
- Notifications
- Analytics

## 4. External Systems

Potential external systems include:

- Payment Provider
- Email Provider
- Monitoring Platform

## 5. High-Level Flow

    Customer
        |
        v
    API Gateway
        |
        v
    Microservices
        |
        +---- PostgreSQL
        +---- Redis
        +---- Kafka