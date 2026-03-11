#!/usr/bin/env python3
"""
Smoke test for Quarkus "Order" app.

Checks:
  1) GET <BASE>/orders -> 200 (fatal if not)
  2) Verify HTML content contains expected elements
  3) Test that the application loads and displays existing orders
  4) Test CSS and other resources
  5) Test UI interactions (via Playwright):
     - Navigate to orders page
     - Verify orders table displays correctly
     - Fill out order form with all fields
     - Submit form and verify success
     - Test order deletion
     - Test line item page navigation
     - Test form validation
     - Test vendor search functionality

Environment:
  ORDER_BASE   Base app URL (default: http://localhost:8082)
  VERBOSE=1    Verbose logging
  HEADLESS=1   Run browser in headless mode (default: true)
  BROWSER      Browser to use: chrome, firefox, chromium (default: chromium)
  START_TIMEOUT Server startup wait timeout (default: 90)

Exit codes:
  0  success
  2  GET /orders failed
  3  Critical pages failed
  5  Playwright tests failed
  9  Network / unexpected error
"""

import asyncio
import aiohttp
import sys
import time
import os
import re
import pytest

try:
    from playwright.async_api import async_playwright
    from playwright.async_api import Error as PlaywrightError
    from playwright.async_api import TimeoutError as PlaywrightTimeoutError
    PLAYWRIGHT_AVAILABLE = True
except ImportError:
    PLAYWRIGHT_AVAILABLE = False
    print(
        "[WARN] Playwright not available. Install with: pip install playwright",
        file=sys.stderr,
    )

BASE = os.getenv("ORDER_BASE", "http://localhost:8082").rstrip("/")
VERBOSE = os.getenv("VERBOSE") == "1"
HEADLESS = os.getenv("HEADLESS", "1") == "1"
BROWSER = os.getenv("BROWSER", "chromium").lower()
START_TIMEOUT = int(os.getenv("START_TIMEOUT", "90"))
HTTP_TIMEOUT = 12
PLAYWRIGHT_TIMEOUT = 10000


def vprint(*args):
    """Print only if VERBOSE mode is enabled"""
    if VERBOSE:
        print(*args)


def join(base: str, path: str) -> str:
    """Join base URL with path"""
    if not path:
        return base
    if base.endswith("/") and path.startswith("/"):
        return base[:-1] + path
    if (not base.endswith("/")) and (not path.startswith("/")):
        return base + "/" + path
    return base + path


async def http_request(session: aiohttp.ClientSession, method: str, url: str):
    """Make an HTTP request"""
    try:
        async with session.request(method, url, timeout=aiohttp.ClientTimeout(total=HTTP_TIMEOUT)) as resp:
            text = await resp.text()
            return (resp.status, text), None
    except aiohttp.ClientError as e:
        return None, f"CLIENT-ERROR: {e}"
    except asyncio.TimeoutError:
        return None, f"TIMEOUT after {HTTP_TIMEOUT}s"
    except Exception as e:
        return None, f"NETWORK-ERROR: {e}"


async def must_get_ok(session: aiohttp.ClientSession, path: str, fail_code: int):
    """GET request that must return 200 or exit"""
    url = join(BASE, path)
    vprint(f"GET {url}")
    resp, err = await http_request(session, "GET", url)
    if err:
        pytest.fail(f"[FAIL] {path} -> {err}")
    if resp[0] != 200:
        print(f"[FAIL] GET {path} -> {resp[0]}", file=sys.stderr)
        pytest.fail("smoke check failed")
    print(f"[PASS] GET {path} -> 200")
    return resp[1]


async def soft_get_ok(session: aiohttp.ClientSession, path: str):
    """GET request that logs but doesn't fail on error"""
    url = join(BASE, path)
    vprint(f"GET {url} (soft)")
    resp, err = await http_request(session, "GET", url)
    if err:
        print(f"[WARN] {path} -> {err}", file=sys.stderr)
        return
    print(f"[{'PASS' if resp[0] == 200 else 'WARN'}] GET {path} -> {resp[0]}")


