#!/bin/bash

echo "[INFO] Running a mock agent"

if [[ "$FAIL" == "true" ]]; then
    echo "[ERROR] Agent run failed" >&2
    exit 1
else
    echo "[INFO] Agent successfully ran"
    exit 0
fi
