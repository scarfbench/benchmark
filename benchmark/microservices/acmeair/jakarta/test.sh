#!/usr/bin/env bash
set -euo pipefail

# Nginx proxy exposes all services on port 8080
NGINX_URL="${NGINX_URL:-http://localhost:8080}"

PASS=0
FAIL=0

check_status() {
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

check_body_nonempty() {
  local name="$1"
  local url="$2"

  BODY=$(curl -sL --max-time 10 "$url" || echo "")
  if [ -n "$BODY" ]; then
    echo "PASS - $name: non-empty body (${#BODY} bytes)"
    PASS=$((PASS + 1))
  else
    echo "FAIL - $name: empty body"
    FAIL=$((FAIL + 1))
  fi
}

echo "=== AcmeAir Microservices (Jakarta EE) Smoke Tests ==="
echo ""

echo "--- Service Health (root via nginx) ---"
check_status "main-service root"     "${NGINX_URL}/acmeair/"
check_status "auth-service root"     "${NGINX_URL}/auth/"
check_status "booking-service root"  "${NGINX_URL}/booking/"
check_status "customer-service root" "${NGINX_URL}/customer/"
check_status "flight-service root"   "${NGINX_URL}/flight/"

echo ""
echo "--- Config / Runtime Endpoints ---"
check_status "auth     /config/runtime"    "${NGINX_URL}/auth/config/runtime"
check_status "booking  /config/runtime"    "${NGINX_URL}/booking/config/runtime"
check_status "customer /config/runtime"    "${NGINX_URL}/customer/config/runtime"
check_status "flight   /config/runtime"    "${NGINX_URL}/flight/config/runtime"

echo ""
echo "--- Count / Active-Data Endpoints ---"
check_status "flight   /config/countAirports"       "${NGINX_URL}/flight/config/countAirports"
check_status "flight   /config/countFlights"        "${NGINX_URL}/flight/config/countFlights"
check_status "flight   /config/countFlightSegments" "${NGINX_URL}/flight/config/countFlightSegments"
check_status "booking  /config/countBookings"       "${NGINX_URL}/booking/config/countBookings"
check_status "customer /config/countCustomers"      "${NGINX_URL}/customer/config/countCustomers"
check_status "flight   /config/activeDataService"   "${NGINX_URL}/flight/config/activeDataService"

echo ""
echo "--- Response Content Validation ---"
check_body_nonempty "flight /config/runtime body" "${NGINX_URL}/flight/config/runtime"

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
exit 0
