#!/usr/bin/env python3
"""Smoke test for producermethods-spring

Checks:
  1) Visit and validate contents of Base Page
  2) Validate Shift Encoder result
  3) Validate Test Encoder result
  4) Trigger a validation error
  5) Reset encoder form

Exit codes:
  0 success
  1 failure
"""

import os
import sys
import pytest
from playwright.sync_api import Page, sync_playwright


DEFAULT_BASE = "http://localhost:8080"
BASE_URL = os.getenv("PRODUCER_METHODS_BASE_URL", DEFAULT_BASE)
DEFAULT_ENDPOINT = "/producermethods"
HOME_URI = os.getenv("PRODUCER_METHODS_HOME_URI", DEFAULT_ENDPOINT)


def visit_main_page(page: Page) -> int:
    passed = 0
    page.goto(BASE_URL + HOME_URI)
    if "String Encoder" in page.content():
        print("[PASS] Page loaded successfully and contains expected text.")
        passed = 1
    else:
        print("[FAIL] Page did not contain expected text.", file=sys.stderr)

    return passed


def encode(
    page: Page,
    encoder_label: str,
    input: str,
    expected_encoding: str,
    shift_by: int = 2,
) -> int:
    passed = 0

    page.get_by_label(encoder_label).check()
    page.get_by_label("Enter a string:").fill(input)
    page.get_by_label("Enter the number of letters to shift by:").fill(f"{shift_by}")
    with page.expect_navigation():
        page.get_by_role("button", name="Encode").click()

    # Assert we got the correct encoding on page
    if expected_encoding in page.content():
        print("[PASS] Shift letters encode displayed correctly.")
        passed = 1
    else:
        print(
            "[FAIL] Shift letters encode not displayed as expected.",
            file=sys.stderr,
        )

    return passed


def trigger_validation_error(page: Page) -> int:
    passed = 0
    page.get_by_label("Enter a string:").fill("aa2")
    page.get_by_label("Enter the number of letters to shift by:").fill("33")
    with page.expect_navigation():
        page.get_by_role("button", name="Encode").click()

    # Assert we have an error on page
    value = page.get_by_label("Enter a string:").input_value()
    shift = page.get_by_label("Enter the number of letters to shift by:").input_value()
    if (
        "must be less than or equal to 26" in page.content()
        and "aa2" == value
        and "33" == shift
    ):
        print("[PASS] Error displayed correctly.")
        passed = 1
    else:
        print("[FAIL] Error not displayed as expected.", file=sys.stderr)

    return passed


def reset(page: Page) -> int:
    passed = 0
    # JSF does the validation on reset, so the input needs to be valid
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


def test_visit_main_page(page):
    assert visit_main_page(page) == 1


def test_form_has_coder_selector(page):
    """Verify the form has Shift and Test radio options, with Shift as default."""
    shift_radio = page.get_by_label("Shift Letters")
    test_radio = page.get_by_label("Test")
    assert shift_radio.is_visible(), "Shift Letters radio should be visible"
    assert test_radio.is_visible(), "Test radio should be visible"
    assert shift_radio.is_checked(), "Shift Letters should be selected by default"


def test_form_has_input_fields(page):
    """Verify the form has string input, shift input, and Encode button."""
    assert page.get_by_label("Enter a string:").is_visible()
    assert page.get_by_label("Enter the number of letters to shift by:").is_visible()
    assert page.get_by_role("button", name="Encode").is_visible()


def test_encode_shift_letters(page):
    # Test shift letters encoder
    assert encode(
        page=page,
        encoder_label="Shift Letters",
        input="aa2",
        expected_encoding="cc2",
    ) == 1


def test_shift_uppercase_wraparound(page):
    """Shift coder: XYZ with shift 3 should become ABC."""
    assert encode(
        page=page,
        encoder_label="Shift Letters",
        input="XYZ",
        expected_encoding="ABC",
        shift_by=3,
    ) == 1


def test_shift_preserves_spaces(page):
    """Shift coder: spaces should be preserved."""
    assert encode(
        page=page,
        encoder_label="Shift Letters",
        input="a b",
        expected_encoding="b c",
        shift_by=1,
    ) == 1


def test_shift_no_modify_punctuation(page):
    """Shift coder: punctuation should not be modified."""
    assert encode(
        page=page,
        encoder_label="Shift Letters",
        input="a.b!",
        expected_encoding="b.c!",
        shift_by=1,
    ) == 1


def test_encode_test(page):
    # Fill fields and use test encoder
    assert encode(
        page=page,
        encoder_label="Test",
        input="aa2",
        expected_encoding="input string is aa2, shift value is 2",
        shift_by=2,
    ) == 1


def test_test_coder_shift_zero(page):
    """Test coder: should echo input and shift 0."""
    assert encode(
        page=page,
        encoder_label="Test",
        input="World",
        expected_encoding="input string is World, shift value is 0",
        shift_by=0,
    ) == 1


def test_trigger_validation_error(page):
    assert trigger_validation_error(page) == 1


def test_validation_negative_shift(page):
    """Negative shift value should trigger a validation error."""
    page.get_by_label("Enter the number of letters to shift by:").fill("-1")
    with page.expect_navigation():
        page.get_by_role("button", name="Encode").click()
    assert "must be greater than or equal to 0" in page.content()


def test_reset(page):
    # Fix shift to valid value before reset (JSF validates on reset)
    page.get_by_label("Enter the number of letters to shift by:").fill("2")
    assert reset(page) == 1


def main() -> int:
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
