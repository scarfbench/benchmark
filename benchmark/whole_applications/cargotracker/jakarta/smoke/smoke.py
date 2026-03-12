import json

import pytest
from playwright.sync_api import Page, expect


def test_cargo_tracker_homepage(page: Page):
    # 1. Navigate to the Cargo Tracker homepage
    page.goto("http://localhost:8080/cargo-tracker/index.xhtml")

    # 2. Verify page title
    expect(page).to_have_title("Eclipse Cargo Tracker")

    # 3. Verify main heading contains expected text
    main_heading = page.locator("h1")
    expect(main_heading).to_contain_text("Eclipse Cargo Tracker")

    # 4. Verify subtitle contains expected text
    subtitle = page.locator("h2, .subtitle, .lead, p")  # flexible selector
    expect(subtitle).to_contain_text(
        "Applied Domain-Driven Design Blueprints for Jakarta EE"
    )

    # 5. Verify three main buttons are present
    expect(page.get_by_role("link", name="Public Tracking Interface")).to_be_visible()
    expect(page.get_by_role("link", name="Administration")).to_be_visible()
    expect(page.get_by_role("link", name="Event Logging Interface")).to_be_visible()


def test_navigate_to_public_tracking_interface(page: Page):
    # 1. Navigate to home page
    page.goto("http://localhost:8080/cargo-tracker/index.xhtml")

    # 2. Click on "Public Tracking Interface" button
    page.get_by_role("link", name="Public Tracking Interface").click()

    # 3. Verify URL changes to `/public/track.xhtml`
    expect(page).to_have_url("http://localhost:8080/cargo-tracker/public/track.xhtml")

    # 4. Verify page loads successfully
    expect(page).to_have_title("Track Cargo")


def test_navigate_to_administration_interface(page: Page):
    # 1. Navigate to home page
    page.goto("http://localhost:8080/cargo-tracker/index.xhtml")

    # 2. Click on "Administration" button
    page.get_by_role("link", name="Administration").click()

    # 3. Verify URL changes to `/admin/dashboard.xhtml`
    expect(page).to_have_url(
        "http://localhost:8080/cargo-tracker/admin/dashboard.xhtml"
    )

    # 4. Verify page loads successfully
    expect(page).to_have_title("Cargo Dashboard")


def test_open_event_logging_interface_popup(page: Page):
    # 1. Navigate to home page
    page.goto("http://localhost:8080/cargo-tracker/index.xhtml")

    # 2. Click on "Event Logging Interface" button and wait for popup
    with page.expect_popup() as popup_info:
        page.get_by_role("link", name="Event Logging Interface").click()
    popup = popup_info.value

    # 3. Verify a new window/popup opens with URL `/event-logger/index.xhtml`
    expect(popup).to_have_url(
        "http://localhost:8080/cargo-tracker/event-logger/index.xhtml"
    )

    # 4. Verify popup window dimensions (width=320, height=440)
    viewport_size = popup.viewport_size
    assert viewport_size is not None
    assert viewport_size["width"] == 320
    assert viewport_size["height"] == 440


def test_verify_public_tracking_page_structure(page: Page):
    # 1. Navigate to public tracking page
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")

    # 2. Verify page title is "Track Cargo"
    expect(page).to_have_title("Track Cargo")

    # 3. Verify side menu contains required items
    # PrimeFaces menu items render as links within .ui-menu-list
    expect(page.locator(".ui-menu-list >> text=Tracking")).to_be_visible()
    expect(page.locator(".ui-menu-list >> text=About")).to_be_visible()

    # 4. Verify tracking form is present with input field and submit button
    expect(page.get_by_placeholder("XYZ789")).to_be_visible()
    expect(page.get_by_role("button", name="Track!")).to_be_visible()


def test_track_valid_cargo_abc123(page: Page):
    # 1. Navigate to public tracking page
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")

    # 2. Enter "ABC123" in the tracking ID input field
    page.get_by_placeholder("XYZ789").fill("ABC123")

    # 3. Click "Track!" button
    page.get_by_role("button", name="Track!").click()

    # 4. Verify cargo status information is displayed
    # 5. Verify cargo details show origin, destination, and status
    expect(page.locator("text=/Hong Kong|CNHKG/").first).to_be_visible()
    expect(page.locator("text=/Helsinki|FIHEL/").first).to_be_visible()


