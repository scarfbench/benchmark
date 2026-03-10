#!/usr/bin/env python3
"""Smoke test for simplegreeting-spring

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


DEFAULT_BASE = "http://localhost:8080"
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


def greet_check(page: Page, name: str, expected: str) -> None:
    """Helper: enter name, submit, and verify expected greeting."""
    page.get_by_label("Enter your name:").fill(name)
    with page.expect_navigation():
        page.get_by_role("button", name="Say Hello").click()
    assert expected in page.content(), f"Expected '{expected}' for name '{name}'"


def test_visit_main_page(page):
    assert visit_main_page(page) == 1


def test_form_has_name_input(page):
    """Verify the form has a name input and Submit button."""
    assert page.get_by_label("Enter your name:").is_visible()
    assert page.get_by_role("button", name="Say Hello").is_visible()


def test_greet(page):
    assert greet(page) == 1


def test_greet_duke(page):
    """Greeting Duke should return informal 'Hi, Duke!'"""
    greet_check(page, "Duke", "Hi, Duke!")


def test_greet_alice(page):
    """Greeting Alice should return informal 'Hi, Alice!'"""
    greet_check(page, "Alice", "Hi, Alice!")


def test_greet_multi_word_name(page):
    """Multi-word names should work correctly."""
    greet_check(page, "Mary Jane", "Hi, Mary Jane!")


def test_greeting_ends_with_exclamation(page):
    """Informal greeting should end with '!' not '.'"""
    page.get_by_label("Enter your name:").fill("Test")
    with page.expect_navigation():
        page.get_by_role("button", name="Say Hello").click()
    content = page.content()
    assert "Hi, Test!" in content, "Should use informal 'Hi, Test!' format"
    assert "Hello, Test." not in content, "Should NOT use formal 'Hello, Test.' format"


def main() -> int:
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
