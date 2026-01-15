---
icon: cldk/layers-20
hide:
  - toc
---

![CLDK](./assets/images/scarf-light.png#only-light)
![CLDK](./assets/images/scarf-dark.png#only-dark)

<p align='center'>
<a href="https://arxiv.org/" class="md-button md-button--primary" target="_blank" rel="noopener noreferrer" style="margin-right: 1rem;">
    Read Paper ↗
  </a>
  <a href="https://github.com/ibm/scarfbench" class="md-button" target="_blank" rel="noopener noreferrer">
    <svg style="width: 18px; height: 18px; vertical-align: middle; margin-right: 0.5rem;" viewBox="0 0 24 24" fill="currentColor">
      <path d="M12 0c-6.626 0-12 5.373-12 12 0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23.957-.266 1.983-.399 3.003-.404 1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576 4.765-1.589 8.199-6.086 8.199-11.386 0-6.627-5.373-12-12-12z"/>
    </svg>
    View on GitHub
  </a>  
</p>
---

**Scarf** (short for **S**elf-**C**ontained **A**pplication **R**efactoring) benchmark is a suite of Java applications across frameworks: Jakarta EE, Quarkus, and Spring for evaluating agentic transformation between the frameworks. This suite enables systematic assessment of AI agents' ability to migrate enterprise Java applications while preserving functionality, idiomatic patterns, and architectural integrity across different runtime environments.

The benchmark includes comprehensive examples ranging from focused layer-specific demonstrations to complete production-grade applications, each with verified implementations across all supported frameworks.

!!! tip "Manual Conversions with Developer Verification"
    All applications in this benchmark have been manually converted and verified by experienced developers. Each implementation has undergone rigorous testing to ensure functional correctness, adherence to framework-specific idioms, and preservation of architectural integrity across Jakarta EE, Quarkus, and Spring frameworks.

---

# :cldk-layers-20: Getting Started with the Scarf Benchmark

Get started with using Scarf benchmark:

<div class="grid cards" markdown>

- [:cldk-flame-16: Quickstart](quickstart.md)

    ---

    Run through an example to quickly set up CLDK and perform multilingual code analysis.

- [:cldk-results-16: Leaderboard](leaderboard.md)

    ---

    Compare AI agents and transformation tools on the benchmark suite. Track performance metrics and identify best practices for automated application migration.

</div>
---

# :cldk-learning-20: Benchmark Applications

This benchmark contains self-contained applications demonstrating core Java EE functionalities and their framework-specific implementations. Each example has been manually converted and verified across all target frameworks, with smoke tests included to verify application behavior after transformation.

The benchmark includes two types of examples:

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

### :cldk-briefcase-16: Whole Applications 

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
---

## :cldk-rocket-20: Roadmap

ScarfBench is actively maintained and continuously evolving to support the research community. We are committed to expanding the benchmark's capabilities and improving its utility for evaluating AI-driven application transformation. Here's what's coming:

<div class="grid cards" markdown>

- :cldk-test-suite-16: **Comprehensive Smoke Tests**

    ---

    We are developing an extensive suite of automated smoke tests to validate functional equivalence across framework migrations. These tests will ensure that transformed applications maintain their original behavior, catching subtle regressions and framework-specific issues that may arise during migration.

- :cldk-results-16: **Dynamic Leaderboard**

    ---

    A live leaderboard will track and compare the performance of different AI agents and transformation tools across the benchmark suite. This will provide transparent, reproducible metrics for the research community and help identify best practices in automated application migration.

- :cldk-classification-16: **Rich Taxonomy of Errors**

    ---

    We are building a comprehensive taxonomy that categorizes transformation errors, anti-patterns, and common pitfalls. This taxonomy will help researchers understand where current approaches struggle and guide development of more robust transformation strategies.

</div>

ScarfBench will continue to receive regular updates with new applications, enhanced documentation, and improved tooling. We welcome community contributions and feedback to make this benchmark more valuable for advancing the state of automated application transformation.

---

## Contact

For any questions, feedback, or suggestions, please contact the authors:

| Name           | Email                                    |
| -------------- | ---------------------------------------- |
| Rahul Krishna  | [i.m.ralk@gmail.com](mailto:imralk+oss@gmail.com) |
| Raju Pavuluri    | [pavuluri@us.ibm.com](mailto:pavuluri@us.ibm.com) |
