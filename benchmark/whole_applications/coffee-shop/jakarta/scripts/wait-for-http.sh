#!/usr/bin/env bash
# wait-for-http.sh <url> [timeout_seconds]
set -euo pipefail

URL="${1:-http://localhost:9081/health}"
TIMEOUT="${2:-60}"

echo "Waiting up to ${TIMEOUT}s for ${URL} ..."
end=$((SECONDS+TIMEOUT))
while [ $SECONDS -lt $end ]; do
  if curl -fsS "${URL}" >/dev/null 2>&1; then
    echo "OK: ${URL}"
    exit 0
  fi
  sleep 2
done

echo "TIMEOUT waiting for ${URL}"
exit 1
