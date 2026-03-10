#!/usr/bin/env python3
"""Smoke tests for converter-quarkus.

Checks:
  1) Page structure (label, input, button, title)
  2) Result correctness for 1, 5, and 10 dollars
  3) Result echoes input amount and contains no stack traces
  4) Can convert twice in a row
  5) Zero, large, and empty inputs produce no server error
"""

import os
import sys
import pytest
from playwright.sync_api import Page, sync_playwright

DEFAULT_BASE = "http://localhost:8080"
BASE_URL = os.getenv("CONVERTER_BASE_URL", DEFAULT_BASE)
DEFAULT_ENDPOINT = "/converter"
HOME_URI = os.getenv("CONVERTER_HOME_URI", DEFAULT_ENDPOINT)


# ---------------------------------------------------------------------------
# Fixtures
# ---------------------------------------------------------------------------

@pytest.fixture(scope="module")
def page():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        pg = browser.new_page()
        yield pg
        browser.close()


@pytest.fixture(autouse=True)
def go_home(page):
    """Navigate to the form before every test so tests are independent."""
    page.goto(BASE_URL + HOME_URI)


# ---------------------------------------------------------------------------
# Helper
# ---------------------------------------------------------------------------

def _submit(page: Page, amount: str) -> str:
    page.get_by_title("Amount").fill(amount)
    with page.expect_navigation():
        page.get_by_role("button", name="Submit").click()
    return page.content()


# ---------------------------------------------------------------------------
# Page structure
# ---------------------------------------------------------------------------

def test_page_has_expected_label(page):
    assert "Enter a dollar amount to convert:" in page.content()


def test_form_has_amount_input(page):
    assert page.get_by_title("Amount").is_visible()


def test_form_has_submit_button(page):
    assert page.get_by_role("button", name="Submit").is_visible()


def test_page_title_is_non_empty(page):
    assert page.title() != ""


# ---------------------------------------------------------------------------
# Result correctness
# ---------------------------------------------------------------------------

def test_convert_5_dollars(page):
    content = _submit(page, "5")
    assert "521.70 yen" in content
    assert "3.66 Euro" in content


def test_convert_1_dollar(page):
    content = _submit(page, "1")
    assert "104.34 yen" in content
    assert "0.74 Euro" in content


def test_convert_10_dollars(page):
    content = _submit(page, "10")
    assert "1043.40 yen" in content
    assert "7.31 Euro" in content


def test_result_echoes_input_amount(page):
    assert "5" in _submit(page, "5")


def test_no_stack_trace_in_result(page):
    content = _submit(page, "5")
    assert "at java." not in content
    assert "Exception" not in content


# ---------------------------------------------------------------------------
# Navigation
# ---------------------------------------------------------------------------

def test_can_convert_twice_in_a_row(page):
    _submit(page, "5")
    page.goto(BASE_URL + HOME_URI)
    assert "Enter a dollar amount to convert:" in page.content()
    assert "104.34 yen" in _submit(page, "1")


# ---------------------------------------------------------------------------
# Edge cases
# ---------------------------------------------------------------------------

def test_zero_amount_no_server_error(page):
    content = _submit(page, "0")
    assert "500" not in content
    assert "Exception" not in content


def test_large_amount_no_server_error(page):
    content = _submit(page, "1000000")
    assert "500" not in content
    assert "Exception" not in content


def test_empty_submission_no_server_error(page):
    page.get_by_title("Amount").fill("")
    page.get_by_role("button", name="Submit").click()
    page.wait_for_load_state()
    content = page.content()
    assert "500" not in content
    assert "Exception" not in content


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
