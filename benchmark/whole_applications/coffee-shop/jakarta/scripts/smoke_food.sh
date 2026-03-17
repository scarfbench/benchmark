#!/usr/bin/env bash
set -euo pipefail

ORDERS_URL=${ORDERS_URL:-http://localhost:8081/orders-service}
POST_URL="$ORDERS_URL/api/orders"

command -v curl >/dev/null || { echo "Missing curl"; exit 1; }
command -v jq >/dev/null || { echo "Missing jq"; exit 1; }

echo "==> placing FOOD order (sandwich)"
resp=$(curl -sS -X POST "$POST_URL" \
  -H 'Content-Type: application/json' \
  -d '{"customer":"Raju","item":"sandwich","quantity":1}')

echo "POST response: $resp"
order_id=$(echo "$resp" | jq -r '.id // .orderId // empty')
[[ -z "${order_id}" || "${order_id}" == "null" ]] && { echo "No order id"; exit 2; }

echo "==> waiting for status to become READY"
for i in {1..40}; do
  status=$(curl -sS "$ORDERS_URL/api/orders/$order_id" | jq -r '.status // empty')
  echo "poll $i -> $status"
  [[ "$status" == "READY" ]] && { echo "✅ food order READY"; exit 0; }
  sleep 1
done

echo "❌ timeout waiting for READY"; exit 3