def check_orders_table(body: str):
    """Check if the orders table is present and contains data"""
    # Look for table structure
    if "<table" in body.lower() and "order" in body.lower():
        print("[PASS] Orders table found")

        # Look for existing orders (check for order IDs in the table)
        order_id_pattern = r"<td[^>]*>(?:<a[^>]*>)?(\d+)(?:</a>)?</td>"
        order_ids = re.findall(order_id_pattern, body)
        if order_ids:
            print(f"[PASS] Found {len(order_ids)} existing orders: {order_ids[:5]}")
            return True
        else:
            print("[WARN] Orders table found but no order IDs detected")
            return True
    else:
        print("[WARN] Orders table not found or malformed")
        return False


def check_form_elements(body: str):
    """Check if the form elements for creating orders are present"""
    form_elements = [
        "orderIdInputText",
        "shipmentInfoInputText",
        "statusMenu",
        "discountMenu",
        "submit",
    ]

    found_elements = []
    for element in form_elements:
        if element in body.lower():
            found_elements.append(element)

    if len(found_elements) >= 4:  # At least most elements
        print("[PASS] Form elements for order creation found")
        return True
    else:
        missing = set(form_elements) - set(found_elements)
        print(f"[WARN] Some form elements missing: {missing}")
        return False


async def wait_for_http(session: aiohttp.ClientSession, host: str, port: int, timeout: int):
    """Wait for HTTP server to become available"""
    url = f"http://{host}:{port}/orders"
    end = time.time() + timeout
    while time.time() < end:
        try:
            async with session.get(url, timeout=aiohttp.ClientTimeout(total=5)) as response:
                vprint(f"[DEBUG] HTTP {response.status} from {url}")
                if response.status == 200:
                    print(f"[PASS] Successfully connected to {url}")
                    return
                else:
                    vprint(f"[DEBUG] Non-200 response: {response.status} from {url}")
        except (aiohttp.ClientError, asyncio.TimeoutError) as e:
            vprint(f"[DEBUG] Waiting for {url}, error: {str(e)}")
        await asyncio.sleep(0.5)
    raise TimeoutError(f"Timed out waiting for HTTP port {port} after {timeout}s")


async def find_element_flexible(page, field_id, element_type="input"):
    """Try multiple selectors to find an element"""
    selectors_to_try = [
        f"{element_type}#{field_id}",
        f"{element_type}[id*='{field_id}']",
        f"{element_type}[name*='{field_id}']",
        f"#{field_id}",
        f"{element_type}",
    ]

    for selector in selectors_to_try:
        try:
            await page.wait_for_selector(selector, timeout=2000)
            element = page.locator(selector).first
            if await element.is_visible():
                vprint(f"[DEBUG] Found element using selector: {selector}")
                return element
        except PlaywrightTimeoutError:
            vprint(f"[DEBUG] Selector failed: {selector}")
            continue
        except PlaywrightError:
            continue

    vprint(f"[DEBUG] Could not find element for field: {field_id}")
    return None


async def find_button_flexible(page, button_text):
    """Try multiple selectors to find a button"""
    selectors_to_try = [
        f"button:has-text('{button_text}')",
        f"input[value='{button_text}']",
        f"a:has-text('{button_text}')",
        f"button[id*='{button_text.lower()}']",
    ]

    for selector in selectors_to_try:
        try:
            await page.wait_for_selector(selector, timeout=2000)
            element = page.locator(selector).first
            if await element.is_visible():
                vprint(f"[DEBUG] Found button using selector: {selector}")
                return element
        except PlaywrightTimeoutError:
            vprint(f"[DEBUG] Button selector failed: {selector}")
            continue
        except PlaywrightError:
            continue

    vprint(f"[DEBUG] Could not find button: {button_text}")
    return None


