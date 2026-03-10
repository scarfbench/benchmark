#!/usr/bin/env python3
"""Smoke test for guessnumber-spring

Checks:
  1) Visit and validate contents of Base Page
  2) Fill form and guess a number
  3) Trigger a validation error
  4) Reset the guessing game

Exit codes:
  0 success
  1 failure

Since we have no control over the random number that's
generated, the test might be flaky.
"""

import os
import sys
import pytest
from playwright.sync_api import Page, sync_playwright


DEFAULT_BASE = "http://localhost:8080"
BASE_URL = os.getenv("GUESS_NUMBER_BASE_URL", DEFAULT_BASE)
DEFAULT_ENDPOINT = "/guessnumber"
HOME_URI = os.getenv("GUESS_NUMBER_HOME_URI", DEFAULT_ENDPOINT)


def visit_main_page(page: Page) -> int:
    passed = 0
    page.goto(BASE_URL + HOME_URI)
    # Ensure that the page loads successfully
    if "Guess My Number" in page.content():
        print("[PASS] Page loaded successfully and contains expected text.")
        passed = 1
    else:
        print("[FAIL] Page did not contain expected text.", file=sys.stderr)

    return passed


def guess(page: Page, number: int) -> int:
    passed = 0

    # Guess and hope it's not the selected number
    page.get_by_label("Number:").fill(f"{number}")
    with page.expect_navigation():
        page.get_by_role("button", name="Guess").click()

    # Assert we got 9 guesses now, the HTML is annoying
    if ">9<" in page.content():
        print("[PASS] Number of remaining guesses displayed correctly.")
        passed += 1
    else:
        print(
            "[FAIL] Number of remaining guesses not displayed as expected.",
            file=sys.stderr,
        )

    return passed


def trigger_validation_error(page: Page, number: int) -> int:
    passed = 0
    page.get_by_label("Number:").fill(f"{number}")
    with page.expect_navigation():
        page.get_by_role("button", name="Guess").click()

    # Assert we have an error on page
    number = page.get_by_label("Number:").input_value()
    if "Invalid guess" in page.content() and "1" == number:
        print("[PASS] Error displayed correctly.")
        passed = 1
    else:
        print("[FAIL] Error not displayed as expected.", file=sys.stderr)

    return passed


def reset(page: Page) -> int:
    passed = 0
    with page.expect_navigation():
        page.get_by_role("button", name="Reset").click()

    # should have 10 guesses
    if ">10<" in page.content():
        print("[PASS] Reset successful.")
        passed = 1
    else:
        print("[FAIL] Reset failed.", file=sys.stderr)

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


def test_guess(page):
    # Guess 1 and hope it's not the selected number
    assert guess(page=page, number=1) == 1


def test_trigger_validation_error(page):
    # Try number out of range, since we selected 1 before let's do it again
    assert trigger_validation_error(page=page, number=1) == 1


def test_reset(page):
    assert reset(page) == 1


def main() -> int:
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
