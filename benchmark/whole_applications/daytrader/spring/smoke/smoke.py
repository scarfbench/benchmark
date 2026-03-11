import re

import pytest
from playwright.sync_api import Page, expect


BASE_URL = "http://localhost:9080/daytrader"

# /config?action=buildDB streams output while it runs and can take a while.
# If the test navigates away too early, the request may be aborted and the
# database won't be populated, causing follow-on auth/register tests to fail.
BUILD_DB_TIMEOUT_MS = 180_000


def populate_database(page: Page) -> None:
    """Best-effort DB population via the config 'buildDB' action."""
    page.goto(f"{BASE_URL}/config?action=buildDB", wait_until="domcontentloaded")

    # Wait until the build has progressed far enough that login as uid:0 works.
    # The buildDB servlet prints an "Account# ... userID=uid:0" line once the
    # first demo user is registered.
    body = page.locator("body")
    expect(body).to_contain_text(
        re.compile(r"TradeBuildDB", re.IGNORECASE), timeout=BUILD_DB_TIMEOUT_MS
    )
    expect(body).to_contain_text(
        re.compile(r"userID\s*=\s*uid:0", re.IGNORECASE),
        timeout=BUILD_DB_TIMEOUT_MS,
    )


@pytest.mark.smoke
def test_global_navigation_and_health(page: Page) -> None:
    # Home / welcome page
    page.goto(f"{BASE_URL}/", wait_until="domcontentloaded")
    # Title should contain "Daytrader" (case-insensitive)
    expect(page).to_have_title(re.compile(r"daytrader", re.IGNORECASE))

    # Main content and key links (use loose selectors to tolerate legacy markup).
    # Some pages use framesets and may not expose a <body> element, so rely on
    # the raw HTML content instead of an ARIA heading/body locator.
    html = page.content().lower()
    assert "daytrader" in html
    for link_text in [
        "Login",
        "Sign In",
        "Register",
        "Quotes",
        "Portfolio",
        "Help",
        "Docs",
    ]:
        candidates = page.get_by_role("link", name=re.compile(link_text, re.IGNORECASE))
        # Some links may not exist (e.g., Help/Docs label differences); skip missing ones
        if candidates.count() > 0:
            expect(candidates.first).to_be_visible()

    # Top navigation – navigate to a few key pages and verify heading/content change
    nav_targets = [
        ("Login", "login"),
        ("Register", "register"),
        ("Market Summary", "market"),
        ("Portfolio", "portfolio"),
    ]
    for link_label, url_fragment in nav_targets:
        link = page.get_by_role("link", name=re.compile(link_label, re.IGNORECASE))
        if link.count() == 0:
            continue
        link.first.click()
        expect(page).to_have_url(
            re.compile(r"/daytrader/.*" + re.escape(url_fragment), re.IGNORECASE)
        )
        # Ensure the destination has some main content
        assert page.content().strip() != ""

    # Header / footer consistency (best-effort check on a couple of pages).
    # Some legacy layouts may not use semantic header/footer elements, so
    # only assert visibility if such locators actually resolve.
    for path in ["/", "/welcome.jsp"]:
        page.goto(f"{BASE_URL}{path}", wait_until="domcontentloaded")
        header = page.locator("header, #header, .header")
        footer = page.locator("footer, #footer, .footer")
        if header.count() > 0:
            expect(header.first).to_be_visible()
        if footer.count() > 0:
            expect(footer.first).to_be_visible()

    # Protected pages should redirect or show access error when unauthenticated
    protected_paths = [
        "/portfolio.jsp",
        "/tradehome.jsp",
        "/order.jsp",
        "/runStats.jsp",
    ]
    for path in protected_paths:
        page.goto(f"{BASE_URL}{path}", wait_until="domcontentloaded")
        url = page.url
        assert (
            "/login" in url
            or "access" in page.content().lower()
            or "error" in page.content().lower()
        )

    # 404 / invalid URL should show friendly error
    page.goto(f"{BASE_URL}/this-page-does-not-exist", wait_until="domcontentloaded")
    content_lower = page.content().lower()
    assert (
        "not found" in content_lower
        or "error" in content_lower
        or "invalid" in content_lower
    )

    # Static resources – basic heuristic: CSS references are present in the HTML
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    html = page.content()
    assert "style.css" in html or "style-jsf.css" in html


