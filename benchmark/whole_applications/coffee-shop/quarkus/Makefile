# Quarkus Coffeeshop Project Justfile
# Run `make` to see available recipes

# Set shell
SHELL := /bin/bash

# Default recipe - shows available commands
default:
	@make help || make --help

# Start PostgreSQL database for development
db-start:
	@echo "Starting PostgreSQL database for development..."
	docker run --name coffeeshop-pg \
	-e POSTGRES_PASSWORD=redhat-21 \
	-e POSTGRES_DB=coffeeshopdb \
	-p 5432:5432 \
	-v coffeeshop_pgdata:/var/lib/postgresql/data \
	-v "$$PWD/infra/postgres/init":/docker-entrypoint-initdb.d:ro,Z \
	-d postgres:15

# Stop and remove PostgreSQL database container
db-stop:
	@echo "Stopping and removing PostgreSQL database container..."
	docker stop coffeeshop-pg || echo "Container not running"
	docker rm coffeeshop-pg || echo "Container not found"

# Run in development mode (with hot reload)
dev: db-start
	@echo "Starting Quarkus in development mode..."
	./mvnw quarkus:dev

# Start application with Docker Compose
up: down
	@echo "Starting application with Docker Compose..."
	docker-compose up -d

# Stop Docker Compose services
down:
	@echo "Stopping Docker Compose services..."
	docker-compose down

# View Docker Compose logs
logs:
	@echo "Viewing application logs..."
	docker-compose logs -f

# Restart Docker Compose services
restart:
	@echo "Restarting services..."
	docker-compose restart

# Build and deploy (build + docker-build + up)
deploy: up
	@echo "Application deployed successfully!"
	@echo ""
	@echo "Access the application at: http://localhost:9080/"
	@echo "PostgreSQL available at: localhost:5432"

# Show application status
status:
	@echo "Docker Compose services status:"
	docker-compose ps
	@echo ""
	@echo "Docker images:"
	docker images | grep coffeeshop

# View application in browser (requires xdg-open)
open:
	@echo "Opening application in browser..."
	xdg-open http://localhost:8080/ || echo "Please open http://localhost:8080/ manually"

# Complete dedeployment - stops containers, removes images, and frees all resources
dedeploy:
	@echo "Dedeploying application..."
	@echo "Stopping all containers..."
	docker-compose down -v || echo "No containers to stop"
	@echo "Removing Docker image..."
	docker rmi coffeeshop-quarkus:latest || echo "Image not found"
	@echo "Removing stopped containers and freeing ports..."
	docker container prune -f
	@echo "Cleaning up networks..."
	docker network prune -f
	@echo "Dedeployment complete - all ports released!"

# Run Playwright-based smoke tests inside the running container
# This uses single container mode for smoke testing
smoke: smoke-single

test: smoke

# Build Docker image for smoke testing (single container mode)
build:
	docker build -f Dockerfile -t coffeeshop-quarkus:latest .
	@echo "[INFO] Built image: coffeeshop-quarkus:latest"

# Start single container for smoke testing
up-single: build
	- docker rm -f coffeeshop-quarkus-single 2>/dev/null || true
	docker run -d --name coffeeshop-quarkus-single -p 8080:8080 coffeeshop-quarkus:latest
	@echo "[INFO] Started coffeeshop-quarkus-single container..."
	@echo "[INFO] Waiting for application to start (this may take several minutes for first build)..."
	@bash -c 'TIMEOUT=600; ELAPSED=0; while [ $ELAPSED -lt $TIMEOUT ]; do \
	FULL_LOGS=$(docker logs coffeeshop-quarkus-single 2>&1); \
	RECENT_LOGS=$(echo "$FULL_LOGS" | tail -10); \
	if echo "$FULL_LOGS" | grep -qE "started in|Started.*Application"; then \
	echo "[INFO] Application is ready!"; \
	exit 0; \
	fi; \
	if echo "$FULL_LOGS" | grep -qiE "(BUILD FAILURE|Failed to execute|MojoExecutionException|Could not transfer artifact|Premature end|ERROR \[.*Exception|startup failed)" && ! echo "$FULL_LOGS" | grep -qi "SLF4J\|StaticLoggerBinder"; then \
	echo "[ERROR] Application startup error detected:"; \
	docker logs coffeeshop-quarkus-single 2>&1 | tail -30; \
	echo "[ERROR] Check full logs with \"docker logs coffeeshop-quarkus-single\""; \
	exit 1; \
	fi; \
	sleep 3; \
	ELAPSED=$((ELAPSED + 3)); \
	if [ $((ELAPSED % 30)) -eq 0 ]; then \
	echo "[INFO] Still waiting... ($ELAPSED seconds elapsed)"; \
	echo "[DEBUG] Recent logs:"; \
	docker logs coffeeshop-quarkus-single 2>&1 | tail -3; \
	fi; \
	done; \
	echo "[ERROR] Timeout waiting for application to start after $TIMEOUT seconds."; \
	echo "[ERROR] Check logs with \"docker logs coffeeshop-quarkus-single\""; \
	exit 1;'

# Run smoke tests on single container
smoke-single: up-single
	@echo "[INFO] Running smoke tests inside container coffeeshop-quarkus-single..."
	docker exec coffeeshop-quarkus-single bash -lc "cd smoke && uv sync && uv run playwright install chromium --only-shell && uv run smoke.py"
	- docker stop coffeeshop-quarkus-single 2>/dev/null || true
	- docker rm -f coffeeshop-quarkus-single 2>/dev/null || true

# Show project information
info:
	@echo "Quarkus Coffeeshop Project Information"
	@echo "======================================"
	@echo "Project: coffeeshop-quarkus (Quarkus Majestic Monolith)"
	@echo "Version: 1.0.0-SNAPSHOT"
	@echo "Java Version: 17"
	@echo "Framework: Quarkus 2.14.1.Final"
	@echo ""
	@echo "URLs when running:"
	@echo "  Application: http://localhost:9080/"
	@echo "  PostgreSQL: localhost:5432"
	@echo "  Redpanda: localhost:9092"
	@echo ""
	@echo "Key files:"
	@echo "  JAR file: target/quarkuscoffeeshop-majestic-monolith-1.0.0-SNAPSHOT.jar"
	@echo "  Source: src/main/java/"
	@echo "  Resources: src/main/resources/"