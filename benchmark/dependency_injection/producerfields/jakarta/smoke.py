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
import pytest

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


@pytest.fixture(scope="module")
def page():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        pg = browser.new_page()
        yield pg
        browser.close()


def test_visit_main_page(page):
    assert visit_main_page(page) == 1


def test_form_has_input_and_buttons(page):
    """Verify the form has text input, Submit, and Show Items buttons."""
    assert page.get_by_label("Enter a string:").is_visible()
    assert page.get_by_role("button", name="Submit").is_visible()
    assert page.get_by_role("button", name="Show Items").is_visible()


def test_add_todo(page):
    assert add_todo(page) == 1


def test_add_second_todo(page):
    """Add a second todo item to verify multiple items work."""
    page.get_by_label("Enter a string:").fill("Walk the dog")
    with page.expect_navigation():
        page.get_by_role("button", name="Submit").click()
    assert "Create To Do List" in page.content()


def test_add_third_todo(page):
    """Add a third todo item."""
    page.get_by_label("Enter a string:").fill("Read a book")
    with page.expect_navigation():
        page.get_by_role("button", name="Submit").click()
    assert "Create To Do List" in page.content()


def test_display_todos_shows_all(page):
    """Show Items should display all added todos."""
    with page.expect_navigation():
        page.get_by_role("button", name="Show Items").click()
    content = page.content()
    assert "Smoke Test" in content, "First todo should be in list"
    assert "Walk the dog" in content, "Second todo should be in list"
    assert "Read a book" in content, "Third todo should be in list"


def test_todos_are_ordered(page):
    """Todos should be ordered by creation time (first added appears first)."""
    content = page.content()
    pos_first = content.index("Smoke Test")
    pos_second = content.index("Walk the dog")
    pos_third = content.index("Read a book")
    assert pos_first < pos_second < pos_third, "Todos should be in creation order"


def test_back(page):
    assert back(page) == 1


def main() -> int:
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
