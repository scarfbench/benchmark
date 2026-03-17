#!/usr/bin/env bash
set -euo pipefail

echo "Tailing service logs (Ctrl+C to stop) ..."
docker compose -f docker/docker-compose.yml logs -f orders-service barista-service kitchen-service
