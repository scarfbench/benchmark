#!/usr/bin/env bash
set -euo pipefail

# Nginx proxy exposes all services on port 8080
NGINX_URL="${NGINX_URL:-http://localhost:8080}"

PASS=0
FAIL=0

check_status() {
  local name="$1"
  local url="$2"
  local expected="$3"
  local method="${4:-GET}"
  local data="${5:-}"

  if [ "$method" = "POST" ]; then
    HTTP_STATUS=$(curl -sL -o /dev/null -w "%{http_code}" --max-time 10 \
      -X POST -H "Content-Type: application/json" -d "$data" "$url" || echo "000")
  else
    HTTP_STATUS=$(curl -sL -o /dev/null -w "%{http_code}" --max-time 10 "$url" || echo "000")
  fi

  if [ "$HTTP_STATUS" -eq "$expected" ]; then
    echo "PASS - $name: HTTP $HTTP_STATUS"
    PASS=$((PASS + 1))
  else
    echo "FAIL - $name: expected $expected, got HTTP $HTTP_STATUS"
    FAIL=$((FAIL + 1))
  fi
}

check_body_contains() {
  local name="$1"
  local url="$2"
  local needle="$3"

  BODY=$(curl -sL --max-time 10 "$url" || echo "")
  if echo "$BODY" | grep -q "$needle"; then
    echo "PASS - $name: body contains '$needle'"
    PASS=$((PASS + 1))
  else
    echo "FAIL - $name: body missing '$needle' — got: ${BODY:0:120}"
    FAIL=$((FAIL + 1))
  fi
}

echo "=== Cloud-Native Starter Microservices (Quarkus) Smoke Tests ==="
echo ""

echo "--- articles-service: reads ---"
check_status "articles /getmultiple?amount=5"   "${NGINX_URL}/articles/v1/getmultiple?amount=5"   200
check_status "articles /getmultiple?amount=1"   "${NGINX_URL}/articles/v1/getmultiple?amount=1"   200
check_status "articles /getmultiple?amount=50"  "${NGINX_URL}/articles/v1/getmultiple?amount=50"  200
check_status "articles /getone (unknown id)"    "${NGINX_URL}/articles/v1/getone?id=999999" 404

echo ""
echo "--- articles-service: write ---"
check_status "articles POST /create"            "${NGINX_URL}/articles/v1/create" 201 POST \
  '{"title":"Smoke Test Article","url":"https://example.com","author":"Niklas Heidloff","creationDate":"2026-04-23"}'

echo ""
echo "--- authors-service: reads ---"
check_status "authors /getauthor (Niklas)"      "${NGINX_URL}/authors/v1/getauthor?name=Niklas%20Heidloff" 200
check_status "authors /getauthor (Billy)"       "${NGINX_URL}/authors/v1/getauthor?name=Billy%20Korando"   200

echo ""
echo "--- web-api-service: chained calls ---"
check_status "web-api /getmultiple"             "${NGINX_URL}/web-api/v1/getmultiple" 200
check_body_contains "web-api /getmultiple body" "${NGINX_URL}/web-api/v1/getmultiple" "Niklas Heidloff"
check_body_contains "articles body has title"   "${NGINX_URL}/articles/v1/getmultiple?amount=5" "Cloud Native"

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
exit 0
