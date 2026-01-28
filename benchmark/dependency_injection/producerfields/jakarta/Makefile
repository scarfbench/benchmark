# Jakarta EE Producer Fields Project Justfile
# Run `make` to see available recipes

ARTIFACT_NAME  := "producerfields"
APP_NAME       := $(ARTIFACT_NAME)-glassfish
IMAGE_NAME   := $(APP_NAME):latest
APP_SERVER_PORT    := "8080"               # Application server internal HTTP
HOST_PORT      := "10151"               # Host port to map to application server internal HTTP
ADMIN_PORT  := "4848" # Host port to map to application server internal admin console port
HTTPS_PORT  := "8181" # Host port to map to application server internal HTTPS port

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
	docker run -d --name $(APP_NAME) -p $(HOST_PORT):$(APP_SERVER_PORT) -p $(ADMIN_PORT):4848 -p $(HTTPS_PORT):8181 $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME), waiting for app to start..."
	@until docker logs $(APP_NAME) 2>&1 | grep -Eq "Successfully autodeployed .*$(ARTIFACT_NAME).war"; do sleep 1; done
	@echo "[INFO] App started, port $(HOST_PORT) is ready."

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
	@docker exec $(APP_NAME) sh -c 'uv run /opt/smoke-test/smoke.py'

# Run the app locally (it runs in docker)
local: up
	@echo "[INFO] App running."
