### Cargo Tracker (Spring Boot) Justfile
APP_NAME        := "cargo-tracker"
IMAGE_NAME      := "cargo-tracker:latest"

build:
	docker build -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Built image: $(IMAGE_NAME)"

rebuild:
	docker build --no-cache -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Rebuilt image (no cache): $(IMAGE_NAME)"

up: build
	- docker rm -f $(APP_NAME) 2>/dev/null || true
	docker run -d --name $(APP_NAME) $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME) container..."
	@echo "[INFO] Waiting for application to start..."
	@while true; do \
	if docker logs $(APP_NAME) 2>&1 | grep -q "started in"; then \
	echo "[INFO] Application is ready!"; \
	break; \
	fi; \
	sleep 1; \
	done

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

# Optional: run app locally without Docker, if you still want that path.
local:
	./mvnw clean package -DskipTests quarkus:run