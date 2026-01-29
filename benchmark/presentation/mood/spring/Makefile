# Mood (Spring) Makefile
APP_NAME       = mood-spring
IMAGE_NAME     = mood-spring:latest

build:
	docker build -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Built image: $(IMAGE_NAME)"

rebuild:
	docker build --no-cache -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Rebuilt image (no cache): $(IMAGE_NAME)"

up: build
	# The below section will look for existing containers and if so, remove them.
	@if docker ps --all --quiet --filter name=^/$(APP_NAME)$ | grep -q .; then \
		echo "[INFO] Removing existing container"; \
		docker rm -f $(APP_NAME) >/dev/null; \
	fi

	# Run the container in detached mode
	docker run -d --name $(APP_NAME) $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME), waiting for app to start..."

	# Wait until the app responds (avoids relying on log format / Maven buffering)
	@for i in $$(seq 1 90); do \
		docker ps -q -f name=^/$(APP_NAME)$$ -f status=running | grep -q . || { echo "[FAIL] Container exited. Run: docker logs $(APP_NAME)" 1>&2; exit 1; }; \
		code=$$(docker exec $(APP_NAME) curl -sf -o /dev/null -w "%{http_code}" http://localhost:8080/report 2>/dev/null); \
		[ "$$code" = "200" ] && break; \
		sleep 2; \
	done
	@docker exec $(APP_NAME) curl -sf -o /dev/null http://localhost:8080/report || { echo "[FAIL] App did not respond on /report within 3 minutes" 1>&2; exit 1; }
	@echo "[INFO] App started and ready."

logs:
	docker logs -f $(APP_NAME)

down:
	- docker rm -f $(APP_NAME)
	@echo "[INFO] Container removed (if it existed)"

test: up
	@docker exec $(APP_NAME) sh -c 'python3 /app/smoke.py'

local:
	./mvnw clean package spring-boot:run
