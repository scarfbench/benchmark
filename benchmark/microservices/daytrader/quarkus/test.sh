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

echo "=== DayTrader Microservices (Quarkus) Smoke Tests ==="
echo ""

echo "--- Service Health (SmallRye Health) ---"
check "accounts-service /q/health"   "${ACCOUNTS_URL}/q/health"
check "gateway-service /q/health"    "${GATEWAY_URL}/q/health"
check "portfolios-service /q/health" "${PORTFOLIOS_URL}/q/health"
check "quotes-service /q/health"     "${QUOTES_URL}/q/health"
check "web-service /q/health"        "${WEB_URL}/q/health"

echo ""
echo "--- Liveness Probes ---"
check "accounts   /q/health/live"   "${ACCOUNTS_URL}/q/health/live"
check "gateway    /q/health/live"   "${GATEWAY_URL}/q/health/live"
check "quotes     /q/health/live"   "${QUOTES_URL}/q/health/live"

echo ""
echo "--- Readiness Probes ---"
check "accounts   /q/health/ready"  "${ACCOUNTS_URL}/q/health/ready"
check "portfolios /q/health/ready"  "${PORTFOLIOS_URL}/q/health/ready"
check "quotes     /q/health/ready"  "${QUOTES_URL}/q/health/ready"

echo ""
echo "--- Web Service UI ---"
check "web-service root"                 "${WEB_URL}/"
check "web-service /servlet/PingServlet" "${WEB_URL}/servlet/PingServlet"

echo ""
echo "--- Response Content Validation ---"
check_body_contains "quotes /q/health body"         "${QUOTES_URL}/q/health" "status"
check_body_contains "gateway /q/health/ready body"  "${GATEWAY_URL}/q/health/ready" "status"

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
exit 0
