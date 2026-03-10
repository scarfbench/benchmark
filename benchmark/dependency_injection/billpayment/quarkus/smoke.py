#!/usr/bin/env python3
"""Smoke test for billpayment-quarkus

Checks:
  1) Visit and validate contents of Base Page
  2) Verify form has debit and credit payment options
  3) Verify default payment option is Debit
  4) Pay using the Debit card option
  5) Verify result page structure
  6) Go back to main page
  7) Pay using the Credit card option
  8) Go back to main page
  9) Pay with a decimal amount
  10) Go back to main page
  11) Reset payment form and verify defaults restored

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
DEFAULT_ENDPOINT = "/billpayment"
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


def pay(page: Page, amount: str, card_type: str) -> int:
    passed = 0

    # Fill the amount input and pay
    page.get_by_label("Amount: $").fill(amount)
    page.get_by_label(f"{card_type} Card").check()
    with page.expect_navigation():
        page.get_by_role("button", name="Pay").click()

    # Assert we're on result page
    page_content = page.content().lower()
    if all(
        elem.lower() in page_content
        for elem in ["Bill Payment: Result", card_type.upper(), amount]
    ):
        print(f"[PASS] {card_type} payment of ${amount} displayed correctly.")
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
    page.get_by_label("Credit Card").check()
    with page.expect_navigation():
        page.get_by_role("button", name="Reset").click()

    value_ok = "0" == page.get_by_label("Amount: $").input_value()
    debit_ok = page.get_by_label("Debit Card").is_checked()
    if value_ok and debit_ok:
        print("[PASS] Reset successful: value=0 and Debit selected.")
        passed = 1
    else:
        if not value_ok:
            print("[FAIL] Reset did not clear value to 0.", file=sys.stderr)
        if not debit_ok:
            print("[FAIL] Reset did not restore Debit as default.", file=sys.stderr)

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


def test_form_has_payment_options(page):
    """Verify the payment form has both Debit and Credit radio options."""
    debit = page.get_by_label("Debit Card")
    credit = page.get_by_label("Credit Card")
    assert debit.is_visible(), "Debit Card option not visible"
    assert credit.is_visible(), "Credit Card option not visible"


def test_default_payment_is_debit(page):
    """Verify the default payment option is Debit."""
    assert page.get_by_label("Debit Card").is_checked(), "Debit should be selected by default"
    assert not page.get_by_label("Credit Card").is_checked(), "Credit should not be selected by default"


def test_pay_debit(page):
    assert pay(page=page, amount="12.00", card_type="Debit") == 1


def test_result_page_structure(page):
    """Verify result page has expected heading and Back button."""
    content = page.content()
    assert "Bill Payment: Result" in content, "Result page missing heading"
    assert "Amount Paid with" in content, "Result page missing 'Amount Paid with' text"
    assert page.get_by_role("button", name="Back").is_visible(), "Back button not visible"


def test_back_after_debit(page):
    assert back(page) == 1


def test_pay_credit(page):
    assert pay(page=page, amount="5.00", card_type="Credit") == 1


def test_back_after_credit(page):
    assert back(page) == 1


def test_pay_decimal_amount(page):
    """Verify payment with decimal cents displays correctly."""
    assert pay(page=page, amount="25.50", card_type="Debit") == 1


def test_back_after_decimal(page):
    assert back(page) == 1


def test_reset(page):
    assert reset(page) == 1


# ---------------------------------------------------------------------------
# Server-side validation tests (@Digits(integer=10, fraction=2))
# JSF text input allows arbitrary values; server validates via Bean Validation.
# ---------------------------------------------------------------------------

def test_validation_rejects_excess_decimals(page):
    """@Digits(fraction=2) should reject 50.123 and show error on the form."""
    page.goto(BASE_URL + HOME_URI)
    page.get_by_label("Amount: $").fill("50.123")
    page.get_by_label("Debit Card").check()
    with page.expect_navigation():
        page.get_by_role("button", name="Pay").click()
    content = page.content()
    assert "Bill Payment: Result" not in content, "Should not reach result page"
    assert "Invalid value" in content, "Should display validation error"


def test_validation_rejects_excess_integer_digits(page):
    """@Digits(integer=10) should reject 12345678901.00 (11 integer digits)."""
    page.goto(BASE_URL + HOME_URI)
    page.get_by_label("Amount: $").fill("12345678901.00")
    page.get_by_label("Debit Card").check()
    with page.expect_navigation():
        page.get_by_role("button", name="Pay").click()
    content = page.content()
    assert "Bill Payment: Result" not in content, "Should not reach result page"
    assert "Invalid value" in content, "Should display validation error"


def test_validation_accepts_max_valid_value(page):
    """@Digits(integer=10, fraction=2) should accept 1234567890.99"""
    page.goto(BASE_URL + HOME_URI)
    page.get_by_label("Amount: $").fill("1234567890.99")
    page.get_by_label("Debit Card").check()
    with page.expect_navigation():
        page.get_by_role("button", name="Pay").click()
    content = page.content()
    assert "Bill Payment: Result" in content, "Should reach result page"
    assert "1234567890.99" in content or "1,234,567,890.99" in content


def main() -> int:
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
