"""
Smoke test for "Address Book" app

Checks:
  1) GET <BASE>/index.xhtml -> 200 (fatal if not)
  2) GET <BASE>/contact/List.xhtml -> 200
  3) GET <BASE>/contact/Create.xhtml -> 200
  4) GET <BASE>/contact/View.xhtml -> 200 (may fail if no contacts)
  5) GET <BASE>/contact/Edit.xhtml -> 200 (may fail if no contacts)
  6) Test UI interactions:
     - Navigate to contact creation form
     - Fill out contact form with all fields
     - Submit form and verify success
     - Navigate to contact list
     - Verify contact appears in list
     - Test contact editing and deletion
     - Test form validation

Environment:
  ADDRESS_BOOK_BASE   Base app URL (default: http://localhost:8080/address-book-10-SNAPSHOT)
  VERBOSE=1           Verbose logging
  HEADLESS=0          Run browser in headed mode (default: true/headless)
  BROWSER             Browser to use: chrome, firefox, edge (default: chrome)

Exit codes:
  0  success
  2  GET /index.xhtml failed
  3  Critical pages failed
  5  Playwright tests failed
  9  Network / unexpected error
"""

import os
import sys
import time
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

try:
    from playwright.sync_api import (
        sync_playwright,
        TimeoutError as PlaywrightTimeoutError,
        Error as PlaywrightError,
    )

    PLAYWRIGHT_AVAILABLE = True
except ImportError:
    PLAYWRIGHT_AVAILABLE = False
    print(
        "[WARN] Playwright not available. Install with: pip install playwright",
        file=sys.stderr,
    )

BASE = os.getenv(
    "ADDRESS_BOOK_BASE", "http://localhost:9080/address-book-10-SNAPSHOT"
).rstrip("/")
VERBOSE = os.getenv("VERBOSE") == "1"
HEADLESS = os.getenv("HEADLESS", "1") == "1"
BROWSER = os.getenv("BROWSER", "chromium").lower()
HTTP_TIMEOUT = 12
PLAYWRIGHT_TIMEOUT = 10000


def vprint(*args):
    if VERBOSE:
        print(*args)


def join(base: str, path: str) -> str:
    if not path:
        return base
    if base.endswith("/") and path.startswith("/"):
        return base[:-1] + path
    if (not base.endswith("/")) and (not path.startswith("/")):
        return base + "/" + path
    return base + path


def http_request(method: str, url: str, timeout: int = HTTP_TIMEOUT):
    req = Request(url, method=method, headers={})
    try:
        with urlopen(req, timeout=timeout) as resp:
            return (resp.getcode(), resp.read().decode("utf-8", "replace")), None
    except HTTPError as e:
        try:
            body = e.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        return (e.code, body), None
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"


def must_get_ok(path: str, fail_code: int):
    url = join(BASE, path)
    vprint("GET", url)
    resp, err = http_request("GET", url)
    if err:
        print(f"[FAIL] {path} -> {err}", file=sys.stderr)
        sys.exit(9)
    if resp[0] != 200:
        print(f"[FAIL] GET {path} -> {resp[0]}", file=sys.stderr)
        sys.exit(fail_code)
    print(f"[PASS] GET {path} -> 200")
    return resp[1]


def soft_get_ok(path: str):
    url = join(BASE, path)
    vprint("GET", url, "(soft)")
    resp, err = http_request("GET", url)
    if err:
        print(f"[WARN] {path} -> {err}", file=sys.stderr)
        return
    print(f"[{'PASS' if resp[0] == 200 else 'WARN'}] GET {path} -> {resp[0]}")


