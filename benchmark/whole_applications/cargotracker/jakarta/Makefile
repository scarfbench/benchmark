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
	@if docker ps --all --quiet --filter name=^/$(APP_NAME)$ | grep -q .; then \
		echo "[INFO] Removing existing container"; \
		docker rm -f $(APP_NAME) >/dev/null; \
	fi
	docker run -d --name $(APP_NAME) $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME), waiting for app to start..."
	@until docker logs $(APP_NAME) 2>&1 | grep -Eq "started on port"; do sleep 1; done
	@echo "[INFO] Application server started."

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

# Clean up
clean: down
	docker rmi $(APP_NAME)
	@echo "[INFO] Image removed (if it existed)"

# Optional: run app locally without Docker, if you still want that path.
local:
	./mvnw clean package cargo:run