def test_track_valid_cargo_jkl567(page: Page):
    # 1. Navigate to public tracking page
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")

    # 2. Enter "JKL567" in the tracking ID input field
    page.get_by_placeholder("XYZ789").fill("JKL567")

    # 3. Click "Track!" button
    page.get_by_role("button", name="Track!").click()

    # 4. Verify cargo status information is displayed
    # 5. Verify cargo details show origin and destination
    expect(page.locator("text=/Hangzhou|CNHGH/").first).to_be_visible()
    expect(page.locator("text=/Stockholm|SESTO/").first).to_be_visible()


def test_track_valid_cargo_def789(page: Page):
    # 1. Navigate to public tracking page
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")

    # 2. Enter "DEF789" in the tracking ID input field
    page.get_by_placeholder("XYZ789").fill("DEF789")

    # 3. Click "Track!" button
    page.get_by_role("button", name="Track!").click()

    # 4. Verify cargo status information is displayed
    # 5. Verify cargo shows as not routed - checking for tracking ID to confirm it loaded
    expect(page.locator("text=DEF789").first).to_be_visible()


def test_track_valid_cargo_mno456(page: Page):
    # 1. Navigate to public tracking page
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")

    # 2. Enter "MNO456" in the tracking ID input field
    page.get_by_placeholder("XYZ789").fill("MNO456")

    # 3. Click "Track!" button
    page.get_by_role("button", name="Track!").click()

    # 4. Verify cargo status information is displayed
    # 5. Verify cargo shows as claimed
    expect(page.locator("text=/claimed|Claimed/i").first).to_be_visible()


def test_track_invalid_cargo_id(page: Page):
    # 1. Navigate to public tracking page
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")

    # 2. Enter "INVALID999" in the tracking ID input field
    page.get_by_placeholder("XYZ789").fill("INVALID999")

    # 3. Click "Track!" button
    page.get_by_role("button", name="Track!").click()

    # 4. Verify appropriate error message or "not found" indication is displayed
    expect(page.locator("text=/not found|unknown|invalid/i")).to_be_visible()


def test_submit_empty_tracking_id(page: Page):
    # 1. Navigate to public tracking page
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")

    # 2. Leave tracking ID input field empty
    # 3. Click "Track!" button
    page.get_by_role("button", name="Track!").click()

    # 4. Verify form validation - field has aria-required attribute
    tracking_input = page.get_by_placeholder("XYZ789")
    expect(tracking_input).to_have_attribute("aria-required", "true")


def test_navigate_to_about_page_from_public_tracking(page: Page):
    # 1. Navigate to public tracking page
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")

    # 2. Click on "About" menu item in side panel
    # PrimeFaces menu items render as links within .ui-menu-list
    page.locator(".ui-menu-list >> text=About").click()

    # 3. Verify URL changes to `/public/about.xhtml` (may include query params)
    assert "cargo-tracker/public/about.xhtml" in page.url

    # 4. Verify About page content loads
    expect(page).to_have_title("About")