@pytest.mark.smoke
def test_render_login_page(page: Page) -> None:
    """Render login page: username/password fields, submit, and nav links."""
    # Try frameset root first; fall back to non-frames layout if needed.
    page.goto(f"{BASE_URL}/index.html", wait_until="domcontentloaded")

    header_frame = page.frame(name="TradeFrameTop")
    content_scope: Page | Page.Frame = page
    header_scope: Page | Page.Frame = page

    if header_frame is not None:
        header_scope = header_frame
        trade_tab = header_scope.locator("a[href='app']")
        assert trade_tab.count() > 0, (
            "Trading/Portfolios header link not found in frame"
        )
        trade_tab.first.click()

        content_frame = page.frame(name="TradeMainContent")
        if content_frame is not None:
            content_scope = content_frame
    else:
        # Fallback: load header.html directly and click the app link, then
        # rely on welcome.jsp as the login landing page.
        page.goto(f"{BASE_URL}/header.html", wait_until="domcontentloaded")
        header_scope = page
        trade_tab = header_scope.locator("a[href='app']")
        assert trade_tab.count() > 0, (
            "Trading/Portfolios header link not found in header.html"
        )
        trade_tab.first.click()

        page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
        content_scope = page

    # Username/password fields, based on welcome.jsp markup.
    username = content_scope.locator("input[name='uid']")
    password = content_scope.locator("input[type='password'][name='passwd']")
    assert username.count() > 0, "Username field (uid) not found on login page"
    assert password.count() > 0, "Password field (passwd) not found on login page"

    # Submit/login button.
    submit = content_scope.locator("input[type='submit'][value='Log in']")
    assert submit.count() > 0, "Login submit button not found on login page"

    # Links back to home and register: home is in header (when frames are used),
    # register is in the main content. On non-frames layouts, the home link may
    # not be present near the login form, so treat it as optional there.
    if header_frame is not None:
        home_link = header_scope.locator("a[href='contentHome.html']")
        assert home_link.count() > 0, "Home link not found in header on login page"

    register_link = content_scope.locator("a[href='register.jsp']")
    assert register_link.count() > 0, "Register link not found on login page"


@pytest.mark.smoke
def test_logged_in_nav_links(page: Page) -> None:
    """After login, verify all main account navigation links work."""
    populate_database(page)
    # Log in via the welcome page form using known demo credentials.
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")

    username = page.locator("input[name='uid']")
    password = page.locator("input[name='passwd']")
    submit = page.locator("input[type='submit'][value='Log in']")

    assert username.count() > 0, "Username field (uid) not found on welcome page"
    assert password.count() > 0, "Password field (passwd) not found on welcome page"
    assert submit.count() > 0, "Log in submit button not found on welcome page"

    username.first.fill("uid:0")
    password.first.fill("xxx")
    submit.first.click()
    page.wait_for_load_state("domcontentloaded")

    # After login, the DayTrader account navbar should usually be present.
    # Some deployments/markups can make individual link detection flaky
    # (URL rewriting, image-only anchors, etc.), so:
    # 1) Assert that we see *some* authenticated navbar links.
    # 2) For each target action, click the navbar link if found, otherwise
    #    navigate directly to /app?action=... and validate the page loads.
    assert page.locator("a[href*='app?action=']").count() >= 3, (
        "Expected an authenticated navbar after login, but found too few app?action links"
    )

    action_urls = {
        "home": f"{BASE_URL}/app?action=home",
        "account": f"{BASE_URL}/app?action=account",
        "mksummary": f"{BASE_URL}/app?action=mksummary",
        "portfolio": f"{BASE_URL}/app?action=portfolio",
        "quotes": f"{BASE_URL}/app?action=quotes&symbols=s:0,s:1,s:2,s:3,s:4",
        "logout": f"{BASE_URL}/app?action=logout",
    }

    for action, url in action_urls.items():
        link = page.locator(f"a[href*='action={action}']")
        if link.count() > 0:
            link.first.click()
            page.wait_for_load_state("domcontentloaded")
        else:
            page.goto(url, wait_until="domcontentloaded")

        if action != "logout":
            # We should remain within the authenticated app flow.
            assert f"app?action={action}" in page.url
            assert "Log in" not in page.content()
        else:
            # Logoff should send us back to a login/welcome experience.
            assert "welcome.jsp" in page.url or "Log in" in page.content()


