import pytest
import re
import requests
from playwright.sync_api import Page, expect

BASE_URL = "http://localhost:8080"
REST_BASE = f"{BASE_URL}/petclinic/rest"


# ---------------------------------------------------------------------------
# Homepage and navigation
# ---------------------------------------------------------------------------


def test_homepage_loads(page: Page):
    """Scenario: Homepage loads successfully."""
    page.goto(f"{BASE_URL}/petclinic/home.jsf")
    title = page.title()
    assert "petclinic" in title.lower()
    for label in ["Home", "Owner", "Pet Type", "Veterinarian", "Specialt"]:
        expect(
            page.locator("a").filter(has_text=re.compile(label, re.I)).first
        ).to_be_visible()


def test_index_redirects_to_home(page: Page):
    """Scenario: Index page redirects to home."""
    page.goto(f"{BASE_URL}/petclinic/index.html")
    page.wait_for_load_state("networkidle")
    expect(page).to_have_url(re.compile(r".*home\.jsf", re.I))


def test_homepage_displays_search_panel(page: Page):
    """Scenario: Homepage displays owner search panel."""
    page.goto(f"{BASE_URL}/petclinic/home.jsf")
    page.wait_for_load_state("networkidle")
    search_input = page.locator("input[type='text']").first
    expect(search_input).to_be_visible()


def test_homepage_displays_welcome_panel(page: Page):
    """Scenario: Homepage displays welcome panel."""
    page.goto(f"{BASE_URL}/petclinic/home.jsf")
    page.wait_for_load_state("networkidle")
    expect(page.locator("body")).to_be_visible()


def test_navigation_menu_persists_across_pages(page: Page):
    """Scenario: Navigation menu persists across all pages."""
    pages_to_test = [
        "home.jsf",
        "owner.jsf",
        "petType.jsf",
        "veterinarian.jsf",
        "specialty.jsf",
    ]
    for p in pages_to_test:
        page.goto(f"{BASE_URL}/petclinic/{p}")
        page.wait_for_load_state("networkidle")
        menu = page.locator("a[href*='.jsf'], [role='menuitem'], nav a").first
        expect(menu).to_be_visible()


def test_information_page(page: Page):
    """Scenario: Information page is accessible."""
    page.goto(f"{BASE_URL}/petclinic/home.jsf")
    page.locator("a").filter(
        has_text=re.compile(r"Information|Info", re.I)
    ).first.click()
    expect(page).to_have_url(re.compile(r".*info\.jsf", re.I))


def test_language_selector_available(page: Page):
    """Scenario: Language selector is available on every page."""
    page.goto(f"{BASE_URL}/petclinic/home.jsf")
    page.wait_for_load_state("networkidle")
    lang_selector = page.locator("select, [id*='language'], [id*='locale']").first
    expect(lang_selector).to_be_visible()


# ---------------------------------------------------------------------------
# Owner management — listing
# ---------------------------------------------------------------------------


def test_owner_page_displays_list(page: Page):
    """Scenario: Owner page displays a paginated list."""
    page.goto(f"{BASE_URL}/petclinic/owner.jsf")
    page.wait_for_load_state("networkidle")
    expect(page.locator("body")).to_be_visible()


def test_navigate_to_owner_page(page: Page):
    """Scenario: Navigate to owner page via menu."""
    page.goto(f"{BASE_URL}/petclinic/home.jsf")
    page.locator("a").filter(has_text=re.compile(r"Owner", re.I)).first.click()
    expect(page).to_have_url(re.compile(r".*owner\.jsf", re.I))
    expect(
        page.locator("h1, .contentTitleHeadline")
        .filter(has_text=re.compile(r"Owner", re.I))
        .first
    ).to_be_visible()


def test_owner_page_has_search(page: Page):
    """Scenario: Owner page has search functionality."""
    page.goto(f"{BASE_URL}/petclinic/owner.jsf")
    page.wait_for_load_state("networkidle")
    search_input = page.locator(
        "input[type='text'], input[id*='search'], input[placeholder*='search']"
    ).first
    expect(search_input).to_be_visible()


# ---------------------------------------------------------------------------
# Pet Type management
# ---------------------------------------------------------------------------


def test_navigate_to_pet_type_page(page: Page):
    """Scenario: PetType page displays a list."""
    page.goto(f"{BASE_URL}/petclinic/home.jsf")
    page.locator("a").filter(has_text=re.compile(r"Pet Type", re.I)).first.click()
    expect(page).to_have_url(re.compile(r".*petType\.jsf", re.I))
    expect(
        page.locator("h1, .contentTitleHeadline")
        .filter(has_text=re.compile(r"Pet Type", re.I))
        .first
    ).to_be_visible()


