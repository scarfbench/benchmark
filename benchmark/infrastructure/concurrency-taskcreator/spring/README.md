# Task Creator (Quarkus Port)

This module is a Quarkus-based conversion of the original Jakarta EE `concurrency-taskcreator` example.

## Features
- JSF (MyFaces Quarkus extension) front-end (`index.xhtml` served from `META-INF/resources`)
- WebSocket endpoint (`/wsinfo`) broadcasting task updates
- REST endpoint (`POST /taskinfo`) receiving task execution events
- CDI application-scoped service (`TaskService`) replacing the original EJB singleton
- Periodic, delayed, and immediate task submission using Java executors

## Run
```bash
mvn -f quarkus/concurrency-taskcreator/pom.xml quarkus:dev
```
Then open: http://localhost:8080/

## Build (jar)
```bash
mvn -f quarkus/concurrency-taskcreator/pom.xml clean package
```

## Native (optional)
```bash
mvn -f quarkus/concurrency-taskcreator/pom.xml -Dnative clean package
```

## Differences vs Original
- Removed EJB annotations (`@Singleton`, `@Startup`, `@LocalBean`) in favor of CDI `@ApplicationScoped`.
- Replaced Managed Executor Services with standard JDK `ExecutorService` / `ScheduledExecutorService` (could be swapped for SmallRye Managed Executors if desired).
- Eliminated `web.xml` (Quarkus auto-configures Faces servlet via extension).
- Simplified REST client usage with a tiny `TaskRestPoster` helper.

## Next Ideas
- Replace manual HTTP posting with a REST Client interface + injection.
- Externalize thread pool sizes via configuration properties.
- Add basic tests for REST endpoint and WebSocket broadcasting.

## Container Usage

### Build Image
```bash
just -f quarkus/concurrency-taskcreator/Makefile build-image
```

### Run Container
```bash
just -f quarkus/concurrency-taskcreator/Makefile run
```
Visit: http://localhost:8080/

### Smoke Test (after container starts)
```bash
just -f quarkus/concurrency-taskcreator/Makefile smoke
```

Verbose smoke test:
```bash
just -f quarkus/concurrency-taskcreator/Makefile smoke-v
```

### Direct Docker (without just)
```bash
docker build -t taskcreator-quarkus quarkus/concurrency-taskcreator
docker run --rm -p 8080:8080 --name taskcreator-quarkus taskcreator-quarkus &
sleep 5
./quarkus/concurrency-taskcreator/smoke.sh
```

### Stopping
```bash
just -f quarkus/concurrency-taskcreator/Makefile stop
```

### Cleaning
```bash
just -f quarkus/concurrency-taskcreator/Makefile clean
```
