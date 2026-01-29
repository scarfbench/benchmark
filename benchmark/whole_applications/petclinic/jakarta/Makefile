### Petclinic Jakarta EE (Petclinic + Liberty) Justfile
APP_NAME        := "petclinic"
IMAGE_NAME      := "petclinic:latest"
APP_PORT        := "8080"   # Internal HTTP port (petclinic/Liberty)
HOST_PORT       := "18080"  # Host port to map to app's internal HTTP

build:
	docker build -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Built image: $(IMAGE_NAME)"

rebuild:
	docker build --no-cache -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Rebuilt image (no cache): $(IMAGE_NAME)"

up: build
        - docker rm -f $(APP_NAME) 2>/dev/null || true
        @echo "[INFO] Checking for port conflicts..."
        @docker ps | grep -q "petclinic-spring" && echo "[WARN] petclinic-spring container is running - may conflict on port 8080" || true
        docker run -d --name $(APP_NAME) -p 8080:8080 $(IMAGE_NAME)
        @echo "[INFO] Started $(APP_NAME) container..."
        @echo "[INFO] Waiting for application to start (this may take several minutes for first build)..."
        @bash -c '\
                TIMEOUT=600; \
                ELAPSED=0; \
                while [ $$ELAPSED -lt $$TIMEOUT ]; do \
                        if docker logs $(APP_NAME) 2>&1 | grep -q "CWWKF0011I"; then \
                                echo "[INFO] Liberty server is ready!"; \
                                sleep 3; \
                                if docker logs $(APP_NAME) 2>&1 | grep -q "CWWKT0016I\|CWWKZ0001I\|Application.*started\|started in"; then \
                                        echo "[INFO] Application is ready!"; \
                                        exit 0; \
                                fi; \
                        fi; \
                        sleep 3; \
                        ELAPSED=$$((ELAPSED + 3)); \
                        if [ $$((ELAPSED % 30)) -eq 0 ]; then \
                                echo "[INFO] Still waiting... ($$ELAPSED seconds elapsed)"; \
                        fi; \
                done; \
                echo "[ERROR] Timeout waiting for application to start after $$TIMEOUT seconds."; \
                echo "[ERROR] Check logs with \"just logs\""; \
                exit 1; \
        '

logs:
	docker logs -f $(APP_NAME)

down:
	- docker stop $(APP_NAME) 2>/dev/null || true
	- docker rm -f $(APP_NAME) 2>/dev/null || true
	@echo "[INFO] Container $(APP_NAME) stopped and removed (if it existed)."

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
# Uses Liberty profile by default - you can modify to use other profiles (-Pwildfly, -Ppayara, etc.)
local:
	./mvnw -Pliberty

