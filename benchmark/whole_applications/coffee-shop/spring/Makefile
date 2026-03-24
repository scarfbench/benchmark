APP_NAME        := coffeeshop-spring
IMAGE_NAME      := $(APP_NAME):$(shell uuidgen | tr "[:upper:]" "[:lower:]" | head -c 8)
CONTAINER_NAME  := $(APP_NAME)-$(shell uuidgen | tr "[:upper:]" "[:lower:]" | head -c 8)
APP_PORT        := 8080
SUCCESS_PATTERN := Started
FAILURE_PATTERN := BUILD FAILURE|\[ERROR\] COMPILATION ERROR|COMPILATION ERROR|\[ERROR\] Failed to execute goal|\[ERROR\] \*\*\*|\[FATAL\]|MojoExecutionException|ProjectBuildingException|ModelParseException|cannot find symbol|package .* does not exist| error:|APPLICATION FAILED TO START|BeanCreationException|UnsatisfiedDependencyException|Could not find or load main class|ClassNotFoundException

compile:
	@echo "=== COMPILE START ==="
	@docker build -f Dockerfile -t $(IMAGE_NAME) . || { echo "=== COMPILE FAIL ==="; docker logs $(CONTAINER_NAME) 2>&1 || true; exit 1; }
	@echo "[INFO] Built image: $(IMAGE_NAME)"
	@echo "=== COMPILE OK ==="

rebuild:
	docker build --no-cache -f Dockerfile -t $(IMAGE_NAME) .
	@echo "[INFO] Rebuilt image (no cache): $(IMAGE_NAME)"

deploy: compile
	@echo "=== DEPLOY START ==="
	-@docker rm -f $(CONTAINER_NAME) >/dev/null 2>&1 || true

	docker run -d --name $(CONTAINER_NAME) $(IMAGE_NAME)
	@echo "[INFO] Started $(CONTAINER_NAME), waiting for app to start..."

	@docker logs -f $(CONTAINER_NAME) 2>&1 & LOG_PID=$$!; \
	while ! docker logs $(CONTAINER_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)|$(SUCCESS_PATTERN)"; do sleep 1; done; \
	kill $$LOG_PID 2>/dev/null; wait $$LOG_PID 2>/dev/null || true; \
	if docker logs $(CONTAINER_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)"; then \
		echo "\[ERROR\] Build failed in container:"; \
		docker logs $(CONTAINER_NAME) 2>&1; \
		echo "=== DEPLOY FAIL ==="; docker logs $(CONTAINER_NAME) 2>&1; exit 1; \
	fi
	@echo "[INFO] Application started and ready."
	@echo "=== DEPLOY OK ==="

logs:
	docker logs $(CONTAINER_NAME)

down:
	- docker rm -f $(CONTAINER_NAME) >/dev/null 2>&1 || true
	@echo "[INFO] Container removed (if it existed)"

clean: down
	- docker rmi $$(docker images -q $(APP_NAME)) 2>/dev/null
	- docker image prune -f
	@echo "[INFO] Removed app image(s) and pruned dangling Docker layers"

test: deploy
	@echo "=== TEST START ==="
	@echo "[INFO] === Container build/startup logs ==="
	@docker logs $(CONTAINER_NAME)
	@docker exec $(CONTAINER_NAME) sh -c 'until curl -sS "http://localhost:$(APP_PORT)" >/dev/null; do sleep 1; done'
	@echo "[INFO] Running smoke tests inside container $(CONTAINER_NAME)..."
	@status=0; \
	docker exec $(CONTAINER_NAME) bash -lc "cd smoke && uv sync && uv run playwright install chromium && uv run smoke.py" || status=$$?; \
	if [ $$status -ne 0 ]; then echo "=== TEST FAIL ==="; docker logs $(CONTAINER_NAME) 2>&1; else echo "=== TEST OK ==="; fi; \
	echo "[INFO] === Container logs after test ==="; \
	docker logs $(CONTAINER_NAME); \
	docker rm -f $(CONTAINER_NAME) >/dev/null 2>&1 || true; \
	docker rmi $$(docker images -q $(APP_NAME)) 2>/dev/null || true; \
	docker image prune -f || true; \
	exit $$status

local:
	./mvnw -pl web-service -am clean install -DskipTests
	./mvnw -f web-service/pom.xml spring-boot:run
