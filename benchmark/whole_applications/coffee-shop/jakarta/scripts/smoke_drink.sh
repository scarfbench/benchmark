#!/usr/bin/env bash
set -euo pipefail

ORDERS_URL=${ORDERS_URL:-http://localhost:8081/orders-service}
POST_URL="$ORDERS_URL/api/orders"

require() {
  command -v "$1" >/dev/null 2>&1 || { echo "Missing dependency: $1"; exit 1; }
}
require curl
require jq

echo "==> placing DRINK order (latte)"
resp=$(curl -sS -X POST "$POST_URL" \
  -H 'Content-Type: application/json' \
  -d '{"customer":"Raju","item":"latte","quantity":1}')

echo "POST response: $resp"
order_id=$(echo "$resp" | jq -r '.id // .orderId // empty')
if [[ -z "${order_id}" || "${order_id}" == "null" ]]; then
  echo "Could not parse order id from response."; exit 2
fi
echo "Order id: $order_id"

echo "==> waiting for status to become READY"
for i in {1..40}; do
  status=$(curl -sS "$ORDERS_URL/api/orders/$order_id" | jq -r '.status // empty')
  echo "poll $i -> $status"
  [[ "$status" == "READY" ]] && { echo "✅ drink order READY"; exit 0; }
  sleep 1
done

echo "❌ timeout waiting for READY"; exit 3