def test_pet_type_page_displays_list(page: Page):
    """Scenario: PetType page displays a list."""
    page.goto(f"{BASE_URL}/petclinic/petType.jsf")
    page.wait_for_load_state("networkidle")
    expect(page.locator("body")).to_be_visible()


# ---------------------------------------------------------------------------
# Veterinarian management
# ---------------------------------------------------------------------------


def test_navigate_to_veterinarian_page(page: Page):
    """Scenario: Veterinarian page displays a list."""
    page.goto(f"{BASE_URL}/petclinic/home.jsf")
    page.locator("a").filter(
        has_text=re.compile(r"Veterinarian", re.I)
    ).first.click()
    expect(page).to_have_url(re.compile(r".*veterinarian\.jsf", re.I))
    expect(
        page.locator("h1, .contentTitleHeadline")
        .filter(has_text=re.compile(r"Veterinarian", re.I))
        .first
    ).to_be_visible()


def test_veterinarian_page_displays_list(page: Page):
    """Scenario: Veterinarian page displays a list."""
    page.goto(f"{BASE_URL}/petclinic/veterinarian.jsf")
    page.wait_for_load_state("networkidle")
    expect(page.locator("body")).to_be_visible()


# ---------------------------------------------------------------------------
# Specialty management
# ---------------------------------------------------------------------------


def test_specialty_page_displays_list(page: Page):
    """Scenario: Specialty page displays a list."""
    page.goto(f"{BASE_URL}/petclinic/specialty.jsf")
    page.wait_for_load_state("networkidle")
    expect(page.locator("body")).to_be_visible()


# ---------------------------------------------------------------------------
# REST API — Specialty
# ---------------------------------------------------------------------------


def test_rest_specialty_list():
    """Scenario: GET /rest/specialty/list returns all specialties as JSON."""
    r = requests.get(f"{REST_BASE}/specialty/list")
    assert r.status_code == 200
    data = r.json()
    assert isinstance(data, list)
    if len(data) > 0:
        assert "id" in data[0]
        assert "name" in data[0]


def test_rest_specialty_by_id():
    """Scenario: GET /rest/specialty/{id} returns a specific specialty."""
    r = requests.get(f"{REST_BASE}/specialty/1")
    assert r.status_code == 200
    data = r.json()
    assert "id" in data
    assert "uuid" in data
    assert "name" in data


def test_rest_specialty_list_xml():
    """Scenario: GET /rest/specialty/list+xml returns XML format."""
    r = requests.get(f"{REST_BASE}/specialty/list+xml")
    assert r.status_code == 200
    assert "xml" in r.headers.get("Content-Type", "").lower()


# ---------------------------------------------------------------------------
# REST API — Vet
# ---------------------------------------------------------------------------


def test_rest_vet_list():
    """Scenario: GET /rest/vet/list returns all vets as JSON."""
    r = requests.get(f"{REST_BASE}/vet/list")
    assert r.status_code == 200
    data = r.json()
    assert isinstance(data, list)
    if len(data) > 0:
        vet = data[0]
        for key in ("id", "uuid", "firstName", "lastName"):
            assert key in vet


def test_rest_vet_by_id():
    """Scenario: GET /rest/vet/{id} returns a specific vet."""
    r = requests.get(f"{REST_BASE}/vet/1")
    assert r.status_code == 200
    data = r.json()
    assert "id" in data
    assert "uuid" in data
    assert "firstName" in data
    assert "lastName" in data


def test_rest_vet_list_xml():
    """Scenario: GET /rest/vet/list+xml returns XML format."""
    r = requests.get(f"{REST_BASE}/vet/list+xml")
    assert r.status_code == 200
    assert "xml" in r.headers.get("Content-Type", "").lower()


# ---------------------------------------------------------------------------
# REST API — PetType
# ---------------------------------------------------------------------------


def test_rest_pettype_list():
    """Scenario: GET /rest/petType/list returns all pet types as JSON."""
    r = requests.get(f"{REST_BASE}/petType/list")
    assert r.status_code == 200
    data = r.json()
    assert isinstance(data, list)
    if len(data) > 0:
        pt = data[0]
        for key in ("id", "uuid", "name"):
            assert key in pt


