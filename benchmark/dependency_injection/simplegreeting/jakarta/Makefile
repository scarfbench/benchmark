# Jakarta EE Simple Greeting Project Justfile
# Run `make` to see available recipes

APP_NAME       := "simplegreeting-liberty"
IMAGE_NAME   := $(APP_NAME):latest
LIBERTY_PORT    := "9080"               # Liberty internal HTTP
HOST_PORT      := "10161"               # Host port to map to Liberty internal HTTP

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
	docker run -d --name $(APP_NAME) -p $(HOST_PORT):$(LIBERTY_PORT) $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME), waiting for app to start..."
	@until docker logs $(APP_NAME) 2>&1 | grep -q "CWWKF0011I"; do sleep 1; done
	@echo "[INFO] Liberty app started, port $(HOST_PORT) is ready."

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
	./mvnw clean package liberty:run
