### TaskCreator (Spring Boot) Justfile
APP_NAME        := "taskcreator-springboot"
SPRING_IMAGE    := "taskcreator-springboot:latest"
SPRING_PORT     := "9080"      # Spring Boot server.port
HOST_PORT       := "10023"     # Host port mapped to container's SPRING_PORT

# Build Docker image (expects a Dockerfile for Spring Boot)
build:
	docker build -f Dockerfile -t $(SPRING_IMAGE) .
	@echo "[INFO] Built image: $(SPRING_IMAGE)"

# Rebuild without cache
rebuild:
	docker build --no-cache -f Dockerfile -t $(SPRING_IMAGE) .
	@echo "[INFO] Rebuilt image (no cache): $(SPRING_IMAGE)"

# Run container and wait until Spring is ready
up: build
	@if docker ps --all --quiet --filter name=^/$(APP_NAME)$ | grep -q .; then \
	echo "[INFO] Removing existing container"; \
	docker rm -f $(APP_NAME) >/dev/null; \
	fi
	docker run -d --name $(APP_NAME) -p $(HOST_PORT):$(SPRING_PORT) $(SPRING_IMAGE)
	@echo "[INFO] Started $(APP_NAME), waiting for app to start..."
	@until docker logs $(APP_NAME) 2>&1 | grep -Eq "Tomcat started on port $(SPRING_PORT)|Started .* in .* seconds"; do sleep 1; done
	@echo "[INFO] Spring app started, port $(HOST_PORT) is ready."

# Tail logs
logs:
	docker logs -f $(APP_NAME)

# Stop & remove container (ignore errors)
down:
	- docker rm -f $(APP_NAME)
	@echo "[INFO] Container removed (if it existed)"

# Clean up
clean: down
	docker rmi $(APP_NAME)
	@echo "[INFO] Image removed (if it existed)"

# Run smoke test inside the container (expects /app/smoke.py to be present in the image)
test: up
	@docker exec $(APP_NAME) sh -c 'python3 /app/smoke.py'

# Run locally with Maven (no Docker)
local:
	./mvnw -q -DskipTests spring-boot:run