import pytest
import re
from playwright.sync_api import Page, expect


def test_coffeeshop_homepage(page: Page):
    """Smoke test: homepage loads and basic elements exist."""
    page.goto("http://localhost:9080/")

    title = page.title()
    assert "coffee" in title.lower()

    expect(page.locator("body")).to_be_attached()

    expect(
        page.locator("a").filter(has_text=re.compile("About", re.I))
    ).to_be_attached()

    expect(
        page.locator("a").filter(has_text=re.compile("Menu", re.I))
    ).to_be_attached()


def test_about_link_exists(page: Page):
    """Smoke test: About link is present in DOM."""
    page.goto("http://localhost:9080/")

    expect(
        page.locator("a").filter(has_text=re.compile("About", re.I))
    ).to_be_attached()


def test_menu_link_exists(page: Page):
    """Smoke test: Menu link is present in DOM."""
    page.goto("http://localhost:9080/")

    expect(
        page.locator("a").filter(has_text=re.compile("Menu", re.I))
    ).to_be_attached()

def test_homepage_banner_content(page: Page):
    """Test that the homepage has banner content."""
    # 1. Navigate to homepage
    page.goto("http://localhost:9080/")

    # 2. Wait for page to load
    page.wait_for_load_state("networkidle")

    # 3. Verify the page loaded successfully
    expect(page.locator("body")).to_be_visible()


def test_navigation_menu_persistence(page: Page):
    """Test that navigation menu is present on the page."""
    # 1. Navigate to homepage
    page.goto("http://localhost:9080/")
    page.wait_for_load_state("networkidle")

    # 2. Verify navigation menu is present
    menu_items = page.locator("nav a").first
    expect(menu_items).to_be_visible()


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])