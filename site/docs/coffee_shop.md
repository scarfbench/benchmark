---
hide:
  - toc
---
# Coffee Shop

Microservices architecture with Orders, Barista, and Kitchen services communicating via Apache Kafka.

## Overview

Coffee Shop demonstrates event-driven microservices architecture with asynchronous inter-service communication and eventual consistency. The application models a coffee shop ordering system with separate services handling orders, beverage preparation, and food preparation.

## Key Technologies

- **MicroProfile Config** - External configuration
- **MicroProfile Health** - Service health checks
- **MicroProfile OpenAPI** - API documentation
- **MicroProfile Metrics** - Application monitoring
- **JPA with PostgreSQL** - Data persistence
- **JAX-RS** - REST API endpoints
- **Apache Kafka** - Event streaming
- **Reactive Messaging** - Async message handling

## Architecture Highlights

- Microservices architecture with service decomposition
- Event-driven communication via Kafka topics
- Distributed transaction coordination
- Eventual consistency patterns
- Service health monitoring and metrics
- RESTful API design
