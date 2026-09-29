#!/usr/bin/env bash
set -euo pipefail

root="$(cd "$(dirname "$0")" && pwd)"
for target in jakarta quarkus spring; do
  if [[ "${1:-}" == "--check" ]]; then
    cmp "$root/behavior_test.py" "$root/$target/behavior_test.py"
  else
    cp "$root/behavior_test.py" "$root/$target/behavior_test.py"
  fi
done