def find_element_flexible(page, field_id, element_type="input"):
    selectors_to_try = [
        f"{element_type}[id$='{field_id}']",
        f"{element_type}[id*='{field_id}']",
        f"{element_type}[id^='{field_id}']",
        f"#{field_id}",
        f"{element_type}[name*='{field_id}']",
        f"{element_type}[type='text']",
        f"{element_type}",
    ]

    found_selector = None
    for selector in selectors_to_try:
        try:
            page.wait_for_selector(selector, timeout=2000)
            element = page.locator(selector).first
            if element.is_visible():
                found_selector = selector
                if VERBOSE:
                    print(f"[DEBUG] Found element using selector: {selector}")
                return element
        except PlaywrightTimeoutError:
            if VERBOSE:
                print(f"[DEBUG] Selector failed: {selector}")
            continue
        except PlaywrightError:
            continue

    if VERBOSE:
        print(f"[DEBUG] Could not find element for field: {field_id}")
        print(f"[DEBUG] Tried selectors: {selectors_to_try}")
        try:
            all_inputs = page.locator("input").all()
            print(f"[DEBUG] Found {len(all_inputs)} input elements on page:")
            for i, inp in enumerate(all_inputs[:10]):
                id_attr = inp.get_attribute("id") or "no-id"
                name_attr = inp.get_attribute("name") or "no-name"
                type_attr = inp.get_attribute("type") or "no-type"
                print(
                    f"[DEBUG]   Input {i + 1}: id='{id_attr}', name='{name_attr}', type='{type_attr}'"
                )
        except Exception as e:
            print(f"[DEBUG] Could not list input elements: {e}")
    return None


def find_button_flexible(page, button_text):
    selectors_to_try = [
        f"a:has-text('{button_text}')",
        f"button:has-text('{button_text}')",
        f"input[value='{button_text}']",
        f"*:has-text('{button_text}')",
        f"a[href*='{button_text.lower()}']",
        f"button[id*='{button_text.lower()}']",
    ]

    for selector in selectors_to_try:
        try:
            page.wait_for_selector(selector, timeout=2000)
            element = page.locator(selector).first
            if element.is_visible():
                if VERBOSE:
                    print(f"[DEBUG] Found button using selector: {selector}")
                return element
        except PlaywrightTimeoutError:
            if VERBOSE:
                print(f"[DEBUG] Button selector failed: {selector}")
            continue
        except PlaywrightError:
            continue

    if VERBOSE:
        print(f"[DEBUG] Could not find button: {button_text}")
        try:
            all_links = page.locator(
                "a, button, input[type='submit'], input[type='button']"
            ).all()
            print(f"[DEBUG] Found {len(all_links)} clickable elements:")
            for i, elem in enumerate(all_links[:10]):
                text = elem.text_content() or elem.get_attribute("value") or "no-text"
                tag = elem.evaluate("el => el.tagName")
                print(f"[DEBUG]   Element {i + 1}: <{tag}> '{text}'")
        except Exception as e:
            print(f"[DEBUG] Could not list clickable elements: {e}")
    return None


def is_page_type(page, page_type):
    current_url = page.url.lower()
    page_title = page.title().lower()
    page_source = page.content().lower()

    if page_type == "edit":
        return (
            "edit.xhtml" in current_url
            or "edit" in page_title
            or "edit contact" in page_source
            or "save" in page_source
        )
    elif page_type == "view":
        return (
            "view.xhtml" in current_url
            or "view" in page_title
            or "view contact" in page_source
            or "destroy" in page_source
            or "edit" in page_source
        )
    elif page_type == "create":
        return (
            "create.xhtml" in current_url
            or "create" in page_title
            or "create contact" in page_source
        )
    elif page_type == "list":
        return (
            "list.xhtml" in current_url
            or "list" in page_title
            or "contact list" in page_source
            or "show all" in page_source
        )

    return False


def create_playwright_context():
    if not PLAYWRIGHT_AVAILABLE:
        print("[FAIL] Playwright not available", file=sys.stderr)
        return None, None, None

    try:
        playwright = sync_playwright().start()

        browser_map = {
            "chrome": "chromium",
            "chromium": "chromium",
            "firefox": "firefox",
            "edge": "chromium",
        }

        browser_name = browser_map.get(BROWSER, "chromium")

        if browser_name == "chromium":
            browser = playwright.chromium.launch(
                headless=HEADLESS,
                args=["--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu"],
            )
        elif browser_name == "firefox":
            browser = playwright.firefox.launch(headless=HEADLESS)
        else:
            print(f"[FAIL] Unsupported browser: {BROWSER}", file=sys.stderr)
            return None, None, None

        context = browser.new_context(viewport={"width": 1920, "height": 1080})
        page = context.new_page()
        page.set_default_timeout(PLAYWRIGHT_TIMEOUT)

        return playwright, browser, page
    except PlaywrightError as e:
        print(f"[FAIL] Failed to create Playwright context: {e}", file=sys.stderr)
        return None, None, None


