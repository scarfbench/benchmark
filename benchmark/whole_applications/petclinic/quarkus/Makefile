### Petclinic Quarkus Justfile
APP_NAME        := "petclinic-quarkus"
IMAGE_NAME      := "petclinic-quarkus:latest"
APP_PORT        := "8080"

build:
	docker build -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Built image: $(IMAGE_NAME)"

rebuild:
	docker build --no-cache -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Rebuilt image (no cache): $(IMAGE_NAME)"

up: build
	- docker rm -f $(APP_NAME) 2>/dev/null || true
	docker run -d --name $(APP_NAME) -p $(APP_PORT):$(APP_PORT) $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME) container..."
	@echo "[INFO] Waiting for application to start (this may take several minutes for first build)..."
	@bash -c 'TIMEOUT=600; ELAPSED=0; while [ $$ELAPSED -lt $$TIMEOUT ]; do \
	LOGS=$$(docker logs $(APP_NAME) 2>&1 | tail -5); \
	if echo "$$LOGS" | grep -q "started in"; then \
	echo "[INFO] Application is ready!"; \
	exit 0; \
	fi; \
	if echo "$$LOGS" | grep -qiE "(Build failure|BuildException|ConfigurationException|ERROR \[|Exception:|Failed to|startup failed)"; then \
	echo "[ERROR] Application startup error detected:"; \
	docker logs $(APP_NAME) 2>&1 | tail -30; \
	echo "[ERROR] Check full logs with \"just logs\""; \
	exit 1; \
	fi; \
	sleep 3; \
	ELAPSED=$$((ELAPSED + 3)); \
	if [ $$((ELAPSED % 30)) -eq 0 ]; then \
	echo "[INFO] Still waiting... ($$ELAPSED seconds elapsed)"; \
	echo "[DEBUG] Recent logs:"; \
	docker logs $(APP_NAME) 2>&1 | tail -3; \
	fi; \
	done; \
	echo "[ERROR] Timeout waiting for application to start after $$TIMEOUT seconds."; \
	echo "[ERROR] Check logs with \"just logs\""; \
	exit 1;'

logs:
	docker logs -f $(APP_NAME)

down:
	- docker rm -f $(APP_NAME)
	@echo "[INFO] Container $(APP_NAME) removed (if it existed)."

# Run Playwright-based smoke tests inside the running container
# This does:
#   cd smoke
#   uv sync
#   uv run playwright install chromium --only-shell
#   uv run playwright install-deps chromium
#   uv run smoke.py
smoke: up
	@echo "[INFO] Running smoke tests inside container $(APP_NAME)..."
	docker exec $(APP_NAME) bash -lc "cd smoke && uv sync && uv run playwright install chromium --only-shell && uv run playwright install-deps chromium && uv run smoke.py"

test: smoke

# Optional: run app locally without Docker
local:
	./mvnw quarkus:dev