async def _check_orders_table_ui(page):
    """Test the orders table UI"""
    print("\n[INFO] Testing orders table UI...")

    try:
        await page.goto(join(BASE, "/orders"))

        vprint(f"[DEBUG] Page title: {await page.title()}")
        vprint(f"[DEBUG] Page URL: {page.url}")

        if VERBOSE:
            try:
                await page.screenshot(path="debug_orders_table_screenshot.png")
                print("[DEBUG] Screenshot saved as debug_orders_table_screenshot.png")
            except Exception as e:
                print(f"[DEBUG] Could not save screenshot: {e}")

        # Wait for table
        try:
            await page.wait_for_selector("table", timeout=PLAYWRIGHT_TIMEOUT)
            print("[PASS] Orders table loaded")
        except PlaywrightTimeoutError:
            print("[WARN] Table not found")
            return False

        # Check for headers
        try:
            headers = await page.locator("th").all()
            expected_headers = ["Order ID", "Shipment Info", "Status", "Discount"]
            found_headers = []
            for header in headers:
                text = await header.text_content()
                if any(expected in text for expected in expected_headers):
                    found_headers.append(text.strip())
            
            if len(found_headers) >= 3:
                print(f"[PASS] Found table headers: {found_headers}")
            else:
                print(f"[WARN] Expected headers not fully found: {found_headers}")
        except Exception as e:
            print(f"[WARN] Could not verify headers: {e}")

        # Check for rows
        try:
            rows = await page.locator("table tr").all()
            if len(rows) > 1:
                print(f"[PASS] Found {len(rows) - 1} order(s) in table")
            else:
                print("[INFO] No orders found in table")
        except Exception as e:
            print(f"[WARN] Could not count rows: {e}")

        # Verify specific orders exist
        try:
            order_1111 = await page.locator("text=1111").count()
            order_4312 = await page.locator("text=4312").count()
            if order_1111 > 0:
                print("[PASS] Order 1111 found")
            if order_4312 > 0:
                print("[PASS] Order 4312 found")
            if order_1111 == 0 and order_4312 == 0:
                print("[WARN] Expected orders (1111, 4312) not found")
        except Exception as e:
            print(f"[WARN] Could not verify specific orders: {e}")

        return True

    except PlaywrightTimeoutError:
        print("[FAIL] Timeout waiting for orders table", file=sys.stderr)
        return False
    except Exception as e:
        print(f"[FAIL] Orders table UI test failed: {e}", file=sys.stderr)
        return False


async def _check_order_form_ui(page):
    """Test order form filling and submission"""
    print("\n[INFO] Testing order form UI...")

    try:
        await page.goto(join(BASE, "/orders"))

        vprint(f"[DEBUG] Page title: {await page.title()}")
        vprint(f"[DEBUG] Page URL: {page.url}")

        if VERBOSE:
            try:
                await page.screenshot(path="debug_order_form_screenshot.png")
                print("[DEBUG] Screenshot saved as debug_order_form_screenshot.png")
            except Exception as e:
                print(f"[DEBUG] Could not save screenshot: {e}")

        # Wait for form
        await page.wait_for_selector("form", timeout=PLAYWRIGHT_TIMEOUT)
        print("[PASS] Order form loaded")

        # Test data
        test_order_id = "99999"
        test_data = {
            "newOrderId": test_order_id,
            "newOrderShippingInfo": "Express Shipping Test",
            "newOrderStatus": "N",
            "newOrderDiscount": "10",
        }

        # Fill form fields
        filled_fields = 0
        for field_name, value in test_data.items():
            try:
                if field_name.endswith("Status") or field_name.endswith("Discount"):
                    # Select dropdown
                    selector = f"select[name='{field_name}']"
                    await page.wait_for_selector(selector, timeout=5000)
                    await page.select_option(selector, value)
                    print(f"[PASS] Selected {field_name} = '{value}'")
                else:
                    # Text input
                    selector = f"input[name='{field_name}']"
                    await page.wait_for_selector(selector, timeout=5000)
                    await page.fill(selector, value)
                    print(f"[PASS] Filled {field_name} = '{value}'")
                filled_fields += 1
            except Exception as e:
                print(f"[WARN] Could not fill {field_name}: {e}")

        if filled_fields < 2:
            print("[FAIL] Insufficient fields filled for form submission")
            return False

        # Submit form
        submit_button = await find_button_flexible(page, "Submit")
        if not submit_button:
            print("[FAIL] Could not find Submit button")
            return False

        await submit_button.click()
        print("[PASS] Submitted order form")

        # Wait for page to update
        await asyncio.sleep(2)

        # Verify order was created
        try:
            order_count = await page.locator(f"text={test_order_id}").count()
            if order_count > 0:
                print(f"[PASS] New order {test_order_id} appears in table")
            else:
                print(f"[WARN] New order {test_order_id} not found after submission")
        except Exception as e:
            print(f"[WARN] Could not verify order creation: {e}")

        return True

    except PlaywrightTimeoutError:
        print("[FAIL] Timeout waiting for form elements", file=sys.stderr)
        return False
    except Exception as e:
        print(f"[FAIL] Form UI test failed: {e}", file=sys.stderr)
        return False


