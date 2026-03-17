# Coffee Shop — Jakarta EE + MicroProfile on Open Liberty

This repo runs the Orders, Barista, and Kitchen services on Open Liberty, with Kafka and Postgres via Docker Compose.

## Prerequisites
- Java 21 (LTS) and Maven 3.9+
- Docker Desktop (Compose v2)
- Ports free: 9092 (Kafka external), 2181 (ZK), 5432 (PG), 9081/9082/9083 (apps)

## Project layout (expected)
```
coffee-shop/
  common/
  orders-service/
  barista-service/
  kitchen-service/
  docker/               <-- place docker-compose.yml + init DB here
  scripts/              <-- helper scripts
```
> If your layout differs, adjust the `build.context` paths in `docker/docker-compose.yml`.

## Build the services
At repo root:
```bash
mvn -DskipTests package
```

## Start the stack
```bash
# From repo root
docker compose -f docker/docker-compose.yml up -d --build
```

Services:
- Orders:  http://localhost:9081/orders-service/
- Barista: http://localhost:9082/barista-service/
- Kitchen: http://localhost:9083/kitchen-service/
- Health:  http://localhost:9081/health (and 9082/9083)
- OpenAPI: http://localhost:9081/openapi/ui/

Kafka bootstrap (in-Docker): `kafka:9092`  
Kafka bootstrap (from host): `localhost:9092`

Postgres: `postgres://cafe:cafe@localhost:5432/coffeeshop`

### Initialize database schema
The `docker/initdb/01_init.sql` file creates the `orders` table with the same columns used by the original Quarkus app:
```sql
CREATE TABLE IF NOT EXISTS orders (
  id       SERIAL PRIMARY KEY,
  customer VARCHAR(255) NOT NULL,
  item     VARCHAR(255) NOT NULL,
  quantity INTEGER      NOT NULL,
  status   VARCHAR(32)  NOT NULL,
  created  TIMESTAMPTZ  NOT NULL,
  updated  TIMESTAMPTZ  NOT NULL
);
```
The file is automatically executed on first `postgres` start.  
If your `pgdata` volume existed earlier, run `docker compose -f docker/docker-compose.yml down -v` once and start again to re-seed.

## Topics (Kafka)
We use exactly the original topic names:
- `barista-commands` (produced by Orders, consumed by Barista)
- `kitchen-commands`  (produced by Orders, consumed by Kitchen)
- `order-updates`     (produced by Barista/Kitchen, consumed by Orders)

The image enables auto-create by default. To create them explicitly:
```bash
./scripts/create-topics.sh
```

## Smoke test
1) Wait for services (optional):
```bash
./scripts/wait-for-http.sh http://localhost:9081/health 60
./scripts/wait-for-http.sh http://localhost:9082/health 60
./scripts/wait-for-http.sh http://localhost:9083/health 60
```
2) Place an order:
```bash
./scripts/smoke-orders.sh
```
This calls:
```bash
curl -sS -i -X POST http://localhost:9081/orders-service/api/orders   -H 'Content-Type: application/json'   -d '{"customer":"Raju","item":"latte","quantity":1}'
```
3) Verify DB row:
```bash
docker compose -f docker/docker-compose.yml exec -T postgres   psql -U cafe -d coffeeshop   -c "SELECT id, customer, item, quantity, status, created, updated FROM orders ORDER BY id DESC LIMIT 5;"
```
4) Watch service logs (see updates flow):
```bash
docker compose -f docker/docker-compose.yml logs -f orders-service barista-service kitchen-service
```
Or run:
```bash
./scripts/smoke-consumers.sh
```
# drink path (barista)
```bash
./scripts/smoke_drink.sh
```

# food path (kitchen)
```bash
./scripts/smoke_food.sh
```

## Tear down
```bash
docker compose -f docker/docker-compose.yml down
# remove DB volume too (drops data):
docker compose -f docker/docker-compose.yml down -v
```

## Troubleshooting

### Kafka: “Each listener must have a different port”
We set two listeners on *different container ports*:
```yaml
KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,PLAINTEXT_HOST://0.0.0.0:9093
KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092,PLAINTEXT_HOST://localhost:9092
ports:
  - "9092:9093"   # host:container (external listener)
```
- In-Docker services should use `kafka:9092`.
- Host tools should use `localhost:9092`.

### MP Reactive Messaging wiring errors (“no downstream” / “no upstream”)
Check that:
- Your `@Incoming/@Outgoing` channel names match those in your `application.properties` exactly.
- The `order-updates` listener exists in Orders (so incoming connector has a downstream).

### Bean Validation “Validation not enabled for module”
Ensure the Liberty features include Bean Validation:
- Either `webProfile-10.0` (already covers it), or explicitly `beanValidation-3.0` in `server.xml`.

### DB “column does not exist” / wrong type
Use the provided `initdb/01_init.sql` or run the DDL manually as above.  
If an older volume exists, run with `-v` to recreate.

### Offset reset value invalid
Config values must not include inline comments. Use:
```
mp.messaging.incoming.order-updates.auto.offset.reset=earliest
```
(not `earliest  # ...`).

## Notes
- App context roots are the module artifactIds (e.g., `/orders-service`). Adjust NGINX/API-gateway rules accordingly.
- By default logs go to stdout; use `docker compose logs -f <service>` to view.