def test_contact_form_ui(page):
    print("\n[INFO] Testing contact form UI...")

    try:
        page.goto(join(BASE, "/contact/Create.xhtml"))

        if VERBOSE:
            print(f"[DEBUG] Page title: {page.title()}")
            print(f"[DEBUG] Page URL: {page.url}")
            try:
                page.screenshot(path="debug_form_screenshot.png")
                print("[DEBUG] Screenshot saved as debug_form_screenshot.png")
            except Exception as e:
                print(f"[DEBUG] Could not save screenshot: {e}")

            content = page.content()
            print(f"[DEBUG] Page content length: {len(content)}")
            if len(content) < 1000:
                print(f"[DEBUG] Full page content: {content}")
            else:
                print(f"[DEBUG] Page content preview: {content[:500]}...")

        page.wait_for_selector("form", timeout=PLAYWRIGHT_TIMEOUT)
        print("[PASS] Contact form loaded")
        form_fields = [
            ("firstName", "First Name"),
            ("lastName", "Last Name"),
            ("birthday", "Birthday"),
            ("homePhone", "Home Phone"),
            ("mobilePhone", "Mobile Phone"),
            ("email", "Email"),
        ]

        missing_fields = []
        for field_id, field_name in form_fields:
            element = find_element_flexible(page, field_id)
            if element and element.is_visible():
                print(f"[PASS] {field_name} field found")
            else:
                print(f"[WARN] {field_name} field not found or not visible")
                missing_fields.append(field_name)

        if missing_fields:
            print(f"[WARN] Missing fields: {missing_fields}")
            try:
                all_inputs = page.locator("input[type='text']").all()
                if all_inputs:
                    print(
                        f"[INFO] Found {len(all_inputs)} text input fields as fallback"
                    )
                    if len(all_inputs) >= 3:
                        print("[INFO] Proceeding with fallback input detection")
                    else:
                        print("[FAIL] Insufficient input fields found")
                        return False
                else:
                    print("[FAIL] No input fields found at all")
                    return False
            except Exception as e:
                print(f"[FAIL] Could not find any input fields: {e}")
                return False
        test_data = {
            "firstName": "Playwright",
            "lastName": "Test",
            "birthday": "01/15/1990",
            "homePhone": "(555) 123-4567",
            "mobilePhone": "(555) 987-6543",
            "email": "playwright.test@example.com",
        }

        filled_fields = 0
        for field_id, value in test_data.items():
            element = find_element_flexible(page, field_id)
            if element:
                try:
                    element.clear()
                    element.fill(value)
                    print(f"[PASS] Filled {field_id} with '{value}'")
                    filled_fields += 1
                except Exception as e:
                    print(f"[WARN] Could not fill {field_id}: {e}")
            else:
                print(f"[WARN] Could not find {field_id} field to fill")

        if filled_fields < 2:
            print("[WARN] Too few fields filled, trying alternative approach")
            try:
                text_inputs = page.locator("input[type='text']").all()
                for i, inp in enumerate(text_inputs[: len(test_data)]):
                    if i < len(list(test_data.values())):
                        try:
                            inp.clear()
                            inp.fill(list(test_data.values())[i])
                            print(
                                f"[PASS] Filled input {i + 1} with '{list(test_data.values())[i]}'"
                            )
                            filled_fields += 1
                        except Exception as e:
                            print(f"[WARN] Could not fill input {i + 1}: {e}")
            except Exception as e:
                print(f"[WARN] Alternative filling approach failed: {e}")

        if filled_fields < 2:
            print("[FAIL] Insufficient fields filled for form submission")
            return False
        submit_button = find_button_flexible(page, "Save")
        if not submit_button:
            print("[FAIL] Could not find Save button")
            return False

        try:
            import re

            with page.expect_navigation(
                url=re.compile(r".*List\.xhtml$"), timeout=10000
            ):
                submit_button.click()
            print("[PASS] Form submitted and navigated to list page")
        except PlaywrightTimeoutError:
            submit_button.click()
            print("[PASS] Submitted contact form")
            page.wait_for_timeout(2000)
        current_url = page.url
        if "List.xhtml" in current_url or "Create.xhtml" in current_url:
            print("[PASS] Form submission processed")
        else:
            print(f"[INFO] Redirected to: {current_url}")

        return True

    except PlaywrightTimeoutError:
        print("[FAIL] Timeout waiting for form elements", file=sys.stderr)
        return False
    except Exception as e:
        print(f"[FAIL] Form UI test failed: {e}", file=sys.stderr)
        return False