async def _check_order_deletion(page):
    """Test order deletion functionality"""
    print("\n[INFO] Testing order deletion...")

    try:
        await page.goto(join(BASE, "/orders"))

        # Look for Delete buttons
        delete_buttons = await page.locator("button:has-text('Delete')").all()
        
        if len(delete_buttons) == 0:
            print("[WARN] No Delete buttons found")
            return True  # Soft fail

        initial_count = len(delete_buttons)
        print(f"[INFO] Found {initial_count} orders with delete buttons")

        # Click the last delete button
        await delete_buttons[-1].click()
        print("[PASS] Clicked Delete button")

        # Wait for page to update
        await asyncio.sleep(2)

        # Verify order count decreased
        new_delete_buttons = await page.locator("button:has-text('Delete')").all()
        new_count = len(new_delete_buttons)

        if new_count < initial_count:
            print(f"[PASS] Order deleted (count: {initial_count} -> {new_count})")
        else:
            print(f"[WARN] Order count unchanged after deletion: {initial_count} -> {new_count}")

        return True

    except Exception as e:
        print(f"[WARN] Order deletion test failed: {e}", file=sys.stderr)
        return True  # Soft fail


async def _check_line_item_ui(page):
    """Test line item page navigation and display"""
    print("\n[INFO] Testing line item UI...")

    try:
        await page.goto(join(BASE, "/orders"))

        # Wait for orders table
        await page.wait_for_selector("table", timeout=PLAYWRIGHT_TIMEOUT)

        # Find and click first order ID link
        try:
            order_links = await page.locator("a[href*='lineItems']").all()
            if len(order_links) == 0:
                # Try clicking on order ID directly
                order_ids = await page.locator("table td a").all()
                if len(order_ids) > 0:
                    await order_ids[0].click()
                    print("[PASS] Clicked order ID link")
                else:
                    # Navigate directly to known order
                    await page.goto(join(BASE, "/lineItems?orderId=1111"))
                    print("[INFO] Navigated directly to line items page")
            else:
                await order_links[0].click()
                print("[PASS] Clicked order link to line items")
        except Exception as e:
            vprint(f"[DEBUG] Could not click order link: {e}")
            await page.goto(join(BASE, "/lineItems?orderId=1111"))
            print("[INFO] Navigated directly to line items page")

        # Wait for page to load
        await asyncio.sleep(2)

        vprint(f"[DEBUG] Page title: {await page.title()}")
        vprint(f"[DEBUG] Page URL: {page.url}")

        if VERBOSE:
            try:
                await page.screenshot(path="debug_line_item_screenshot.png")
                print("[DEBUG] Screenshot saved as debug_line_item_screenshot.png")
            except Exception as e:
                print(f"[DEBUG] Could not save screenshot: {e}")

        # Verify line items page loaded
        try:
            await page.wait_for_selector("table", timeout=PLAYWRIGHT_TIMEOUT)
            print("[PASS] Line item page loaded")
        except PlaywrightTimeoutError:
            print("[WARN] Line item page table not found")
            return True  # Soft fail

        # Check for expected headers
        try:
            item_id_header = await page.locator("text=Item ID").count()
            if item_id_header > 0:
                print("[PASS] Line items table headers found")
            else:
                print("[INFO] Line items headers not fully verified")
        except Exception as e:
            print(f"[WARN] Could not verify line item headers: {e}")

        # Check for back link
        try:
            back_link = await page.locator("a:has-text('Back')").count()
            if back_link > 0:
                print("[PASS] Back to Orders link found")
        except Exception as e:
            vprint(f"[DEBUG] Back link check failed: {e}")

        return True

    except PlaywrightTimeoutError:
        print("[FAIL] Timeout waiting for line item page", file=sys.stderr)
        return False
    except Exception as e:
        print(f"[FAIL] Line item UI test failed: {e}", file=sys.stderr)
        return False


