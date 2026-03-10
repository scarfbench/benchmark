#!/usr/bin/env python3
"""Smoke tests for counter-spring."""

import os
import re
import sys

import pytest
from playwright.sync_api import Page, sync_playwright

DEFAULT_BASE = "http://localhost:8080"
BASE_URL = os.getenv("COUNTER_BASE_URL", DEFAULT_BASE)
HOME_URI = os.getenv("COUNTER_HOME_URI", "/counter")
COUNTER_PATTERN = r"This page has been accessed (\d+) time\(s\)\."


@pytest.fixture(scope="module")
def page():
    with sync_playwright() as playwright:
        browser = playwright.chromium.launch(headless=True)
        browser_page = browser.new_page()
        yield browser_page
        browser.close()


def _visit_counter(page: Page) -> str:
    page.goto(BASE_URL + HOME_URI)
    return page.content()


def _get_access_count(page: Page) -> int:
    match = re.search(COUNTER_PATTERN, _visit_counter(page))
    assert match, "Page did not contain expected counter text"
    return int(match.group(1))


def test_page_renders_without_errors(page: Page):
    html = _visit_counter(page)
    assert html.strip()


def test_page_contains_hit_count_sentence(page: Page):
    assert re.search(COUNTER_PATTERN, _visit_counter(page))


def test_first_observed_count_is_positive(page: Page):
    assert _get_access_count(page) > 0


def test_counter_increments_by_exactly_one(page: Page):
    count1 = _get_access_count(page)
    count2 = _get_access_count(page)
    assert count2 == count1 + 1


def test_counter_increments_monotonically(page: Page):
    counts = [_get_access_count(page) for _ in range(3)]
    assert counts[1] == counts[0] + 1
    assert counts[2] == counts[1] + 1


def test_page_has_no_java_exception_text(page: Page):
    html = _visit_counter(page)
    assert "Exception" not in html
    assert "at java." not in html


def test_page_has_no_http_500_error_text(page: Page):
    html = _visit_counter(page)
    assert "500" not in html
    assert "Internal Server Error" not in html


def test_counter_text_format_is_exact(page: Page):
    assert re.search(rf"^\s*(?:<[^>]+>)?\s*{COUNTER_PATTERN}\s*(?:</[^>]+>)?\s*$", _visit_counter(page), re.MULTILINE)


def test_counter_values_are_non_negative(page: Page):
    counts = [_get_access_count(page) for _ in range(4)]
    assert all(count >= 0 for count in counts)


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
