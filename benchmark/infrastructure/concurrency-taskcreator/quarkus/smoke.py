#!/usr/bin/env python3
"""Smoke test for Jakarta taskcreator.

Checks:
  1) POST a log line to /taskinfo and expect 200 or 204.
  2) GET /taskinfo and expect 405 (Method Not Allowed).

Env:
    TASKCREATOR_BASE_URL (default: tries http://localhost:9080/taskcreator then http://localhost:9080)
    VERBOSE=1   enables verbose output

Exit: 0 on success, non-zero otherwise.
"""
import argparse
import os
import sys
import pytest
from datetime import datetime
from urllib.error import HTTPError
from urllib.request import Request, urlopen

# Try both possible base URLs if not set
BASE_CANDIDATES = [
    os.getenv("TASKCREATOR_BASE_URL"),
    "http://localhost:9080/taskcreator",
    "http://localhost:9080",
    "http://localhost:10022/taskcreator",
    "http://localhost:10022"
]
DEFAULT_ENDPOINT = "/taskinfo"


def post_log(base_url: str, message: str) -> bool:
    url = f"{base_url.rstrip('/')}{DEFAULT_ENDPOINT}"
    req = Request(url, data=message.encode(), headers={"Content-Type": "text/plain"}, method="POST")
    print(f"POST {url} :: {message}")
    try:
        with urlopen(req, timeout=10) as resp:
            status = resp.getcode()
            body = resp.read().decode("utf-8", "replace")
    except HTTPError as e:
        status = e.code
        body = e.read().decode("utf-8", "replace")
    except Exception as e:  # network failure
        print(f"[FAIL] POST failed: {e}", file=sys.stderr)
        return False

    print(f"RESP {status}\n{body.strip()}")

    if status not in (200, 204):
        print(f"[FAIL] Unexpected HTTP status {status}", file=sys.stderr)
        return False

    print(f"[PASS] POST {DEFAULT_ENDPOINT} -> {status}")
    return True


def get_taskinfo_expect_405(base_url: str) -> bool:
    url = f"{base_url.rstrip('/')}{DEFAULT_ENDPOINT}"
    req = Request(url, method="GET")
    print(f"GET {url}")
    try:
        with urlopen(req, timeout=10) as resp:
            status = resp.getcode()
            body = resp.read().decode("utf-8", "replace")
    except HTTPError as e:
        status = e.code
        body = e.read().decode("utf-8", "replace")
    except Exception as e:
        print(f"[FAIL] GET failed: {e}", file=sys.stderr)
        return False

    print(f"RESP {status}\n{body.strip()}")
    if status == 405:
        print(f"[PASS] GET {DEFAULT_ENDPOINT} -> 405 (Method Not Allowed)")
        return True
    print(f"[FAIL] GET {DEFAULT_ENDPOINT} -> {status} (expected 405)", file=sys.stderr)
    return False


def pick_base_url() -> str:
    for base in BASE_CANDIDATES:
        if not base:
            continue
        msg = f"{datetime.now().strftime('%H:%M:%S')} - Smoke test"
        if post_log(base, msg):
            return base
    # fallback to first candidate (even if failed)
    return BASE_CANDIDATES[1]


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description="Smoke test for taskcreator-quarkus")
    p.add_argument("--base-url", default=DEFAULT_BASE, help=f"Base URL (env BASE_URL or {DEFAULT_BASE})")
    return p.parse_args()


def test_smoke():
    assert main() == 0


def main() -> int:
    base_url = pick_base_url()
    print(f"Using base URL: {base_url}")
    msg = f"{datetime.now().strftime('%H:%M:%S')} - Smoke test"
    post_ok = post_log(base_url, msg)
    get_ok = get_taskinfo_expect_405(base_url)
    if post_ok and get_ok:
        print("[PASS] Smoke tests complete")
        return 0
    print("[FAIL] Smoke tests failed", file=sys.stderr)
    return 1


if __name__ == "__main__":  # pragma: no cover
    sys.exit(main())