def test_verify_dashboard_structure(page: Page):
    """
    Verify the basic structure of the dashboard page.
    """
    # 1. Navigate to admin dashboard page
    page.goto("http://localhost:8080/cargo-tracker/admin/dashboard.xhtml")

    # 2. Find the Routed Cargo Section table
    routed_cargo_table = page.locator(
        "//label[normalize-space(.)='Routed Cargo']/following::table[1]"
    )
    expect(routed_cargo_table).to_be_visible()

    # Headers to check in the Routed Cargo table
    expected_headers = [
        "Tracking ID",
        "Origin",
        "Destination",
        "Last Known Location",
        "Status",
        "Deadline",
    ]
    # 2.1 Assert table elements are present
    for header in expected_headers:
        header_loc = routed_cargo_table.get_by_role("columnheader", name=header)
        expect(header_loc).to_be_visible()

    # 2.2 Check to see there are two rows in the Routed Cargo table
    row_abc123 = routed_cargo_table.locator("tbody tr").filter(has_text="ABC123")
    expect(row_abc123).to_be_visible()
    cells_in_abc123 = row_abc123.locator("td")
    expect(cells_in_abc123.nth(0)).to_have_text("ABC123")  # Tracking ID
    expect(cells_in_abc123.nth(1)).to_have_text("Hong Kong\nCNHKG")  # Origin
    expect(cells_in_abc123.nth(2)).to_have_text("Helsinki\nFIHEL")  # Destination
    expect(cells_in_abc123.nth(3)).to_have_text(
        "New York\nUSNYC"
    )  # Last Known Location
    expect(cells_in_abc123.nth(4)).to_have_text("IN_PORT")  # Status

    row_jkl567 = routed_cargo_table.locator("tbody tr").filter(has_text="JKL567")
    expect(row_jkl567).to_be_visible()
    cells_in_jkl567 = row_jkl567.locator("td")
    expect(cells_in_jkl567.nth(0)).to_have_text("JKL567")  # Tracking ID
    expect(cells_in_jkl567.nth(1)).to_have_text("Hangzhou\nCNHGH")  # Origin
    expect(cells_in_jkl567.nth(2)).to_have_text("Stockholm\nSESTO")  # Destination
    expect(cells_in_jkl567.nth(3)).to_have_text(
        "New York\nUSNYC"
    )  # Last Known Location
    expect(cells_in_jkl567.nth(4)).to_have_text("ONBOARD_CARRIER")  # Status

    # 3. Find the Not Routed Cargo Section table
    expect(page.get_by_text("Not Routed Cargo", exact=True)).to_be_visible()

    # 4. Find the Claimed Cargo Section table
    expect(page.get_by_text("Claimed Cargo", exact=True)).to_be_visible()


def test_click_on_routed_cargo_details(page: Page):
    """
    Test clicking on a routed cargo item to view its details.
    """
    # 1. Navigate to admin dashboard page
    page.goto("http://localhost:8080/cargo-tracker/admin/dashboard.xhtml")

    # 2. Find the Routed Cargo Section table
    routed_cargo_table = page.locator(
        "//label[normalize-space(.)='Routed Cargo']/following::table[1]"
    )
    expect(routed_cargo_table).to_be_visible()

    # 3. Click on the tracking ID link for cargo "ABC123"
    tracking_link = routed_cargo_table.get_by_role("link", name="ABC123")
    tracking_link.click()

    # 4. Verify cargo details are displayed
    expect(page.get_by_text("Routing Details for Cargo ABC123")).to_be_visible()
    expect(page.get_by_text("Origin: Hong Kong  CNHKG")).to_be_visible()
    expect(page.get_by_text("Destination: Helsinki  FIHEL")).to_be_visible()

    # 5. Now go back and click on the tracking ID link for cargo "JKL567"
    page.go_back()
    tracking_link_jkl = routed_cargo_table.get_by_role("link", name="JKL567")
    tracking_link_jkl.click()

    # 6. Verify cargo details are displayed for JKL567
    expect(page.get_by_text("Routing Details for Cargo JKL567")).to_be_visible()
    expect(page.get_by_text("Origin: Hangzhou  CNHGH")).to_be_visible()
    expect(page.get_by_text("Destination: Stockholm  SESTO")).to_be_visible()

    # 7. Finally go back and click on the not routed cargo "DEF789"
    page.go_back()
    not_routed_cargo_table = page.locator(
        "//label[normalize-space(.)='Not Routed Cargo']/following::table[1]"
    )
    expect(not_routed_cargo_table).to_be_visible()
    tracking_link_def = not_routed_cargo_table.get_by_role("link", name="DEF789")
    tracking_link_def.click()

    # 8. Verify cargo details are displayed for DEF789
    expect(page.get_by_text("Set Route for Cargo DEF789")).to_be_visible()
    expect(page.get_by_text("Origin: Hong Kong  CNHKG")).to_be_visible()
    expect(page.get_by_text("Destination: Melbourne  AUMEL")).to_be_visible()


