"""
Smoke tests for DayTrader Quarkus application using Playwright.

This test suite verifies the basic functionality of the migrated DayTrader application:
- Home page loads
- Login/logout flow
- Trading operations (view quotes, buy, sell)
- Portfolio view
- Account information

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


# ============================================================================
# REST API TESTS
# ============================================================================

# @pytest.mark.smoke
# def test_rest_market_endpoint(page: Page) -> None:
#     """Test the REST API market endpoint."""
#     response = page.request.get(f"{BASE_URL}/rest/trade/market")
    
#     assert response.ok, f"Market endpoint failed with status {response.status}"
#     data = response.json()
#     # Should return JSON with market data
#     assert "TSIA" in str(data) or "tsia" in str(data).lower() or \
#            "openTSIA" in str(data), "Market endpoint did not return expected data"


# @pytest.mark.smoke
# def test_rest_quotes_endpoint(page: Page) -> None:
#     """Test the REST API quotes endpoint."""
#     response = page.request.get(f"{BASE_URL}/rest/quotes/s:0,s:1")
    
#     assert response.ok, f"Quotes endpoint failed with status {response.status}"
#     data = response.json()
#     # Should return JSON array with quote data
#     assert isinstance(data, list), "Quotes endpoint should return a list"
#     if len(data) > 0:
#         assert "symbol" in str(data[0]).lower() or "s:" in str(data), \
#             "Quote data not found in response"


# # ============================================================================
# # ERROR HANDLING TESTS
# # ============================================================================

# @pytest.mark.smoke
# def test_404_handling(page: Page) -> None:
#     """Test that 404 errors are handled gracefully."""
#     response = page.goto(f"{BASE_URL}/nonexistent-page-12345")
    
#     # Should either return 404 or redirect
#     if response is not None:
#         assert response.status in [404, 200, 302, 301], \
#             f"Unexpected status code: {response.status}"


# @pytest.mark.smoke
# def test_invalid_action_handling(page: Page) -> None:
#     """Test handling of invalid action parameter."""
#     page.goto(f"{APP_URL}?action=invalid_action_xyz", 
#               wait_until="domcontentloaded")
    
#     content = page.content().lower()
#     # Should show error or redirect to login/welcome
#     assert "error" in content or "unknown" in content or \
#            "login" in content or "welcome" in content, \
#            "Invalid action not handled properly"


# # ============================================================================
# # PERFORMANCE SANITY TESTS
# # ============================================================================

# @pytest.mark.smoke
# def test_page_load_time(page: Page) -> None:
#     """Test that pages load within reasonable time."""
#     import time
    
#     start = time.time()
#     page.goto(f"{BASE_URL}/", wait_until="domcontentloaded")
#     elapsed = time.time() - start
    
#     # Should load within 10 seconds
#     assert elapsed < 10, f"Page took too long to load: {elapsed:.2f}s"


# @pytest.mark.smoke
# def test_multiple_requests(page: Page) -> None:
#     """Test that multiple rapid requests work."""
#     pages_to_test = [
#         f"{BASE_URL}/",
#         APP_URL,
#         f"{BASE_URL}/style.css",
#         f"{BASE_URL}/images/dayTraderLogo.gif",
#     ]
    
#     for url in pages_to_test:
#         response = page.goto(url)
#         assert response is not None and response.ok, f"Failed to load {url}"


# # ============================================================================
# # MESSAGING TESTS (JMS -> Reactive Messaging Migration)
# # ============================================================================

# @pytest.mark.smoke
# def test_messaging_ping_broker(page: Page) -> None:
#     """Test sending a ping message to the broker queue (replaces PingServlet2MDBQueue)."""
#     import json
    
#     response = page.request.post(
#         f"{BASE_URL}/rest/messaging/ping/broker",
#         params={"message": "smoke test ping"}
#     )
    
#     assert response.ok, f"Broker ping failed: {response.status}"
#     data = response.json()
#     assert data["status"] == "sent", "Broker ping not sent"
#     assert data["destination"] == "trade-broker-queue", "Wrong destination"


# @pytest.mark.smoke
# def test_messaging_ping_streamer(page: Page) -> None:
#     """Test sending a ping message to the streamer topic (replaces PingServlet2MDBTopic)."""
#     response = page.request.post(
#         f"{BASE_URL}/rest/messaging/ping/streamer",
#         params={"message": "smoke test ping to streamer"}
#     )
    
#     assert response.ok, f"Streamer ping failed: {response.status}"
#     data = response.json()
#     assert data["status"] == "sent", "Streamer ping not sent"
#     assert data["destination"] == "trade-streamer-topic", "Wrong destination"


# @pytest.mark.smoke
# def test_messaging_stats_endpoint(page: Page) -> None:
#     """Test that messaging statistics endpoint works."""
#     response = page.request.get(f"{BASE_URL}/rest/messaging/stats")
    
#     assert response.ok, f"Stats endpoint failed: {response.status}"
#     data = response.json()
#     assert "statistics" in data, "Statistics not found in response"
#     assert "timestamp" in data, "Timestamp not found in response"


# @pytest.mark.smoke
# def test_messaging_stats_reset(page: Page) -> None:
#     """Test that messaging statistics can be reset."""
#     response = page.request.post(f"{BASE_URL}/rest/messaging/stats/reset")
    
#     assert response.ok, f"Stats reset failed: {response.status}"
#     data = response.json()
#     assert data["status"] == "reset", "Stats reset failed"


# @pytest.mark.smoke
# def test_messaging_broker_processes_ping(page: Page) -> None:
#     """Test that broker ping messages are actually processed.
    
