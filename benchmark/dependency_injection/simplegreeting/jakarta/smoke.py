#!/usr/bin/env python3
"""Smoke test for simplegreeting-jakarta

Checks:
  1) Visit and validate contents of Base Page
  2) Test greeting action

Exit codes:
  0 success
  1 failure
"""

import os
import sys
import pytest
from playwright.sync_api import Page, sync_playwright


DEFAULT_BASE = "http://localhost:9080"
BASE_URL = os.getenv("SIMPLE_GREETING_BASE_URL", DEFAULT_BASE)
DEFAULT_ENDPOINT = "/simplegreeting"
HOME_URI = os.getenv("SIMPLE_GREETING_HOME_URI", DEFAULT_ENDPOINT)


def visit_main_page(page: Page) -> int:
    passed = 0
    page.goto(BASE_URL + HOME_URI)
    # Ensure that the page loads successfully
    if "Simple Greeting" in page.content():
        print("[PASS] Page loaded successfully and contains expected text.")
        passed = 1
    else:
        print("[FAIL] Page did not contain expected text.", file=sys.stderr)

    return passed


def greet(page: Page) -> int:
    passed = 0

    page.get_by_label("Enter your name:").fill("John")
    with page.expect_navigation():
        page.get_by_role("button", name="Say Hello").click()

    # Assert we got the correct greeting
    if "Hi, John!" in page.content():
        print("[PASS] Greeting displayed correctly.")
        passed = 1
    else:
        print(
            "[FAIL] Greeting not displayed as expected.",
            file=sys.stderr,
        )

    return passed


@pytest.fixture(scope="module")
def page():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        pg = browser.new_page()
        yield pg
        browser.close()


def test_visit_main_page(page):
    assert visit_main_page(page) == 1


def test_greet(page):
    assert greet(page) == 1


def main() -> int:
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
