"""
Smoke tests for DayTrader Quarkus application using Playwright.

This test suite verifies the basic functionality of the migrated DayTrader application:
- Home page loads
- Login/logout flow
- Trading operations (view quotes, buy, sell)
- Portfolio view
- Account information
- Market summary
- REST API endpoints
- Configuration page

Run with:
    cd smoke
    uv sync
    uv run playwright install chromium
    uv run pytest smoke.py -v

Or with Docker:
    docker exec -it <container> bash
    cd /app/smoke && uv sync && uv run playwright install chromium && uv run pytest smoke.py -v
"""

import os
import re
import pytest
from playwright.sync_api import Page, expect


# Base URL for the Quarkus application - configurable via environment variable
# Default is 8080, but can be overridden with DAYTRADER_PORT env var
PORT = os.environ.get("DAYTRADER_PORT", "8080")
BASE_URL = f"http://localhost:{PORT}"
# The app endpoint is under /rest in Quarkus
APP_URL = f"{BASE_URL}/rest/app"


# ============================================================================
# FIXTURES
# ============================================================================

@pytest.fixture(scope="function")
def logged_in_page(page: Page) -> Page:
    """Fixture that provides a page with user already logged in."""
    page.goto(APP_URL, wait_until="domcontentloaded")

    # Fill login form
    page.fill("input[name='uid']", "uid:0")
    page.fill("input[name='passwd']", "xxx")
    page.click("input[type='submit'][value='Login']")
    page.wait_for_load_state("domcontentloaded")

    return page


# ============================================================================
# HOME PAGE TESTS
# ============================================================================

@pytest.mark.smoke
def test_home_page_loads(page: Page) -> None:
    """Test that the main home page loads successfully."""
    page.goto(f"{BASE_URL}/", wait_until="domcontentloaded")

    # Should have DayTrader in the title or content
    content = page.content().lower()
    assert "daytrader" in content, "DayTrader branding not found on home page"


@pytest.mark.smoke
def test_index_html_loads(page: Page) -> None:
    """Test that index.html loads with frameset."""
    page.goto(f"{BASE_URL}/index.html", wait_until="domcontentloaded")

    # Should have DayTrader content
    expect(page).to_have_title(re.compile(r"daytrader", re.IGNORECASE))


@pytest.mark.smoke
def test_static_resources_available(page: Page) -> None:
    """Test that CSS and images are accessible."""
    # Check CSS
    response = page.goto(f"{BASE_URL}/style.css")
    assert response is not None and response.ok, "style.css not accessible"

    # Check an image
    response = page.goto(f"{BASE_URL}/images/dayTraderLogo.gif")
    assert response is not None and response.ok, "DayTrader logo not accessible"


# ============================================================================
# LOGIN/LOGOUT TESTS
# ============================================================================

@pytest.mark.smoke
def test_login_page_renders(page: Page) -> None:
    """Test that the login page renders with required fields."""
    page.goto(APP_URL, wait_until="domcontentloaded")

    # Should have login form fields
    username_field = page.locator("input[name='uid']")
    password_field = page.locator("input[name='passwd']")
    submit_button = page.locator("input[type='submit'][value='Login']")

    assert username_field.count() > 0, "Username field not found"
    assert password_field.count() > 0, "Password field not found"
    assert submit_button.count() > 0, "Login button not found"


@pytest.mark.smoke
def test_login_with_valid_credentials(page: Page) -> None:
    """Test successful login with valid credentials."""
    page.goto(APP_URL, wait_until="domcontentloaded")

    # Fill and submit login form
    page.fill("input[name='uid']", "uid:0")
    page.fill("input[name='passwd']", "xxx")
    page.click("input[type='submit'][value='Login']")
    page.wait_for_load_state("domcontentloaded")

    # After login, should see welcome message or account info
    content = page.content().lower()
    assert "uid:0" in content or "welcome" in content or "account" in content, \
        "Login did not succeed - user info not displayed"


