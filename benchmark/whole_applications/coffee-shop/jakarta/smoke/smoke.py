import pytest
import re
import requests
from playwright.sync_api import Page, expect


BASE_URL = "http://localhost:9080"


# ---------------------------------------------------------------------------
# Helper
# ---------------------------------------------------------------------------

def place_order(customer="Duke", item="latte", quantity=1):
    return requests.post(
        f"{BASE_URL}/api/orders",
        json={"customer": customer, "item": item, "quantity": quantity},
    )


# ---------------------------------------------------------------------------
# Health check  (MicroProfile Health on orders-service)
# ---------------------------------------------------------------------------

def test_orders_service_health():
    r = requests.get(f"{BASE_URL}/health")
    assert r.status_code == 200
    body = r.json()
    assert body["status"] == "UP"
    checks = body.get("checks", [])
    names = [c.get("name") for c in checks]
    assert "orders-service" in names


# ---------------------------------------------------------------------------
# Input validation  (Bean Validation on OrderRequest — runs before DB access)
# ---------------------------------------------------------------------------

def test_blank_customer_rejected():
    r = place_order(customer="", item="latte", quantity=1)
    assert r.status_code == 400


def test_blank_item_rejected():
    r = place_order(customer="Duke", item="", quantity=1)
    assert r.status_code == 400


def test_quantity_less_than_1_rejected():
    r = place_order(customer="Duke", item="latte", quantity=0)
    assert r.status_code == 400


# ---------------------------------------------------------------------------
# OpenAPI UI
# ---------------------------------------------------------------------------

def test_openapi_ui():
    r = requests.get(f"{BASE_URL}/openapi/ui/")
    assert r.status_code == 200
    assert len(r.text) > 0


# ---------------------------------------------------------------------------
# Web UI (Playwright)
# ---------------------------------------------------------------------------

def test_coffeeshop_homepage(page: Page):
    page.goto(f"{BASE_URL}/")
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
    page.goto(f"{BASE_URL}/")
    expect(
        page.locator("a").filter(has_text=re.compile("About", re.I))
    ).to_be_attached()


def test_menu_link_exists(page: Page):
    page.goto(f"{BASE_URL}/")
    expect(
        page.locator("a").filter(has_text=re.compile("Menu", re.I))
    ).to_be_attached()


def test_navigation_menu_persistence(page: Page):
    page.goto(f"{BASE_URL}/")
    page.wait_for_load_state("domcontentloaded")
    menu_items = page.locator("nav a").first
    expect(menu_items).to_be_visible()


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
