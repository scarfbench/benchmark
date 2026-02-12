APP_NAME        := "petclinic-jakarta"
IMAGE_NAME      := "petclinic-jakarta:latest"
APP_PORT        := 8080
SUCCESS_PATTERN := CWWKF0011I
FAILURE_PATTERN := BUILD FAILURE|\[ERROR\] \[ERROR\] Some problems were encountered while processing the POMs:|\[ERROR\] The build could not read [0-9]+ projects|\[FATAL\]|Malformed POM|ProjectBuildingException|ModelParseException|NoSuchFieldError|Fatal error compiling|Could not find or load main class

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

	@until docker logs $(APP_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)"; do \
		if docker logs $(APP_NAME) 2>&1 | grep -Fq "$(SUCCESS_PATTERN)"; then \
			break; \
		fi; \
		sleep 1; \
	done
	@if docker logs $(APP_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)"; then \
		echo "\[ERROR\] Build failed in container:"; \
		docker logs $(APP_NAME) 2>&1; \
		exit 1; \
	fi

	@until docker logs $(APP_NAME) 2>&1 | grep -Fq "$(SUCCESS_PATTERN)"; do sleep 1; done
	@echo "[INFO] Application started and ready."

logs:
	docker logs $(APP_NAME)

down:
	- docker rm -f $(APP_NAME)
	@echo "[INFO] Container removed (if it existed)"

clean: down
	- docker rmi $(IMAGE_NAME)
	- docker image prune -f
	@echo "[INFO] Removed app image and pruned dangling Docker layers"

test: up
	@docker exec $(APP_NAME) sh -c 'until curl -sS "http://localhost:$(APP_PORT)" >/dev/null; do sleep 1; done'
	@echo "[INFO] Running smoke tests inside container $(APP_NAME)..."
	docker exec $(APP_NAME) bash -lc "cd smoke && uv sync && uv run playwright install chromium && uv run smoke.py"
	$(MAKE) clean

local:
	./mvnw clean liberty:run
