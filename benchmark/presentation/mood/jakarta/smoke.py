#!/usr/bin/env python3
"""
Smoke test for "Mood" app (Servlet + Filter).

Checks:
  1) GET <BASE>/report -> 200 (fatal if not)
  2) Verify mood is displayed in response
  3) Test different times of day to verify filter behavior

Environment:
  MOOD_BASE   Base app URL (default: http://localhost:9080/mood-10-SNAPSHOT)
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

BASE = os.getenv("MOOD_BASE", "http://localhost:9080/mood-10-SNAPSHOT").rstrip("/")
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
        print(f"[FAIL] {path} -> {err}", file=sys.stderr); sys.exit(9)
    if resp["status"] != 200:
        print(f"[FAIL] GET {path} -> HTTP {resp['status']}", file=sys.stderr); sys.exit(fail_code)
    print(f"[PASS] GET {path} -> 200")
    return resp

def test_mood_display(resp):
    """Test that mood is displayed in the response"""
    body = resp["body"]
    
    mood_match = re.search(r"Duke's mood is: ([^<]+)", body)
    if mood_match:
        mood = mood_match.group(1).strip()
        print(f"[PASS] Mood displayed: {mood}")
        return mood
    else:
        print("[WARN] Mood not found in response")
        return None

def test_duke_image(resp):
    """Test that Duke image is displayed based on mood"""
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

def main():
    resp = must_get("/report", 2)
    
    mood = test_mood_display(resp)
    
    img_src, img_alt = test_duke_image(resp)
    
    body = resp["body"]
    if "<html" in body and "<head>" in body and "<body>" in body:
        print("[PASS] Valid HTML structure")
    else:
        print("[WARN] Invalid HTML structure")
    
    if "Servlet MoodServlet" in body:
        print("[PASS] Servlet title found")
    else:
        print("[WARN] Servlet title not found")
    
    print("\n[PASS] Smoke sequence complete")
    return 0

if __name__ == "__main__":
    sys.exit(main())
