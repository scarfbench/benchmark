"""
Smoke tests for Hello Servlet application.

Covers all scenarios from hello-servlet.feature:
  - Core greeting with various names (Duke, Alice, World, etc.)
  - Missing/blank/whitespace name returns 400
  - Response content type is text/plain
  - Exact response body format
  - Edge cases: spaces, special characters, long names

Environment:
  HELLO_BASE   Base URL (default: http://localhost:8080/)
  VERBOSE=1    Enable verbose logging
"""

import os
import sys
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
from urllib.parse import urlencode, quote
import pytest

BASE = os.getenv("HELLO_BASE", "http://localhost:8080/").rstrip("/")
VERBOSE = os.getenv("VERBOSE") == "1"


def vprint(*args):
    if VERBOSE:
        print(*args)


def http_request(method: str, url: str, timeout: int = 10):
    """Returns ((status, body, content_type), None) or (None, error_str)."""
    req = Request(url, method=method)
    try:
        with urlopen(req, timeout=timeout) as resp:
            status = resp.getcode()
            body = resp.read().decode("utf-8", "replace")
            ct = resp.headers.get("Content-Type", "")
            return (status, body, ct), None
    except HTTPError as e:
        try:
            body = e.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        ct = e.headers.get("Content-Type", "") if e.headers else ""
        return (e.code, body, ct), None
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"


def _get_greeting(name: str):
    """GET /greeting?name=... and return (status, body, content_type)."""
    url = f"{BASE}/greeting?{urlencode({'name': name})}"
    vprint(f"GET {url}")
    resp, err = http_request("GET", url)
    if err:
        pytest.fail(f"[FAIL] {url} -> {err}")
    return resp


def _get_greeting_raw(query_string: str = ""):
    """GET /greeting with raw query string."""
    url = f"{BASE}/greeting"
    if query_string:
        url += "?" + query_string
    vprint(f"GET {url}")
    resp, err = http_request("GET", url)
    if err:
        pytest.fail(f"[FAIL] {url} -> {err}")
    return resp


# ===================================================================
# Core greeting functionality
# ===================================================================

def test_greet_duke():
    """Scenario: Greet a user by name."""
    status, body, _ = _get_greeting("Duke")
    assert status == 200, f"Expected 200, got {status}"
    assert body.strip() == "Hello, Duke!", f"Expected 'Hello, Duke!', got: {body.strip()!r}"
    print(f"[PASS] Greet Duke -> {body.strip()}")


def test_greet_alice():
    """Scenario: Greet another user by name."""
    status, body, _ = _get_greeting("Alice")
    assert status == 200, f"Expected 200, got {status}"
    assert body.strip() == "Hello, Alice!", f"Expected 'Hello, Alice!', got: {body.strip()!r}"
    print(f"[PASS] Greet Alice -> {body.strip()}")


def test_greet_world():
    """Scenario: Greet with name 'World'."""
    status, body, _ = _get_greeting("World")
    assert status == 200, f"Expected 200, got {status}"
    assert body.strip() == "Hello, World!", f"Expected 'Hello, World!', got: {body.strip()!r}"
    print(f"[PASS] Greet World -> {body.strip()}")


@pytest.mark.parametrize("name", ["Duke", "Alice", "Bob", "Charlie"])
def test_various_names(name):
    """Scenario Outline: Various names produce correct greetings."""
    status, body, _ = _get_greeting(name)
    assert status == 200, f"Expected 200 for {name}, got {status}"
    assert body.strip() == f"Hello, {name}!", f"Expected 'Hello, {name}!', got: {body.strip()!r}"
    print(f"[PASS] Greet {name} -> {body.strip()}")


# ===================================================================
# Missing name parameter
# ===================================================================

def test_missing_name_returns_400():
    """Scenario: Missing name parameter returns 400."""
    status, body, _ = _get_greeting_raw()
    assert status == 400, f"Expected 400 for missing name, got {status}"
    assert "Missing required parameter: name" in body, f"Expected error message in body, got: {body}"
    print(f"[PASS] Missing name -> {status}")


def test_blank_name_returns_400():
    """Scenario: Blank name parameter returns 400."""
    status, body, _ = _get_greeting_raw("name=")
    assert status == 400, f"Expected 400 for blank name, got {status}"
    assert "Missing required parameter: name" in body, f"Expected error message in body, got: {body}"
    print(f"[PASS] Blank name -> {status}")


def test_whitespace_name_returns_400():
    """Scenario: Whitespace-only name parameter returns 400."""
    status, body, _ = _get_greeting_raw("name=%20%20")
    assert status == 400, f"Expected 400 for whitespace name, got {status}"
    assert "Missing required parameter: name" in body, f"Expected error message in body, got: {body}"
    print(f"[PASS] Whitespace name -> {status}")


# ===================================================================
# Response format
# ===================================================================

def test_response_content_type_text_plain():
    """Scenario: Response content type is text/plain."""
    status, body, ct = _get_greeting("Duke")
    assert status == 200
    assert "text/plain" in ct.lower(), f"Expected text/plain Content-Type, got: {ct}"
    print(f"[PASS] Content-Type: {ct}")


def test_response_body_exact_greeting():
    """Scenario: Response body contains only the greeting text."""
    status, body, _ = _get_greeting("Duke")
    assert status == 200
    assert body.strip() == "Hello, Duke!", f"Expected exactly 'Hello, Duke!', got: {body.strip()!r}"
    print(f"[PASS] Exact body: {body.strip()!r}")


# ===================================================================
# Edge cases
# ===================================================================

def test_name_with_spaces():
    """Scenario: Name with spaces is handled correctly."""
    status, body, _ = _get_greeting("Mary Jane")
    assert status == 200, f"Expected 200, got {status}"
    assert body.strip() == "Hello, Mary Jane!", f"Expected 'Hello, Mary Jane!', got: {body.strip()!r}"
    print(f"[PASS] Name with spaces -> {body.strip()}")


def test_name_with_special_characters():
    """Scenario: Name with special characters."""
    status, body, _ = _get_greeting("O'Brien")
    assert status == 200, f"Expected 200, got {status}"
    assert "Hello, O'Brien!" in body, f"Expected 'Hello, O'Brien!' in body, got: {body}"
    print(f"[PASS] Special chars -> {body.strip()}")


def test_long_name():
    """Scenario: Long name is handled correctly."""
    status, body, _ = _get_greeting("Bartholomew")
    assert status == 200, f"Expected 200, got {status}"
    assert body.strip() == "Hello, Bartholomew!", f"Expected 'Hello, Bartholomew!', got: {body.strip()!r}"
    print(f"[PASS] Long name -> {body.strip()}")


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