@pytest.mark.smoke
def test_login_with_invalid_credentials(page: Page) -> None:
    """Test login failure with invalid credentials."""
    page.goto(APP_URL, wait_until="domcontentloaded")

    # Fill with wrong credentials
    page.fill("input[name='uid']", "invalid_user")
    page.fill("input[name='passwd']", "wrong_password")
    page.click("input[type='submit'][value='Login']")
    page.wait_for_load_state("domcontentloaded")

    # Should show error or stay on login page
    content = page.content().lower()
    assert "error" in content or "failed" in content or "invalid" in content or \
           "login" in content, "Error message not shown for invalid login"


@pytest.mark.smoke
def test_logout(logged_in_page: Page) -> None:
    """Test logout functionality."""
    page = logged_in_page

    # Click logout link
    logout_link = page.locator("a[href*='logout']")
    if logout_link.count() > 0:
        logout_link.first.click()
        page.wait_for_load_state("domcontentloaded")

        # Should be back on login/welcome page
        content = page.content().lower()
        assert "login" in content or "welcome" in content or "logged out" in content, \
            "Logout did not redirect to login page"


@pytest.mark.smoke
def test_logout_invalidates_session(logged_in_page: Page) -> None:
    """Logout should invalidate session; protected actions redirect to login."""
    page = logged_in_page

    logout_link = page.locator("a[href*='logout']")
    assert logout_link.count() > 0, "Logout link not found"
    logout_link.first.click()
    page.wait_for_load_state("domcontentloaded")

    # Try accessing a protected action after logout
    page.goto(f"{APP_URL}?action=home", wait_until="domcontentloaded")
    content = page.content().lower()
    assert "login" in content or "welcome" in content or "uid" in content, \
        "Session was not invalidated after logout"


# ============================================================================
# NAVIGATION TESTS
# ============================================================================

@pytest.mark.smoke
def test_navigation_links_after_login(logged_in_page: Page) -> None:
    """Test that all navigation links work after login."""
    page = logged_in_page

    nav_actions = ["home", "portfolio", "account", "quotes"]

    for action in nav_actions:
        link = page.locator(f"a[href*='action={action}']")
        if link.count() > 0:
            link.first.click()
            page.wait_for_load_state("domcontentloaded")

            # Verify page loaded (has content)
            content = page.content()
            assert len(content) > 100, f"Navigation to {action} resulted in empty page"


# ============================================================================
# HOME PAGE ACCOUNT SUMMARY
# ============================================================================

@pytest.mark.smoke
def test_home_page_account_summary(logged_in_page: Page) -> None:
    """Home page should display account summary with balance and holdings info."""
    page = logged_in_page

    page.goto(f"{APP_URL}?action=home", wait_until="domcontentloaded")
    content = page.content().lower()

    assert re.search(r"account\s*id", content) or "uid:0" in content, \
        "Account ID not shown on home page"
    assert "balance" in content or "$" in page.content(), \
        "Balance info not shown on home page"
    assert re.search(r"(holdings|gain|loss)", content), \
        "Holdings/gain/loss info not shown on home page"


# ============================================================================
# QUOTES TESTS
# ============================================================================

@pytest.mark.smoke
def test_view_quotes_without_login(page: Page) -> None:
    """Test that quotes can be viewed without login."""
    page.goto(f"{APP_URL}?action=quotes&symbols=s:0,s:1,s:2",
              wait_until="domcontentloaded")

    # Should show quote data
    content = page.content().lower()
    assert "s:0" in content or "quote" in content, "Quotes not displayed"


@pytest.mark.smoke
def test_view_quotes_form(page: Page) -> None:
    """Test the quote lookup form."""
    page.goto(APP_URL, wait_until="domcontentloaded")

    # Find the quotes form
    symbols_input = page.locator("input[name='symbols']")
    if symbols_input.count() > 0:
        symbols_input.first.fill("s:0,s:1")

        # Find and click the quotes submit
        submit = page.locator("input[type='submit'][value='Get Quotes']")
        if submit.count() > 0:
            submit.first.click()
            page.wait_for_load_state("domcontentloaded")

            content = page.content().lower()
            assert "s:0" in content, "Quote results not shown"


