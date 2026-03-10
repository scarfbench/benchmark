#!/usr/bin/env python3
"""Smoke tests for converter-spring."""

import os
import sys

import pytest
from playwright.sync_api import Page, sync_playwright

DEFAULT_BASE = "http://localhost:8080"
BASE_URL = os.getenv("CONVERTER_BASE_URL", DEFAULT_BASE)
HOME_URI = os.getenv("CONVERTER_HOME_URI", "/converter")

EXPECTED_RESULTS = {
    "1": ("104.34 yen", "0.74 Euro"),
    "5": ("521.70 yen", "3.66 Euro"),
    "10": ("1043.40 yen", "7.31 Euro"),
    "50": ("5217.00 yen", "36.52 Euro"),
    "100": ("10434.00 yen", "73.04 Euro"),
}


@pytest.fixture(scope="module")
def page():
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        browser_page = browser.new_page()
        yield browser_page
        browser.close()


@pytest.fixture(autouse=True)
def go_home(page: Page):
    page.goto(BASE_URL + HOME_URI)


def _submit(page: Page, amount: str) -> str:
    page.get_by_title("Amount").fill(amount)
    with page.expect_navigation():
        page.get_by_role("button", name="Submit").click()
    return page.content()


def _submit_empty(page: Page) -> str:
    page.get_by_title("Amount").fill("")
    with page.expect_navigation():
        page.get_by_role("button", name="Submit").click()
    return page.content()


def test_page_has_expected_label(page: Page):
    assert "Enter a dollar amount to convert:" in page.content()


def test_form_has_amount_input(page: Page):
    assert page.get_by_title("Amount").is_visible()


def test_form_has_submit_button(page: Page):
    assert page.get_by_role("button", name="Submit").is_visible()


def test_page_title_is_non_empty(page: Page):
    assert page.title() != ""


@pytest.mark.parametrize(
    ("amount", "yen_text", "euro_text"),
    [
        ("1", "104.34 yen", "0.74 Euro"),
        ("5", "521.70 yen", "3.66 Euro"),
        ("10", "1043.40 yen", "7.31 Euro"),
        ("50", "5217.00 yen", "36.52 Euro"),
        ("100", "10434.00 yen", "73.04 Euro"),
    ],
)
def test_expected_conversions(page: Page, amount: str, yen_text: str, euro_text: str):
    content = _submit(page, amount)
    assert yen_text in content
    assert euro_text in content


@pytest.mark.parametrize(
    ("amount", "yen_text"),
    [("1", "104.34"), ("5", "521.70"), ("10", "1043.40")],
)
def test_outline_yen_conversions(page: Page, amount: str, yen_text: str):
    assert f"{yen_text} yen" in _submit(page, amount)


def test_result_echoes_input_amount(page: Page):
    assert "5" in _submit(page, "5")


def test_no_stack_trace_in_result(page: Page):
    content = _submit(page, "5")
    assert "at java." not in content
    assert "Exception" not in content


def test_page_title_is_non_empty_after_conversion(page: Page):
    _submit(page, "5")
    assert page.title() != ""


def test_can_convert_twice_in_a_row(page: Page):
    _submit(page, "5")
    page.goto(BASE_URL + HOME_URI)
    assert "Enter a dollar amount to convert:" in page.content()
    assert EXPECTED_RESULTS["1"][0] in _submit(page, "1")


@pytest.mark.parametrize("amount", ["0", "1000000", "2.5", "-1"])
def test_problem_inputs_do_not_show_server_errors(page: Page, amount: str):
    content = _submit(page, amount)
    assert "500" not in content
    assert "Exception" not in content


def test_empty_submission_does_not_show_server_errors(page: Page):
    content = _submit_empty(page)
    assert "500" not in content
    assert "Exception" not in content


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
