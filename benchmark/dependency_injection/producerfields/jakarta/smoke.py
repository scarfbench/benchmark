#!/usr/bin/env python3
"""Smoke test for producerfields-jakarta

Checks:
  1) Visit and validate contents of Base Page
  2) Add a new Todo
  3) Display registered Todos
  4) Navigate back to main page

Exit codes:
  0 success
  1 failure
"""

import os
import sys
from datetime import datetime

from playwright.sync_api import Page, sync_playwright

DEFAULT_BASE = "http://localhost:9080"
BASE_URL = os.getenv("PRODUCER_FIELDS_BASE_URL", DEFAULT_BASE)
DEFAULT_ENDPOINT = "/producerfields"
HOME_URI = os.getenv("PRODUCER_FIELDS_HOME_URI", DEFAULT_ENDPOINT)


def visit_main_page(page: Page) -> int:
    passed = 0
    page.goto(BASE_URL + HOME_URI)
    # Ensure that the page loads successfully
    if "Create To Do List" in page.content():
        print("[PASS] Page loaded successfully and contains expected text.")
        passed = 1
    else:
        print("[FAIL] Page did not contain expected text.", file=sys.stderr)

    return passed


def add_todo(page: Page) -> int:
    passed = 0

    page.get_by_label("Enter a string:").fill("Smoke Test")
    with page.expect_navigation():
        page.get_by_role("button", name="Submit").click()

    todo = page.get_by_label("Enter a string:").input_value()
    # Assert we're still on the same page
    if "Create To Do List" in page.content() and todo == "Smoke Test":
        print("[PASS] Page displayed correctly after submit.")
        passed = 1
    else:
        print("[FAIL] Page not displayed as expected after submit.", file=sys.stderr)

    return passed


def display_todos(page: Page) -> int:
    passed = 0
    with page.expect_navigation():
        page.get_by_role("button", name="Show Items").click()

    # Assert page content contains previously added todo
    if "To Do List" in page.content() and "Smoke Test" in page.content():
        print("[PASS] Todo list page displayed correctly.")
        passed = 1
    else:
        print("[FAIL] Todo list page not displayed as expected.", file=sys.stderr)

    return passed


def back(page: Page) -> int:
    passed = 0
    with page.expect_navigation():
        page.get_by_role("button", name="Back").click()

    # should be main page
    if "Create To Do List" in page.content():
        print("[PASS] Back successful.")
        passed = 1
    else:
        print("[FAIL] Back failed.", file=sys.stderr)

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
        # Add a new to do item
        passed_tests += add_todo(page)

        num_tests += 1
        # Show the list of registered todos
        passed_tests += display_todos(page)

        num_tests += 1
        # Go back to main page
        passed_tests += back(page)

        print(f"Summary: {passed_tests}/{num_tests} tests passed.")
        print(f"---[ {datetime.now().strftime('%H:%M:%S')} - Smoke test complete ]---")
        return 0 if num_tests == passed_tests else 1


if __name__ == "__main__":
    sys.exit(main())
