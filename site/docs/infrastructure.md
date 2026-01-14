---
icon: cldk/server-16
hide:
  - toc
---
# :cldk-server-16: Infrastructure

Enterprise features including managed executors for concurrency, asynchronous EJB methods, interceptors for cross-cutting concerns, and timer services for scheduled task execution.

## Examples

- **concurrency-jobs** - ManagedExecutorService with `@Resource` injection for concurrent task execution
- **concurrency-taskcreator** - ManagedExecutorService and ManagedScheduledExecutorService for async tasks with WebSocket updates
- **ejb-async** - Asynchronous EJB methods with Future<T> return types
- **ejb-interceptor** - EJB interceptors for method-level cross-cutting concerns
- **ejb-timersession** - EJB timer service with `@Schedule` and `@Timeout` for scheduled tasks