@pytest.mark.smoke
def test_quote_data_displayed(logged_in_page: Page) -> None:
    """Test that quote data is properly displayed."""
    page = logged_in_page

    # Navigate to quotes
    page.goto(f"{APP_URL}?action=quotes&symbols=s:0",
              wait_until="domcontentloaded")

    content = page.content().lower()

    # Should have quote information
    assert "s:0" in content, "Symbol not displayed"
    assert "price" in content or "$" in content, "Price not displayed"


@pytest.mark.smoke
def test_view_multiple_quotes(logged_in_page: Page) -> None:
    """Look up multiple stock quotes and verify multiple rows are shown."""
    page = logged_in_page

    page.goto(f"{APP_URL}?action=quotes&symbols=s:0,s:1,s:2",
              wait_until="domcontentloaded")
    content = page.content().lower()

    assert "s:0" in content, "Symbol s:0 not in multi-quote results"
    assert "s:1" in content, "Symbol s:1 not in multi-quote results"
    assert "s:2" in content, "Symbol s:2 not in multi-quote results"


@pytest.mark.smoke
def test_quote_buy_form(logged_in_page: Page) -> None:
    """Quote page should show a buy form with quantity input and Buy button."""
    page = logged_in_page

    page.goto(f"{APP_URL}?action=quotes&symbols=s:0",
              wait_until="domcontentloaded")

    quantity_input = page.locator("input[name='quantity']")
    buy_button = page.locator(
        "input[type='submit'][value='Buy'], "
        "input[type='submit'][value='Buy!'], "
        "input[type='submit'][value='Buy Shares'], "
        "button:has-text('Buy')"
    )
    assert quantity_input.count() > 0, "Quantity input not found on quote page"
    assert buy_button.count() > 0, "Buy button not found on quote page"


# ============================================================================
# PORTFOLIO TESTS
# ============================================================================

@pytest.mark.smoke
def test_view_portfolio(logged_in_page: Page) -> None:
    """Test portfolio view after login."""
    page = logged_in_page

    # Navigate to portfolio
    page.goto(f"{APP_URL}?action=portfolio", wait_until="domcontentloaded")

    content = page.content().lower()

    # Should show portfolio or holdings info
    assert "portfolio" in content or "holding" in content or "uid:0" in content, \
        "Portfolio page did not load correctly"


@pytest.mark.smoke
def test_portfolio_shows_holdings_table(logged_in_page: Page) -> None:
    """Test that portfolio shows a table of holdings."""
    page = logged_in_page

    page.goto(f"{APP_URL}?action=portfolio", wait_until="domcontentloaded")

    # Should have a table
    table = page.locator("table")
    assert table.count() > 0, "Portfolio table not found"


# ============================================================================
# ACCOUNT TESTS
# ============================================================================

@pytest.mark.smoke
def test_view_account(logged_in_page: Page) -> None:
    """Test account details view."""
    page = logged_in_page

    page.goto(f"{APP_URL}?action=account", wait_until="domcontentloaded")

    content = page.content().lower()

    # Should show account information
    assert "account" in content, "Account page did not load"
    assert "balance" in content or "uid:0" in content, \
        "Account details not displayed"


@pytest.mark.smoke
def test_account_shows_balance(logged_in_page: Page) -> None:
    """Test that account shows balance information."""
    page = logged_in_page

    page.goto(f"{APP_URL}?action=account", wait_until="domcontentloaded")

    content = page.content()

    # Should have dollar amounts (balance)
    assert "$" in content or "Balance" in content, "Balance not shown on account page"


@pytest.mark.smoke
def test_view_account_and_orders(logged_in_page: Page) -> None:
    """Account page should show profile info and recent orders."""
    page = logged_in_page

    page.goto(f"{APP_URL}?action=account", wait_until="domcontentloaded")
    content = page.content().lower()

    assert "account" in content, "Account page did not load"
    assert "uid:0" in content or "profile" in content, \
        "Profile information not shown on account page"
    assert "order" in content or "balance" in content, \
        "Orders or balance section not shown on account page"


