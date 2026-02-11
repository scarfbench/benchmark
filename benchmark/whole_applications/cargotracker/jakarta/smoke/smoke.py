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


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
