### DayTrader Justfile
APP_NAME        := "daytrader"
IMAGE_NAME      := "daytrader:latest"
APP_PORT        := "9080"   # Internal HTTP port

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
	@echo "[INFO] Waiting for application to start..."
	@until docker logs $(APP_NAME) 2>&1 | grep -q "CWWKF0011I"; do sleep 1; done
	@echo "[INFO] Application started, port $(APP_PORT) is ready."
	
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

test: smoke
# Optional: run app locally without Docker, if you still want that path.
local:
	./mvnw clean package cargo:run