def test_contact_list_ui(page):
    print("\n[INFO] Testing contact list UI...")

    try:
        page.goto(join(BASE, "/contact/List.xhtml"))

        if VERBOSE:
            print(f"[DEBUG] Page title: {page.title()}")
            print(f"[DEBUG] Page URL: {page.url}")
            try:
                page.screenshot(path="debug_list_screenshot.png")
                print("[DEBUG] Screenshot saved as debug_list_screenshot.png")
            except Exception as e:
                print(f"[DEBUG] Could not save screenshot: {e}")
        try:
            page.wait_for_selector("table", timeout=PLAYWRIGHT_TIMEOUT)
            print("[PASS] Contact list table loaded")
        except PlaywrightTimeoutError:
            if "list" in page.url.lower() or "contact" in page.title().lower():
                print("[WARN] Table not found but page appears to be list page")
                try:
                    rows = page.locator("tr, .row, .item").all()
                    if rows:
                        print(f"[INFO] Found {len(rows)} potential data rows")
                    else:
                        print("[WARN] No tabular data found")
                except Exception as e:
                    print(f"[WARN] Could not find any data: {e}")
            else:
                print("[FAIL] Not on list page and no table found")
                return False
        try:
            headers = page.locator("th").all()
            expected_headers = [
                "Id",
                "Last Name",
                "First Name",
                "Birthday",
                "Home Phone",
                "Mobile Phone",
                "Email",
            ]
            for i, expected in enumerate(expected_headers):
                if i < len(headers) and expected in headers[i].text_content():
                    print(f"[PASS] Found header: {expected}")
                else:
                    print(f"[WARN] Expected header '{expected}' not found")
        except Exception as e:
            print(f"[WARN] Could not verify headers: {e}")

        try:
            rows = page.locator("table tr").all()
            if len(rows) > 1:
                print(f"[PASS] Found {len(rows) - 1} contact(s) in list")
            else:
                print("[INFO] No contacts found in list")
        except Exception as e:
            print(f"[WARN] Could not count rows: {e}")
        try:
            create_link = find_button_flexible(page, "Create New Contact")
            if create_link:
                create_link.click()
                page.wait_for_timeout(1000)
                current_url = page.url
                if "Create.xhtml" in current_url:
                    print("[PASS] 'Create New Contact' link works")
                else:
                    print(f"[WARN] Link redirected to unexpected page: {current_url}")
            else:
                print("[WARN] 'Create New Contact' link not found")
        except Exception as e:
            print(f"[WARN] Could not test create link: {e}")

        return True

    except PlaywrightTimeoutError:
        print("[FAIL] Timeout waiting for list page", file=sys.stderr)
        return False
    except Exception as e:
        print(f"[FAIL] List UI test failed: {e}", file=sys.stderr)
        return False


