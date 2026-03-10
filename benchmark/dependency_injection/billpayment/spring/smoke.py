#!/usr/bin/env python3
"""Smoke test for billpayment-spring

Checks:
  1) Visit and validate contents of Base Page
  2) Pay using the Debit card option
  3) Go back to main page
  4) Pay using the Credit card option
  5) Go back to main page
  6) Reset payment form

Exit codes:
  0 success
  1 failure
"""

import os
import sys
import pytest
from playwright.sync_api import Page, sync_playwright


DEFAULT_BASE = "http://localhost:8080"
BASE_URL = os.getenv("BILLPAYMENT_BASE_URL", DEFAULT_BASE)
DEFAULT_ENDPOINT = "/"
HOME_URI = os.getenv("BILLPAYMENT_HOME_URI", DEFAULT_ENDPOINT)


def visit_main_page(page: Page) -> int:
    passed = 0
    page.goto(BASE_URL + HOME_URI)
    # Ensure that the page loads successfully
    if "Bill Payment Options" in page.content():
        print("[PASS] Page loaded successfully and contains expected text.")
        passed = 1
    else:
        print("[FAIL] Page did not contain expected text.", file=sys.stderr)

    return passed


def pay(page: Page, amount: int, card_type: str) -> int:
    passed = 0

    # Fill the amount input and pay
    page.get_by_label("Amount: $").fill(f"{amount}")
    page.get_by_label(f"{card_type} Card").check()
    with page.expect_navigation():
        page.get_by_role("button", name="Pay").click()

    # Assert we're on result page
    page_content = page.content().lower()
    if all(
        elem.lower() in page_content
        for elem in ["Bill Payment: Result", card_type.upper(), f"{amount}.00"]
    ):
        print(f"[PASS] {card_type} payment displayed correctly.")
        passed = 1
    else:
        print(f"[FAIL] {card_type} payment not displayed as expected.", file=sys.stderr)

    return passed


def back(page: Page) -> int:
    passed = 0
    # Hit the back button and ensure we are back on the form
    with page.expect_navigation():
        page.get_by_role("button", name="Back").click()
    if "Bill Payment Options" in page.content():
        print("[PASS] Back navigation successful.")
        passed = 1
    else:
        print("[FAIL] Back navigation failed.", file=sys.stderr)

    return passed


def reset(page: Page) -> int:
    passed = 0
    page.get_by_label("Amount: $").fill("12")
    with page.expect_navigation():
        page.get_by_role("button", name="Reset").click()

    if "0" == page.get_by_label("Amount: $").input_value():
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


def test_pay_debit(page):
    assert pay(page=page, amount=12, card_type="Debit") == 1


def test_back_after_debit(page):
    assert back(page) == 1


def test_pay_credit(page):
    assert pay(page=page, amount=5, card_type="Credit") == 1


def test_back_after_credit(page):
    assert back(page) == 1


def test_reset(page):
    assert reset(page) == 1


def main() -> int:
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
