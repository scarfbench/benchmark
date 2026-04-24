#!/usr/bin/env bash
set -euo pipefail

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

echo "=== PetClinic Microservices (Spring) Smoke Tests ==="
echo ""

echo "--- customers-service ---"
check_status "customers /owners (list)"        "${NGINX_URL}/customers/owners"
check_status "customers /owners/1 (George)"    "${NGINX_URL}/customers/owners/1"
check_status "customers /owners/6 (Jean)"      "${NGINX_URL}/customers/owners/6"
check_status "customers /owners/9999 (unknown)" "${NGINX_URL}/customers/owners/9999" 404
check_status "customers /petTypes"             "${NGINX_URL}/customers/petTypes"

echo ""
echo "--- vets-service ---"
check_status "vets /vets (list)"               "${NGINX_URL}/vets"

echo ""
echo "--- visits-service ---"
check_status "visits /pets/visits?petId=7"     "${NGINX_URL}/visits/pets/visits?petId=7"
check_status "visits /pets/visits?petId=1,7"   "${NGINX_URL}/visits/pets/visits?petId=1,7"

echo ""
echo "--- api-gateway: chained aggregation ---"
check_status "api-gateway /owners/1"           "${NGINX_URL}/api/gateway/owners/1"
check_status "api-gateway /owners/6"           "${NGINX_URL}/api/gateway/owners/6"

echo ""
echo "--- Response Content Validation ---"
check_body_contains "customers /owners/6 has 'Jean'"   "${NGINX_URL}/customers/owners/6" "Jean"
check_body_contains "api-gateway /owners/6 has pet"    "${NGINX_URL}/api/gateway/owners/6" "Samantha"

echo ""
echo "=== Results: $PASS passed, $FAIL failed ==="

if [ "$FAIL" -gt 0 ]; then
  exit 1
fi
exit 0
