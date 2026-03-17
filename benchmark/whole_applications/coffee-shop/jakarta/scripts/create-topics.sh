#!/usr/bin/env bash
set -euo pipefail

COMPOSE="docker compose -f docker/docker-compose.yml"

topics=(
  "barista-commands"
  "kitchen-commands"
  "order-updates"
)

for t in "${topics[@]}"; do
  echo "Creating topic: $t (if not exists)"
  $COMPOSE exec -T kafka kafka-topics --bootstrap-server kafka:9092 --create --if-not-exists --topic "$t" --replication-factor 1 --partitions 1 || true
done

echo "Listing topics:"
$COMPOSE exec -T kafka kafka-topics --bootstrap-server kafka:9092 --list
