import time
import pytest
import re
import requests
from playwright.sync_api import Page, expect


ORDERS_URL = "http://localhost:8081"
BARISTA_URL = "http://localhost:8082"
KITCHEN_URL = "http://localhost:8083"
HOMEPAGE_URL = "http://localhost:8080"


# ---------------------------------------------------------------------------
# Helper
# ---------------------------------------------------------------------------

def place_order(customer="Duke", item="latte", quantity=1):
    return requests.post(
        f"{ORDERS_URL}/orders-service/api/orders",
        json={"customer": customer, "item": item, "quantity": quantity},
    )


def poll_order_status(order_id, expected_status="READY", timeout=40):
    deadline = time.time() + timeout
    while time.time() < deadline:
        r = requests.get(f"{ORDERS_URL}/orders-service/api/orders/{order_id}")
        if r.status_code == 200:
            data = r.json()
            if data.get("status") == expected_status:
                return data
        time.sleep(2)
    return None


# ---------------------------------------------------------------------------
# Health checks
# ---------------------------------------------------------------------------

def test_orders_service_health():
    r = requests.get(f"{ORDERS_URL}/health")
    assert r.status_code == 200
    body = r.json()
    assert body["status"] == "UP"
    checks = body.get("checks", [])
    names = [c.get("name") for c in checks]
    assert "orders-service" in names


def test_barista_service_health():
    r = requests.get(f"{BARISTA_URL}/health")
    assert r.status_code == 200
    body = r.json()
    assert body["status"] == "UP"
    checks = body.get("checks", [])
    names = [c.get("name") for c in checks]
    assert "barista-service" in names


def test_kitchen_service_health():
    r = requests.get(f"{KITCHEN_URL}/health")
    assert r.status_code == 200
    body = r.json()
    assert body["status"] == "UP"
    checks = body.get("checks", [])
    names = [c.get("name") for c in checks]
    assert "kitchen-service" in names


# ---------------------------------------------------------------------------
# Service status endpoints
# ---------------------------------------------------------------------------

def test_barista_status():
    r = requests.get(f"{BARISTA_URL}/barista-service/api/status")
    assert r.status_code == 200
    assert r.text.strip() == "barista ok"


def test_kitchen_status():
    r = requests.get(f"{KITCHEN_URL}/kitchen-service/api/status")
    assert r.status_code == 200
    assert r.text.strip() == "kitchen ok"


# ---------------------------------------------------------------------------
# Order creation
# ---------------------------------------------------------------------------

def test_place_drink_order():
    r = place_order(customer="Duke", item="latte", quantity=1)
    assert r.status_code == 202
    body = r.json()
    assert "id" in body


def test_place_food_order():
    r = place_order(customer="Alice", item="sandwich", quantity=2)
    assert r.status_code == 202
    body = r.json()
    assert "id" in body


def test_order_persisted_with_placed_status():
    r = place_order(item="latte")
    assert r.status_code == 202
    order_id = r.json()["id"]
    r2 = requests.get(f"{ORDERS_URL}/orders-service/api/orders/{order_id}")
    assert r2.status_code == 200
    assert r2.json()["status"] == "PLACED"


def test_order_has_timestamps():
    r = place_order()
    assert r.status_code == 202
    order_id = r.json()["id"]
    r2 = requests.get(f"{ORDERS_URL}/orders-service/api/orders/{order_id}")
    assert r2.status_code == 200
    body = r2.json()
    assert body.get("created") is not None
    assert body.get("updated") is not None


# ---------------------------------------------------------------------------
# Input validation
# ---------------------------------------------------------------------------

def test_blank_customer_rejected():
    r = place_order(customer="", item="latte", quantity=1)
    assert r.status_code == 400
    body = r.json()
    errors = body.get("errors", [])
    fields = [e.get("field") for e in errors]
    assert "customer" in fields


def test_blank_item_rejected():
    r = place_order(customer="Duke", item="", quantity=1)
    assert r.status_code == 400
    body = r.json()
    errors = body.get("errors", [])
    fields = [e.get("field") for e in errors]
    assert "item" in fields


def test_quantity_less_than_1_rejected():
    r = place_order(customer="Duke", item="latte", quantity=0)
    assert r.status_code == 400
    body = r.json()
    errors = body.get("errors", [])
    fields = [e.get("field") for e in errors]
    assert "quantity" in fields


def test_validation_error_structure():
    r = place_order(customer="", item="", quantity=0)
    assert r.status_code == 400
    body = r.json()
    assert "errors" in body
    assert isinstance(body["errors"], list)
    for error in body["errors"]:
        assert "field" in error
        assert "message" in error


# ---------------------------------------------------------------------------
# Retrieve order
# ---------------------------------------------------------------------------

def test_get_order_by_id():
    r = place_order(customer="Duke", item="latte", quantity=1)
    assert r.status_code == 202
    order_id = r.json()["id"]
    r2 = requests.get(f"{ORDERS_URL}/orders-service/api/orders/{order_id}")
    assert r2.status_code == 200
    body = r2.json()
    for field in ["id", "customer", "item", "quantity", "status", "created", "updated"]:
        assert field in body, f"Missing field: {field}"


# ---------------------------------------------------------------------------
# End-to-end drink order flow
# ---------------------------------------------------------------------------

def test_e2e_drink_order():
    r = place_order(customer="Raju", item="latte", quantity=1)
    assert r.status_code == 202
    order_id = r.json()["id"]
    result = poll_order_status(order_id, "READY", timeout=40)
    assert result is not None, "Drink order did not reach READY status within 40s"
    assert result["status"] == "READY"


# ---------------------------------------------------------------------------
# End-to-end food order flow
# ---------------------------------------------------------------------------

def test_e2e_food_order():
    r = place_order(customer="Raju", item="sandwich", quantity=1)
    assert r.status_code == 202
    order_id = r.json()["id"]
    result = poll_order_status(order_id, "READY", timeout=40)
    assert result is not None, "Food order did not reach READY status within 40s"
    assert result["status"] == "READY"


# ---------------------------------------------------------------------------
# OpenAPI
# ---------------------------------------------------------------------------

def test_openapi_ui():
    r = requests.get(f"{ORDERS_URL}/openapi/ui/")
    assert r.status_code == 200
    assert len(r.text) > 0


# ---------------------------------------------------------------------------
# Web UI (Playwright)
# ---------------------------------------------------------------------------

def test_coffeeshop_homepage(page: Page):
    page.goto(f"{HOMEPAGE_URL}/")
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
    page.goto(f"{HOMEPAGE_URL}/")
    expect(
        page.locator("a").filter(has_text=re.compile("About", re.I))
    ).to_be_attached()


def test_menu_link_exists(page: Page):
    page.goto(f"{HOMEPAGE_URL}/")
    expect(
        page.locator("a").filter(has_text=re.compile("Menu", re.I))
    ).to_be_attached()


def test_navigation_menu_persistence(page: Page):
    page.goto(f"{HOMEPAGE_URL}/")
    page.wait_for_load_state("networkidle")
    menu_items = page.locator("nav a").first
    expect(menu_items).to_be_visible()


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
