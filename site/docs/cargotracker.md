---
icon: cldk/workflow-16
hide:
  - toc
---
# :cldk-workflow-16: CargoTracker

Domain-Driven Design (DDD) cargo shipping tracker demonstrating Jakarta Faces, CDI, Enterprise Beans, JPA, REST, Batch, JSON Binding, Bean Validation, and JMS.

## Overview

CargoTracker showcases end-to-end application architecture with multiple interfaces (web UI, REST API, file scanning) and complex domain modeling including aggregates, repositories, and domain events. This application implements the cargo tracking example from Eric Evans' seminal Domain-Driven Design book.

## Key Technologies

- **Jakarta Faces** - Web UI layer
- **CDI** - Dependency injection
- **Enterprise Beans** - Business logic
- **JPA** - Data persistence
- **REST** - API integration
- **Batch** - File processing
- **JSON Binding** - Data serialization
- **Bean Validation** - Input validation
- **JMS** - Asynchronous messaging

## Architecture Highlights

- Domain-Driven Design patterns (Aggregates, Entities, Value Objects)
- Repository pattern for data access
- Domain events for decoupled communication
- Multiple presentation interfaces (Web UI, REST API)
- Batch processing for file-based imports
