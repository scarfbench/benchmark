#!/usr/bin/env python3
"""
Smoke test for Jakarta REST "Hello" app.

Checks:
  1) GET <BASE>/helloworld -> 200 and Content-Type text/html

Environment:
  HELLO_BASE   Base HTTP URL (default: http://localhost:9080/jaxrs-hello-10-SNAPSHOT)
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

BASE = os.getenv("HELLO_BASE", "http://localhost:9080/jaxrs-hello-10-SNAPSHOT").rstrip("/")
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
            # Try to capture content-type (may include charset)
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
    """GET /helloworld should return 200 with text/html."""
    must_get_helloworld()


def test_helloworld_body_content():
    """Response body should contain 'Hello, World!!'."""
    url = join(BASE, "/helloworld")
    resp, err = http("GET", url)
    assert err is None, f"GET /helloworld -> {err}"
    assert resp["status"] == 200, f"GET /helloworld -> HTTP {resp['status']}"
    assert "Hello" in resp["body"], f"Expected 'Hello' in body, got: {resp['body'][:100]}"


def test_response_contains_html_document():
    """Scenario: Response contains a complete HTML document."""
    url = join(BASE, "/helloworld")
    resp, err = http("GET", url)
    assert err is None, f"GET /helloworld -> {err}"
    assert resp["status"] == 200
    body = resp["body"]
    assert "<html" in body, "Expected <html in body"
    assert "<body>" in body or "<body " in body, "Expected <body> in body"
    assert "</body>" in body, "Expected </body> in body"
    assert "</html>" in body, "Expected </html> in body"
    print("[PASS] Response contains complete HTML document")


def test_response_h1_heading():
    """Scenario: Response contains an H1 heading with the greeting."""
    url = join(BASE, "/helloworld")
    resp, err = http("GET", url)
    assert err is None, f"GET /helloworld -> {err}"
    assert resp["status"] == 200
    assert "<h1>" in resp["body"].lower() or "<h1 " in resp["body"].lower(), \
        "Expected H1 heading in response"
    print("[PASS] Response contains H1 heading")


def test_response_not_empty():
    """Scenario: Response body is not empty."""
    url = join(BASE, "/helloworld")
    resp, err = http("GET", url)
    assert err is None, f"GET /helloworld -> {err}"
    assert resp["status"] == 200
    assert resp["body"].strip(), "Expected non-empty response body"
    print("[PASS] Response body is not empty")


def test_put_method_supported():
    """Scenario: PUT method is supported."""
    url = join(BASE, "/helloworld")
    resp, err = http("PUT", url, headers={"Content-Type": "text/html"})
    assert err is None, f"PUT /helloworld -> {err}"
    assert resp["status"] in [200, 204], \
        f"Expected 200 or 204 for PUT, got {resp['status']}"
    print(f"[PASS] PUT /helloworld -> {resp['status']}")


def test_no_error_messages_in_response():
    """Scenario: Response does not contain error messages."""
    url = join(BASE, "/helloworld")
    resp, err = http("GET", url)
    assert err is None, f"GET /helloworld -> {err}"
    assert resp["status"] == 200
    assert "Exception" not in resp["body"], "Response should not contain 'Exception'"
    assert "Error" not in resp["body"], "Response should not contain 'Error'"
    print("[PASS] No error messages in response")


def test_html_lang_attribute():
    """Scenario: HTML includes lang attribute."""
    url = join(BASE, "/helloworld")
    resp, err = http("GET", url)
    assert err is None, f"GET /helloworld -> {err}"
    assert resp["status"] == 200
    assert 'lang="en"' in resp["body"] or "lang='en'" in resp["body"], \
        f"Expected lang=\"en\" in HTML, got: {resp['body'][:200]}"
    print("[PASS] HTML includes lang attribute")


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
