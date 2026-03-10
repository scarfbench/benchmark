"""
Smoke test for Jakarta REST "Hello" app.

Checks:
  1) GET <BASE>/helloworld -> 200 and Content-Type text/html

Environment:
  HELLO_BASE   Base HTTP URL (default: http://localhost:8080)
  VERBOSE=1    Enable verbose logging

Exit codes:
  0  success
  2  GET /helloworld failed (status or content-type)
  9  Network / unexpected error
"""
import os
import sys
import time
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import pytest

BASE = os.getenv("HELLO_BASE", "http://localhost:8080").rstrip("/")
VERBOSE = os.getenv("VERBOSE") == "1"
TIMEOUT = 10

def vprint(*args):
    if VERBOSE:
        print(*args)

def join(base: str, path: str) -> str:
    if not path:
        return base
    if base.endswith("/") and path.startswith("/"):
        return base[:-1] + path
    if (not base.endswith("/")) and (not path.startswith("/")):
        return base + "/" + path
    return base + path

def http(method: str, url: str, data: bytes | None = None, headers: dict | None = None):
    req = Request(url, data=data, method=method, headers=headers or {})
    try:
        with urlopen(req, timeout=TIMEOUT) as resp:
            status = resp.getcode()
            body = resp.read().decode("utf-8", "replace")
            content_type = resp.headers.get("Content-Type", "")
            return {"status": status, "body": body, "content_type": content_type}, None
    except HTTPError as e:
        try:
            body = e.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        return {"status": e.code, "body": body, "content_type": e.headers.get("Content-Type", "")}, None
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"

def must_get_helloworld():
    url = join(BASE, "/helloworld")
    vprint(f"GET {url}")
    resp, err = http("GET", url)
    if err:
        pytest.fail(f"[FAIL] GET /helloworld -> {err}")
    if resp["status"] != 200:
        pytest.fail(f"[FAIL] GET /helloworld -> HTTP {resp['status']}")
    ctype = resp["content_type"].split(";")[0].strip().lower()
    if ctype != "text/html":
        pytest.fail(f"[FAIL] GET /helloworld -> unexpected Content-Type {resp['content_type']!r}")
    print("[PASS] GET /helloworld -> 200 text/html")


def test_must_get_helloworld():
    must_get_helloworld()


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
