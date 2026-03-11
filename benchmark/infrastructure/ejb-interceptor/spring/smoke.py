#!/usr/bin/env python3
"""Smoke test for taskcreator-quarkus REST API"""

import argparse
import os
import sys
from datetime import datetime
from playwright.sync_api import sync_playwright
import pytest


# Try both possible base URLs if not set
BASE_CANDIDATES = [
    os.getenv("TASKCREATOR_BASE_URL"),
    "http://localhost:8080",  # <-- default
]
DEFAULT_ENDPOINT = "/"
DEFAULT_BASE = "http://localhost:8080"


def pick_base_url() -> str:
    for base in BASE_CANDIDATES:
        if not base:
            continue
        print(f"---[ {datetime.now().strftime('%H:%M:%S')} - Smoke test ]---")
    # fallback to first candidate (even if failed)
    return BASE_CANDIDATES[1]


def greet_check(page, name, expected_greeting):
    """Helper: submit a name and assert the expected greeting appears."""
    base_url = pick_base_url()
    page.goto(base_url + DEFAULT_ENDPOINT)
    page.get_by_role("textbox", name="Enter your name:").fill(name)
    page.get_by_role("button", name="Submit").click()
    page.wait_for_selector(f"text={expected_greeting}")
    assert expected_greeting in page.content()


@pytest.fixture(scope="module")
def page():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        pg = browser.new_page()
        yield pg
        browser.close()


def test_page_loads(page):
    """Greeting page should load with name input."""
    base_url = pick_base_url()
    page.goto(base_url + DEFAULT_ENDPOINT)
    assert "Enter your name:" in page.content()


def test_form_has_input_and_button(page):
    """Form should have a name textbox and Submit button."""
    assert page.get_by_role("textbox", name="Enter your name:").is_visible()
    assert page.get_by_role("button", name="Submit").is_visible()


def test_uppercase_name_lowercased(page):
    """Interceptor should lowercase an all-caps name: DUKE -> duke."""
    greet_check(page, "DUKE", "Hello, duke.")


def test_mixed_case_lowered(page):
    """Interceptor should lowercase mixed-case: Alice -> alice."""
    greet_check(page, "Alice", "Hello, alice.")


def test_already_lowercase_unchanged(page):
    """Already lowercase name should remain unchanged: bob -> bob."""
    greet_check(page, "bob", "Hello, bob.")


def test_name_with_spaces_lowercased(page):
    """Interceptor should lowercase name with spaces: Mary Jane -> mary jane."""
    greet_check(page, "Mary Jane", "Hello, mary jane.")


def test_single_character_name(page):
    """Single character name should be lowercased: X -> x."""
    greet_check(page, "X", "Hello, x.")


def test_back_navigation(page):
    """After greeting, back navigation should return to the form."""
    base_url = pick_base_url()
    page.goto(base_url + DEFAULT_ENDPOINT)
    page.get_by_role("textbox", name="Enter your name:").fill("Test")
    page.get_by_role("button", name="Submit").click()
    page.wait_for_selector("text=Hello, test.")
    page.go_back()
    assert "Enter your name:" in page.content()


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
