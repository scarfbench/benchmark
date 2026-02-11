# Run `make` to see available recipes

APP_NAME       := "standalone-quarkus"
IMAGE_NAME   := $(APP_NAME):latest
SUCCESS_PATTERN := Listening on:
FAILURE_PATTERN := BUILD FAILURE|\\[ERROR\\] \\[ERROR\\] Some problems were encountered while processing the POMs:|\\[ERROR\\] The build could not read [0-9]+ projects|\\[FATAL\\]|Malformed POM|ProjectBuildingException|ModelParseException

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

	### Run the container in detached mode
	docker run -d --name $(APP_NAME) $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME), waiting for app to start..."

	### Check for startup failure or success
	@until docker logs $(APP_NAME) 2>&1 | grep -Fq "$(SUCCESS_PATTERN)"; do \
	if docker logs $(APP_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)"; then \
	echo "[ERROR] Build failed in container:"; \
	docker logs $(APP_NAME) 2>&1; \
	exit 1; \
	fi; \
	if ! docker inspect -f '{{.State.Running}}' $(APP_NAME) 2>/dev/null | grep -q true; then \
	echo "[ERROR] Container exited before startup success:"; \
	docker logs $(APP_NAME) 2>&1; \
	exit 1; \
	fi; \
	sleep 1; \
	done
	@echo "[INFO] Quarkus application started and ready."

# See the app logs
logs:
	docker logs $(APP_NAME)

# Stop the app
down:
	docker rm -f $(APP_NAME)
	@echo "[INFO] Container removed (if it existed)"

clean: down
	- docker rmi $(IMAGE_NAME)
	- docker image prune -f
	@echo "[INFO] Removed app image and pruned dangling Docker layers"

# Clean up

# Run the smoke tests
test: up
	sleep 10
	@docker exec $(APP_NAME) sh -c 'python3 /app/smoke.py'
	$(MAKE) clean

# Run the app locally
local:
	./mvnw clean package quarkus:run
