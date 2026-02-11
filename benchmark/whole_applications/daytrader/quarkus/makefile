.PHONY: build rebuild up down logs smoke smoke-local test local clean help

APP_NAME := daytrader-quarkus
IMAGE_NAME := daytrader-quarkus:latest
APP_PORT := 8080
UV := ~/.local/bin/uv

help:
	@echo "DayTrader Quarkus - Makefile Commands"
	@echo "======================================"
	@echo "  make build       - Build Docker image"
	@echo "  make rebuild     - Rebuild Docker image (no cache)"
	@echo "  make up          - Build and start container"
	@echo "  make down        - Stop and remove container"
	@echo "  make logs        - Show container logs (follow mode)"
	@echo "  make smoke       - Run smoke tests (requires app running via 'make up' or 'make local')"
	@echo "  make test        - Build, start, and run smoke tests (all-in-one)"
	@echo "  make local       - Run locally with quarkus:dev"
	@echo "  make clean       - Clean build artifacts"
	@echo ""

build:
	docker build -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Built image: $(IMAGE_NAME)"

rebuild:
	docker build --no-cache -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Rebuilt image (no cache): $(IMAGE_NAME)"

up: build
	-docker rm -f $(APP_NAME) 2>/dev/null || true
	docker run -d -p $(APP_PORT):$(APP_PORT) --name $(APP_NAME) $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME) on port $(APP_PORT)..."
	@echo "[INFO] Waiting for application to start..."
	@until curl -s http://localhost:$(APP_PORT)/ > /dev/null 2>&1; do sleep 2; done
	@echo "[INFO] Application is running at http://localhost:$(APP_PORT)"

logs:
	docker logs -f $(APP_NAME)

down:
	-docker rm -f $(APP_NAME) 2>/dev/null || true
	@echo "[INFO] Container $(APP_NAME) removed (if it existed)."

# Run smoke tests (app must be running via 'make up' or 'make local')
smoke:
	@echo "[INFO] Checking if app is running on localhost:$(APP_PORT)..."
	@curl -s http://localhost:$(APP_PORT)/ > /dev/null || (echo "[ERROR] App not running on port $(APP_PORT). Run 'make up' or 'make local' first." && exit 1)
	@echo "[INFO] Running smoke tests..."
	cd smoke && $(UV) sync && $(UV) run playwright install chromium && $(UV) run pytest smoke.py -v

# All-in-one: build, start container, and run smoke tests
test: up smoke

# Run app locally with Maven (no Docker)
local:
	./mvnw quarkus:dev -Dquarkus.analytics.disabled=true

clean:
	./mvnw clean
	-docker rm -f $(APP_NAME) 2>/dev/null || true
	-docker rmi $(IMAGE_NAME) 2>/dev/null || true
	@echo "[INFO] Cleaned build artifacts and Docker resources."