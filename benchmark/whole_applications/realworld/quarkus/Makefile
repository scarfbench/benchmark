# Run `make` to see available recipes

APP_NAME       := "realworld-quarkus"
IMAGE_NAME   := $(APP_NAME):latest
SERVER_PORT    := "8080"               # Application server internal HTTP
HOST_PORT      := "10013"               # Host port to map to Application server's internal HTTP
POSTGRES_CONTAINER_NAME  := "postgres-quarkus"
NETWORK_NAME   := "realworld-quarkus-net"

# Set shell
SHELL := /bin/bash

# Default recipe - shows available commands
default:
	@make help || make --help

# Build the docker image
build:
	docker build -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Built image: $(IMAGE_NAME)"

# Rebuild the docker image, without cache
rebuild:
	docker build --no-cache -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Rebuilt image (no cache): $(IMAGE_NAME)"

# Create docker network
network:
	@if docker network ls --quiet --filter name=^/$(NETWORK_NAME)$ | grep -q .; then \
	echo "[INFO] Removing existing network"; \
	docker network rm -f $(NETWORK_NAME) >/dev/null; \
	fi
	docker network create $(NETWORK_NAME)

# Start PostgreSQL container
db-up: network
	@if docker ps --all --quiet --filter name=^/$(POSTGRES_CONTAINER_NAME)$ | grep -q .; then \
	echo "[INFO] Removing existing PostgreSQL container"; \
	docker rm -f $(POSTGRES_CONTAINER_NAME) >/dev/null; \
	fi
	@echo "Starting PostgreSQL 17 container..."
	docker run -d \
	--name $(POSTGRES_CONTAINER_NAME) \
	--network $(NETWORK_NAME) \
	-e POSTGRES_PASSWORD=123456 \
	-p 5432:5432 \
	postgres:17
	@echo "Waiting for PostgreSQL to start..."
	@until docker exec $(POSTGRES_CONTAINER_NAME) pg_isready > /dev/null 2>&1; do sleep 1; done
	@echo "PostgreSQL is ready."

# Run the app in a docker container
up: build db-up
	@if docker ps --all --quiet --filter name=^/$(APP_NAME)$ | grep -q .; then \
	echo "[INFO] Removing existing container"; \
	docker rm -f $(APP_NAME) >/dev/null; \
	fi
	docker run -d \
	--name $(APP_NAME) \
	--network $(NETWORK_NAME) \
	-p $(HOST_PORT):$(SERVER_PORT) \
	-e QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://$(POSTGRES_CONTAINER_NAME):5432/postgres" \
	-e QUARKUS_DATASOURCE_USERNAME="postgres" \
	-e QUARKUS_DATASOURCE_PASSWORD="123456" \
	$(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME), waiting for app to start..."
	@until docker logs $(APP_NAME) 2>&1 | grep -q '[io.quarkus].*started in .*Listening on:'; do sleep 1; done
	@echo "[INFO] Application server started, port $(HOST_PORT) is ready."

# See the app logs
logs:
	docker logs -f $(APP_NAME)

# Stop the app
down:
	docker rm -f $(APP_NAME)
	docker rm -f $(POSTGRES_CONTAINER_NAME)
	@echo "[INFO] Container removed (if it existed)"

# Clean up
clean: down
	docker rmi $(APP_NAME)
	docker rmi postgres:17
	docker network rm $(NETWORK_NAME)
	@echo "[INFO] Image removed (if it existed)"

# Run the smoke tests
test: up
	@docker exec $(APP_NAME) sh -c 'cd smoke && uv sync && uv run smoke.py'

# Run the app locally
local: db-up
	mvn clean package -DskipTests quarkus:run