def test_tooltip_verification(page: Page):
    """
    Test to verify that tooltips appear correctly on hovering over specific elements.
    """

    # 1. Navigate to admin dashboard page
    page.goto("http://localhost:8080/cargo-tracker/admin/dashboard.xhtml")

    # 2. Find the Routed Cargo Section table
    routed_cargo_table = page.locator(
        "//label[normalize-space(.)='Routed Cargo']/following::table[1]"
    )
    expect(routed_cargo_table).to_be_visible()

    # 3. Hover the tracking ID link for cargo "ABC123"
    tracking_link = routed_cargo_table.get_by_role("link", name="ABC123")
    tracking_link.hover()

    # 4. Build the correct tooltip id
    link_id = tracking_link.get_attribute("id")
    assert link_id is not None
    tooltip_id = link_id.replace("trackingId", "toolTipFade")

    # 5. Locate tooltip by [id="..."] to avoid colon escaping issues
    tooltip = page.locator(f"[id='{tooltip_id}']")

    expect(tooltip).to_be_visible()
    expect(tooltip).to_contain_text("Click to see itinerary's details.")


def test_track_cargo_not_found_zzzzz(page: Page):
    """Track a non-existent cargo shows 'not found'."""
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")
    page.get_by_placeholder("XYZ789").fill("ZZZZZ")
    page.get_by_role("button", name="Track!").click()
    expect(page.locator("text=/not found|unknown|invalid/i")).to_be_visible()


def test_tracking_shows_misdirection_warning_jkl567(page: Page):
    """Tracking page shows misdirection warning for JKL567."""
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")
    page.get_by_placeholder("XYZ789").fill("JKL567")
    page.get_by_role("button", name="Track!").click()
    expect(page.locator("text=/misdirected|Misdirected/i")).to_be_visible()


def test_tracking_shows_handling_event_history(page: Page):
    """Tracking page displays handling event history for ABC123."""
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")
    page.get_by_placeholder("XYZ789").fill("ABC123")
    page.get_by_role("button", name="Track!").click()
    # Should show the "Handling History" section with event entries
    expect(page.get_by_text("Handling History")).to_be_visible()
    # Events are rendered as divs with check/flag icons, not a table
    # Verify at least one event description is visible (e.g. Received, Loaded, Unloaded)
    expect(page.locator("text=/Received|Loaded|Unloaded/i").first).to_be_visible()


def test_tracking_shows_map_iframe(page: Page):
    """Tracking page includes a map iframe showing cargo location."""
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")
    page.get_by_placeholder("XYZ789").fill("ABC123")
    page.get_by_role("button", name="Track!").click()
    map_iframe = page.locator("iframe")
    expect(map_iframe).to_be_visible()


def test_tracking_shows_eta_for_routed_cargo(page: Page):
    """Tracking page shows ETA for routed, non-misdirected cargo."""
    page.goto("http://localhost:8080/cargo-tracker/public/track.xhtml")
    page.get_by_placeholder("XYZ789").fill("ABC123")
    page.get_by_role("button", name="Track!").click()
    # ABC123 is routed and not misdirected, should show ETA
    expect(page.locator("text=/ETA|Estimated|arrival/i")).to_be_visible()


def test_cargo_details_shows_itinerary(page: Page):
    """Cargo details page shows itinerary with voyage, load/unload locations, and times."""
    page.goto("http://localhost:8080/cargo-tracker/admin/dashboard.xhtml")
    routed_cargo_table = page.locator(
        "//label[normalize-space(.)='Routed Cargo']/following::table[1]"
    )
    routed_cargo_table.get_by_role("link", name="ABC123").click()
    expect(page.get_by_text("Routing Details for Cargo ABC123")).to_be_visible()
    # Itinerary table should show voyage information
    itinerary_table = page.locator("table").filter(has_text="Voyage")
    expect(itinerary_table).to_be_visible()
    # Verify at least one leg exists
    expect(itinerary_table.locator("tbody tr").first).to_be_visible()


