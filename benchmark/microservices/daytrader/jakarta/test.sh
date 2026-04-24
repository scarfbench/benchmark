#!/usr/bin/env bash
set -euo pipefail

# Service endpoints (exposed ports on host)
ACCOUNTS_URL="${ACCOUNTS_URL:-http://localhost:1443}"
GATEWAY_URL="${GATEWAY_URL:-http://localhost:2443}"
PORTFOLIOS_URL="${PORTFOLIOS_URL:-http://localhost:3443}"
QUOTES_URL="${QUOTES_URL:-http://localhost:4443}"
WEB_URL="${WEB_URL:-http://localhost:5443}"

PASS=0
FAIL=0

check() {
  local name="$1"
  local url="$2"
  local expected="${3:-200}"

  HTTP_STATUS=$(curl -sL -o /dev/null -w "%{http_code}" --max-time 10 "$url" || echo "000")

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

echo "=== DayTrader Microservices (Jakarta EE) Smoke Tests ==="
echo ""

echo "--- Service Health (MicroProfile Health) ---"
check "accounts-service /health"   "${ACCOUNTS_URL}/health"
check "gateway-service /health"    "${GATEWAY_URL}/health"
check "portfolios-service /health" "${PORTFOLIOS_URL}/health"
check "quotes-service /health"     "${QUOTES_URL}/health"
check "web-service /health"        "${WEB_URL}/health"

echo ""
echo "--- Liveness Probes ---"
check "accounts   /health/live"   "${ACCOUNTS_URL}/health/live"
check "gateway    /health/live"   "${GATEWAY_URL}/health/live"
check "quotes     /health/live"   "${QUOTES_URL}/health/live"

echo ""
echo "--- Readiness Probes ---"
check "accounts   /health/ready"  "${ACCOUNTS_URL}/health/ready"
check "portfolios /health/ready"  "${PORTFOLIOS_URL}/health/ready"
check "quotes     /health/ready"  "${QUOTES_URL}/health/ready"

echo ""
echo "--- Web Service UI ---"
check "web-service root /api"            "${WEB_URL}/api"
check "web-service /api/servlet/PingServlet" "${WEB_URL}/api/servlet/PingServlet"

echo ""
echo "--- Response Content Validation ---"
check_body_contains "quotes /health body"       "${QUOTES_URL}/health" "status"
check_body_contains "gateway /health/ready body" "${GATEWAY_URL}/health/ready" "status"

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
exit 0