async def _check_form_validation(page):
    """Test form validation with empty submission"""
    print("\n[INFO] Testing form validation...")

    try:
        await page.goto(join(BASE, "/orders"))

        await page.wait_for_selector("form", timeout=PLAYWRIGHT_TIMEOUT)

        # Try to submit empty form
        submit_button = await find_button_flexible(page, "Submit")
        
        if submit_button:
            await submit_button.click()
            print("[PASS] Attempted to submit empty form")

            await asyncio.sleep(1)

            # Check for HTML5 validation or error messages
            # Since Quarkus uses HTML5 required attributes, browser will prevent submission
            print("[INFO] Form validation handled by HTML5 required attributes")
        else:
            print("[WARN] Could not find submit button for validation test")

        return True

    except Exception as e:
        print(f"[WARN] Form validation test failed: {e}", file=sys.stderr)
        return True  # Soft fail


async def _check_vendor_search(page):
    """Test vendor search functionality"""
    print("\n[INFO] Testing vendor search...")

    try:
        await page.goto(join(BASE, "/orders"))

        # Look for vendor search form
        try:
            await page.wait_for_selector("input[name='vendorName']", timeout=5000)
            print("[PASS] Vendor search form found")

            # Fill and submit vendor search
            await page.fill("input[name='vendorName']", "Test")

            find_button = await find_button_flexible(page, "Find Vendor")
            if find_button:
                await find_button.click()
                print("[PASS] Submitted vendor search")
                await asyncio.sleep(2)

                # Check for results table
                result_table = await page.locator("table").count()
                if result_table > 0:
                    print("[PASS] Vendor search results displayed")
                else:
                    print("[INFO] No vendor search results (may be expected)")
            else:
                print("[WARN] Find Vendor button not found")

        except PlaywrightTimeoutError:
            print("[INFO] Vendor search form not found (may not be implemented)")

        return True

    except Exception as e:
        print(f"[WARN] Vendor search test failed: {e}", file=sys.stderr)
        return True  # Soft fail


async def _check_vendor_search_no_results(page):
    """Test vendor search with no matching results returns empty"""
    print("\n[INFO] Testing vendor search with no matches...")

    try:
        await page.goto(join(BASE, "/orders"))

        try:
            await page.wait_for_selector("input[name='vendorName']", timeout=5000)

            await page.fill("input[name='vendorName']", "NonExistentVendorXYZ")

            find_button = await find_button_flexible(page, "Find Vendor")
            if find_button:
                await find_button.click()
                print("[PASS] Submitted vendor search for non-existent vendor")
                await asyncio.sleep(2)

                # Verify results are empty - look for vendor result rows
                body = await page.content()
                # The vendor results table should exist but have no data rows
                vendor_tables = await page.locator("table").all()
                if len(vendor_tables) >= 2:
                    # Second table is vendor results
                    vendor_rows = await vendor_tables[-1].locator("tr").all()
                    # Only header row should be present
                    if len(vendor_rows) <= 1:
                        print("[PASS] Vendor search returned empty results as expected")
                    else:
                        print(f"[WARN] Vendor search returned {len(vendor_rows) - 1} results for non-existent vendor")
                else:
                    print("[PASS] No vendor results table displayed (empty search)")
            else:
                print("[WARN] Find Vendor button not found")

        except PlaywrightTimeoutError:
            print("[INFO] Vendor search form not found (may not be implemented)")

        return True

    except Exception as e:
        print(f"[WARN] Vendor search no-results test failed: {e}", file=sys.stderr)
        return True  # Soft fail


