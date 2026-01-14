---
icon: cldk/workflow-16
hide:
  - toc
---
# :cldk-workflow-16: Whole Applications

Complete, production-grade application examples demonstrating the coordination and integration of multiple Java EE layers and technologies.

## Examples

- **cargotracker** - Domain-Driven Design (DDD) cargo shipping tracker demonstrating Jakarta Faces, CDI, Enterprise Beans, JPA, REST, Batch, JSON Binding, Bean Validation, and JMS. Showcases end-to-end application architecture with multiple interfaces (web UI, REST API, file scanning) and complex domain modeling including aggregates, repositories, and domain events. Implements the cargo tracking example from Eric Evans' DDD book.

- **coffee-shop** - Microservices architecture with Orders, Barista, and Kitchen services communicating via Apache Kafka. Demonstrates MicroProfile (Config, Health, OpenAPI, Metrics), JPA with PostgreSQL, JAX-RS REST APIs, reactive messaging patterns, and distributed transaction coordination. Shows event-driven architecture with asynchronous inter-service communication and eventual consistency.

- **daytrader** - Online stock trading benchmark application demonstrating real-world Java EE workload patterns. Implements user authentication, portfolio management, stock quote lookup, and buy/sell transactions. Showcases performance-oriented design with stateless session beans, JPA entities with optimistic locking, transaction management, connection pooling, and web service interfaces. Used for measuring application server performance.

- **petclinic** - Full-featured veterinary clinic management system using Jakarta Faces (PrimeFaces) for the UI layer. Demonstrates CRUD operations with JPA entities showing one-to-many, many-to-one, and many-to-many relationships (owners-pets, pets-visits, vets-specialties). Includes CDI beans, Bean Validation, JSF navigation, complex forms, and master-detail views. Complete web application with user workflows for managing owners, pets, visits, and veterinarians.

- **realworld** - Medium.com clone (Conduit) implementing the RealWorld specification with full CRUD operations, JWT authentication, article management, comments, favorites, tags, and user following. Demonstrates MicroProfile JWT, JAX-RS REST API design, JPA with PostgreSQL, password hashing (BCrypt), slug generation, pagination, filtering, and comprehensive exception handling. Includes integration tests with Testcontainers and MicroShed testing framework.

