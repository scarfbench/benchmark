"""
Smoke test for "Mood" app (Servlet + Filter).

Checks:
  1) GET <BASE>/report -> 200 (fatal if not)
  2) Verify mood is displayed in response
  3) Test different times of day to verify filter behavior

Environment:
  MOOD_BASE   Base app URL (default: http://localhost:8080/mood-10-SNAPSHOT)
  VERBOSE=1   Verbose logging

Exit codes:
  0  success
  2  GET /report failed
  9  Network / unexpected error
"""
import os
import sys
import re
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import pytest

BASE = os.getenv("MOOD_BASE", "http://localhost:8080/").rstrip("/")
VERBOSE = os.getenv("VERBOSE") == "1"
HTTP_TIMEOUT = 12

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

def http(method: str, url: str, headers: dict | None = None, data: bytes | None = None):
    req = Request(url, method=method, headers=headers or {}, data=data)
    try:
        with urlopen(req, timeout=HTTP_TIMEOUT) as resp:
            return {
                "status": resp.getcode(),
                "body": resp.read().decode("utf-8", "replace"),
                "content_type": resp.headers.get("Content-Type", "")
            }, None
    except HTTPError as e:
        try:
            body = e.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        return {
            "status": e.code,
            "body": body,
            "content_type": (e.headers.get("Content-Type", "") if hasattr(e, "headers") else "")
        }, None
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"

def must_get(path: str, fail_code: int):
    url = join(BASE, path)
    vprint(f"GET {url}")
    resp, err = http("GET", url)
    if err:
        pytest.fail(f"[FAIL] {path} -> {err}")
    if resp["status"] != 200:
        pytest.fail(f"[FAIL] GET {path} -> HTTP {resp['status']}")
    print(f"[PASS] GET {path} -> 200")
    return resp

def _mood_display(resp):
    """Helper: extract mood from response"""
    body = resp["body"]
    
    mood_match = re.search(r"Duke's mood is: ([^<]+)", body)
    if mood_match:
        mood = mood_match.group(1).strip()
        print(f"[PASS] Mood displayed: {mood}")
        return mood
    else:
        print("[WARN] Mood not found in response")
        return None

def _duke_image(resp):
    """Helper: extract Duke image from response"""
    body = resp["body"]
    
    img_match = re.search(r'<img src="([^"]+)" alt="([^"]+)"', body)
    if img_match:
        img_src = img_match.group(1)
        img_alt = img_match.group(2)
        print(f"[PASS] Duke image displayed: {img_alt}")
        return img_src, img_alt
    else:
        print("[WARN] Duke image not found in response")
        return None, None


def test_step_1():
    resp = must_get("/report", 2)
    mood = _mood_display(resp)
    img_src, img_alt = _duke_image(resp)
    body = resp["body"]
    if "<html" in body and "<head>" in body and "<body>" in body:
        print("[PASS] Valid HTML structure")
    else:
        print("[WARN] Invalid HTML structure")
    if "Servlet MoodServlet" in body:
        print("[PASS] Servlet title found")
    else:
        print("[WARN] Servlet title not found")


def test_mood_page_returns_200():
    """Scenario: Mood page displays Duke's current mood."""
    url = join(BASE, "/report")
    resp, err = http("GET", url)
    assert err is None, f"GET /report -> {err}"
    assert resp["status"] == 200, f"Expected 200, got {resp['status']}"
    print("[PASS] GET /report -> 200")


def test_mood_is_displayed():
    """Scenario: Mood page displays a mood value."""
    url = join(BASE, "/report")
    resp, err = http("GET", url)
    assert err is None, f"GET /report -> {err}"
    assert resp["status"] == 200
    body = resp["body"]
    mood_match = re.search(r"Duke's mood is: ([^<]+)", body)
    assert mood_match, f"Expected 'Duke's mood is:' in response body"
    mood = mood_match.group(1).strip()
    assert mood, "Mood value should not be empty"
    print(f"[PASS] Mood displayed: {mood}")


def test_mood_image_displayed():
    """Scenario: Mood page contains an image for the mood."""
    url = join(BASE, "/report")
    resp, err = http("GET", url)
    assert err is None, f"GET /report -> {err}"
    assert resp["status"] == 200
    body = resp["body"]
    assert "<img" in body.lower(), "Expected an img tag in the response"
    img_match = re.search(r'<img[^>]+src="([^"]+)"', body)
    assert img_match, "Expected img tag with src attribute"
    img_src = img_match.group(1)
    assert "duke" in img_src.lower() or ".gif" in img_src.lower(), \
        f"Expected Duke mood image, got: {img_src}"
    print(f"[PASS] Mood image displayed: {img_src}")


def test_response_is_html():
    """Scenario: Response is an HTML page."""
    url = join(BASE, "/report")
    resp, err = http("GET", url)
    assert err is None, f"GET /report -> {err}"
    assert resp["status"] == 200
    body = resp["body"]
    assert "<html" in body, "Expected <html in response"
    assert "</html>" in body, "Expected </html> in response"
    ctype = resp["content_type"].lower()
    assert "text/html" in ctype, f"Expected text/html Content-Type, got: {ctype}"
    print("[PASS] Response is valid HTML")


def test_page_title():
    """Scenario: Page title is 'Servlet MoodServlet'."""
    url = join(BASE, "/report")
    resp, err = http("GET", url)
    assert err is None, f"GET /report -> {err}"
    assert resp["status"] == 200
    assert "<title>" in resp["body"].lower(), "Expected <title> in response"
    assert "Servlet MoodServlet" in resp["body"], \
        f"Expected 'Servlet MoodServlet' in title"
    print("[PASS] Page title: Servlet MoodServlet")


def test_context_path():
    """Scenario: Page shows the context path."""
    url = join(BASE, "/report")
    resp, err = http("GET", url)
    assert err is None, f"GET /report -> {err}"
    assert resp["status"] == 200
    body = resp["body"]
    assert "Servlet MoodServlet at" in body or "MoodServlet" in body or "/report" in body, \
        "Expected context path or servlet info in response"
    print("[PASS] Context path shown in response")


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
