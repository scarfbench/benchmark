### Cloud-Native Starter Microservices (Quarkus) Makefile
APP_NAME        := cloud-native-starter-quarkus
SUCCESS_PATTERN := started in.*Listening on:

.PHONY: build rebuild up logs down test clean

build:
	docker compose build
	@echo "[INFO] Built all microservice images"

rebuild:
	docker compose build --no-cache
	@echo "[INFO] Rebuilt all microservice images (no cache)"

up: build
	-docker compose down --remove-orphans 2>/dev/null || true
	docker compose up --detach
	@echo "[INFO] Started all services, waiting for readiness..."
	@for svc in cns-articles-service-q cns-authors-service-q cns-web-api-service-q; do \
		echo "[INFO] Waiting for $$svc..."; \
		timeout=120; elapsed=0; \
		until docker logs $$svc 2>&1 | grep -qE "$(SUCCESS_PATTERN)"; do \
			if docker logs $$svc 2>&1 | grep -q "BUILD FAILURE"; then \
				echo "[ERROR] Build failed in $$svc:"; \
				docker logs $$svc 2>&1 | tail -50; \
				exit 1; \
			fi; \
			sleep 2; elapsed=$$((elapsed + 2)); \
			if [ $$elapsed -ge $$timeout ]; then \
				echo "[ERROR] Timeout waiting for $$svc"; \
				docker logs $$svc 2>&1 | tail -30; \
				exit 1; \
			fi; \
		done; \
		echo "[INFO] $$svc is ready."; \
	done
	@echo "[INFO] All services started and ready."

logs:
	docker compose logs -f

down:
	-docker compose down --remove-orphans
	@echo "[INFO] All containers removed (if they existed)"

test: up
	@echo "[INFO] Running smoke tests..."
	@chmod +x test.sh && ./test.sh

clean: down
	docker compose down --rmi local --volumes --remove-orphans 2>/dev/null || true
	@echo "[INFO] Cleaned all images and volumes"
