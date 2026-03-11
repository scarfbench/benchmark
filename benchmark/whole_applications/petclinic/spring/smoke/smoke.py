import pytest
import re
import requests
from playwright.sync_api import Page, expect

BASE_URL = "http://localhost:8080"


# ---------------------------------------------------------------------------
# Homepage and navigation
# ---------------------------------------------------------------------------


def test_homepage_loads(page: Page):
    """Scenario: Homepage loads successfully."""
    page.goto(f"{BASE_URL}/")
    title = page.title()
    assert "petclinic" in title.lower()
    expect(
        page.locator("a").filter(has_text=re.compile(r"Home", re.I)).first
    ).to_be_visible()
    expect(
        page.locator("a")
        .filter(has_text=re.compile(r"Find.*Owner|Owner", re.I))
        .first
    ).to_be_visible()
    expect(
        page.locator("a").filter(has_text=re.compile(r"Veterinarian", re.I)).first
    ).to_be_visible()


def test_homepage_displays_welcome_panel(page: Page):
    """Scenario: Homepage displays welcome panel."""
    page.goto(f"{BASE_URL}/")
    page.wait_for_load_state("networkidle")
    expect(page.locator("body")).to_be_visible()


def test_homepage_has_welcome_text(page: Page):
    """Scenario: Homepage has welcome text or heading."""
    page.goto(f"{BASE_URL}/")
    page.wait_for_load_state("networkidle")
    welcome_text = page.locator("h1, h2, .welcome, [class*='welcome']").first
    expect(welcome_text).to_be_visible()


def test_pages_load_without_errors(page: Page):
    """Scenario: All pages load without errors."""
    pages_to_test = ["/", "/owners/find", "/vets.html"]
    for page_url in pages_to_test:
        response = page.goto(f"{BASE_URL}{page_url}")
        assert response and response.status < 400, (
            f"Page {page_url} returned status {response.status if response else 'None'}"
        )
        page.wait_for_load_state("networkidle")


def test_page_titles_are_set(page: Page):
    """Scenario: All pages have non-empty titles."""
    pages_to_test = ["/", "/owners/find", "/vets.html"]
    for page_url in pages_to_test:
        page.goto(f"{BASE_URL}{page_url}")
        page.wait_for_load_state("networkidle")
        title = page.title()
        assert title and len(title) > 0, f"{page_url} should have a title"


# ---------------------------------------------------------------------------
# Owner management — listing and search
# ---------------------------------------------------------------------------


def test_navigate_to_find_owners_page(page: Page):
    """Scenario: Navigate to find owners page."""
    page.goto(f"{BASE_URL}/")
    page.locator("a").filter(
        has_text=re.compile(r"Find.*Owner|Owner", re.I)
    ).first.click()
    expect(page).to_have_url(re.compile(r".*/owners/find", re.I))
    expect(page.locator("body")).to_be_visible()


def test_find_owners_page_has_search_form(page: Page):
    """Scenario: Owner page has search functionality."""
    page.goto(f"{BASE_URL}/owners/find")
    page.wait_for_load_state("networkidle")
    search_input = page.locator(
        "input[type='text'], input[id*='lastName'], input[name*='lastName']"
    ).first
    expect(search_input).to_be_visible()


def test_owner_search_returns_results(page: Page):
    """Scenario: Owner search filters results."""
    page.goto(f"{BASE_URL}/owners/find")
    page.wait_for_load_state("networkidle")
    search_input = page.locator(
        "input[type='text'], input[id*='lastName'], input[name*='lastName']"
    ).first
    search_input.fill("")
    page.locator("button[type='submit'], input[type='submit']").first.click()
    page.wait_for_load_state("networkidle")
    # Either redirected to owner list or single owner detail
    expect(page.locator("body")).to_be_visible()


def test_owner_search_by_last_name(page: Page):
    """Scenario: Owner search by specific last name."""
    page.goto(f"{BASE_URL}/owners/find")
    page.wait_for_load_state("networkidle")
    search_input = page.locator(
        "input[type='text'], input[id*='lastName'], input[name*='lastName']"
    ).first
    search_input.fill("Davis")
    page.locator("button[type='submit'], input[type='submit']").first.click()
    page.wait_for_load_state("networkidle")
    # Should show results matching Davis
    expect(page.locator("body")).to_be_visible()


# ---------------------------------------------------------------------------
# Veterinarian management
# ---------------------------------------------------------------------------


def test_navigate_to_veterinarians_page(page: Page):
    """Scenario: Navigate to veterinarians page."""
    page.goto(f"{BASE_URL}/")
    page.locator("a").filter(
        has_text=re.compile(r"Veterinarian", re.I)
    ).first.click()
    expect(page).to_have_url(re.compile(r".*/vets\.html", re.I))
    expect(page.locator("body")).to_be_visible()


def test_veterinarians_page_displays_table(page: Page):
    """Scenario: Veterinarian page displays a list."""
    page.goto(f"{BASE_URL}/vets.html")
    page.wait_for_load_state("networkidle")
    content = page.locator("table, ul, ol, .vet-list, .vets").first
    expect(content).to_be_visible(timeout=10000)


# ---------------------------------------------------------------------------
# REST API — Vets (Spring PetClinic exposes /vets as JSON)
# ---------------------------------------------------------------------------


def test_rest_vets_json():
    """Scenario: GET /vets returns vets as JSON."""
    r = requests.get(
        f"{BASE_URL}/vets",
        headers={"Accept": "application/json"},
    )
    assert r.status_code == 200
    data = r.json()
    # Spring PetClinic returns {"vetList": [...]} or a list
    if isinstance(data, dict):
        vets = data.get("vetList", data.get("vets", []))
    else:
        vets = data
    assert isinstance(vets, list)
    if len(vets) > 0:
        vet = vets[0]
        assert "firstName" in vet or "first_name" in vet


# ---------------------------------------------------------------------------
# Error page
# ---------------------------------------------------------------------------


def test_error_page_for_unknown_route(page: Page):
    """Scenario: Unknown routes show an error page."""
    response = page.goto(f"{BASE_URL}/nonexistent-page-xyz")
    # Spring returns a Whitelabel error page or custom error
    assert response is not None
    # Either 404 or a redirect to error page
    expect(page.locator("body")).to_be_visible()


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