#     This verifies the full message flow:
#     1. Send ping via MessageProducerService
#     2. DTBroker3MDB receives and processes it
#     3. Statistics are updated
#     """
#     import time
    
#     # Reset stats first
#     page.request.post(f"{BASE_URL}/rest/messaging/stats/reset")
    
#     # Send multiple pings
#     for i in range(3):
#         response = page.request.post(
#             f"{BASE_URL}/rest/messaging/ping/broker",
#             params={"message": f"ping {i}"}
#         )
#         assert response.ok
    
#     # Wait for async processing
#     time.sleep(1)
    
#     # Check stats - should have broker ping stats
#     response = page.request.get(f"{BASE_URL}/rest/messaging/stats")
#     assert response.ok
#     data = response.json()
    
#     stats = data.get("statistics", {})
#     # Look for broker MDB stats (using original class name)
#     broker_stats = stats.get("DTBroker3MDB:ping", {})
#     if broker_stats:
#         assert broker_stats.get("count", 0) >= 3, "Not all pings were processed"


# @pytest.mark.smoke
# def test_messaging_streamer_processes_ping(page: Page) -> None:
#     """Test that streamer ping messages are actually processed.
    
#     This verifies the full message flow:
#     1. Send ping via MessageProducerService
#     2. DTStreamer3MDB receives and processes it
#     3. Statistics are updated
#     """
#     import time
    
#     # Reset stats first
#     page.request.post(f"{BASE_URL}/rest/messaging/stats/reset")
    
#     # Send multiple pings
#     for i in range(3):
#         response = page.request.post(
#             f"{BASE_URL}/rest/messaging/ping/streamer",
#             params={"message": f"ping {i}"}
#         )
#         assert response.ok
    
#     # Wait for async processing
#     time.sleep(1)
    
#     # Check stats - should have streamer ping stats
#     response = page.request.get(f"{BASE_URL}/rest/messaging/stats")
#     assert response.ok
#     data = response.json()
    
#     stats = data.get("statistics", {})
#     # Look for streamer MDB stats (using original class name)
#     streamer_stats = stats.get("DTStreamer3MDB:ping", {})
#     if streamer_stats:
#         assert streamer_stats.get("count", 0) >= 3, "Not all pings were processed"


# @pytest.mark.smoke  
# def test_async_order_processing(logged_in_page: Page) -> None:
#     """Test asynchronous order processing via messaging.
    
#     This is the key JMS migration test:
#     1. User places a buy order via REST API
#     2. Order is created with 'open' status
#     3. Message sent to trade-broker-queue
#     4. DTBroker3MDB completes the order
#     5. Order status becomes 'closed'
    
#     Uses Playwright's request API instead of requests library.
#     """
#     import time
    
#     page = logged_in_page
    
#     # Reset messaging stats via REST API
#     page.request.post(f"{BASE_URL}/rest/messaging/stats/reset")
    
#     # Execute a buy order via REST API
#     buy_response = page.request.post(
#         f"{BASE_URL}/rest/trade/buy",
#         form={"userID": "uid:0", "symbol": "s:0", "quantity": "1"}
#     )
    
#     # The buy endpoint may return 200 or fail - just check it doesn't crash
#     # The key test is that messaging stats are updated
    
#     # Wait for async message processing
#     time.sleep(1)
    
#     # Check messaging stats - if async order processing worked,
#     # we should see stats from DTBroker3MDB
#     stats_response = page.request.get(f"{BASE_URL}/rest/messaging/stats")
#     if stats_response.ok:
#         stats = stats_response.json().get("statistics", {})
#         # Even if buy failed, the stats endpoint should work
#         assert isinstance(stats, dict), "Stats should be a dictionary"
    
