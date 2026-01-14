---
icon: cldk/cube-16
hide:
  - toc
---
# :cldk-developer-16: Benchmark

Scarf is a benchmark suite for evaluating agentic transformation of Java applications across frameworks: Jakarta EE, Quarkus, and Spring. This suite enables systematic assessment of AI agents' ability to migrate enterprise Java applications while preserving functionality, idiomatic patterns, and architectural integrity across different runtime environments.

This benchmark contains self-contained applications demonstrating core Java EE functionalities and their framework-specific implementations. Each example has been manually converted and verified across all target frameworks, with smoke tests included to verify application behavior after transformation.

The benchmark includes two types of examples:

1. **Focused examples** - Application examples organized per layer, where each example demonstrates a specific technology within that layer (e.g., persistence, presentation, integration).
2. **Whole applications** - Complete, functioning applications that demonstrate the coordination and interaction between multiple layers.

### :cldk-learning-20: Focused Examples 

Application examples organized per layer, where each example demonstrates a specific technology within that layer (e.g., persistence, presentation, integration).

<div class="grid cards" markdown>

- [:cldk-developer-16: Business Domain Layer](business_domain.md)

    ---

    Core business logic implementations using Enterprise JavaBeans (EJBs). Demonstrates stateful, stateless, and singleton session beans for shopping carts, currency conversion, hit counters, web services, and standalone EJB usage.

- [:cldk-developer-16: Dependency Injection Layer](dependency_injection.md)

    ---

    CDI and dependency injection patterns including custom qualifiers, interceptors, decorators, producer methods, event observers, and alternative implementations for conditional bean selection.

- [:cldk-developer-16: Infrastructure Layer](infrastructure.md)

    ---

    Enterprise features including managed executors for concurrency, asynchronous EJB methods, interceptors for cross-cutting concerns, and timer services for scheduled task execution.

- [:cldk-developer-16: Integration Layer](integration.md)

    ---

    Integration technologies featuring Jakarta Batch processing, JMS messaging patterns, message-driven beans, JAX-WS web services, and Java Connector Architecture for enterprise system integration.

- [:cldk-developer-16: Persistence Layer](persistence.md)

    ---

    Data persistence patterns using JPA entities with CRUD operations, complex entity relationships, composite keys, inheritance strategies, and JPQL queries for database interactions.

- [:cldk-developer-16: Presentation Layer](presentation.md)

    ---

    Web tier implementations including servlets, JAX-RS REST APIs, WebSocket endpoints, server-sent events, file uploads, filters, listeners, and real-time communication patterns.

- [:cldk-developer-16: Security Layer](security.md)

    ---

    Authentication and authorization patterns featuring Jakarta Security identity stores, form-based and basic authentication, EJB security, role-based access control, and password hashing.
</div>

### :cldk-learning-20: Whole Applications 

Complete, functioning applications that demonstrate the coordination and interaction between multiple layers.

<div class="grid cards" markdown>

- [:cldk-workflow-16: CargoTracker](cargotracker.md)

    ---

    Domain-Driven Design cargo shipping tracker with Jakarta Faces, CDI, Enterprise Beans, JPA, REST, Batch, and JMS. Showcases aggregates, repositories, and domain events following Eric Evans' DDD patterns.

- [:cldk-workflow-16: Coffee Shop](coffee_shop.md)

    ---

    Event-driven microservices with Orders, Barista, and Kitchen services via Kafka. Demonstrates MicroProfile stack, reactive messaging, distributed transactions, and eventual consistency.

- [:cldk-workflow-16: DayTrader](daytrader.md)

    ---

    High-performance stock trading benchmark with stateless session beans, JPA optimistic locking, transaction management, and connection pooling. Used for measuring server performance.

- [:cldk-workflow-16: PetClinic](petclinic.md)

    ---

    Veterinary clinic management with Jakarta Faces (PrimeFaces), complex JPA relationships, CDI, and Bean Validation. Complete workflows for owners, pets, visits, and veterinarians.

- [:cldk-workflow-16: RealWorld](realworld.md)

    ---

    Medium.com clone with MicroProfile JWT, JAX-RS REST API, article management, comments, favorites, tags, and user following. Includes Testcontainers integration tests.

</div>