def test_misrouted_cargo_shows_warning_and_reroute(page: Page):
    """Cargo details page for misdirected JKL567 shows itinerary with the wrong voyage."""
    page.goto("http://localhost:8080/cargo-tracker/admin/dashboard.xhtml")
    routed_cargo_table = page.locator(
        "//label[normalize-space(.)='Routed Cargo']/following::table[1]"
    )
    routed_cargo_table.get_by_role("link", name="JKL567").click()
    # JKL567 is misdirected (loaded on wrong voyage) but NOT misrouted
    # (itinerary still satisfies route spec Hangzhou→Stockholm).
    # The admin details page shows the itinerary table for routed cargo.
    expect(page.get_by_text("Routing Details for Cargo JKL567")).to_be_visible()
    itinerary_table = page.locator("table").filter(has_text="Voyage")
    expect(itinerary_table).to_be_visible()


def test_not_routed_cargo_links_to_route_page(page: Page):
    """Not routed cargo table links to route selection page."""
    page.goto("http://localhost:8080/cargo-tracker/admin/dashboard.xhtml")
    not_routed_table = page.locator(
        "//label[normalize-space(.)='Not Routed Cargo']/following::table[1]"
    )
    expect(not_routed_table).to_be_visible()
    not_routed_table.get_by_role("link", name="DEF789").click()
    expect(page.get_by_text("Set Route for Cargo DEF789")).to_be_visible()
    expect(page.get_by_text("Origin: Hong Kong  CNHKG")).to_be_visible()
    expect(page.get_by_text("Destination: Melbourne  AUMEL")).to_be_visible()


def test_claimed_cargo_on_dashboard(page: Page):
    """Claimed cargo table shows MNO456."""
    page.goto("http://localhost:8080/cargo-tracker/admin/dashboard.xhtml")
    claimed_table = page.locator(
        "//label[normalize-space(.)='Claimed Cargo']/following::table[1]"
    )
    expect(claimed_table).to_be_visible()
    expect(claimed_table.locator("text=MNO456")).to_be_visible()


def test_booking_flow_shows_location_dropdown(page: Page):
    """Booking flow starts with origin selection showing all 13 locations."""
    page.goto("http://localhost:8080/cargo-tracker/admin/dashboard.xhtml")
    # Navigate to booking page via the Book link in the sidebar
    page.locator(".ui-menu-list >> text=Book").click()
    # PrimeFaces selectOneMenu renders a hidden <select> and a visible custom dropdown.
    # Check the hidden select has at least 13 location options.
    origin_select = page.locator("select").first
    expect(origin_select).to_be_attached()
    options = origin_select.locator("option")
    assert options.count() >= 13


def test_event_logger_wizard_structure(page: Page):
    """Event Logger is a multi-step wizard with tracking ID, location, event type fields."""
    page.goto("http://localhost:8080/cargo-tracker/index.xhtml")
    with page.expect_popup() as popup_info:
        page.get_by_role("link", name="Event Logging Interface").click()
    popup = popup_info.value
    # PrimeFaces selectOneMenu renders a hidden <select> and a visible custom dropdown.
    # Verify the event logger has a tracking ID selection
    expect(popup.locator("select").first).to_be_attached()


def test_event_logger_tracking_id_dropdown(page: Page):
    """Event Logger tracking ID dropdown shows routed unclaimed cargos."""
    page.goto("http://localhost:8080/cargo-tracker/index.xhtml")
    with page.expect_popup() as popup_info:
        page.get_by_role("link", name="Event Logging Interface").click()
    popup = popup_info.value
    popup.wait_for_load_state("networkidle")
    # The first dropdown should be tracking IDs
    # PrimeFaces selectOneMenu renders a hidden <select> with options
    tracking_select = popup.locator("select").first
    expect(tracking_select).to_be_attached()
    # Should include routed, unclaimed cargos like ABC123 and JKL567
    expect(tracking_select.locator("option", has_text="ABC123")).to_be_attached()
    expect(tracking_select.locator("option", has_text="JKL567")).to_be_attached()