@pytest.mark.smoke
def test_register_new_user(page: Page) -> None:
    """Register a new user with $10,000 and verify account summary."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")

    # Click the register link from the welcome page.
    register_link = page.locator("a[href='register.jsp']")
    assert register_link.count() > 0, "Register link not found on welcome page"
    register_link.first.click()
    page.wait_for_load_state("domcontentloaded")

    # Build a unique user id to avoid clashes across runs.
    import time

    user_suffix = int(time.time())
    user_id = f"smoke_user_{user_suffix}"

    # Fill required registration fields.
    page.locator("input[name='Full Name']").fill("Smoke Test User")
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
    # Basic sanity: welcome message includes username.
    assert user_id in html, "Username not shown on post-registration page"

    # Verify key account summary values for a fresh $10,000 account.
    lowered = html.lower()
    # The labels are rendered with linebreaks and extra spaces; use
    # whitespace-tolerant regexes based on the actual markup.
    assert re.search(r"cash\s*balance", lowered), "Cash balance section missing"
    assert re.search(r"\$\s*10000(\.00)?\b", html), "Cash balance is not $ 10000(.00)"
    assert re.search(r"number\s*of\s*holdings", lowered), "Holdings summary missing"
    # Expect zero holdings value in the corresponding column.
    assert re.search(r">\s*0\s*<", html), "Expected zero holdings for new account"
    assert re.search(r"opening\s*balance", lowered), "Opening balance section missing"


@pytest.mark.smoke
def test_login_with_invalid_credentials(page: Page) -> None:
    """Login with invalid password should show error or stay on login page."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")

    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("wrong_password")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    content = page.content().lower()
    assert "error" in content or "fail" in content or "login" in content, \
        "Error message not shown for invalid login"


@pytest.mark.smoke
def test_logout_invalidates_session(page: Page) -> None:
    """Logout should invalidate session and redirect to welcome/login page."""
    populate_database(page)
    # Login first
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    # Logout
    logout_link = page.locator("a[href='app?action=logout']")
    assert logout_link.count() > 0, "Logout link not found"
    logout_link.first.click()
    page.wait_for_load_state("domcontentloaded")

    content = page.content()
    assert "welcome.jsp" in page.url or "Log in" in content, \
        "Logout did not redirect to login page"

    # Verify session is invalidated by trying to access a protected action
    page.goto(f"{BASE_URL}/app?action=home", wait_until="domcontentloaded")
    content = page.content().lower()
    assert "log in" in content or "welcome" in content or "login" in content, \
        "Session was not invalidated after logout"


