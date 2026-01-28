### TaskCreator (Liberty-only) Justfile
APP_NAME       := "jobs-quarkus"
LIBERTY_IMAGE   := "jobs-quarkus:latest"
LIBERTY_PORT    := "9080"               # Liberty internal HTTP
HOST_PORT      := "10012"               # Host port to map to Liberty internal HTTP

build:
	docker build -t $(LIBERTY_IMAGE) .
	@echo "[INFO] Built image: $(LIBERTY_IMAGE)"

rebuild:
	docker build --no-cache -f Dockerfile -t $(LIBERTY_IMAGE) .
	@echo "[INFO] Rebuilt image (no cache): $(LIBERTY_IMAGE)"

up: build
	@if docker ps --all --quiet --filter name=^/jobs-quarkus$ | grep -q .; then \
	echo "[INFO] Removing existing container"; \
	docker rm -f jobs-quarkus >/dev/null; \
	fi
	docker run -d --name jobs-quarkus -p $(HOST_PORT):$(LIBERTY_PORT) $(LIBERTY_IMAGE)
	@echo "[INFO] Started jobs-quarkus, waiting for app to start..."
	@until docker logs jobs-quarkus 2>&1 | grep -q '\"loggerName\":\"io.quarkus\".*started in .*Listening on:'; do sleep 1; done
	@echo "[INFO] Liberty app started, port $(HOST_PORT) is ready."

logs:
	docker logs -f jobs-quarkus

down:
	- docker rm -f jobs-quarkus
	@echo "[INFO] Container removed (if it existed)"

# Clean up
clean: down
	docker rmi $(APP_NAME)
	@echo "[INFO] Image removed (if it existed)"

test: up
	@docker exec $(APP_NAME) sh -c 'python3 /app/smoke.py'

local:
	./mvnw clean package quarkus:run