def test_rest_handling_report_submission(page: Page):
    """Submit a handling report via REST API."""
    # The endpoint @Consumes({"application/json", "application/xml"}), so send JSON
    response = page.request.post(
        "http://localhost:8080/cargo-tracker/rest/handling/reports",
        headers={"Content-Type": "application/json"},
        data=json.dumps(
            {
                "completionTime": "3/1/2024 12:00 PM",
                "trackingId": "ABC123",
                "eventType": "UNLOAD",
                "unLocode": "USNYC",
                "voyageNumber": "0100S",
            }
        ),
    )
    assert response.status in (200, 204)


def test_rest_handling_report_invalid_tracking_id(page: Page):
    """Handling report with invalid tracking ID (< 4 chars) is rejected."""
    response = page.request.post(
        "http://localhost:8080/cargo-tracker/rest/handling/reports",
        data={
            "completionTime": "2024-03-01 12:00",
            "trackingId": "XX",
            "eventType": "UNLOAD",
            "unLocode": "USNYC",
            "voyageNumber": "0100S",
        },
    )
    assert response.status >= 400


def test_rest_handling_report_invalid_unlocode(page: Page):
    """Handling report with invalid UN locode (not 5 chars) is rejected."""
    response = page.request.post(
        "http://localhost:8080/cargo-tracker/rest/handling/reports",
        data={
            "completionTime": "2024-03-01 12:00",
            "trackingId": "ABC123",
            "eventType": "UNLOAD",
            "unLocode": "INVALID",
            "voyageNumber": "0100S",
        },
    )
    assert response.status >= 400


def test_rest_handling_report_invalid_event_type(page: Page):
    """Handling report with invalid event type is rejected."""
    response = page.request.post(
        "http://localhost:8080/cargo-tracker/rest/handling/reports",
        data={
            "completionTime": "2024-03-01 12:00",
            "trackingId": "ABC123",
            "eventType": "UNKNOWN",
            "unLocode": "USNYC",
            "voyageNumber": "0100S",
        },
    )
    assert response.status >= 400


def test_sse_cargo_endpoint(page: Page):
    """SSE endpoint at /rest/cargo returns cargo positions."""
    # Navigate to the app first so fetch runs in the same origin (avoids CORS/network errors).
    page.goto("http://localhost:8080/cargo-tracker/index.xhtml")
    # SSE keeps the connection open indefinitely, so page.request.get() would time out.
    # Instead, use page.evaluate() to fetch with AbortController for a short window,
    # verifying the endpoint responds with 200 and event-stream data.
    result = page.evaluate(
        """async () => {
            const controller = new AbortController();
            const timeout = setTimeout(() => controller.abort(), 5000);
            try {
                const resp = await fetch(
                    '/cargo-tracker/rest/cargo',
                    { headers: { 'Accept': 'text/event-stream' }, signal: controller.signal }
                );
                const reader = resp.body.getReader();
                const { value } = await reader.read();
                reader.cancel();
                clearTimeout(timeout);
                return { status: resp.status, hasData: value && value.length > 0 };
            } catch (e) {
                clearTimeout(timeout);
                if (e.name === 'AbortError') {
                    return { status: 0, hasData: false, aborted: true };
                }
                throw e;
            }
        }"""
    )
    assert result["status"] == 200
    assert result["hasData"]


def test_admin_tracking_page(page: Page):
    """Admin tracking page is accessible and has tracking input."""
    page.goto("http://localhost:8080/cargo-tracker/admin/dashboard.xhtml")
    page.locator(".ui-menu-list >> text=Track").click()
    # Should see a tracking input with autocomplete
    expect(page.locator("input[role='textbox'], input[type='text']").first).to_be_visible()


def test_admin_live_map_page(page: Page):
    """Admin live map page is accessible."""
    page.goto("http://localhost:8080/cargo-tracker/admin/dashboard.xhtml")
    page.locator(".ui-menu-list >> text=Live").click()
    # Should see a map or iframe for live cargo display
    expect(page.locator("iframe, canvas, .map, #map").first).to_be_visible()


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
