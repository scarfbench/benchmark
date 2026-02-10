#!/bin/bash

echo "[INFO] Running a mock agent"
sleep 2

if [[ "$FAIL" == "true" ]]; then
    echo "[ERROR] Agent run failed"
    exit 1
else
    echo "[INFO] Agent successfully ran"
    exit 0
fi
