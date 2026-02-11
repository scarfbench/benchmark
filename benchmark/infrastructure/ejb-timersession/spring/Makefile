### Spring Boot Justfile
APP_NAME        := "timersession-spring"
IMAGE_NAME      := "timersession-spring:latest"
APP_PORT        := 9080
SUCCESS_PATTERN := Started
FAILURE_PATTERN := BUILD FAILURE|\[ERROR\] COMPILATION ERROR|COMPILATION ERROR|\[ERROR\] Failed to execute goal|\[ERROR\] \*\*\*|\[FATAL\]|MojoExecutionException|ProjectBuildingException|ModelParseException|cannot find symbol|package .* does not exist| error:|APPLICATION FAILED TO START|BeanCreationException|UnsatisfiedDependencyException

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

	### Run the container in detached mode
	docker run -d --name $(APP_NAME) $(IMAGE_NAME)
	@echo "[INFO] Started $(APP_NAME), waiting for app to start..."

	### Check for build failure
	@until docker logs $(APP_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)"; do \
	if docker logs $(APP_NAME) 2>&1 | grep -Fq "$(SUCCESS_PATTERN)"; then \
	break; \
	fi; \
	sleep 1; \
	done
	@if docker logs $(APP_NAME) 2>&1 | grep -Eq "$(FAILURE_PATTERN)"; then \
	echo "[ERROR] Build failed in container:"; \
	docker logs $(APP_NAME) 2>&1; \
	exit 1; \
	fi

	### Wait for success pattern
	@until docker logs $(APP_NAME) 2>&1 | grep -Fq "$(SUCCESS_PATTERN)"; do sleep 1; done
	@echo "[INFO] Spring Boot application started and ready."

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
	@docker exec $(APP_NAME) sh -c 'python3 /app/smoke.py'
	$(MAKE) clean

local:
	./mvnw clean package spring-boot:run
