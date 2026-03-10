"""
Smoke test for DukeETF application.

Checks:
  1) GET <base>/main.xhtml returns 200.
  2) (Soft) GET <base>/resources/css/default.css returns 200, otherwise WARN (not fatal).
  3) Long-poll endpoint responds:
       - Attempt GET on one of:
           a) <base>              (when base already ends with '/dukeetf')
           b) <base>/dukeetf
           c) <base>/
         Expect HTTP 200 within LONG_TIMEOUT and a non-empty body (ideally containing digits).
  4) DukeETF number changes: GET the dukeetf endpoint, wait 5s, GET again.
       Expect the numeric payload to differ (price or volume changed).

Environment:
  DUKEETF_BASE   Base URL for app (default: http://localhost:8080)
  VERBOSE=1      Verbose logging

Exit codes:
  0  success
  2  GET main.xhtml failed
  4  Long-poll endpoint failed or timed out
  6  DukeETF payload did not change after 5s
  9  Network / unexpected error
"""

import os
import sys
import time
import re
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import pytest

BASE = os.getenv("DUKEETF_BASE", "http://localhost:8080")
VERBOSE = os.getenv("VERBOSE") == "1"
HTTP_TIMEOUT = 10
LONG_TIMEOUT = 12

def vprint(*args):
    if VERBOSE:
        print(*args)

def http_request(method: str, url: str, data: bytes | None = None, headers: dict | None = None, timeout: int = HTTP_TIMEOUT):
    req = Request(url, data=data, method=method, headers=headers or {})
    try:
        with urlopen(req, timeout=timeout) as resp:
            status = resp.getcode()
            body = resp.read().decode("utf-8", "replace")
            return (status, body), None
    except HTTPError as e:
        try:
            body = e.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        return (e.code, body), None
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"

def join(base: str, path: str) -> str:
    if not path:
        return base
    if base.endswith("/") and path.startswith("/"):
        return base[:-1] + path
    if not base.endswith("/") and not path.startswith("/"):
        return base + "/" + path
    return base + path

def must_get_ok(path: str, fail_code: int):
    url = join(BASE, path)
    vprint(f"GET {url}")
    resp, err = http_request("GET", url)
    if err:
        pytest.fail(f"[FAIL] {path} -> {err}")
    status, _ = resp
    if status != 200:
        print(f"[FAIL] GET {path} -> {status}", file=sys.stderr)
        pytest.fail("smoke check failed")
    print(f"[PASS] GET {path} -> 200")

def soft_get_ok(path: str):
    url = join(BASE, path)
    vprint(f"GET {url} (soft)")
    resp, err = http_request("GET", url)
    if err:
        print(f"[WARN] {path} -> {err}", file=sys.stderr)
        return
    status, _ = resp
    if status != 200:
        print(f"[WARN] GET {path} -> {status}", file=sys.stderr)
    else:
        print(f"[PASS] GET {path} -> 200")

def try_long_poll(url: str, label: str) -> bool:
    vprint(f"Long-poll TRY {label}: {url} (timeout={LONG_TIMEOUT}s)")
    start = time.time()
    resp, err = http_request("GET", url, timeout=LONG_TIMEOUT)
    elapsed = time.time() - start
    if err:
        vprint(f"Long-poll error: {err}")
        return False
    status, body = resp
    if status == 200 and body.strip():
        print(f"[PASS] Long-poll {label} -> 200 in {elapsed:.2f}s, {len(body.strip())} bytes")
        return True
    vprint(f"Long-poll unexpected: status={status}, body={body[:200]!r}")
    return False

def assert_long_poll():
    candidates = []
    candidates.append(BASE)
    candidates.append(join(BASE, "/dukeetf"))
    candidates.append(join(BASE, "/"))

    for i, u in enumerate(candidates, 1):
        if try_long_poll(u, f"cand#{i}"):
            return
    print("[FAIL] Long-poll endpoint did not respond with 200 + non-empty body within timeout.", file=sys.stderr)
    pytest.fail("smoke test failed with code 4")

_num_re = re.compile(r"\s*(-?\d+(?:\.\d+)?)\s*/\s*(-?\d+)\s*")

def canonical_dukeetf_url() -> str:
    """Return the URL that should hit the DukeETF servlet directly."""
    b = BASE.rstrip("/")
    if b.endswith("/dukeetf"):
        return b
    return join(BASE, "/dukeetf")

def parse_price_volume(body: str):
    """
    Parse "123.45 / 67890" into (price: float, volume: int).
    Returns None if pattern not found.
    """
    m = _num_re.search(body or "")
    if not m:
        return None
    try:
        price = float(m.group(1))
        volume = int(m.group(2))
        return (price, volume)
    except Exception:
        return None

def assert_price_changes():
    url = canonical_dukeetf_url()
    vprint(f"Change-check URL: {url}")

    resp1, err1 = http_request("GET", url, timeout=LONG_TIMEOUT)
    if err1:
        pytest.fail(f"[FAIL] change-check first read -> {err1}")
    s1, b1 = resp1
    if s1 != 200:
        pytest.fail(f"[FAIL] change-check first read -> HTTP {s1}")
    pv1 = parse_price_volume(b1)
    if not pv1:
        pytest.fail(f"[FAIL] change-check first read: could not parse numbers from body: {b1!r}")

    time.sleep(5.0)

    resp2, err2 = http_request("GET", url, timeout=LONG_TIMEOUT)
    if err2:
        pytest.fail(f"[FAIL] change-check second read -> {err2}")
    s2, b2 = resp2
    if s2 != 200:
        pytest.fail(f"[FAIL] change-check second read -> HTTP {s2}")
    pv2 = parse_price_volume(b2)
    if not pv2:
        pytest.fail(f"[FAIL] change-check second read: could not parse numbers from body: {b2!r}")

    (p1, v1) = pv1
    (p2, v2) = pv2
    if p1 != p2 or v1 != v2:
        print(f"[PASS] DukeETF changes over 5s: "
              f"{p1:.2f}/{v1} -> {p2:.2f}/{v2}")
        return

    pytest.fail(f"[FAIL] DukeETF values unchanged after 5s: {p1:.2f}/{v1}")


def test_must_get_ok():
    must_get_ok("/main.xhtml", 2)


def test_soft_get_ok():
    soft_get_ok("/resources/css/default.css")


def test_assert_long_poll():
    assert_long_poll()


def test_assert_price_changes():
    assert_price_changes()  


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
