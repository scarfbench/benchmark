---
language:
  - java
task_categories:
  - text-generation
tags:
  - code
  - benchmark
  - evaluation
  - java
  - code-translation
  - agentic
pretty_name: Scarf Benchmark
---

![Scarf Benchmark](https://github.com/scarfbench/site/blob/main/public/assets/images/scarf-light.png?raw=true)

<div align="center">
  <a href="https://scarfbench.info"><img src="https://img.shields.io/badge/site-scarfbench.info-blue?style=for-the-badge" alt="Documentation"></a>
  <a href="https://scarfbench.info/leaderboard/"><img src="https://img.shields.io/badge/leaderboard-view%20results-orange?style=for-the-badge" alt="Leaderboard"></a>
  <a href="https://scarfbench.info/quickstart/"><img src="https://img.shields.io/badge/quickstart-get%20started-green?style=for-the-badge" alt="Quickstart"></a>
</div>

---

**Scarf** (**S**elf-**C**ontained **A**pplication **R**efactoring) is a benchmark suite for evaluating AI agents' ability to migrate enterprise Java applications across frameworks while preserving functionality, idiomatic patterns, and architectural integrity.

| Metric | Count |
|---|---|
| Applications | 102 |
| Architectural layers | 6 |
| Frameworks | 3 (Jakarta EE, Quarkus, Spring) |
| Validation tests | 1,331 |

> All applications have been manually converted and verified by experienced developers, with rigorous testing for functional correctness, framework-specific idiom adherence, and architectural integrity.

---

## Table of Contents

- [Releases](#releases)
- [Benchmark Structure](#benchmark-structure)
- [Evaluation & Scoring](#evaluation--scoring)
- [Quickstart](#quickstart)
- [Benchmark Applications](#benchmark-applications)
- [Roadmap](#roadmap)
- [Citation](#citation)
- [Contact](#contact)

---

## Releases

| Version | Date | Description |
|---------|------|-------------|
| [v0.1.2](https://github.com/scarfbench/benchmark/releases/tag/v0.1.2) | 2026-03-25 | Standardized all Dockerfiles to use framework-native run commands (`spring-boot:run`, `quarkus:run`, `liberty:run`) |
| [v0.1.1](https://github.com/scarfbench/benchmark/releases/tag/v0.1.1) | 2026-03-24 | Consolidated multi-module coffee-shop (Jakarta, Spring) into single Maven modules for strict architectural parity across frameworks; Dockerfile updates for PetClinic |
| [v0.1.0](https://github.com/scarfbench/benchmark/releases/tag/v0.1.0) | 2026-03-19 | Initial release — 87 focused examples across 5 layers and 3 frameworks, plus 15 whole application variants |

---

## Benchmark Structure

### Migration Paths

| Source | Target |
|--------|--------|
| Jakarta EE | Quarkus |
| Jakarta EE | Spring |
| Quarkus | Spring |
| Spring | Quarkus |

### Architectural Layers

| Layer | Description |
|-------|-------------|
| Business Domain | Stateful, stateless, and singleton EJBs |
| Dependency Injection | CDI qualifiers, interceptors, decorators, producers |
| Infrastructure | Managed executors, async EJBs, timer services |
| Persistence | JPA entities, relationships, JPQL queries |
| Presentation | Servlets, JAX-RS, WebSocket, SSE |
| Whole Applications | Complete production-grade multi-layer systems |

---

## Evaluation & Scoring

Each migration attempt is scored across three dimensions:

| Metric | Description |
|--------|-------------|
| **Compilation** | Converted code compiles without errors |
| **Runtime** | Application starts and serves requests |
| **Test pass rate** | Smoke tests pass (passed / total) |

Evaluation uses **pass@k** — each conversion is attempted multiple times to assess consistency alongside raw correctness.

### Running an Evaluation

Install the `scarf` CLI (see [installation guide](https://scarfbench.info/installation/)), then:

```bash
scarf eval run \
  --benchmark-dir <path-to-benchmark> \
  --agent-dir <path-to-agent> \
  --source-framework <jakarta|quarkus|spring> \
  --target-framework <jakarta|quarkus|spring> \
  --layer <layer> \
  --app <app-name> \
  --eval-out <output-directory> \
  --pass-at-k <k>
```

Output structure:

```
eval_out/
├── input/         # source framework code
├── output/        # agent-converted code
├── validation/    # agent.err, agent.out, run.log
└── metadata.json  # evaluation metadata
```

Validate converted code with:

```bash
scarf validate --conversions-dir <eval-out> --benchmark-dir <path-to-benchmark>
```

---

## Quickstart

### Building an Agent

Your agent needs two files:

**`agent.toml`** — metadata and entrypoint:
```toml
name = "my-migration-agent"
entrypoint = ["run.sh"]
```

**`run.sh`** — the migration logic. The `scarf` CLI sets three environment variables before invoking it:

| Variable | Description |
|----------|-------------|
| `SCARF_WORK_DIR` | Output directory — all writes must stay here |
| `SCARF_FRAMEWORK_FROM` | Source framework identifier |
| `SCARF_FRAMEWORK_TO` | Target framework identifier |

Recommended practices:
- Keep framework-specific logic in skill files (e.g., `skills/spring-to-quarkus/SKILL.md`), not hardcoded prompts
- Use `run.sh` for orchestration and validation only
- Print diagnostics to stderr; exit non-zero on failure
- Never write outside `SCARF_WORK_DIR`

### Running a Single Application Locally

Each application also ships with Docker and a `Makefile` for standalone use:

```bash
cd business_domain/counter/spring
make up       # build and start
make test     # run smoke tests
make logs     # tail logs
make down     # stop
```

No Java or Maven installation required — Docker handles dependencies.

---

## Benchmark Applications

### Focused Examples

Isolated demonstrations of a specific technology per layer. Each example is verified across all supported frameworks.

#### Business Domain

Core business logic via Enterprise JavaBeans (EJBs):

- **cart** — Stateful session bean with shopping cart lifecycle and `@Remove` methods
- **converter** — Stateless session bean for currency conversion
- **counter** — Singleton session bean tracking shared web page hit counts
- **helloservice** — JAX-WS web service as a stateless session bean
- **standalone** — Stateless session bean for standalone EJB container usage

#### Dependency Injection

CDI patterns including custom qualifiers, interceptors, decorators, producer methods, event observers, and alternative implementations.

#### Infrastructure

Managed executors for concurrency, asynchronous EJB methods, interceptors for cross-cutting concerns, and timer services.

#### Integration

Jakarta Batch, JMS messaging, message-driven beans, JAX-WS web services, and Java Connector Architecture.

#### Persistence

JPA entities with CRUD operations, complex relationships, composite keys, inheritance strategies, and JPQL queries.

#### Presentation

Servlets, JAX-RS REST APIs, WebSocket endpoints, server-sent events, file uploads, filters, and listeners.

#### Security

Jakarta Security identity stores, form-based and basic authentication, EJB security, role-based access control, and password hashing.

---

### Whole Applications

Complete production-grade applications demonstrating multi-layer coordination.

#### CargoTracker

Domain-Driven Design cargo shipping tracker. Demonstrates Jakarta Faces, CDI, Enterprise Beans, JPA, REST, Batch, JSON Binding, Bean Validation, and JMS. Implements aggregates, repositories, and domain events following Eric Evans' DDD patterns.

#### Coffee Shop

Event-driven microservices with Orders, Barista, and Kitchen services via Apache Kafka. Demonstrates MicroProfile (Config, Health, OpenAPI, Metrics), JPA with PostgreSQL, reactive messaging, and eventual consistency.

#### DayTrader

High-performance stock trading benchmark. Demonstrates stateless session beans, JPA optimistic locking, transaction management, connection pooling, and web service interfaces.

#### PetClinic

Veterinary clinic management using Jakarta Faces (PrimeFaces). Demonstrates complex JPA relationships (owners-pets, pets-visits, vets-specialties), CDI beans, Bean Validation, and master-detail views.

#### RealWorld

Medium.com clone (Conduit) implementing the RealWorld specification. Demonstrates MicroProfile JWT, JAX-RS REST API design, JPA with PostgreSQL, BCrypt password hashing, pagination, and Testcontainers integration tests.

---

## Citation

If you use this benchmark in your research, please cite:

```bibtex
[Placeholder: BibTeX citation will be added when paper is published]
```

---

## Contact

For questions, feedback, or to submit agent results to the leaderboard:

| Name           | Email |
|----------------|-------|
| Rahul Krishna  | [imralk+oss@gmail.com](mailto:imralk+oss@gmail.com) |
| Bridget McGinn | [bridget.mcginn@ibm.com](mailto:bridget.mcginn@ibm.com) |
| Raju Pavuluri  | [pavuluri@us.ibm.com](mailto:pavuluri@us.ibm.com) |

---

## License

See [LICENSE](LICENSE) file for details.