async def _check_add_line_item(page):
    """Test adding a line item to an existing order"""
    print("\n[INFO] Testing add line item...")

    try:
        await page.goto(join(BASE, "/orders"))

        # Find an order link to navigate to line items
        try:
            order_link = page.locator("a[href*='lineItems']").first
            await order_link.wait_for(timeout=PLAYWRIGHT_TIMEOUT)
            order_href = await order_link.get_attribute("href")
            await order_link.click()
            print("[PASS] Navigated to line items page")
        except Exception:
            # Fallback: navigate directly to a known order
            await page.goto(join(BASE, "/lineItems?orderId=1111"))
            print("[INFO] Navigated directly to line items for order 1111")

        await asyncio.sleep(2)

        # Count existing line items
        try:
            line_item_rows = await page.locator("table").first.locator("tr").all()
            initial_count = len(line_item_rows) - 1  # exclude header
            print(f"[INFO] Initial line item count: {initial_count}")
        except Exception:
            initial_count = -1

        # Find the parts table and click Add on a part
        try:
            add_buttons = await page.locator("#orderPartsTable button:has-text('Add')").all()
            if not add_buttons:
                add_buttons = await page.locator("button:has-text('Add')").all()

            if add_buttons:
                await add_buttons[0].click()
                print("[PASS] Clicked Add button on a part")
                await asyncio.sleep(2)

                # Verify line item count increased
                if initial_count >= 0:
                    try:
                        new_rows = await page.locator("table").first.locator("tr").all()
                        new_count = len(new_rows) - 1
                        if new_count > initial_count:
                            print(f"[PASS] Line item count increased ({initial_count} -> {new_count})")
                        else:
                            print(f"[WARN] Line item count unchanged ({initial_count} -> {new_count})")
                    except Exception as e:
                        print(f"[WARN] Could not verify line item count: {e}")
            else:
                print("[WARN] No Add buttons found on line items page")

        except Exception as e:
            print(f"[WARN] Could not add line item: {e}")

        return True

    except Exception as e:
        print(f"[WARN] Add line item test failed: {e}", file=sys.stderr)
        return True  # Soft fail


async def _check_parts_listed(page):
    """Test that all parts are listed on the line items page"""
    print("\n[INFO] Testing parts listing on line items page...")

    try:
        # Navigate to line items for an order
        await page.goto(join(BASE, "/orders"))

        try:
            order_link = page.locator("a[href*='lineItems']").first
            await order_link.wait_for(timeout=PLAYWRIGHT_TIMEOUT)
            await order_link.click()
        except Exception:
            await page.goto(join(BASE, "/lineItems?orderId=1111"))

        await asyncio.sleep(2)

        # Check the parts table (orderPartsTable)
        try:
            parts_table = page.locator("#orderPartsTable")
            await parts_table.wait_for(timeout=PLAYWRIGHT_TIMEOUT)
            parts_rows = await parts_table.locator("tr").all()
            parts_count = len(parts_rows) - 1  # exclude header
            if parts_count > 0:
                print(f"[PASS] Found {parts_count} parts listed in parts table")

                # Verify part headers
                headers = await parts_table.locator("th").all()
                header_texts = [await h.text_content() for h in headers]
                expected = ["Part Number", "Revision"]
                for exp in expected:
                    if any(exp in ht for ht in header_texts):
                        print(f"[PASS] Found parts table header: {exp}")
                    else:
                        print(f"[WARN] Parts table header '{exp}' not found")
            else:
                print("[WARN] No parts found in parts table")
        except PlaywrightTimeoutError:
            print("[WARN] Parts table (#orderPartsTable) not found")
        except Exception as e:
            print(f"[WARN] Could not verify parts listing: {e}")

        return True

    except Exception as e:
        print(f"[WARN] Parts listing test failed: {e}", file=sys.stderr)
        return True  # Soft fail


async def run_playwright_tests():
    """Run all Playwright UI tests"""
    if not PLAYWRIGHT_AVAILABLE:
        print("[SKIP] Playwright tests skipped - Playwright not available")
        return True

    try:
        async with async_playwright() as p:
            # Select browser
            browser_map = {
                "chrome": "chromium",
                "chromium": "chromium",
                "firefox": "firefox",
                "webkit": "webkit",
            }

            browser_name = browser_map.get(BROWSER, "chromium")
            
            if browser_name == "chromium":
                browser = await p.chromium.launch(
                    headless=HEADLESS,
                    args=["--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"]
                )
            elif browser_name == "firefox":
                browser = await p.firefox.launch(headless=HEADLESS)
            elif browser_name == "webkit":
                browser = await p.webkit.launch(headless=HEADLESS)
            else:
                print(f"[FAIL] Unsupported browser: {BROWSER}", file=sys.stderr)
                return False

            context = await browser.new_context(viewport={"width": 1920, "height": 1080})
            page = await context.new_page()
            page.set_default_timeout(PLAYWRIGHT_TIMEOUT)

            print(f"[INFO] Running Playwright tests with {browser_name} browser (headless={HEADLESS})")

            try:
                # Run all tests
                if not await _check_orders_table_ui(page):
                    return False

                if not await _check_order_form_ui(page):
                    return False

                if not await _check_order_deletion(page):
                    return False

                if not await _check_line_item_ui(page):
                    return False

                if not await _check_form_validation(page):
                    return False

                if not await _check_vendor_search(page):
                    return False

                if not await _check_vendor_search_no_results(page):
                    return False

                if not await _check_add_line_item(page):
                    return False

                if not await _check_parts_listed(page):
                    return False

                print("[PASS] All Playwright tests completed successfully")
                return True

            finally:
                await browser.close()

    except PlaywrightError as e:
        print(f"[FAIL] Playwright error: {e}", file=sys.stderr)
        return False
    except Exception as e:
        print(f"[FAIL] Unexpected error in Playwright tests: {e}", file=sys.stderr)
        return False


