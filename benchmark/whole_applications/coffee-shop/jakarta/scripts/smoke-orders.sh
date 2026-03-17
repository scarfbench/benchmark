#!/usr/bin/env bash
set -euo pipefail

URL="http://localhost:8081/orders-service/api/orders"
echo "POST -> ${URL}"
curl -sS -i -X POST "${URL}"   -H 'Content-Type: application/json'   -d '{"customer":"Raju","item":"latte","quantity":1}'
echo
echo "Query DB for latest rows:"
docker compose -f docker/docker-compose.yml exec -T postgres   psql -U cafe -d coffeeshop   -c "SELECT id, customer, item, quantity, status, created, updated FROM orders ORDER BY id DESC LIMIT 5;"
