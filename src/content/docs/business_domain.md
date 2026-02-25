---
title: "Business Domain"
---

# Business Domain

Core business logic implementations using Enterprise JavaBeans (EJBs). These examples demonstrate different EJB types—stateful, stateless, and singleton session beans—for common enterprise patterns like shopping carts, currency conversion, hit counters, web services, and standalone EJB usage.

## Examples

- **cart** - Stateful session bean with shopping cart lifecycle management and `@Remove` methods
- **converter** - Stateless session bean demonstrating currency conversion business logic
- **counter** - Singleton session bean with shared state for tracking web page hits
- **helloservice** - JAX-WS web service implemented as a stateless session bean
- **standalone** - Stateless session bean for standalone EJB container usage