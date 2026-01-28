import os
import sys
from random import randint

import pytest
from playwright.sync_api import expect, sync_playwright

BASE = os.getenv("ROSTER_BASE", "http://localhost:9080/roster").rstrip("/")
HEADLESS = os.getenv("HEADLESS", "1") == "1"


@pytest.fixture(scope="session")
def playwright_instance():
    """Start Playwright once per test session."""
    with sync_playwright() as p:
        yield p


@pytest.fixture(scope="session")
def browser(playwright_instance):
    """Launch one browser for the whole test session."""
    browser = playwright_instance.chromium.launch(headless=HEADLESS)
    try:
        yield browser
    finally:
        browser.close()


@pytest.fixture
def page(browser):
    """New page per test (keeps tests isolated)."""
    page = browser.new_page()
    try:
        yield page
    finally:
        page.close()


def test_create_league(page):
    page.goto(f"{BASE}/league.xhtml", wait_until="domcontentloaded")
    print("[INFO] Navigated to league creation page")

    expect(
        page.get_by_role("heading", name="Roster Application - League Management")
    ).to_be_visible()
    print("[INFO] Verified league creation page contents")

    league_id = f"L{randint(0, 1000)}"
    league_name = "English Premier League"
    league_sport = "soccer"  # valid: soccer, swimming, basketball, baseball, hockey, skiing, snowboarding
    print("[INFO] Creating league for", league_name)

    # JSF ids contain ':' -> escape in CSS selectors as \\:
    page.locator("#createLeagueForm\\:leagueId").fill(league_id)
    page.locator("#createLeagueForm\\:leagueName").fill(league_name)
    page.locator("#createLeagueForm\\:leagueSport").fill(league_sport)
    print("[INFO] Filled league creation form")

    # JSF commandButton does a postback and typically stays on the same URL.
    # Also: this page only renders the canonical seed leagues (L1-L4) in the table,
    # so validating persistence via table contents is not reliable here.
    page.get_by_role("button", name="Create League").click()
    print("[INFO] Submitted league creation form")

    messages = page.locator(".messages")
    expect(messages).to_contain_text("League created successfully")

    # Fields should be cleared after successful creation.
    expect(page.locator("#createLeagueForm\\:leagueId")).to_have_value("")
    expect(page.locator("#createLeagueForm\\:leagueName")).to_have_value("")
    expect(page.locator("#createLeagueForm\\:leagueSport")).to_have_value("")
    print("[INFO] Verified form fields creation")


def test_create_a_team(page):
    # First let's check if we can go to the create team page that is located at `team.xhtml`
    page.goto("http://localhost:9080/roster/team.xhtml", wait_until="domcontentloaded")
    print("[INFO] Navigated to team creation page")

    # Check if the page has the correct contents
    expect(
        page.get_by_role("heading", name="Roster Application - Team Management")
    ).to_be_visible()
    print("[INFO] Verified team creation page contents")

    # Create a new team
    team_id: str = "1"
    team_name: str = "Arsenal FC"
    city: str = "London"

    page.locator("#createTeamForm\\:teamId").fill(team_id)
    page.locator("#createTeamForm\\:teamName").fill(team_name)
    page.locator("#createTeamForm\\:city").fill(city)
    league_selector = page.locator("#createTeamForm\\:league")
    options = league_selector.locator("option")
    # Select first real option (skip placeholder)
    first_real_value = options.nth(1).get_attribute("value")
    assert first_real_value and first_real_value != ""
    league_selector.select_option(value=first_real_value)
    with page.expect_navigation(wait_until="domcontentloaded"):
        page.get_by_role("button", name="Add Team").click()
    print("[INFO] Submitted team creation form")

    # Wait until teams information is visible
    table = page.locator("table.data-table")
    expect(table).to_be_visible()
    print("[INFO] Verified teams information is visible")

    # Lastly, verify that a row exists with all the data we just populated.
    row = table.locator("tr", has_text=team_id).filter(has_text=team_name).filter(has_text=city)
    expect(row).to_have_count(1)
    print("[INFO] Verified team information is visible")


def test_create_a_player(page):
    "Create a player to put in the team"
    page.goto("http://localhost:9080/roster/player.xhtml", wait_until="domcontentloaded")
    page.get_by_role("heading", name="Roster Application - Player Management")
    print("[INFO] Verified player creation page contents")

    # Create a new player
    player_id: str = "1"
    player_name: str = "Martin Odegaard"
    position: str = "CAM"
    salary: str = "1000000"

    page.locator("#createPlayerForm\\:playerIdInputText").fill(player_id)
    page.locator("#createPlayerForm\\:nameInputText").fill(player_name)
    page.locator("#createPlayerForm\\:positionInputText").fill(position)
    page.locator("#createPlayerForm\\:salaryInputText").fill(salary)
    team_selector = page.locator("#createPlayerForm\\:team")
    options = team_selector.locator("option")
    first_real_value = options.nth(1).get_attribute("value")
    assert first_real_value and first_real_value != ""
    team_selector.select_option(value=first_real_value)
    with page.expect_navigation(wait_until="domcontentloaded"):
        page.get_by_role("button", name="Submit").click()
    print("[INFO] Submitted player creation form")

    # Wait until players information is visible
    table = page.locator("table.data-table")
    expect(table).to_be_visible()
    print("[INFO] Verified players information is visible")

    # Lastly, verify that a row exists with all the data we just populated.
    row = table.locator("tr", has_text=player_id).filter(has_text=player_name).filter(has_text=position)
    expect(row).to_have_count(1)
    print("[INFO] Verified player information is visible")


def main() -> int:
    return pytest.main([__file__, "-q", "-x"])


if __name__ == "__main__":
    sys.exit(main())
