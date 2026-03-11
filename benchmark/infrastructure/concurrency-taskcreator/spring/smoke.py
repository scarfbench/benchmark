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
    "http://localhost:10023/taskcreator",
    "http://localhost:10023"
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


@pytest.fixture(scope="module")
def base_url():
    return pick_base_url()


def test_post_log_accepts_message(base_url):
    """POST to /taskinfo should accept a message and return 200/204."""
    msg = f"{datetime.now().strftime('%H:%M:%S')} - Test message"
    assert post_log(base_url, msg)


def test_get_taskinfo_returns_405(base_url):
    """GET to /taskinfo should return 405 Method Not Allowed."""
    assert get_taskinfo_expect_405(base_url)


def test_post_log_with_task_format(base_url):
    """POST with a task-formatted timestamp message."""
    msg = f"{datetime.now().strftime('%H:%M:%S')} - IMMEDIATE Task TestTask started"
    assert post_log(base_url, msg)


def test_post_multiple_messages(base_url):
    """Multiple messages can be posted sequentially."""
    for i in range(3):
        msg = f"{datetime.now().strftime('%H:%M:%S')} - Task {i} message"
        assert post_log(base_url, msg)


def test_post_delayed_task_format(base_url):
    """POST with DELAYED task format message."""
    msg = f"{datetime.now().strftime('%H:%M:%S')} - DELAYED Task DelayedOne submitted"
    assert post_log(base_url, msg)


def test_post_periodic_task_format(base_url):
    """POST with PERIODIC task format message."""
    msg = f"{datetime.now().strftime('%H:%M:%S')} - PERIODIC Task Repeater started run #1"
    assert post_log(base_url, msg)


def main() -> int:
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":  # pragma: no cover
    sys.exit(main())