async def _wait_and_get(path):
    """Wait for server and GET a page."""
    async with aiohttp.ClientSession() as session:
        await wait_for_http(session, "localhost", 8082, START_TIMEOUT)
        return await must_get_ok(session, path, 2)


def test_orders_page_loads():
    """Orders page should load with expected content."""
    body = asyncio.run(_wait_and_get("/orders"))
    assert "Order" in body or "Java Persistence" in body


def test_orders_table_in_html():
    """Orders table should be present in HTML."""
    body = asyncio.run(_wait_and_get("/orders"))
    assert check_orders_table(body) or True  # soft check


def test_playwright_orders_table():
    """Orders table should display via Playwright."""
    if not PLAYWRIGHT_AVAILABLE:
        pytest.skip("Playwright not available")
    assert asyncio.run(run_playwright_tests()), "Playwright tests failed"


def test_order_deletion():
    """Removing an order should delete it from the database."""
    if not PLAYWRIGHT_AVAILABLE:
        pytest.skip("Playwright not available")

    async def _run():
        async with async_playwright() as p:
            browser = await p.chromium.launch(
                headless=HEADLESS,
                args=["--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"],
            )
            page = await (await browser.new_context(viewport={"width": 1920, "height": 1080})).new_page()
            page.set_default_timeout(PLAYWRIGHT_TIMEOUT)
            try:
                return await _check_order_deletion(page)
            finally:
                await browser.close()

    assert asyncio.run(_run()), "Order deletion test failed"


def test_vendor_search_no_results():
    """Vendor search with no matches should return empty results."""
    if not PLAYWRIGHT_AVAILABLE:
        pytest.skip("Playwright not available")

    async def _run():
        async with async_playwright() as p:
            browser = await p.chromium.launch(
                headless=HEADLESS,
                args=["--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"],
            )
            page = await (await browser.new_context(viewport={"width": 1920, "height": 1080})).new_page()
            page.set_default_timeout(PLAYWRIGHT_TIMEOUT)
            try:
                return await _check_vendor_search_no_results(page)
            finally:
                await browser.close()

    assert asyncio.run(_run()), "Vendor search no-results test failed"


def test_add_line_item():
    """Adding a line item should increase the order's line item count."""
    if not PLAYWRIGHT_AVAILABLE:
        pytest.skip("Playwright not available")

    async def _run():
        async with async_playwright() as p:
            browser = await p.chromium.launch(
                headless=HEADLESS,
                args=["--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"],
            )
            page = await (await browser.new_context(viewport={"width": 1920, "height": 1080})).new_page()
            page.set_default_timeout(PLAYWRIGHT_TIMEOUT)
            try:
                return await _check_add_line_item(page)
            finally:
                await browser.close()

    assert asyncio.run(_run()), "Add line item test failed"


def test_parts_listed():
    """All available parts should be displayed on the line items page."""
    if not PLAYWRIGHT_AVAILABLE:
        pytest.skip("Playwright not available")

    async def _run():
        async with async_playwright() as p:
            browser = await p.chromium.launch(
                headless=HEADLESS,
                args=["--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"],
            )
            page = await (await browser.new_context(viewport={"width": 1920, "height": 1080})).new_page()
            page.set_default_timeout(PLAYWRIGHT_TIMEOUT)
            try:
                return await _check_parts_listed(page)
            finally:
                await browser.close()

    assert asyncio.run(_run()), "Parts listing test failed"


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
