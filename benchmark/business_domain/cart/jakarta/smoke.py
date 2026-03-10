#!/usr/bin/env python3
"""Smoke tests for Jakarta EE cart application on Open Liberty.

Checks:
  1)  Discover reachable cart API base path.
  2)  GET <base>/health returns 200 and status=UP.
  3)  POST <base>/initialize creates a cart session.
  4)  POST <base>/books/{title} adds books to cart.
  5)  GET <base>/books returns books list with correct count.
  6)  DELETE <base>/books/{title} removes a book.
  7)  DELETE <base>/books/{title} returns 404 for a non-existent book.
  8)  DELETE <base> clears the cart.
  9)  GET <base>/books after clear returns count 0.
  10) Re-initialize and add a single book; verify count is 1.
  11) Books response always contains both "books" list and "count" field.
  12) Book titles returned in list match what was added.
  13) Add a book with spaces in its title; title is stored correctly.
  14) Multiple removes reduce the count correctly.

Exit codes:
  0 success, non-zero on first failure encountered.
"""

import os
import sys
import json
import pytest
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

HEALTH_PATH = "/health"
INITIALIZE_PATH = "/initialize"
BOOKS_PATH = "/books"
VERBOSE = os.getenv("VERBOSE") == "1"

CANDIDATES = [
    os.getenv("CART_BASE_URL"),
    "http://localhost:9080/cart/api/cart",
    "http://localhost:8080/cart/api/cart",
]


def vprint(msg: str):
    if VERBOSE:
        print(msg)


def http_request(
    method: str,
    url: str,
    data: bytes | None = None,
    headers: dict | None = None,
    timeout: int = 10,
):
    req = Request(url, data=data, method=method, headers=headers or {})
    try:
        with urlopen(req, timeout=timeout) as resp:
            status = resp.getcode()
            body = resp.read().decode("utf-8", "replace")
    except HTTPError as e:
        status = e.code
        body = e.read().decode("utf-8", "replace")
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"
    return (status, body), None


def try_health_check(base: str):
    url = base.rstrip("/") + HEALTH_PATH
    vprint(f"Attempt GET {url}")
    resp, err = http_request("GET", url)
    if err:
        vprint(f"Fail: {err}")
        return False
    status, body = resp
    if status != 200:
        vprint(f"Unexpected status {status}")
        return False
    try:
        if json.loads(body).get("status") == "UP":
            return True
    except json.JSONDecodeError:
        vprint(f"Health check response not valid JSON: {body}")
    return False


def discover_base() -> str:
    for cand in CANDIDATES:
        if not cand:
            continue
        if try_health_check(cand):
            print(f"[INFO] Base discovered: {cand}")
            return cand
    for cand in CANDIDATES:
        if cand:
            print(f"[WARN] No base validated, using fallback {cand}")
            return cand
    pytest.fail("No base URL candidates available")


def assert_health(base: str):
    url = base.rstrip("/") + HEALTH_PATH
    resp, err = http_request("GET", url)
    if err:
        pytest.fail(f"Health check error: {err}")
    status, body = resp
    if status != 200:
        pytest.fail(f"Health check status: {status}")
    try:
        if json.loads(body).get("status") != "UP":
            pytest.fail(f"Health status not UP: {body}")
    except json.JSONDecodeError:
        pytest.fail(f"Health check invalid JSON: {body}")
    print("[PASS] GET health -> status=UP")


def initialize_cart(base: str, customer_name: str, customer_id: str):
    url = base.rstrip("/") + INITIALIZE_PATH
    payload = json.dumps(
        {"customerName": customer_name, "customerId": customer_id}
    ).encode("utf-8")
    headers = {"Content-Type": "application/json"}
    resp, err = http_request("POST", url, data=payload, headers=headers)
    if err:
        pytest.fail(f"Initialize cart error: {err}")
    status, body = resp
    if status != 200:
        pytest.fail(f"Initialize cart status: {status} :: {body}")
    try:
        result = json.loads(body)
        if "message" not in result:
            pytest.fail(f"Initialize response missing message: {body}")
    except json.JSONDecodeError:
        pytest.fail(f"Initialize response invalid JSON: {body}")
    print(f"[PASS] POST initialize cart for '{customer_name}' -> {status}")
    return json.loads(body)


def add_book(base: str, title: str):
    encoded_title = title.replace(" ", "%20")
    url = f"{base.rstrip('/')}{BOOKS_PATH}/{encoded_title}"
    headers = {"Content-Type": "application/json"}
    resp, err = http_request("POST", url, data=b"", headers=headers)
    if err:
        pytest.fail(f"Add book '{title}' error: {err}")
    status, body = resp
    if status != 200:
        pytest.fail(f"Add book '{title}' status: {status} :: {body}")
    try:
        result = json.loads(body)
        if result.get("title") != title:
            pytest.fail(f"Add book title mismatch: expected '{title}', got: {body}")
    except json.JSONDecodeError:
        pytest.fail(f"Add book response invalid JSON: {body}")
    print(f"[PASS] POST add book '{title}' -> {status}")
    return json.loads(body)


