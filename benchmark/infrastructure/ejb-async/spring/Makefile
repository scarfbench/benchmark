### TaskCreator (Liberty-only) Justfile
APP_NAME       := "async-spring"
LIBERTY_IMAGE   := "async-spring:latest"
LIBERTY_PORT    := "9080"               # Liberty internal HTTP
HOST_PORT      := "10011"               # Host port to map to Liberty internal HTTP

build:
	docker build -f Dockerfile -t $(LIBERTY_IMAGE) .
	@echo "[INFO] Built image: $(LIBERTY_IMAGE)"

rebuild:
	docker build --no-cache -f Dockerfile -t $(LIBERTY_IMAGE) .
	@echo "[INFO] Rebuilt image (no cache): $(LIBERTY_IMAGE)"

up: build
	@if docker ps --all --quiet --filter name=^/async-spring | grep -q .; then \
	echo "[INFO] Removing existing container"; \
	docker rm -f async-spring >/dev/null; \
	fi
	docker run --name async-spring -p $(HOST_PORT):$(LIBERTY_PORT) $(LIBERTY_IMAGE)

logs:
	docker logs -f async-spring

down:
	- docker rm -f async-spring
	@echo "[INFO] Container removed (if it existed)"

# Clean up
clean: down
	docker rmi $(APP_NAME)
	@echo "[INFO] Image removed (if it existed)"

test: up

local:
	./mvnw -pl async-service clean package spring-boot:run
