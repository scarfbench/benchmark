# DukeETF (Quarkus) Makefile
APP_NAME       = dukeetf-quarkus
IMAGE_NAME     = dukeetf-quarkus:latest

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

	# Look for framework-specific startup pattern in logs and wait until found
	@until docker logs $(APP_NAME) 2>&1 | grep -q '[io.quarkus].*started in .*Listening on:'; do sleep 1; done
	# Wait until HTTP is actually accepting (avoids connection refused)
	@ok=0; for i in 1 2 3 4 5 6 7 8 9 10; do docker exec $(APP_NAME) curl -sf -o /dev/null http://127.0.0.1:8080/main.xhtml && ok=1 && break; sleep 2; done; test $$ok -eq 1 || (echo "[FAIL] App did not respond on 8080" >&2; exit 1)
	@echo "[INFO] App started and ready."

logs:
	docker logs -f $(APP_NAME)

down:
	- docker rm -f $(APP_NAME)
	@echo "[INFO] Container removed (if it existed)"

test: up
	@docker exec $(APP_NAME) sh -c 'python3 /app/smoke.py'

local:
	./mvnw clean package quarkus:run