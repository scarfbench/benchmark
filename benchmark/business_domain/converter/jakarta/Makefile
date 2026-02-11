# Run `make` to see available recipes

APP_NAME       := "converter-liberty"
IMAGE_NAME     := "converter-liberty:latest"
SUCCESS_PATTERN := CWWKF0011I
FAILURE_PATTERN := BUILD FAILURE|ProjectBuildingException|The build could not read
SERVER_PORT    := "9080"               # Application server internal HTTP
HOST_PORT      := "10021"               # Host port to map to Application server's internal HTTP

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
	@until docker logs $(APP_NAME) 2>&1 | grep -q "CWWKF0011I"; do sleep 1; done
	### Check for build failure
	@until docker logs $(APP_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)"; do \
	if docker logs $(APP_NAME) 2>&1 | grep -Fq "$(SUCCESS_PATTERN)"; then \
	break; \
	fi; \
	sleep 1; \
	done
	@if docker logs $(APP_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)"; then \
	echo "[ERROR] Build failed in container:"; \
	docker logs $(APP_NAME) 2>&1; \
	exit 1; \
	fi
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
	./mvnw clean package liberty:run