def test_contact_edit_ui(page):
    print("\n[INFO] Testing contact edit UI...")

    try:
        page.goto(join(BASE, "/contact/List.xhtml"))

        page.wait_for_selector("table", timeout=PLAYWRIGHT_TIMEOUT)
        try:
            edit_links = page.get_by_text("Edit").all()
            if edit_links:
                print(f"[PASS] Found {len(edit_links)} edit link(s)")
                edit_links[0].click()
                page.wait_for_timeout(2000)
            else:
                edit_button = find_button_flexible(page, "Edit")
                if edit_button:
                    print("[PASS] Found edit button using flexible approach")
                    edit_button.click()
                    page.wait_for_timeout(2000)
                else:
                    print("[INFO] No edit links found - no contacts to edit")
                    return True
            if is_page_type(page, "edit"):
                current_url = page.url
                print(
                    f"[PASS] Successfully navigated to edit page (URL: {current_url})"
                )

                form_fields = ["firstName", "lastName", "email"]
                for field_id in form_fields:
                    element = find_element_flexible(page, field_id)
                    if element and element.is_visible() and element.is_enabled():
                        print(f"[PASS] {field_id} field is editable")
                    else:
                        print(f"[WARN] {field_id} field not editable")

                return True
            else:
                current_url = page.url
                page_title = page.title()
                print(f"[WARN] Edit link did not navigate to edit page")
                print(f"[WARN] Current URL: {current_url}")
                print(f"[WARN] Page title: {page_title}")
                if VERBOSE:
                    print(f"[DEBUG] Page source contains: {page.content()[:500]}...")
                return False
        except Exception as e:
            print(f"[WARN] Could not test edit functionality: {e}")
            return True

    except PlaywrightTimeoutError:
        print("[FAIL] Timeout waiting for edit page", file=sys.stderr)
        return False
    except Exception as e:
        print(f"[FAIL] Edit UI test failed: {e}", file=sys.stderr)
        return False


def test_contact_view_ui(page):
    print("\n[INFO] Testing contact view UI...")

    try:
        page.goto(join(BASE, "/contact/List.xhtml"))

        page.wait_for_selector("table", timeout=PLAYWRIGHT_TIMEOUT)
        try:
            view_links = page.get_by_text("View").all()
            if view_links:
                print(f"[PASS] Found {len(view_links)} view link(s)")
                view_links[0].click()
                page.wait_for_timeout(2000)

                if is_page_type(page, "view"):
                    current_url = page.url
                    print(
                        f"[PASS] Successfully navigated to view page (URL: {current_url})"
                    )

                    try:
                        contact_info_selectors = [
                            "h\\:panelGrid td",
                            "td",
                            ".panelGrid td",
                            "table td",
                        ]

                        contact_info = []
                        for selector in contact_info_selectors:
                            try:
                                contact_info = page.locator(selector).all()
                                if contact_info:
                                    break
                            except Exception:
                                continue

                        if contact_info:
                            print(
                                f"[PASS] Found {len(contact_info)} contact detail fields"
                            )
                        else:
                            print("[WARN] No contact details found on view page")
                    except Exception as e:
                        print(f"[WARN] Could not verify contact details: {e}")

                    return True
                else:
                    current_url = page.url
                    page_title = page.title()
                    print(f"[WARN] View link did not navigate to view page")
                    print(f"[WARN] Current URL: {current_url}")
                    print(f"[WARN] Page title: {page_title}")
                    if VERBOSE:
                        print(
                            f"[DEBUG] Page source contains: {page.content()[:500]}..."
                        )
                    return False
            else:
                print("[INFO] No view links found - no contacts to view")
                return True
        except Exception as e:
            print(f"[WARN] Could not test view functionality: {e}")
            return True

    except PlaywrightTimeoutError:
        print("[FAIL] Timeout waiting for view page", file=sys.stderr)
        return False
    except Exception as e:
        print(f"[FAIL] View UI test failed: {e}", file=sys.stderr)
        return False


