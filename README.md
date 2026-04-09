<picture>
  <source media="(prefers-color-scheme: dark)" srcset="https://github.com/scarfbench/site/blob/main/public/assets/images/scarf-dark.png?raw=true">
  <source media="(prefers-color-scheme: light)" srcset="https://github.com/scarfbench/site/blob/main/public/assets/images/scarf-light.png?raw=true">
  <img alt="Scarf Benchmark Logo">
</picture>

<div align="center">
  <a href="https://scarfbench.info"><img src="https://img.shields.io/badge/site-scarfbench.info-blue?style=for-the-badge" alt="Documentation"></a>
  <a href="https://scarfbench.info/leaderboard/"><img src="https://img.shields.io/badge/leaderboard-view%20results-orange?style=for-the-badge" alt="Leaderboard"></a>
  <a href="https://scarfbench.info/quickstart/"><img src="https://img.shields.io/badge/quickstart-get%20started-green?style=for-the-badge" alt="Quickstart"></a>
</div>

---

**Scarf** (**S**elf-**C**ontained **A**pplication **R**efactoring) is a benchmark suite for evaluating AI agents' ability to migrate enterprise Java applications across Jakarta EE, Quarkus, and Spring while preserving functionality, idiomatic patterns, and architectural integrity.

| Applications | Layers | Frameworks | Tests |
|:---:|:---:|:---:|:---:|
| 102 | 6 | 3 | 1,331 |

> All applications have been manually converted and verified by experienced developers.

**Resources:** [Quickstart](https://scarfbench.info/quickstart/) · [Installation](https://scarfbench.info/installation/) · [Building an Agent](https://scarfbench.info/agent/) · [Leaderboard](https://scarfbench.info/leaderboard/) · [Submit Results](https://scarfbench.info/submit/)

---

## Releases

| Version | Date | Description |
|---------|------|-------------|
| [v0.1.2](https://github.com/scarfbench/benchmark/releases/tag/v0.1.2) | 2026-03-25 | Standardized all Dockerfiles to use framework-native run commands (`spring-boot:run`, `quarkus:run`, `liberty:run`) |
| [v0.1.1](https://github.com/scarfbench/benchmark/releases/tag/v0.1.1) | 2026-03-24 | Consolidated multi-module coffee-shop (Jakarta, Spring) into single Maven modules for strict architectural parity across frameworks; Dockerfile updates for PetClinic |
| [v0.1.0](https://github.com/scarfbench/benchmark/releases/tag/v0.1.0) | 2026-03-19 | Initial release — 87 focused examples across 5 layers and 3 frameworks, plus 15 whole application variants |

---

## Benchmark Applications

### Migration Paths

| Source | Target |
|--------|--------|
| Jakarta EE | Quarkus |
| Jakarta EE | Spring |
| Quarkus | Spring |
| Spring | Quarkus |

### Focused Examples

| Layer | Description |
|-------|-------------|
| Business Domain | Stateful, stateless, and singleton EJBs (cart, converter, counter, helloservice, standalone) |
| Dependency Injection | CDI qualifiers, interceptors, decorators, producer methods, event observers |
| Infrastructure | Managed executors, async EJBs, timer services |
| Integration | Jakarta Batch, JMS, message-driven beans, JAX-WS, JCA |
| Persistence | JPA entities, relationships, composite keys, JPQL queries |
| Presentation | Servlets, JAX-RS, WebSocket, SSE, file uploads, filters |
| Security | Identity stores, form/basic auth, EJB security, RBAC, password hashing |

### Whole Applications

| Application | Description |
|-------------|-------------|
| CargoTracker | Domain-Driven Design cargo shipping tracker with Jakarta Faces, CDI, JPA, REST, Batch, and JMS |
| Coffee Shop | Event-driven microservices (Orders, Barista, Kitchen) via Apache Kafka with MicroProfile |
| DayTrader | High-performance stock trading benchmark with JPA optimistic locking and transaction management |
| PetClinic | Veterinary clinic management with Jakarta Faces (PrimeFaces) and complex JPA relationships |
| RealWorld | Medium.com clone (Conduit) with MicroProfile JWT, JAX-RS, and Testcontainers integration tests |

---

## Citation

```bibtex
@misc{scarfbench,
  author       = {Krishna, Rahul and McGinn, Bridget and Pavuluri, Raju},
  title        = {{ScarfBench}: A Benchmark for AI-Driven Enterprise Java Framework Migration},
  year         = {2026},
  howpublished = {\url{https://scarfbench.info}},
}
```

---

## Contact

| Name           | Email |
|----------------|-------|
| Rahul Krishna  | [imralk+oss@gmail.com](mailto:imralk+oss@gmail.com) |
| Bridget McGinn | [bridget.mcginn@ibm.com](mailto:bridget.mcginn@ibm.com) |
| Raju Pavuluri  | [pavuluri@us.ibm.com](mailto:pavuluri@us.ibm.com) |

---

## License

See [LICENSE](LICENSE) file for details.