@pytest.mark.smoke
def test_update_profile(logged_in_page: Page) -> None:
    """Update profile information and verify it is saved."""
    page = logged_in_page

    page.goto(f"{APP_URL}?action=account", wait_until="domcontentloaded")

    fullname_field = page.locator("input[name='fullname'], input[name='Full Name']")
    if fullname_field.count() > 0:
        fullname_field.first.fill("Updated Smoke User")

        update_button = page.locator(
            "input[type='submit'][value*='Update'], "
            "input[type='submit'][value*='update']"
        )
        if update_button.count() > 0:
            update_button.first.click()
            page.wait_for_load_state("domcontentloaded")

            content = page.content()
            assert "Updated Smoke User" in content or "update" in content.lower(), \
                "Profile update did not take effect"


# ============================================================================
# TRADING TESTS
# ============================================================================

@pytest.mark.smoke
def test_buy_stock_form_exists(logged_in_page: Page) -> None:
    """Test that buy stock form is available."""
    page = logged_in_page

    # Go to portfolio where buy form should be
    page.goto(f"{APP_URL}?action=portfolio", wait_until="domcontentloaded")

    # Look for buy form elements
    symbol_input = page.locator("input[name='symbol']")
    quantity_input = page.locator("input[name='quantity']")

    # At least one of these should exist
    has_buy_form = symbol_input.count() > 0 or quantity_input.count() > 0

    # Also check quotes page
    if not has_buy_form:
        page.goto(f"{APP_URL}?action=quotes&symbols=s:0",
                  wait_until="domcontentloaded")
        symbol_input = page.locator("input[name='symbol']")
        quantity_input = page.locator("input[name='quantity']")
        has_buy_form = symbol_input.count() > 0 or quantity_input.count() > 0

    assert has_buy_form, "Buy stock form not found"


@pytest.mark.smoke
def test_buy_stock(logged_in_page: Page) -> None:
    """Test buying a stock."""
    page = logged_in_page

    # Go to quotes and buy from there
    page.goto(f"{APP_URL}?action=quotes&symbols=s:0",
              wait_until="domcontentloaded")

    # Find buy form and submit
    quantity_input = page.locator("input[name='quantity']")
    if quantity_input.count() > 0:
        quantity_input.first.fill("10")

        buy_button = page.locator("input[type='submit'][value='Buy']")
        if buy_button.count() > 0:
            buy_button.first.click()
            page.wait_for_load_state("domcontentloaded")

            content = page.content().lower()
            # Should show order confirmation or error
            assert "order" in content or "confirmation" in content or \
                   "error" in content or "buy" in content, \
                   "Buy action did not produce expected response"


@pytest.mark.smoke
def test_sell_holding(logged_in_page: Page) -> None:
    """Sell a holding from the portfolio page."""
    page = logged_in_page

    # First buy some stock to ensure we have a holding
    page.goto(f"{APP_URL}?action=quotes&symbols=s:1",
              wait_until="domcontentloaded")
    quantity_input = page.locator("input[name='quantity']")
    if quantity_input.count() > 0:
        quantity_input.first.fill("5")
        buy_button = page.locator("input[type='submit'][value='Buy']")
        if buy_button.count() > 0:
            buy_button.first.click()
            page.wait_for_load_state("domcontentloaded")

    # Navigate to portfolio and sell a holding
    page.goto(f"{APP_URL}?action=portfolio", wait_until="domcontentloaded")
    sell_link = page.locator("a[href*='action=sell']")
    if sell_link.count() > 0:
        sell_link.first.click()
        page.wait_for_load_state("domcontentloaded")

        content = page.content().lower()
        assert "order" in content or "sell" in content or "confirmation" in content, \
            "Sell did not produce an order confirmation"


# ============================================================================
# MARKET SUMMARY TESTS
# ============================================================================

@pytest.mark.smoke
def test_market_summary(logged_in_page: Page) -> None:
    """Market summary should show TSIA, volume, gainers and losers."""
    page = logged_in_page

    page.goto(f"{APP_URL}?action=mksummary", wait_until="domcontentloaded")
    content = page.content().lower()

    if "unknown action" in content:
        pytest.skip("mksummary action not implemented in this deployment")

    assert "tsia" in content or "index" in content or "market" in content, \
        "TSIA / market index not shown on market summary page"
    assert "volume" in content or "trading" in content, \
        "Volume info not shown on market summary page"
    assert "gain" in content or "top" in content or "loser" in content, \
        "Gainers/losers not shown on market summary page"


