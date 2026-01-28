# Run `make` to see available recipes

APP_NAME       := "producerfields-quarkus"
IMAGE_NAME   := $(APP_NAME):latest
SERVER_PORT    := "8080"               # Application server internal HTTP
HOST_PORT      := "10152"               # Host port to map to Application server's internal HTTP

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

# Run the app in a docker container
up: build
	@if docker ps --all --quiet --filter name=^/$(APP_NAME)$ | grep -q .; then \
	echo "[INFO] Removing existing container"; \
	docker rm -f $(APP_NAME) >/dev/null; \
	fi
	docker run -d --name $(APP_NAME) -p $(HOST_PORT):$(SERVER_PORT) $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME), waiting for app to start..."
	@until docker logs $(APP_NAME) 2>&1 | grep -q '[io.quarkus].*started in .*Listening on:.*:$(SERVER_PORT)'; do sleep 1; done
	@echo "[INFO] Application server started, port $(HOST_PORT) is ready."

# See the app logs
logs:
	docker logs -f $(APP_NAME)

# Stop the app
down:
	docker rm -f $(APP_NAME)
	@echo "[INFO] Container removed (if it existed)"

# Clean up
clean: down
	docker rmi $(APP_NAME)
	@echo "[INFO] Image removed (if it existed)"

# Run the smoke tests
test: up
	@docker exec $(APP_NAME) sh -c 'uv run /app/smoke.py'

# Run the app locally
local:
	./mvnw clean package quarkus:run