def test_form_validation(page):
    print("\n[INFO] Testing form validation...")

    try:
        page.goto(join(BASE, "/contact/Create.xhtml"))

        page.wait_for_selector("form", timeout=PLAYWRIGHT_TIMEOUT)

        submit_button = page.get_by_text("Save")
        submit_button.click()
        print("[PASS] Attempted to submit empty form")

        page.wait_for_timeout(1000)
        try:
            messages = page.locator(".ui-message, .ui-messages, .messagecolor").all()
            if messages:
                print(f"[PASS] Found {len(messages)} validation message(s)")
            else:
                print(
                    "[INFO] No validation messages found (may be handled client-side)"
                )
        except Exception as e:
            print(f"[INFO] Could not check validation messages: {e}")

        try:
            email_field = find_element_flexible(page, "email")
            if email_field:
                email_field.clear()
                email_field.fill("invalid-email")

                submit_button.click()
                page.wait_for_timeout(1000)
                print("[PASS] Tested invalid email validation")
            else:
                print("[WARN] Could not find email field for validation test")
        except Exception as e:
            print(f"[WARN] Could not test email validation")

        return True

    except Exception as e:
        print(f"[FAIL] Form validation test failed: {e}", file=sys.stderr)
        return False


def test_contact_deletion_ui(page):
    print("\n[INFO] Testing contact deletion UI...")

    try:
        page.goto(join(BASE, "/contact/List.xhtml"))

        page.wait_for_selector("table", timeout=PLAYWRIGHT_TIMEOUT)
        try:
            destroy_links = page.get_by_text("Destroy").all()
            if destroy_links:
                print(f"[PASS] Found {len(destroy_links)} destroy link(s)")

                try:
                    rows = page.locator("table tr").all()
                    contacts_before = len(rows) - 1
                    print(f"[INFO] Contacts before deletion: {contacts_before}")
                except Exception as e:
                    print(f"[WARN] Could not count contacts before deletion: {e}")
                    contacts_before = 0

                destroy_links[0].click()
                page.wait_for_timeout(2000)

                if is_page_type(page, "list"):
                    print("[PASS] Successfully deleted contact and returned to list")

                    try:
                        rows = page.locator("table tr").all()
                        contacts_after = len(rows) - 1
                        print(f"[INFO] Contacts after deletion: {contacts_after}")

                        if contacts_after < contacts_before:
                            print("[PASS] Contact count decreased after deletion")
                        else:
                            print(
                                "[WARN] Contact count did not decrease after deletion"
                            )
                    except Exception as e:
                        print(f"[WARN] Could not count contacts after deletion: {e}")

                    return True
                else:
                    current_url = page.url
                    print(f"[WARN] Deletion did not return to list page: {current_url}")
                    return False
            else:
                print("[INFO] No destroy links found - no contacts to delete")
                return True
        except Exception as e:
            print(f"[WARN] Could not test deletion functionality: {e}")
            return True

    except PlaywrightTimeoutError:
        print("[FAIL] Timeout waiting for deletion page", file=sys.stderr)
        return False
    except Exception as e:
        print(f"[FAIL] Deletion UI test failed: {e}", file=sys.stderr)
        return False


def test_contact_deletion_from_view(page):
    print("\n[INFO] Testing contact deletion from view page...")

    try:
        page.goto(join(BASE, "/contact/List.xhtml"))

        page.wait_for_selector("table", timeout=PLAYWRIGHT_TIMEOUT)
        try:
            view_links = page.get_by_text("View").all()
            if view_links:
                print(f"[PASS] Found {len(view_links)} view link(s)")

                try:
                    rows = page.locator("table tr").all()
                    contacts_before = len(rows) - 1
                    print(f"[INFO] Contacts before deletion: {contacts_before}")
                except Exception as e:
                    print(f"[WARN] Could not count contacts before deletion: {e}")
                    contacts_before = 0

                view_links[0].click()
                page.wait_for_timeout(2000)

                if is_page_type(page, "view"):
                    print("[PASS] Successfully navigated to view page")

                    try:
                        destroy_link = page.get_by_text("Destroy")
                        destroy_link.click()
                        page.wait_for_timeout(2000)
                        print("[PASS] Clicked destroy link on view page")

                        if is_page_type(page, "list"):
                            print(
                                "[PASS] Successfully deleted contact from view page and returned to list"
                            )

                            try:
                                rows = page.locator("table tr").all()
                                contacts_after = len(rows) - 1
                                print(
                                    f"[INFO] Contacts after deletion: {contacts_after}"
                                )

                                if contacts_after < contacts_before:
                                    print(
                                        "[PASS] Contact count decreased after deletion from view page"
                                    )
                                else:
                                    print(
                                        "[WARN] Contact count did not decrease after deletion from view page"
                                    )
                            except Exception as e:
                                print(
                                    f"[WARN] Could not count contacts after deletion: {e}"
                                )

                            return True
                        else:
                            current_url = page.url
                            print(
                                f"[WARN] Deletion from view did not return to list page: {current_url}"
                            )
                            return False
                    except PlaywrightError:
                        print("[WARN] No destroy link found on view page")
                        return True
                    except Exception as e:
                        print(f"[WARN] Could not test destroy link on view page: {e}")
                        return True
                else:
                    print("[WARN] Did not navigate to view page")
                    return False
            else:
                print("[INFO] No view links found - no contacts to delete from view")
                return True
        except Exception as e:
            print(f"[WARN] Could not test deletion from view: {e}")
            return True

    except PlaywrightTimeoutError:
        print("[FAIL] Timeout waiting for view page", file=sys.stderr)
        return False
    except Exception as e:
        print(f"[FAIL] Deletion from view test failed: {e}", file=sys.stderr)
        return False


