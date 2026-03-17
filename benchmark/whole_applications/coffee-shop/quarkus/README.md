# Quarkus Coffeeshop (Majestic Monolith)

Minimal Quarkus 2.14.x application using:
- PostgreSQL (multiple schemas: coffeeshop, barista, kitchen)
- Redpanda (Kafka API)
- Kafka Connect (optional)
- pgAdmin (optional UI)

## Prerequisites
- JDK 17
- Docker
- Docker Compose
- [`just`](https://just.systems) (optional, simplifies commands)

## Quick Start (All Services)
Using just (recommended):
```
just deploy
```
Manual (without just):
```
./mvnw clean package -DskipTests
docker build -t coffeeshop-quarkus:latest .
docker-compose down
docker-compose up -d
```
Application: http://localhost:9080/

To stop & clean everything:
```
just dedeploy
# or
docker-compose down -v
```

## Available just Recipes
```
just            # list (default)
just clean      # mvn clean
just deploy     # build jar + image + compose up
just dedeploy   # full teardown (containers, image, networks)
```

## Dev Mode (Optional – not scripted)
If you want hot reload instead of the containerized app:
1. Start only required services:
```
docker-compose up -d coffeeshop-db redpanda
```
2. Run Quarkus dev mode:
```
./mvnw quarkus:dev
```
This serves on http://localhost:8080/ (different from containerized port 8081).

## Postgres Init
Schemas and baseline objects are created from: `infra/postgres/init/*.sql` (mounted at container start).

## Ports
| Service         | Host Port |
| --------------- | --------- |
| App (container) | 8081      |
| App (dev mode)  | 8080      |
| PostgreSQL      | 5432      |
| Redpanda Kafka  | 19092     |
| Schema Registry | 18081     |
| Pandaproxy      | 18082     |
| Kafka Connect   | 8083      |
| pgAdmin         | 5050      |

## Build Options
Uber jar:
```
./mvnw package -Dquarkus.package.type=uber-jar
```
Native (optional):
```
./mvnw package -Pnative
```

## Cleanup Snippets
Remove only containers (keep volumes):
```
docker-compose down
```
Full reset (containers + volumes):
```
docker-compose down -v
```
Remove the built image:
```
docker rmi coffeeshop-quarkus:latest || true
```

## REST Endpoints (Examples)
- POST /api/order
- GET  /dashboard/stream (SSE)

## Next Steps (Ideas)
- Upgrade to Quarkus 3.x
- Add health checks & metrics
- Harden Kafka configuration