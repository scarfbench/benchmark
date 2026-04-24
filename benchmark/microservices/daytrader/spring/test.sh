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

echo "=== DayTrader Microservices (Spring) Smoke Tests ==="
echo ""

echo "--- Setup: Initializing databases (not counted as tests) ---"
for url in "$QUOTES_URL" "$ACCOUNTS_URL" "$PORTFOLIOS_URL"; do
  curl -sL --max-time 30 -X POST "$url/admin/recreateDBTables" > /dev/null 2>&1 || true
done
curl -sL --max-time 60 -X POST "$QUOTES_URL/admin/tradeBuildDB?limit=20&offset=0" > /dev/null 2>&1 || true
curl -sL --max-time 60 -X POST "$ACCOUNTS_URL/admin/tradeBuildDB?limit=10&offset=0" > /dev/null 2>&1 || true
echo "[INFO] Setup complete"
echo ""

echo "--- Service Health (Spring Actuator) ---"
check "accounts-service health"   "${ACCOUNTS_URL}/actuator/health"
check "gateway-service health"    "${GATEWAY_URL}/actuator/health"
check "portfolios-service health" "${PORTFOLIOS_URL}/actuator/health"
check "quotes-service health"     "${QUOTES_URL}/actuator/health"
check "web-service health"        "${WEB_URL}/actuator/health"

echo ""
echo "--- Web Service UI ---"
check "web-service root"        "${WEB_URL}/"
check "web-service PingServlet" "${WEB_URL}/servlet/PingServlet"

echo ""
echo "--- Quotes Business Endpoints ---"
check "quotes /quotes?limit=5"    "${QUOTES_URL}/quotes?limit=5"
check "quotes /quotes/s:0"        "${QUOTES_URL}/quotes/s:0"
check "quotes /markets/NASDAQ"    "${QUOTES_URL}/markets/NASDAQ"

echo ""
echo "--- Gateway: chained calls to other services ---"
check "gateway /quotes?limit=5"   "${GATEWAY_URL}/quotes?limit=5&offset=0"
check "gateway /quotes/s:0"       "${GATEWAY_URL}/quotes/s:0"

echo ""
echo "--- Response Content Validation ---"
check_body_contains "quotes /quotes?limit=5 has symbol"     "${QUOTES_URL}/quotes?limit=5" "s:0"
check_body_contains "gateway /quotes?limit=5 has symbol"    "${GATEWAY_URL}/quotes?limit=5&offset=0" "s:0"

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
exit 0