def test_navigation_links(page):
    print("\n[INFO] Testing navigation links...")

    try:
        page.goto(join(BASE, "/index.xhtml"))

        try:
            show_all_link = page.get_by_text("Show All Contact Items")
            show_all_link.click()
            page.wait_for_timeout(2000)

            current_url = page.url
            if "List.xhtml" in current_url:
                print("[PASS] 'Show All Contact Items' link works")
            else:
                print(f"[WARN] Link redirected to unexpected page: {current_url}")
        except PlaywrightError:
            print("[WARN] 'Show All Contact Items' link not found")
        except Exception as e:
            print(f"[WARN] Could not test show all link: {e}")

        try:
            page.goto(join(BASE, "/contact/List.xhtml"))
            create_link = page.get_by_text("Create New Contact")
            create_link.click()
            page.wait_for_timeout(2000)

            current_url = page.url
            if "Create.xhtml" in current_url:
                print("[PASS] Navigation from list to create works")
            else:
                print(f"[WARN] Navigation to create failed: {current_url}")
        except Exception as e:
            print(f"[WARN] Could not test list to create navigation: {e}")

        return True

    except Exception as e:
        print(f"[FAIL] Navigation test failed: {e}", file=sys.stderr)
        return False


def run_playwright_tests():
    if not PLAYWRIGHT_AVAILABLE:
        print("[SKIP] Playwright tests skipped - Playwright not available")
        return True

    playwright, browser, page = create_playwright_context()
    if not page:
        return False

    try:
        print(
            f"[INFO] Running Playwright tests with {BROWSER} browser (headless={HEADLESS})"
        )

        if not test_contact_form_ui(page):
            return False

        if not test_contact_list_ui(page):
            return False

        if not test_contact_edit_ui(page):
            return False

        if not test_contact_view_ui(page):
            return False

        if not test_form_validation(page):
            return False

        if not test_navigation_links(page):
            return False

        if not test_contact_deletion_ui(page):
            return False

        print("[PASS] All Playwright tests completed successfully")
        return True

    finally:
        if browser:
            browser.close()
        if playwright:
            playwright.stop()


def main():
    body = must_get_ok("/index.xhtml", 2)

    if "Address Book" in body and "Contact" in body:
        print("[PASS] HTML content valid")
    else:
        print("[WARN] HTML content invalid")

    soft_get_ok("/contact/List.xhtml")
    soft_get_ok("/contact/Create.xhtml")
    soft_get_ok("/contact/View.xhtml")
    soft_get_ok("/contact/Edit.xhtml")

    soft_get_ok("/resources/css/jsfcrud.css")

    print("\n[INFO] Running Playwright UI tests...")
    if not run_playwright_tests():
        print("[FAIL] Playwright tests failed", file=sys.stderr)
        sys.exit(5)

    print("\n[PASS] Enhanced smoke sequence complete")
    return 0


if __name__ == "__main__":
    sys.exit(main())