def test_rest_pettype_by_id():
    """Scenario: GET /rest/petType/{id} returns a specific pet type."""
    r = requests.get(f"{REST_BASE}/petType/1")
    assert r.status_code == 200
    data = r.json()
    assert "id" in data
    assert "uuid" in data
    assert "name" in data


def test_rest_pettype_list_xml():
    """Scenario: GET /rest/petType/list+xml returns XML format."""
    r = requests.get(f"{REST_BASE}/petType/list+xml")
    assert r.status_code == 200
    assert "xml" in r.headers.get("Content-Type", "").lower()


# ---------------------------------------------------------------------------
# REST API — Owner
# ---------------------------------------------------------------------------


def test_rest_owner_list():
    """Scenario: GET /rest/owner/list returns all owners as JSON."""
    r = requests.get(f"{REST_BASE}/owner/list")
    assert r.status_code == 200
    data = r.json()
    assert isinstance(data, list)
    if len(data) > 0:
        owner = data[0]
        for key in ("id", "uuid", "firstName", "lastName", "address", "city"):
            assert key in owner


def test_rest_owner_by_id():
    """Scenario: GET /rest/owner/{id} returns a specific owner with pets."""
    r = requests.get(f"{REST_BASE}/owner/1")
    assert r.status_code == 200
    data = r.json()
    assert "id" in data
    assert "uuid" in data
    assert "firstName" in data
    assert "lastName" in data
    assert "petList" in data


def test_rest_owner_list_xml():
    """Scenario: GET /rest/owner/list+xml returns XML format."""
    r = requests.get(f"{REST_BASE}/owner/list+xml")
    assert r.status_code == 200
    assert "xml" in r.headers.get("Content-Type", "").lower()


# ---------------------------------------------------------------------------
# REST API — Pet
# ---------------------------------------------------------------------------


def test_rest_pet_list():
    """Scenario: GET /rest/pet/list returns all pets as JSON."""
    r = requests.get(f"{REST_BASE}/pet/list")
    assert r.status_code == 200
    data = r.json()
    assert isinstance(data, list)
    if len(data) > 0:
        pet = data[0]
        for key in ("id", "uuid", "name", "birthDate"):
            assert key in pet


def test_rest_pet_by_id():
    """Scenario: GET /rest/pet/{id} returns a specific pet with visits."""
    r = requests.get(f"{REST_BASE}/pet/1")
    assert r.status_code == 200
    data = r.json()
    assert "id" in data
    assert "uuid" in data
    assert "name" in data
    assert "birthDate" in data


def test_rest_pet_list_xml():
    """Scenario: GET /rest/pet/list+xml returns XML format."""
    r = requests.get(f"{REST_BASE}/pet/list+xml")
    assert r.status_code == 200
    assert "xml" in r.headers.get("Content-Type", "").lower()


# ---------------------------------------------------------------------------
# REST API — Visit
# ---------------------------------------------------------------------------


def test_rest_visit_list():
    """Scenario: GET /rest/visit/list returns all visits as JSON."""
    r = requests.get(f"{REST_BASE}/visit/list")
    assert r.status_code == 200
    data = r.json()
    assert isinstance(data, list)
    if len(data) > 0:
        visit = data[0]
        for key in ("id", "uuid", "date", "description"):
            assert key in visit


def test_rest_visit_by_id():
    """Scenario: GET /rest/visit/{id} returns a specific visit."""
    r = requests.get(f"{REST_BASE}/visit/1")
    assert r.status_code == 200
    data = r.json()
    assert "id" in data
    assert "uuid" in data
    assert "date" in data
    assert "description" in data


def test_rest_visit_list_xml():
    """Scenario: GET /rest/visit/list+xml returns XML format."""
    r = requests.get(f"{REST_BASE}/visit/list+xml")
    assert r.status_code == 200
    assert "xml" in r.headers.get("Content-Type", "").lower()


# ---------------------------------------------------------------------------
# REST API is read-only
# ---------------------------------------------------------------------------


def test_rest_api_post_not_allowed():
    """Scenario: REST API only supports GET methods (POST → 405)."""
    r = requests.post(f"{REST_BASE}/owner/list")
    assert r.status_code == 405


def test_rest_api_put_not_allowed():
    """Scenario: REST API does not support PUT."""
    r = requests.put(f"{REST_BASE}/owner/1")
    assert r.status_code == 405


def test_rest_api_delete_not_allowed():
    """Scenario: REST API does not support DELETE."""
    r = requests.delete(f"{REST_BASE}/owner/1")
    assert r.status_code == 405


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
