#!/usr/bin/env python3
"""Smoke test for decorators-jakarta

Checks:
  1) Visit and validate contents of Base Page
  2) Fill form and encode string
  3) Trigger a validation error
  4) Reset encode form

Exit codes:
  0 success
  1 failure
"""

import os
import sys
import pytest
from playwright.sync_api import Page, sync_playwright


DEFAULT_BASE = "http://localhost:9080"
BASE_URL = os.getenv("DECORATORS_BASE_URL", DEFAULT_BASE)
DEFAULT_ENDPOINT = "/decorators"
HOME_URI = os.getenv("DECORATORS_HOME_URI", DEFAULT_ENDPOINT)


def visit_main_page(page: Page) -> int:
    passed = 0
    page.goto(BASE_URL + HOME_URI)
    # Ensure that the page loads successfully
    if "Decorated String Encoder" in page.content():
        print("[PASS] Page loaded successfully and contains expected text.")
        passed = 1
    else:
        print("[FAIL] Page did not contain expected text.", file=sys.stderr)

    return passed


def encode(page: Page) -> int:
    passed = 0

    # Fill fields and encode
    page.get_by_label("Enter a string:").fill("aa")
    page.get_by_label("Enter the number of letters to shift by:").fill("2")
    with page.expect_navigation():
        page.get_by_role("button", name="Encode").click()

    # Assert we got the correct encoding on page
    if """"aa" becomes "cc", 2 characters in length""" in page.content():
        print("[PASS] Encode displayed correctly.")
        passed = 1
    else:
        print("[FAIL] Encode not displayed as expected.", file=sys.stderr)

    return passed


def trigger_validation_error(page: Page) -> int:
    passed = 0

    # Validate number of shifts
    page.get_by_label("Enter a string:").fill("aa")
    page.get_by_label("Enter the number of letters to shift by:").fill("33")
    with page.expect_navigation():
        page.get_by_role("button", name="Encode").click()

    # Assert we have an error on page
    value = page.get_by_label("Enter a string:").input_value()
    shift = page.get_by_label("Enter the number of letters to shift by:").input_value()
    if (
        "must be less than or equal to 26" in page.content()
        and "aa" == value
        and "33" == shift
    ):
        print("[PASS] Error displayed correctly.")
        passed = 1
    else:
        print("[FAIL] Error not displayed as expected.", file=sys.stderr)

    return passed


def reset(page: Page) -> int:
    passed = 0
    # JSF does the validation before reset, so the input needs to be valid
    page.get_by_label("Enter the number of letters to shift by:").fill("2")
    with page.expect_navigation():
        page.get_by_role("button", name="Reset").click()

    value = page.get_by_label("Enter a string:").input_value()
    shift = page.get_by_label("Enter the number of letters to shift by:").input_value()
    if "" == value and "0" == shift:
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


def encode_check(page: Page, input_str: str, shift: int, expected: str) -> None:
    """Helper: encode input_str with shift and assert expected substring in result."""
    page.get_by_label("Enter a string:").fill(input_str)
    page.get_by_label("Enter the number of letters to shift by:").fill(str(shift))
    with page.expect_navigation():
        page.get_by_role("button", name="Encode").click()
    assert expected in page.content(), (
        f"Expected '{expected}' in page after encoding '{input_str}' with shift {shift}"
    )


def test_visit_main_page(page):
    assert visit_main_page(page) == 1


def test_form_has_input_fields(page):
    """Verify the form has string input, shift input, and Encode button."""
    assert page.get_by_label("Enter a string:").is_visible()
    assert page.get_by_label("Enter the number of letters to shift by:").is_visible()
    assert page.get_by_role("button", name="Encode").is_visible()


def test_encode(page):
    assert encode(page) == 1


def test_encode_uppercase_wraparound(page):
    """XYZ with shift 3 should become ABC (decorator format)."""
    encode_check(page, "XYZ", 3, '"XYZ" becomes "ABC", 3 characters in length')


def test_encode_mixed_case(page):
    """Hello with shift 1 should become Ifmmp (decorator format)."""
    encode_check(page, "Hello", 1, '"Hello" becomes "Ifmmp", 5 characters in length')


def test_encode_spaces_preserved(page):
    """Spaces should be preserved during encoding."""
    encode_check(page, "a b", 1, 'becomes "b c"')


def test_encode_shift_zero(page):
    """Shift of 0 leaves the string unchanged."""
    encode_check(page, "test", 0, '"test" becomes "test", 4 characters in length')


def test_encode_z_wraps_to_a(page):
    """Lowercase z with shift 1 should wrap to a."""
    encode_check(page, "z", 1, 'becomes "a"')


def test_encode_character_length(page):
    """Decorator should report correct character length."""
    encode_check(page, "Hello World", 1, "11 characters in length")


def test_trigger_validation_error(page):
    assert trigger_validation_error(page) == 1


def test_validation_negative_shift(page):
    """Negative shift value should trigger a validation error."""
    page.get_by_label("Enter the number of letters to shift by:").fill("-1")
    with page.expect_navigation():
        page.get_by_role("button", name="Encode").click()
    content = page.content()
    assert "must be greater than or equal to 0" in content, "Negative shift should show validation error"


def test_reset(page):
    # Fix shift to valid value before reset (JSF validates on reset)
    page.get_by_label("Enter the number of letters to shift by:").fill("2")
    assert reset(page) == 1


def main() -> int:
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
