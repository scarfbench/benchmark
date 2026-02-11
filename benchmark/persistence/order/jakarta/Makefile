### OpenLiberty (Jakarta) Justfile
APP_NAME       := "order-jakarta"
IMAGE_NAME     := "order-jakarta:latest"
SUCCESS_PATTERN := CWWKF0011I

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

	@echo "[INFO] Building $(APP_NAME) in container..."
	docker run -d --name $(APP_NAME) $(IMAGE_NAME)

	@echo "[INFO] Assessing build failure"
	@until docker logs $(APP_NAME) 2>&1 | grep -Fq "BUILD FAILURE"; do \
	if docker logs $(APP_NAME) 2>&1 | grep -Fq "$(SUCCESS_PATTERN)"; then \
	break; \
	fi; \
	sleep 1; \
	done
	@if docker logs $(APP_NAME) 2>&1 | grep -Fq "BUILD FAILURE"; then \
	echo "[ERROR] Build failed in container:"; \
	docker logs $(APP_NAME) 2>&1; \
	exit 1; \
	fi

	@echo "[INFO] Waiting to start OpenLiberty application..."
	@until docker logs $(APP_NAME) 2>&1 | grep -Fq "$(SUCCESS_PATTERN)"; do sleep 1; done
	@echo "[INFO] OpenLiberty application started and ready."

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
	@docker exec $(APP_NAME) sh -c 'python3 /app/smoke.py'
	$(MAKE) clean

local:
	./mvnw clean package liberty:run