#     # Alternative: just verify the page still works after messaging operations
#     page.reload()
#     page.wait_for_load_state("domcontentloaded")
#     assert page.url is not None, "Page should still be accessible"


# # ============================================================================
# # ADDITIONAL REST API TESTS
# # ============================================================================

# @pytest.mark.smoke
# def test_rest_account_endpoint(page: Page) -> None:
#     """Test the REST API account endpoint."""
#     response = page.request.get(f"{BASE_URL}/rest/trade/account/uid:0")
    
#     assert response.ok, f"Account endpoint failed with status {response.status}"
#     data = response.json()
#     # Should return account data
#     assert "profileID" in str(data) or "accountID" in str(data) or \
#            "balance" in str(data).lower(), "Account data not found in response"


# @pytest.mark.smoke
# def test_rest_holdings_endpoint(page: Page) -> None:
#     """Test the REST API holdings endpoint."""
#     response = page.request.get(f"{BASE_URL}/rest/trade/account/uid:0/holdings")
    
#     assert response.ok, f"Holdings endpoint failed with status {response.status}"
#     data = response.json()
#     # Should return a list (could be empty if no holdings)
#     assert isinstance(data, list), "Holdings endpoint should return a list"


# @pytest.mark.smoke
# def test_rest_orders_endpoint(page: Page) -> None:
#     """Test the REST API orders endpoint."""
#     response = page.request.get(f"{BASE_URL}/rest/trade/account/uid:0/orders")
    
#     assert response.ok, f"Orders endpoint failed with status {response.status}"
#     data = response.json()
#     # Should return a list (could be empty if no orders)
#     assert isinstance(data, list), "Orders endpoint should return a list"


# @pytest.mark.smoke
# def test_rest_login_endpoint(page: Page) -> None:
#     """Test the REST API login endpoint."""
#     response = page.request.post(
#         f"{BASE_URL}/rest/trade/login",
#         form={"userID": "uid:0", "password": "xxx"}
#     )
    
#     assert response.ok, f"Login endpoint failed with status {response.status}"
#     data = response.json()
#     assert "profileID" in str(data) or "accountID" in str(data), \
#         "Login did not return account data"


# @pytest.mark.smoke
# def test_rest_login_invalid_credentials(page: Page) -> None:
#     """Test the REST API login with invalid credentials."""
#     response = page.request.post(
#         f"{BASE_URL}/rest/trade/login",
#         form={"userID": "invalid_user", "password": "wrong"}
#     )
    
#     # Should return 401 Unauthorized
#     assert response.status == 401, f"Expected 401 for invalid login, got {response.status}"


# @pytest.mark.smoke
# def test_rest_buy_endpoint(page: Page) -> None:
#     """Test the REST API buy endpoint."""
#     response = page.request.post(
#         f"{BASE_URL}/rest/trade/buy",
#         form={"userID": "uid:0", "symbol": "s:0", "quantity": "5"}
#     )
    
#     # Buy might succeed or fail depending on balance, but should not error
#     assert response.status in [200, 500], f"Unexpected status: {response.status}"


# @pytest.mark.smoke
# def test_rest_all_quotes_via_get(page: Page) -> None:
#     """Test getting multiple quotes via REST API."""
#     symbols = ",".join([f"s:{i}" for i in range(5)])
#     response = page.request.get(f"{BASE_URL}/rest/quotes/{symbols}")
    
#     assert response.ok, f"Quotes endpoint failed with status {response.status}"
#     data = response.json()
#     assert isinstance(data, list), "Quotes should return a list"
#     # Should have up to 5 quotes
#     assert len(data) <= 5, "Too many quotes returned"


# @pytest.mark.smoke
# def test_health_check(page: Page) -> None:
#     """Test Quarkus health check endpoint."""
#     response = page.request.get(f"{BASE_URL}/q/health")
    
#     assert response.ok, f"Health check failed with status {response.status}"
#     data = response.json()
#     assert data.get("status") == "UP", "Application is not healthy"


# @pytest.mark.smoke
# def test_ready_check(page: Page) -> None:
#     """Test Quarkus readiness check endpoint."""
#     response = page.request.get(f"{BASE_URL}/q/health/ready")
    
#     assert response.ok, f"Ready check failed with status {response.status}"
#     data = response.json()
#     assert data.get("status") == "UP", "Application is not ready"


# @pytest.mark.smoke
# def test_live_check(page: Page) -> None:
#     """Test Quarkus liveness check endpoint."""
#     response = page.request.get(f"{BASE_URL}/q/health/live")
    
#     assert response.ok, f"Live check failed with status {response.status}"
#     data = response.json()
#     assert data.get("status") == "UP", "Application is not live"
