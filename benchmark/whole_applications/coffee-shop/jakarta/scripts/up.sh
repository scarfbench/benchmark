#!/usr/bin/env bash
set -euo pipefail

# 1) build all wars
mvn -q -DskipTests clean package

# 2) build images
docker compose -f docker/docker-compose.yml build

# 3) up
docker compose -f docker/docker-compose.yml up -d

echo "==> Wait a few seconds, then run: scripts/smoke_drink.sh and scripts/smoke_food.sh"
