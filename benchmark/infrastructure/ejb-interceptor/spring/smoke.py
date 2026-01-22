#!/usr/bin/env python3
"""Smoke test for taskcreator-quarkus REST API"""

import argparse
import os
import sys
from datetime import datetime
from playwright.sync_api import sync_playwright


# Try both possible base URLs if not set
BASE_CANDIDATES = [
    os.getenv("TASKCREATOR_BASE_URL"),
    "http://localhost:8080",  # <-- default
]
DEFAULT_ENDPOINT = "/"
DEFAULT_BASE = "http://localhost:8080"


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
        if "Enter your name:" in page.content():
            print("[PASS] Page loaded successfully and contains expected text.")
            passed_tests += 1
        else:
            print("[FAIL] Page did not contain expected text.", file=sys.stderr)

        num_tests += 1
        # Fill a name in all caps in the input field
        page.get_by_role("textbox", name="Enter your name:").fill("TEST USER")
        page.get_by_role("button", name="Submit").click()

        # Assert the lowercased greeting is displayed
        page.wait_for_selector("text=Hello, test user.")
        if "Hello, test user." in page.content():
            print("[PASS] Greeting displayed correctly.")
            passed_tests += 1
        else:
            print("[FAIL] Greeting not displayed as expected.", file=sys.stderr)

        num_tests += 1

        # Hit the back button and ensure we are back on the form
        page.go_back()
        if "Enter your name:" in page.content():
            print("[PASS] Back navigation successful.")
            passed_tests += 1
        else:
            print("[FAIL] Back navigation failed.", file=sys.stderr)
        num_tests += 1

        print(f"Summary: {passed_tests}/{num_tests} tests passed.")
        print(f"---[ {datetime.now().strftime('%H:%M:%S')} - Smoke test complete ]---")
        return 0


if __name__ == "__main__":  # pragma: no cover
    sys.exit(main())