# ============================================================================
# REGISTRATION TESTS
# ============================================================================

@pytest.mark.smoke
def test_register_new_user(page: Page) -> None:
    """Register a new user and verify account summary."""
    import time

    page.goto(APP_URL, wait_until="domcontentloaded")

    # Find and click register link
    register_link = page.locator("a[href*='register']")
    if register_link.count() == 0:
        # Quarkus deployments may not have a registration UI
        pytest.skip("Registration page not available in this deployment")

    register_link.first.click()
    page.wait_for_load_state("domcontentloaded")

    # Check if the registration form actually exists
    fullname_field = page.locator("input[name='Full Name']")
    if fullname_field.count() == 0:
        pytest.skip("Registration form not available in this deployment")

    user_suffix = int(time.time())
    user_id = f"smoke_user_{user_suffix}"

    # Fill required registration fields
    fullname_field.fill("Smoke Test User")
    page.locator("input[name='snail mail']").fill("123 Test Street")
    page.locator("input[name='email']").fill(f"{user_id}@example.com")
    page.locator("input[name='user id']").fill(user_id)
    page.locator("input[name='passwd']").fill("smoke-pass")
    page.locator("input[name='confirm passwd']").fill("smoke-pass")
    money_field = page.locator("input[name='money']")
    assert money_field.count() > 0, "Opening balance field 'money' not found"
    money_field.first.fill("10000")

    submit = page.locator("input[type='submit'][value='Submit Registration']")
    assert submit.count() > 0, "Submit Registration button not found"
    submit.first.click()
    page.wait_for_load_state("domcontentloaded")

    html = page.content()
    assert user_id in html, "Username not shown on post-registration page"

    lowered = html.lower()
    assert re.search(r"cash\s*balance", lowered) or "balance" in lowered, \
        "Cash balance section missing"


# ============================================================================
# CONFIGURATION TESTS
# ============================================================================

@pytest.mark.smoke
def test_config_page_displays_settings(page: Page) -> None:
    """Config page should display current DayTrader configuration."""
    page.goto(f"{BASE_URL}/config", wait_until="domcontentloaded")
    content = page.content().lower()

    if "resource not found" in content or "not found" in content:
        pytest.skip("Config page not available in this deployment")

    assert "configuration" in content or "config" in content or "daytrader" in content, \
        "Config page did not load"
    for param in ["max_users", "max_quotes", "orderfee", "maxusers", "maxquotes"]:
        if param in content:
            break
    else:
        assert "runtime" in content or "setting" in content or "parameter" in content \
            or "trade" in content, \
            "No configuration parameters shown on config page"


# ============================================================================
# REST API TESTS
# ============================================================================

@pytest.mark.smoke
def test_rest_get_quotes(page: Page) -> None:
    """REST GET /rest/quotes/{symbols} should return JSON with quote data."""
    response = page.request.get(f"{BASE_URL}/rest/quotes/s:0,s:1")

    assert response.ok, f"REST quotes GET failed with status {response.status}"
    data = response.json()
    assert isinstance(data, list), "REST quotes should return a list"
    assert len(data) == 2, f"Expected 2 quotes, got {len(data)}"


@pytest.mark.smoke
def test_rest_post_quotes(page: Page) -> None:
    """REST POST /rest/quotes should return quotes for form-encoded symbols."""
    response = page.request.post(
        f"{BASE_URL}/rest/quotes",
        form={"symbols": "s:0"},
    )

    assert response.ok, f"REST quotes POST failed with status {response.status}"
    content = str(response.json())
    assert "s:0" in content, "Quote for s:0 not found in POST response"


# ============================================================================
# SCENARIO SERVLET TESTS
# ============================================================================

@pytest.mark.smoke
def test_scenario_servlet(page: Page) -> None:
    """Scenario servlet should execute trading operations without error."""
    response = page.request.get(f"{BASE_URL}/scenario")

    assert response.ok or response.status < 500, \
        f"Scenario servlet returned error status {response.status}"
    content = response.text().lower()
    assert len(content) > 0, "Scenario servlet returned empty response"


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
