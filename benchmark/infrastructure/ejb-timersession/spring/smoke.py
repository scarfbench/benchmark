#!/usr/bin/env python3
"""Smoke test for timessession-quarkus"""

import argparse
import os
import sys
import time
from datetime import datetime
from playwright.sync_api import sync_playwright
import pytest


DEFAULT_BASE = "http://localhost:9080"
# Try both possible base URLs if not set
BASE_CANDIDATES = [
    os.getenv("SERVICE_BASE_URL"),
    DEFAULT_BASE,
]
DEFAULT_ENDPOINT = "/timersession"


def pick_base_url() -> str:
    for base in BASE_CANDIDATES:
        if not base:
            continue
        print(f"---[ {datetime.now().strftime('%H:%M:%S')} - Smoke test ]---")
    # fallback to first candidate (even if failed)
    return BASE_CANDIDATES[1]


@pytest.fixture(scope="module")
def base_url():
    return pick_base_url()


@pytest.fixture(scope="module")
def page():
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        pg = browser.new_page()
        yield pg
        browser.close()


def test_page_loads_with_never(page, base_url):
    """Timer page should show 'never' for programmatic timeout initially."""
    page.goto(base_url + DEFAULT_ENDPOINT)
    assert "The last programmatic timeout was: never." in page.content()


def test_page_has_set_timer_button(page):
    """Timer page should have a Set Timer button."""
    assert page.get_by_role("button", name="Set Timer").is_visible()


def test_page_has_refresh_button(page):
    """Timer page should have a Refresh button."""
    assert page.get_by_role("button", name="Refresh").is_visible()


def test_set_timer_submits(page):
    """Clicking Set Timer should submit and stay on timer page."""
    page.get_by_role("button", name="Set Timer").click()
    page.wait_for_selector("text=Timer page")
    assert "Timer page" in page.content()


def test_programmatic_timer_fires(page):
    """After waiting 60s, programmatic timer should have fired."""
    print("[INFO] Waiting 60 seconds for timers to trigger...")
    time.sleep(60)
    page.get_by_role("button", name="Refresh").click()
    page.wait_for_selector("text=Timer page")
    assert "The last programmatic timeout was: never." not in page.content()


def test_automatic_timer_fired(page):
    """After waiting, automatic timer should have fired."""
    assert "The last automatic timeout was: never" not in page.content()


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
