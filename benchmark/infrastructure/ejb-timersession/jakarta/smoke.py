#!/usr/bin/env python3
"""Smoke test for taskcreator-quarkus REST API"""

import argparse
import os
import sys
import time
from datetime import datetime
from playwright.sync_api import sync_playwright


DEFAULT_BASE = "http://localhost:9080"
# Try both possible base URLs if not set
BASE_CANDIDATES = [
    os.getenv("SERVICE_BASE_URL"),
    DEFAULT_BASE,
]
DEFAULT_ENDPOINT = "/timersession"


def pick_base_url() -> str:
    for base in BASE_CANDIDATES:
        if not base:
            continue
        print(f"---[ {datetime.now().strftime('%H:%M:%S')} - Smoke test ]---")
    # fallback to first candidate (even if failed)
    return BASE_CANDIDATES[1]


def main() -> int:
    base_url = pick_base_url()
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()
        page.goto(base_url + DEFAULT_ENDPOINT)
        num_tests = 0
        passed_tests = 0
        # Ensure that the page loads successfully
        if "The last programmatic timeout was: never." in page.content():
            print("[PASS] Page loaded successfully and contains expected text.")
            passed_tests += 1
        else:
            print("[FAIL] Page did not contain expected text.", file=sys.stderr)

        num_tests += 1
        # Trigger programmatic timer
        page.get_by_role("button", name="Set Timer").click()
        page.wait_for_selector("text=Timer page")
        if "Timer page" in page.content():
            print("[PASS] Timer submitted.")
            passed_tests += 1
        else:
            print("[FAIL] Timer could not be submitted", file=sys.stderr)

        print("[INFO] wait for 60 seconds while timers trigger.")
        time.sleep(60)

        num_tests += 1
        # refresh page
        page.get_by_role("button", name="Refresh").click()
        page.wait_for_selector("text=Timer page")
        if "The last programmatic timeout was: never." not in page.content():
            print("[PASS] Programmatic timer triggered.")
            passed_tests += 1
        else:
            print("[FAIL] Programmatic timer did no trigger.", file=sys.stderr)

        num_tests += 1
        if "The last automatic timeout was: never" not in page.content():
            print("[PASS] Automatic timer triggered.")
            passed_tests += 1
        else:
            print("[FAIL] Automatic timer did no trigger.", file=sys.stderr)
        num_tests += 1

        print(f"Summary: {passed_tests}/{num_tests} tests passed.")
        print(f"---[ {datetime.now().strftime('%H:%M:%S')} - Smoke test complete ]---")
        return 0


if __name__ == "__main__":  # pragma: no cover
    sys.exit(main())
