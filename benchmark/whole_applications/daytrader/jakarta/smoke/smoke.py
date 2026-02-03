import re

import pytest
from playwright.sync_api import Page, expect


BASE_URL = "http://localhost:9080/daytrader"


@pytest.mark.smoke
def test_create_and_populate_database(page: Page) -> None:
    """Create the DB tables"""
    page.goto(f"{BASE_URL}/config?action=buildDBTables", wait_until="domcontentloaded")
    # Do not assert strongly on content, just ensure the page rendered.
    assert "daytrader" in page.content().lower()

    """Best-effort DB population via the config 'buildDB' action."""
    page.goto(f"{BASE_URL}/config?action=buildDB", wait_until="domcontentloaded")
    # Do not assert strongly on content, just ensure the page rendered.
    assert "daytrader" in page.content().lower()


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

    # After login, the DayTrader account navbar should be present.
    # Use concrete hrefs from account.jsp for robust selection.
    nav_links = [
        ("app?action=home", "home", False),
        ("app?action=account", "account", False),
        ("app?action=mksummary", "mksummary", False),
        ("app?action=portfolio", "portfolio", False),
        ("app?action=quotes", "quotes", True),  # quotes has extra symbols param
        ("app?action=logout", "logout", False),
    ]

    for href, action, is_prefix in nav_links:
        if is_prefix:
            link = page.locator(f"a[href^='{href}']")
        else:
            link = page.locator(f"a[href='{href}']")
        assert link.count() > 0, (
            f"Navigation link with href '{href}' not found on logged-in page"
        )

        link.first.click()
        page.wait_for_load_state("domcontentloaded")

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
    assert "$ 10000.00" in html, "Cash balance is not $ 10000.00"
    assert re.search(r"number\s*of\s*holdings", lowered), "Holdings summary missing"
    # Expect zero holdings value in the corresponding column.
    assert re.search(r">\s*0\s*<", html), "Expected zero holdings for new account"
    assert re.search(r"opening\s*balance", lowered), "Opening balance section missing"


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