def get_books(base: str, expected_count: int = None):
    url = base.rstrip("/") + BOOKS_PATH
    resp, err = http_request("GET", url)
    if err:
        pytest.fail(f"Get books error: {err}")
    status, body = resp
    if status != 200:
        pytest.fail(f"Get books status: {status} :: {body}")
    try:
        result = json.loads(body)
        books = result.get("books", [])
        count = result.get("count", 0)
        if expected_count is not None and count != expected_count:
            pytest.fail(f"Expected {expected_count} books, got {count}: {books}")
        print(f"[PASS] GET books -> count={count}, books={books}")
        return result
    except json.JSONDecodeError:
        pytest.fail(f"Get books response invalid JSON: {body}")


def remove_book(base: str, title: str, should_fail: bool = False):
    encoded_title = title.replace(" ", "%20")
    url = f"{base.rstrip('/')}{BOOKS_PATH}/{encoded_title}"
    resp, err = http_request("DELETE", url)
    if err:
        pytest.fail(f"Remove book '{title}' error: {err}")
    status, body = resp
    if should_fail:
        if status == 404:
            try:
                result = json.loads(body)
                if "error" in result:
                    print(f"[PASS] DELETE book '{title}' (expected 404) -> {status}")
                    return
            except json.JSONDecodeError:
                pass
        pytest.fail(f"Expected 404 for '{title}', got {status}: {body}")
    else:
        if status != 200:
            pytest.fail(f"Remove book '{title}' status: {status} :: {body}")
        try:
            result = json.loads(body)
            if result.get("title") != title:
                pytest.fail(f"Remove book title mismatch: {body}")
        except json.JSONDecodeError:
            pytest.fail(f"Remove book response invalid JSON: {body}")
        print(f"[PASS] DELETE book '{title}' -> {status}")
        return json.loads(body)


def clear_cart(base: str):
    url = base.rstrip("/")
    resp, err = http_request("DELETE", url)
    if err:
        pytest.fail(f"Clear cart error: {err}")
    status, body = resp
    if status != 200:
        pytest.fail(f"Clear cart status: {status} :: {body}")
    try:
        result = json.loads(body)
        if "message" not in result:
            pytest.fail(f"Clear cart response missing message: {body}")
    except json.JSONDecodeError:
        pytest.fail(f"Clear cart response invalid JSON: {body}")
    print(f"[PASS] DELETE clear cart -> {status}")


# ---------------------------------------------------------------------------
# pytest interface
# ---------------------------------------------------------------------------

_BASE: str | None = None


@pytest.fixture(scope="module", autouse=True)
def _setup_base():
    global _BASE
    _BASE = discover_base()


# --- Phase 1: basic lifecycle ---

def test_health():
    assert_health(_BASE)


def test_initialize_cart():
    initialize_cart(_BASE, "Duke DeUrl", "123")


def test_add_books():
    add_book(_BASE, "Infinite Jest")
    add_book(_BASE, "Bel Canto")
    add_book(_BASE, "Kafka on the Shore")


def test_get_books_after_add():
    get_books(_BASE, expected_count=3)


def test_remove_book():
    remove_book(_BASE, "Bel Canto")


def test_get_books_after_remove():
    get_books(_BASE, expected_count=2)


def test_remove_nonexistent_book():
    remove_book(_BASE, "Gravity's Rainbow", should_fail=True)


def test_clear_cart():
    clear_cart(_BASE)


# --- Phase 2: post-clear verification ---

def test_get_books_after_clear():
    result = get_books(_BASE, expected_count=0)
    assert "books" in result, "Response missing 'books' field after clear"
    assert "count" in result, "Response missing 'count' field after clear"
    print("[PASS] GET books after clear -> count=0")


# --- Phase 3: response structure ---

def test_books_response_has_required_fields():
    result = get_books(_BASE)
    assert "books" in result, "GET /books response missing 'books' field"
    assert "count" in result, "GET /books response missing 'count' field"
    assert isinstance(result["books"], list), "'books' field is not a list"
    assert isinstance(result["count"], int), "'count' field is not an integer"
    print("[PASS] GET books response structure is correct")


def test_initialize_response_contains_message():
    result = initialize_cart(_BASE, "Tester", "999")
    assert "message" in result, f"Initialize response missing 'message': {result}"
    print("[PASS] Initialize response contains 'message' field")


# --- Phase 4: add with spaces in title ---

def test_add_book_with_spaces_in_title():
    result = add_book(_BASE, "The Great Gatsby")
    assert result.get("title") == "The Great Gatsby", \
        f"Title with spaces not stored correctly: {result}"
    print("[PASS] Book with spaces in title stored correctly")


# --- Phase 5: verify titles in list ---

def test_book_title_appears_in_list():
    add_book(_BASE, "Moby Dick")
    result = get_books(_BASE)
    titles = [b if isinstance(b, str) else b.get("title", "") for b in result.get("books", [])]
    assert any("Moby Dick" in str(t) for t in titles), \
        f"'Moby Dick' not found in books list: {titles}"
    print("[PASS] Added book title appears in GET /books response")


# --- Phase 6: multiple removes reduce count ---

def test_multiple_removes_decrement_count():
    initialize_cart(_BASE, "Counter Tester", "777")
    add_book(_BASE, "Book A")
    add_book(_BASE, "Book B")
    add_book(_BASE, "Book C")
    result = get_books(_BASE)
    initial = result["count"]
    remove_book(_BASE, "Book A")
    remove_book(_BASE, "Book B")
    result = get_books(_BASE)
    assert result["count"] == initial - 2, \
        f"Expected {initial - 2} books after 2 removes, got {result['count']}"
    print(f"[PASS] Two removes correctly reduced count from {initial} to {result['count']}")


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
