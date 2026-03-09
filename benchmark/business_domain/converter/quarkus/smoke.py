#!/usr/bin/env python3
"""Smoke test for converter-jakarta

Checks:
  1) Visit and validate contents of Base Page
  2) Fill amount of money and convert

Exit codes:
  0 success
  1 failure
"""

import os
import sys
from datetime import datetime

from playwright.sync_api import Page, sync_playwright

DEFAULT_BASE = "http://localhost:8080"
BASE_URL = os.getenv("CONVERTER_BASE_URL", DEFAULT_BASE)
DEFAULT_ENDPOINT = "/converter"
HOME_URI = os.getenv("CONVERTER_HOME_URI", DEFAULT_ENDPOINT)


def visit_main_page(page: Page) -> int:
    passed = 0
    page.goto(BASE_URL + HOME_URI)
    # Ensure that the page loads successfully
    if "Enter a dollar amount to convert:" in page.content():
        print("[PASS] Page loaded successfully and contains expected text.")
        passed = 1
    else:
        print("[FAIL] Page did not contain expected text.", file=sys.stderr)

    return passed


def convert(page: Page, amount: int) -> int:
    passed = 0

    # Fill the amount input and convert
    page.get_by_title("Amount").fill(f"{amount}")
    with page.expect_navigation():
        page.get_by_role("button", name="Submit").click()

    # Assert we're on result page
    page_content = page.content()
    if all(
        elem in page_content
        for elem in ["5 dollars are 521.70 yen.", "521.70 yen are 3.66 Euro."]
    ):
        print("[PASS] Convertion displayed correctly.")
        passed = 1
    else:
        print("[FAIL] Convertion not displayed as expected.", file=sys.stderr)

    return passed


def main() -> int:
    print(f"---[ {datetime.now().strftime('%H:%M:%S')} - Smoke test ]---")
    with sync_playwright() as p:
        num_tests = 0
        passed_tests = 0
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()

        num_tests += 1
        passed_tests += visit_main_page(page)

        num_tests += 1
        # Fill the amount input and convert
        passed_tests += convert(page=page, amount=5)

        print(f"Summary: {passed_tests}/{num_tests} tests passed.")
        print(f"---[ {datetime.now().strftime('%H:%M:%S')} - Smoke test complete ]---")
        return 0 if num_tests == passed_tests else 1


if __name__ == "__main__":
    sys.exit(main())
