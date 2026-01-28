### TaskCreator (Liberty-only) Justfile
APP_NAME       := "timersession-liberty"
LIBERTY_IMAGE   := "timersession-liberty:latest"
LIBERTY_PORT    := "9080"               # Liberty internal HTTP
HOST_PORT      := "10051"               # Host port to map to Liberty internal HTTP

build:
	docker build -f Dockerfile -t $(LIBERTY_IMAGE) .
	@echo "[INFO] Built image: $(LIBERTY_IMAGE)"

rebuild:
	docker build --no-cache -f Dockerfile -t $(LIBERTY_IMAGE) .
	@echo "[INFO] Rebuilt image (no cache): $(LIBERTY_IMAGE)"

up: build
	@if docker ps --all --quiet --filter name=^/timersession-liberty$ | grep -q .; then \
	echo "[INFO] Removing existing container"; \
	docker rm -f timersession-liberty >/dev/null; \
	fi
	docker run -d --name timersession-liberty -p $(HOST_PORT):$(LIBERTY_PORT) $(LIBERTY_IMAGE)
	@echo "[INFO] Started timersession-liberty, waiting for app to start..."
	@until docker logs timersession-liberty 2>&1 | grep -q "CWWKF0011I"; do sleep 1; done
	@echo "[INFO] Liberty app started, port $(HOST_PORT) is ready."

logs:
	docker logs -f timersession-liberty

down:
	- docker rm -f timersession-liberty
	@echo "[INFO] Container removed (if it existed)"

# Clean up
clean: down
	docker rmi $(APP_NAME)
	@echo "[INFO] Image removed (if it existed)"

test: up
	@docker exec $(APP_NAME) sh -c 'python3 /app/smoke.py'

local:
	./mvnw clean package liberty:run
