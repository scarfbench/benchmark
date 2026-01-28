### Cargo Tracker (Cargo + Payara) Justfile
APP_NAME        := "cargo-tracker"
IMAGE_NAME      := "cargo-tracker:latest"
APP_PORT        := "8080"   # Internal HTTP port (cargo/Payara)
HOST_PORT       := "18080"  # Host port to map to app's internal HTTP

build:
	docker build -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Built image: $(IMAGE_NAME)"

rebuild:
	docker build --no-cache -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Rebuilt image (no cache): $(IMAGE_NAME)"

up: build
	- docker rm -f $(APP_NAME) 2>/dev/null || true
	docker run -d --name $(APP_NAME) $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME) (internal port $(APP_PORT))..."
	@echo "[INFO] Tip: use 'just logs' to watch startup, or 'just smoke' to run Playwright smoke tests once it's up."

logs:
	docker logs -f $(APP_NAME)

down:
	- docker rm -f $(APP_NAME)
	@echo "[INFO] Container $(APP_NAME) removed (if it existed)."

# Run Playwright-based smoke tests inside the running container
# This does:
#   cd smoke
#   uv sync
#   uv run playwright install chromium
#   uv run smoke.py
smoke: up
	@echo "[INFO] Running smoke tests inside container $(APP_NAME)..."
	docker exec $(APP_NAME) bash -lc "cd smoke && uv sync && uv run playwright install chromium && uv run smoke.py"

# Optional: run app locally without Docker, if you still want that path.
local:
	./mvnw clean package cargo:run