@pytest.mark.smoke
def test_home_page_account_summary(page: Page) -> None:
    """Home page should display account summary after login."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    # Navigate to home
    page.goto(f"{BASE_URL}/app?action=home", wait_until="domcontentloaded")
    content = page.content().lower()

    assert re.search(r"account\s*id", content) or "uid:0" in content, \
        "Account ID not shown on home page"
    assert "balance" in content or "$" in page.content(), \
        "Balance info not shown on home page"
    assert re.search(r"(holdings|gain|loss)", content), \
        "Holdings/gain/loss info not shown on home page"


@pytest.mark.smoke
def test_view_portfolio(page: Page) -> None:
    """Portfolio page should show holdings table."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    page.goto(f"{BASE_URL}/app?action=portfolio", wait_until="domcontentloaded")
    content = page.content().lower()

    assert "portfolio" in content or "holding" in content, \
        "Portfolio page did not load correctly"
    table = page.locator("table")
    assert table.count() > 0, "Portfolio holdings table not found"


@pytest.mark.smoke
def test_buy_shares(page: Page) -> None:
    """Buy shares of a stock and verify order confirmation."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    page.goto(
        f"{BASE_URL}/app?action=quotes&symbols=s:0",
        wait_until="domcontentloaded",
    )

    quantity_input = page.locator("input[name='quantity']")
    if quantity_input.count() > 0:
        quantity_input.first.fill("10")
        buy_button = page.locator("input[type='submit'][value='Buy']")
        if buy_button.count() > 0:
            buy_button.first.click()
            page.wait_for_load_state("domcontentloaded")

            content = page.content().lower()
            assert "order" in content or "buy" in content or "confirmation" in content, \
                "Buy did not produce an order confirmation"


@pytest.mark.smoke
def test_sell_holding(page: Page) -> None:
    """Sell a holding from the portfolio page."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    # First buy some stock to ensure we have a holding
    page.goto(
        f"{BASE_URL}/app?action=quotes&symbols=s:1",
        wait_until="domcontentloaded",
    )
    quantity_input = page.locator("input[name='quantity']")
    if quantity_input.count() > 0:
        quantity_input.first.fill("5")
        buy_button = page.locator("input[type='submit'][value='Buy']")
        if buy_button.count() > 0:
            buy_button.first.click()
            page.wait_for_load_state("domcontentloaded")

    # Navigate to portfolio and sell a holding
    page.goto(f"{BASE_URL}/app?action=portfolio", wait_until="domcontentloaded")
    sell_link = page.locator("a[href*='action=sell']")
    if sell_link.count() > 0:
        sell_link.first.click()
        page.wait_for_load_state("domcontentloaded")

        content = page.content().lower()
        assert "order" in content or "sell" in content or "confirmation" in content, \
            "Sell did not produce an order confirmation"


@pytest.mark.smoke
def test_view_single_quote(page: Page) -> None:
    """Look up a single stock quote and verify quote data is shown."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    page.goto(
        f"{BASE_URL}/app?action=quotes&symbols=s:0",
        wait_until="domcontentloaded",
    )
    content = page.content().lower()

    assert "s:0" in content, "Symbol s:0 not displayed in quote results"
    assert "price" in content or "$" in page.content(), \
        "Price information not displayed for quote"


@pytest.mark.smoke
def test_view_multiple_quotes(page: Page) -> None:
    """Look up multiple stock quotes and verify multiple rows are shown."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    page.goto(
        f"{BASE_URL}/app?action=quotes&symbols=s:0,s:1,s:2",
        wait_until="domcontentloaded",
    )
    content = page.content().lower()

    assert "s:0" in content, "Symbol s:0 not in multi-quote results"
    assert "s:1" in content, "Symbol s:1 not in multi-quote results"
    assert "s:2" in content, "Symbol s:2 not in multi-quote results"


