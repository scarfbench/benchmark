import uuid
import pytest
import re
import requests
from playwright.sync_api import Page, expect


BASE_URL = "http://localhost:8080"


# ---------------------------------------------------------------------------
# Helper: build a valid PlaceOrderCommand
# ---------------------------------------------------------------------------

def build_order_command(barista_items=None, kitchen_items=None):
    """Build a PlaceOrderCommand JSON payload for the Quarkus coffeeshop."""
    if barista_items is None and kitchen_items is None:
        barista_items = [{"item": "COFFEE_BLACK", "name": "Duke", "price": 3.00}]
        kitchen_items = []
    return {
        "id": str(uuid.uuid4()),
        "commandType": "PLACE_ORDER",
        "orderSource": "WEB",
        "storeId": "ATLANTA",
        "baristaItems": barista_items or [],
        "kitchenItems": kitchen_items or [],
    }


# ---------------------------------------------------------------------------
# Order placement
# ---------------------------------------------------------------------------

def test_place_drink_order():
    payload = build_order_command(
        barista_items=[{"item": "LATTE", "name": "Duke", "price": 4.50}],
        kitchen_items=[],
    )
    r = requests.post(f"{BASE_URL}/api/order", json=payload)
    assert r.status_code == 202


def test_place_food_order():
    payload = build_order_command(
        barista_items=[],
        kitchen_items=[{"item": "CROISSANT", "name": "Alice", "price": 3.25}],
    )
    r = requests.post(f"{BASE_URL}/api/order", json=payload)
    assert r.status_code == 202


def test_place_mixed_order():
    payload = build_order_command(
        barista_items=[{"item": "CAPPUCCINO", "name": "Raju", "price": 4.50}],
        kitchen_items=[{"item": "MUFFIN", "name": "Raju", "price": 3.00}],
    )
    r = requests.post(f"{BASE_URL}/api/order", json=payload)
    assert r.status_code == 202


# ---------------------------------------------------------------------------
# Message endpoint
# ---------------------------------------------------------------------------

def test_message_endpoint():
    r = requests.post(
        f"{BASE_URL}/api/message",
        json="test-message",
        headers={"Content-Type": "application/json"},
    )
    assert r.status_code in (200, 202, 204)


# ---------------------------------------------------------------------------
# SSE dashboard stream (just check it responds)
# ---------------------------------------------------------------------------

def test_dashboard_stream_endpoint():
    r = requests.get(f"{BASE_URL}/dashboard/stream", stream=True, timeout=5)
    assert r.status_code == 200
    r.close()


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
