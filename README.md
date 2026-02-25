<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://scarfbench.github.io/benchmark/assets/images/scarf-dark.png">
  <source media="(prefers-color-scheme: light)" srcset="https://scarfbench.github.io/benchmark/assets/images/scarf-light.png">
  <img alt="Logo">
</picture>

**Scarf** (short for **S**elf-**C**ontained **A**pplication **R**efactoring) benchmark is a suite of Java applications across frameworks: Jakarta EE, Quarkus, and Spring for evaluating agentic transformation between the frameworks. This suite enables systematic assessment of AI agents' ability to migrate enterprise Java applications while preserving functionality, idiomatic patterns, and architectural integrity across different runtime environments.

The benchmark includes comprehensive examples ranging from focused layer-specific demonstrations to complete production-grade applications, each with verified implementations across all supported frameworks.

> **Note:** All applications in this benchmark have been meticulously converted and verified by experienced developers. Each implementation has undergone rigorous testing to ensure functional correctness, adherence to framework-specific idioms, and preservation of architectural integrity across Jakarta EE, Quarkus, and Spring frameworks.

---

## Table of Contents

- [Quickstart Guide](#quickstart-guide)
- [Benchmark Applications](#benchmark-applications)
  - [Focused Examples](#focused-examples)
  - [Whole Applications](#whole-applications)
- [Roadmap](#roadmap)
- [Contact](#contact)

---

## Quickstart Guide

This benchmark suite comes with most things needed to run the benchmark applications. Everything is already set up!

### What's Included

Each application comes with:

- **Dockerfile** - Pre-configured container with all dependencies installed
- **Makefile** - Simple commands to build and run everything
- **smoke.py or smoke/** - Automated tests to verify the application works

You don't need to install Maven, Java, or any dependencies. Docker handles it all!

### Prerequisites

You only need:

- Docker installed on your machine
- make command runner (you can install it via Cargo or your package manager)

### Running an Application

#### Step 1: Pick an Application

Browse the directory structure and choose any application. For example:

```
business_domain/counter/spring/
dependency_injection/encoder/jakarta/
presentation/mood/quarkus/
```

#### Step 2: Navigate to the Application

```bash
cd business_domain/counter/spring
```

#### Step 3: Run It!

```bash
make up
```

That's it! The `make up` command will:
1. Build your application
2. Build the Docker container
3. Start everything up

#### Step 4: Check the Logs

```bash
make logs
```

#### Step 5: Stop When Done

```bash
make down
```

### Common Commands

Every application supports these commands (via the `Makefile`):

|    Command   |            What it does            |
|--------------|------------------------------------|
| `make help`  | Shows all available commands       |
| `make up`    | Builds and starts the application  |
| `make logs`  | Shows application logs             |
| `make test`  | Runs everything and the smoke tests|
| `make clean` | Removes build artifacts            |

### Running Smoke Tests

Most applications include automated tests. To run, just use `make test`:

```bash
make test
```

### Framework Variations

Each application type comes in three flavors:

- **jakarta/** - Jakarta EE (enterprise Java)
- **quarkus/** - Quarkus (cloud-native Java)
- **spring/** - Spring Boot (popular Java framework)

Pick whichever framework you want to test!

### Troubleshooting

**Port already in use?**
```bash
make rebuild
```

**Want to rebuild from scratch?**
```bash
make clean
make rebuild
make up
```

**Need to see what's happening?**
```bash
make logs
```

---

## Benchmark Applications

This benchmark contains self-contained applications demonstrating core Java EE functionalities and their framework-specific implementations. Each example has been manually converted and verified across all target frameworks, with smoke tests included to verify application behavior after transformation.

The benchmark includes two types of examples:

### Focused Examples

Application examples organized per layer, where each example demonstrates a specific technology within that layer (e.g., persistence, presentation, integration).

#### Business Domain Layer

Core business logic implementations using Enterprise JavaBeans (EJBs). Demonstrates stateful, stateless, and singleton session beans for shopping carts, currency conversion, hit counters, web services, and standalone EJB usage.

**Examples:**
- **cart** - Stateful session bean with shopping cart lifecycle management and `@Remove` methods
- **converter** - Stateless session bean demonstrating currency conversion business logic
- **counter** - Singleton session bean with shared state for tracking web page hits
- **helloservice** - JAX-WS web service implemented as a stateless session bean
- **standalone** - Stateless session bean for standalone EJB container usage

#### Dependency Injection Layer

CDI and dependency injection patterns including custom qualifiers, interceptors, decorators, producer methods, event observers, and alternative implementations for conditional bean selection.

#### Infrastructure Layer

Enterprise features including managed executors for concurrency, asynchronous EJB methods, interceptors for cross-cutting concerns, and timer services for scheduled task execution.

#### Integration Layer

Integration technologies featuring Jakarta Batch processing, JMS messaging patterns, message-driven beans, JAX-WS web services, and Java Connector Architecture for enterprise system integration.

#### Persistence Layer

Data persistence patterns using JPA entities with CRUD operations, complex entity relationships, composite keys, inheritance strategies, and JPQL queries for database interactions.

#### Presentation Layer

Web tier implementations including servlets, JAX-RS REST APIs, WebSocket endpoints, server-sent events, file uploads, filters, listeners, and real-time communication patterns.

#### Security Layer

Authentication and authorization patterns featuring Jakarta Security identity stores, form-based and basic authentication, EJB security, role-based access control, and password hashing.

---

### Whole Applications

Complete, functioning applications that demonstrate the coordination and interaction between multiple layers.

#### CargoTracker

Domain-Driven Design cargo shipping tracker with Jakarta Faces, CDI, Enterprise Beans, JPA, REST, Batch, and JMS. Showcases aggregates, repositories, and domain events following Eric Evans' DDD patterns.

Demonstrates Jakarta Faces, CDI, Enterprise Beans, JPA, REST, Batch, JSON Binding, Bean Validation, and JMS. Showcases end-to-end application architecture with multiple interfaces (web UI, REST API, file scanning) and complex domain modeling including aggregates, repositories, and domain events. Implements the cargo tracking example from Eric Evans' DDD book.

#### Coffee Shop

Event-driven microservices with Orders, Barista, and Kitchen services via Kafka. Demonstrates MicroProfile stack, reactive messaging, distributed transactions, and eventual consistency.

Microservices architecture with Orders, Barista, and Kitchen services communicating via Apache Kafka. Demonstrates MicroProfile (Config, Health, OpenAPI, Metrics), JPA with PostgreSQL, JAX-RS REST APIs, reactive messaging patterns, and distributed transaction coordination. Shows event-driven architecture with asynchronous inter-service communication and eventual consistency.

#### DayTrader

High-performance stock trading benchmark with stateless session beans, JPA optimistic locking, transaction management, and connection pooling. Used for measuring server performance.

Online stock trading benchmark application demonstrating real-world Java EE workload patterns. Implements user authentication, portfolio management, stock quote lookup, and buy/sell transactions. Showcases performance-oriented design with stateless session beans, JPA entities with optimistic locking, transaction management, connection pooling, and web service interfaces.

#### PetClinic

Veterinary clinic management with Jakarta Faces (PrimeFaces), complex JPA relationships, CDI, and Bean Validation. Complete workflows for owners, pets, visits, and veterinarians.

Full-featured veterinary clinic management system using Jakarta Faces (PrimeFaces) for the UI layer. Demonstrates CRUD operations with JPA entities showing one-to-many, many-to-one, and many-to-many relationships (owners-pets, pets-visits, vets-specialties). Includes CDI beans, Bean Validation, JSF navigation, complex forms, and master-detail views.

#### RealWorld

Medium.com clone with MicroProfile JWT, JAX-RS REST API, article management, comments, favorites, tags, and user following. Includes Testcontainers integration tests.

Medium.com clone (Conduit) implementing the RealWorld specification with full CRUD operations, JWT authentication, article management, comments, favorites, tags, and user following. Demonstrates MicroProfile JWT, JAX-RS REST API design, JPA with PostgreSQL, password hashing (BCrypt), slug generation, pagination, filtering, and comprehensive exception handling. Includes integration tests with Testcontainers and MicroShed testing framework.

---

## Contact

For any questions, feedback, or suggestions, or to submit your own agent results for the leaderboard, please contact the authors:

| Name           | Email                                    |
| -------------- | ---------------------------------------- |
| Rahul Krishna  | [i.m.ralk@gmail.com](mailto:imralk+oss@gmail.com) |
| Raju Pavuluri  | [pavuluri@us.ibm.com](mailto:pavuluri@us.ibm.com) |

---

## Citation

If you use this benchmark in your research, please cite our paper:

```bibtex
[Placeholder: BibTeX citation will be added when paper is published]
```

---

## License

See [LICENSE](LICENSE) file for details.
