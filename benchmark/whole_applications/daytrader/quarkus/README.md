# DayTrader Quarkus

A Quarkus-based implementation of the classic DayTrader benchmark application, migrated from Jakarta EE.

## Prerequisites

- Java 17+
- Maven 3.9+ (or use the included wrapper)
- Python 3.11+ with [uv](https://docs.astral.sh/uv/) (for smoke tests)

## Quick Start

### Development Mode

```bash
# Start the application in dev mode with hot reload
./mvnw quarkus:dev
```

The application will be available at:
- **Home**: http://localhost:8080/
- **Trading App**: http://localhost:8080/app
- **REST API**: http://localhost:8080/rest

### Production Build

```bash
# Build the application
./mvnw clean package -DskipTests

# Run the JAR
java -jar target/quarkus-app/quarkus-run.jar
```

### Docker

```bash
# Build the image
docker build -t daytrader-quarkus .

# Run the container
docker run -p 8080:8080 daytrader-quarkus
```

## Default Users

The application automatically populates the database on startup with:
- **50 users**: `uid:0` through `uid:49` (password matches username)
- **100 stock quotes**: `s:0` through `s:99`

Default test user: `uid:0` / `uid:0`

## REST API Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /rest/quotes/{symbol}` | Get quote by symbol |
| `GET /rest/quotes` | Get all quotes |
| `GET /rest/portfolio/{userID}` | Get user's holdings |
| `GET /rest/account/{userID}` | Get account information |
| `GET /rest/market-summary` | Get market summary |
| `POST /rest/orders/buy` | Buy stocks |
| `POST /rest/orders/sell/{holdingID}` | Sell stocks |

## Smoke Tests

Run Playwright-based smoke tests to validate the application:

```bash
cd smoke

# Install dependencies
uv sync

# Install Playwright browsers
uv run playwright install chromium

# Run tests (app must be running on localhost:8080)
uv run pytest smoke.py -v
```

### Test Categories

```bash
# Run only login tests
uv run pytest smoke.py -v -m login

# Run only navigation tests  
uv run pytest smoke.py -v -m navigation

# Run only trading tests
uv run pytest smoke.py -v -m trading

# Run only API tests
uv run pytest smoke.py -v -m api
```

### Docker-based Testing

```bash
# Build test image
docker build -f Dockerfile.test -t daytrader-smoke .

# Run tests (with host networking to reach localhost:8080)
docker run --network host daytrader-smoke
```

## Migration from Jakarta EE

This project was migrated from Jakarta EE 8 (Liberty) with these key changes:

| Jakarta EE | Quarkus |
|------------|---------|
| `javax.*` packages | `jakarta.*` packages |
| `@Stateless` EJB | `@ApplicationScoped` CDI |
| `@PersistenceContext` | `@Inject EntityManager` |
| `persistence.xml` | `application.properties` |
| Servlet (`web.xml`) | JAX-RS Resource |
| Derby database | H2 in-memory |
| Liberty server | Quarkus runtime |
| Port 9080 | Port 8080 |

## Project Structure

```
daytrader-quarkus/
├── pom.xml                    # Maven build with Quarkus BOM
├── Dockerfile                 # Production multi-stage build
├── Dockerfile.test            # Smoke test runner
├── src/main/java/com/ibm/websphere/samples/daytrader/
│   ├── beans/                 # CDI beans and market summary
│   │   ├── MarketSummaryDataBean.java
│   │   └── RunStatsDataBean.java
│   ├── entities/              # JPA entities
│   │   ├── AccountDataBean.java
│   │   ├── AccountProfileDataBean.java
│   │   ├── HoldingDataBean.java
│   │   ├── OrderDataBean.java
│   │   └── QuoteDataBean.java
│   ├── impl/                  # TradeServices implementations (from original DayTrader)
│   │   ├── direct/            # Direct JDBC implementation
│   │   │   └── TradeDirect.java
│   │   ├── ejb3/              # EJB3 implementation (adapted to CDI)
│   │   │   ├── AsyncScheduledCompletedOrders.java
│   │   │   └── TradeSLSBBean.java
│   │   └── session2direct/    # Session-to-direct delegate
│   │       └── DirectSLSBBean.java
│   ├── interfaces/            # Service interfaces
│   │   └── TradeServices.java
│   ├── messaging/             # SmallRye Reactive Messaging (replaces JMS/MDB)
│   │   ├── MessageProducerService.java
│   │   ├── TradeBrokerProcessor.java
│   │   └── TradeStreamerProcessor.java
│   ├── rest/                  # JAX-RS resources
│   │   ├── TradeResource.java
│   │   ├── QuoteResource.java
│   │   └── MessagingResource.java
│   ├── util/                  # Utilities
│   │   ├── TradeConfig.java
│   │   ├── FinancialUtils.java
│   │   └── Log.java
│   └── web/                   # Web servlets and primitives
│       └── prims/
├── src/main/resources/
│   ├── application.properties
│   └── META-INF/resources/    # Static web content (JSP/HTML pages)
└── smoke/                     # Playwright smoke tests
    ├── smoke.py
    ├── pyproject.toml
    ├── pytest.ini
    └── README.md
```

## Architecture

The application preserves the original DayTrader multi-implementation architecture:

| Implementation | Class | Description |
|----------------|-------|-------------|
| EJB3 | `TradeSLSBBean` | Full-featured impl with async messaging (CDI + Reactive Messaging) |
| Direct | `TradeDirect` | Direct JDBC implementation |
| Session2Direct | `DirectSLSBBean` | Session bean delegating to TradeDirect |

**CDI Producer Pattern**: `TradeServicesProducer` provides the `TradeServices` bean injection point.
The active implementation is selected based on `TradeConfig.getRunTimeMode()` (default: EJB3 mode).

The individual implementations use `@Typed` to prevent CDI ambiguity - they're injected by their concrete type, not as `TradeServices`.

## License

Apache License 2.0