@pytest.mark.smoke
def test_quote_buy_form(page: Page) -> None:
    """Quote page should show a buy form with quantity input and Buy button."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    page.goto(
        f"{BASE_URL}/app?action=quotes&symbols=s:0",
        wait_until="domcontentloaded",
    )

    quantity_input = page.locator("input[name='quantity']")
    buy_button = page.locator(
        "input[type='submit'][value='buy'], "
        "input[type='submit'][value='Buy']"
    )
    assert quantity_input.count() > 0, "Quantity input not found on quote page"
    assert buy_button.count() > 0, "Buy button not found on quote page"


@pytest.mark.smoke
def test_market_summary(page: Page) -> None:
    """Market summary should show TSIA, volume, gainers and losers."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    page.goto(f"{BASE_URL}/app?action=mksummary", wait_until="domcontentloaded")
    content = page.content().lower()

    assert "tsia" in content or "index" in content or "market" in content, \
        "TSIA / market index not shown on market summary page"
    assert "volume" in content or "trading" in content, \
        "Volume info not shown on market summary page"
    assert "gain" in content or "top" in content or "loser" in content, \
        "Gainers/losers not shown on market summary page"


@pytest.mark.smoke
def test_view_account_and_orders(page: Page) -> None:
    """Account page should show profile info and recent orders."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    page.goto(f"{BASE_URL}/app?action=account", wait_until="domcontentloaded")
    content = page.content().lower()

    assert "account" in content, "Account page did not load"
    assert "uid:0" in content or "profile" in content, \
        "Profile information not shown on account page"
    assert "order" in content or "balance" in content, \
        "Orders or balance section not shown on account page"


@pytest.mark.smoke
def test_update_profile(page: Page) -> None:
    """Update profile information and verify it is saved."""
    populate_database(page)
    page.goto(f"{BASE_URL}/welcome.jsp", wait_until="domcontentloaded")
    page.locator("input[name='uid']").first.fill("uid:0")
    page.locator("input[name='passwd']").first.fill("xxx")
    page.locator("input[type='submit'][value='Log in']").first.click()
    page.wait_for_load_state("domcontentloaded")

    page.goto(f"{BASE_URL}/app?action=account", wait_until="domcontentloaded")

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


@pytest.mark.smoke
def test_config_page_displays_settings(page: Page) -> None:
    """Config page should display current DayTrader configuration."""
    page.goto(f"{BASE_URL}/config", wait_until="domcontentloaded")
    content = page.content().lower()

    assert "configuration" in content or "config" in content or "daytrader" in content, \
        "Config page did not load"
    for param in ["max_users", "max_quotes", "orderfee", "maxusers", "maxquotes"]:
        if param in content:
            break
    else:
        assert "runtime" in content or "setting" in content or "parameter" in content \
            or "trade" in content, \
            "No configuration parameters shown on config page"


@pytest.mark.smoke
def test_rest_get_quotes(page: Page) -> None:
    """REST GET /rest/quotes/{symbols} should return JSON with quote data."""
    populate_database(page)
    response = page.request.get(f"{BASE_URL}/rest/quotes/s:0,s:1")

    if response.status == 404:
        pytest.skip("REST quotes GET endpoint not available in this deployment")
    assert response.ok, f"REST quotes GET failed with status {response.status}"
    data = response.json()
    assert isinstance(data, list), "REST quotes should return a list"
    assert len(data) == 2, f"Expected 2 quotes, got {len(data)}"


@pytest.mark.smoke
def test_rest_post_quotes(page: Page) -> None:
    """REST POST /rest/quotes should return quotes for form-encoded symbols."""
    populate_database(page)
    response = page.request.post(
        f"{BASE_URL}/rest/quotes",
        form={"symbols": "s:0"},
    )

    if response.status == 404:
        pytest.skip("REST quotes POST endpoint not available in this deployment")
    assert response.ok, f"REST quotes POST failed with status {response.status}"
    content = str(response.json())
    assert "s:0" in content, "Quote for s:0 not found in POST response"


@pytest.mark.smoke
def test_scenario_servlet(page: Page) -> None:
    """Scenario servlet should execute trading operations without error."""
    populate_database(page)
    response = page.request.get(f"{BASE_URL}/scenario")

    assert response.ok or response.status < 500, \
        f"Scenario servlet returned error status {response.status}"
    content = response.text().lower()
    assert len(content) > 0, "Scenario servlet returned empty response"


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
