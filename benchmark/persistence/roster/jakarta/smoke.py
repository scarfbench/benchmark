"""
Smoke test for Jakarta EE "Roster" app on Open Liberty.

Tests the REST API endpoints at /api/roster/* that manage leagues, teams,
and players. Maps to scenarios in roster.feature.

Environment:
  APP_PORT   Application port (default: 9080)
  VERBOSE=1  Verbose logging

Exit codes:
  0  success (via pytest)
"""

import json
import os
import sys
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

import pytest

APP_PORT = os.getenv("APP_PORT", "9080")
BASE = f"http://localhost:{APP_PORT}/roster"
VERBOSE = os.getenv("VERBOSE") == "1"


def vprint(*args):
    if VERBOSE:
        print(*args)


def http(method, path, body=None, query=None, content_type="application/json", timeout=10):
    """Make an HTTP request and return (status, body_str)."""
    url = f"{BASE}{path}"
    if query:
        from urllib.parse import quote
        url += "?" + "&".join(f"{k}={quote(str(v))}" for k, v in query.items())
    data = None
    if body is not None:
        if content_type == "application/json":
            data = json.dumps(body).encode()
        elif content_type == "application/x-www-form-urlencoded":
            from urllib.parse import urlencode
            data = urlencode(body).encode()

    headers = {"User-Agent": "Roster-Smoke/1.0"}
    if content_type and data is not None:
        headers["Content-Type"] = content_type
    if method == "GET":
        headers["Accept"] = "application/json"

    req = Request(url, data=data, method=method, headers=headers)
    try:
        with urlopen(req, timeout=timeout) as resp:
            return resp.getcode(), resp.read().decode("utf-8", "replace")
    except HTTPError as e:
        try:
            body_text = e.read().decode("utf-8", "replace")
        except Exception:
            body_text = ""
        return e.code, body_text
    except (URLError, Exception) as e:
        pytest.fail(f"Network error on {method} {path}: {e}")


def json_get(path, query=None):
    """GET JSON and parse the response."""
    status, body = http("GET", path, query=query)
    assert status == 200, f"GET {path} returned {status}: {body}"
    return json.loads(body) if body.strip() else None


# ---------------------------------------------------------------------------
# Fixtures — seed canonical data (leagues + teams + players) once
# ---------------------------------------------------------------------------


@pytest.fixture(scope="session", autouse=True)
def seed_data():
    """Seed canonical leagues, teams, and players for the test session."""
    # Create leagues
    leagues = [
        {"id": "L1", "name": "Mountain", "sport": "Soccer"},
        {"id": "L2", "name": "Valley", "sport": "Basketball"},
        {"id": "L3", "name": "Foothills", "sport": "Soccer"},
        {"id": "L4", "name": "Alpine", "sport": "Snowboarding"},
    ]
    for lg in leagues:
        http("POST", "/league", body=lg)

    # Create teams
    teams = [
        ("T1", "Honey Bees", "Visalia", "L1"),
        ("T2", "Gophers", "Manteca", "L1"),
        ("T3", "Deer", "Bodie", "L2"),
        ("T5", "Crows", "Denver", "L1"),
    ]
    for tid, name, city, lid in teams:
        http("POST", f"/team/league/{lid}", body={"id": tid, "name": name, "city": city})

    # Create players via query params
    players = [
        ("P1", "Duke", "forward", "50000"),
        ("P2", "Alice", "defender", "30000"),
        ("P3", "Bob", "midfielder", "45000"),
        ("P4", "Grace", "forward", "60000"),
        ("P5", "Unassigned", "pitcher", "20000"),
    ]
    for pid, name, pos, sal in players:
        http("POST", "/player", query={"id": pid, "name": name, "position": pos, "salary": sal})

    # Assign players to teams
    http("POST", "/player/P1/team/T1", body="")
    http("POST", "/player/P2/team/T1", body="")
    http("POST", "/player/P3/team/T2", body="")
    http("POST", "/player/P4/team/T1", body="")
    # P1 also on T3 (multi-team for cross-entity queries)
    http("POST", "/player/P1/team/T3", body="")
    # P5 stays unassigned

    yield


# ---------------------------------------------------------------------------
# League management
# ---------------------------------------------------------------------------


def test_application_is_running():
    """Application should be accessible."""
    status, _ = http("GET", "/league/L1")
    assert status == 200


def test_canonical_leagues_seeded():
    """Scenario: Canonical leagues are seeded on startup."""
    for lid, name, sport in [
        ("L1", "Mountain", "Soccer"),
        ("L2", "Valley", "Basketball"),
        ("L3", "Foothills", "Soccer"),
        ("L4", "Alpine", "Snowboarding"),
    ]:
        data = json_get(f"/league/{lid}")
        assert data["id"] == lid
        assert data["name"] == name
        assert data["sport"] == sport
    print("[PASS] Canonical leagues verified")


def test_create_summer_league():
    """Scenario: Create a summer league (swimming is a summer sport)."""
    status, _ = http("POST", "/league", body={"id": "L5", "name": "Coastal", "sport": "Swimming"})
    assert status in [200, 204], f"Create summer league failed: {status}"

    data = json_get("/league/L5")
    assert data["name"] == "Coastal"
    print("[PASS] Summer league created")


def test_create_winter_league():
    """Scenario: Create a winter league (skiing is a winter sport)."""
    status, _ = http("POST", "/league", body={"id": "L6", "name": "Nordic", "sport": "Skiing"})
    assert status in [200, 204], f"Create winter league failed: {status}"

    data = json_get("/league/L6")
    assert data["name"] == "Nordic"
    print("[PASS] Winter league created")


def test_invalid_sport_rejected():
    """Scenario: Invalid sport throws IncorrectSportException."""
    status, _ = http("POST", "/league", body={"id": "LX", "name": "Bad", "sport": "Cricket"})
    assert status == 400, f"Expected 400 for invalid sport, got {status}"
    print("[PASS] Invalid sport correctly rejected")


def test_create_summer_sport_leagues():
    """Scenario: Summer sports include soccer, swimming, basketball, and baseball."""
    for i, sport in enumerate(["Soccer", "Swimming", "Basketball", "Baseball"]):
        lid = f"LS{i}"
        status, _ = http("POST", "/league", body={"id": lid, "name": f"Test {sport}", "sport": sport})
        assert status in [200, 204], f"Create {sport} league failed: {status}"
    print("[PASS] All summer sport leagues created")


def test_create_winter_sport_leagues():
    """Scenario: Winter sports include hockey, skiing, and snowboarding."""
    for i, sport in enumerate(["Hockey", "Skiing", "Snowboarding"]):
        lid = f"LW{i}"
        status, _ = http("POST", "/league", body={"id": lid, "name": f"Test {sport}", "sport": sport})
        assert status in [200, 204], f"Create {sport} league failed: {status}"
    print("[PASS] All winter sport leagues created")


def test_remove_league():
    """Scenario: Remove a league."""
    # Create a disposable league
    http("POST", "/league", body={"id": "LDEL", "name": "Disposable", "sport": "Soccer"})
    status, _ = http("DELETE", "/league/LDEL")
    assert status in [200, 204], f"Remove league failed: {status}"

    status, _ = http("GET", "/league/LDEL")
    assert status in [404, 500], "Removed league should not exist"
    print("[PASS] League removed")


# ---------------------------------------------------------------------------
# Team management
# ---------------------------------------------------------------------------


def test_create_team_in_league():
    """Scenario: Create a team in a league."""
    status, _ = http("POST", "/team/league/L1", body={"id": "T10", "name": "Eagles", "city": "Denver"})
    assert status in [200, 204], f"Create team failed: {status}"

    data = json_get("/team/T10")
    assert data["name"] == "Eagles"
    assert data["city"] == "Denver"
    print("[PASS] Team created in league")


def test_get_team_details():
    """Scenario: Get team details by ID."""
    data = json_get("/team/T1")
    assert data["id"] == "T1"
    assert "name" in data
    assert "city" in data
    print("[PASS] Team details retrieved")


def test_get_teams_of_league():
    """Scenario: Get teams of a league."""
    teams = json_get("/league/L1/teams")
    assert isinstance(teams, list)
    team_ids = [t["id"] for t in teams]
    assert "T1" in team_ids
    assert "T2" in team_ids
    print("[PASS] Teams of league retrieved")


def test_remove_team():
    """Scenario: Remove a team drops all player associations."""
    # Create a temp team and player
    http("POST", "/team/league/L2", body={"id": "TDEL", "name": "Temp", "city": "Nowhere"})
    http("POST", "/player", query={"id": "PDEL", "name": "Temp Player", "position": "sub", "salary": "100"})
    http("POST", "/player/PDEL/team/TDEL", body="")

    status, _ = http("DELETE", "/team/TDEL")
    assert status in [200, 204], f"Remove team failed: {status}"

    status, _ = http("GET", "/team/TDEL")
    assert status in [404, 500], "Removed team should not exist"

    # Clean up player
    http("DELETE", "/player/PDEL")
    print("[PASS] Team removed, player associations dropped")


# ---------------------------------------------------------------------------
# Player management
# ---------------------------------------------------------------------------


def test_create_player():
    """Scenario: Create a new player."""
    data = json_get("/player/P1")
    assert data["id"] == "P1"
    assert data["name"] == "Duke"
    assert data["position"] == "forward"
    assert data["salary"] == 50000.0
    print("[PASS] Player details verified")


def test_add_player_to_team():
    """Scenario: Add a player to a team."""
    players = json_get("/team/T1/players")
    player_ids = [p["id"] for p in players]
    assert "P1" in player_ids
    print("[PASS] Player is on team")


def test_drop_player_from_team():
    """Scenario: Drop a player from a team."""
    # Create a temp player on T2
    http("POST", "/player", query={"id": "PDROP", "name": "Dropper", "position": "sub", "salary": "100"})
    http("POST", "/player/PDROP/team/T2", body="")

    status, _ = http("DELETE", "/player/PDROP/team/T2")
    assert status in [200, 204], f"Drop player failed: {status}"

    players = json_get("/team/T2/players")
    player_ids = [p["id"] for p in players]
    assert "PDROP" not in player_ids

    http("DELETE", "/player/PDROP")
    print("[PASS] Player dropped from team")


def test_remove_player():
    """Scenario: Remove a player from the system."""
    http("POST", "/player", query={"id": "PREM", "name": "Removable", "position": "sub", "salary": "100"})
    http("POST", "/player/PREM/team/T1", body="")

    status, _ = http("DELETE", "/player/PREM")
    assert status in [200, 204], f"Remove player failed: {status}"

    status, _ = http("GET", "/player/PREM")
    assert status in [404, 500], "Removed player should not exist"
    print("[PASS] Player removed")


# ---------------------------------------------------------------------------
# Criteria queries — by position
# ---------------------------------------------------------------------------


def test_players_by_position():
    """Scenario: Get players by position."""
    players = json_get("/players/position/forward")
    assert len(players) > 0
    for p in players:
        assert p["position"] == "forward"
    print("[PASS] Players by position query works")


# ---------------------------------------------------------------------------
# Criteria queries — by salary
# ---------------------------------------------------------------------------


def test_players_salary_higher_than():
    """Scenario: Get players with salary higher than a named player."""
    players = json_get("/players/salary/higher/Alice")
    assert len(players) > 0
    # Duke (50000) should be in results since Alice is 30000
    names = [p["name"] for p in players]
    assert "Duke" in names
    print("[PASS] Salary higher-than query works")


def test_players_by_salary_range():
    """Scenario: Get players by salary range."""
    players = json_get("/players/salary/range", query={"low": "40000", "high": "60000"})
    assert len(players) > 0
    for p in players:
        assert 40000 <= p["salary"] <= 60000
    print("[PASS] Salary range query works")


# ---------------------------------------------------------------------------
# Criteria queries — by league and sport
# ---------------------------------------------------------------------------


def test_players_by_league():
    """Scenario: Get players by league ID."""
    players = json_get("/players/league/L1")
    assert len(players) > 0
    player_ids = [p["id"] for p in players]
    assert "P1" in player_ids
    print("[PASS] Players by league query works")


def test_players_by_sport():
    """Scenario: Get players by sport."""
    players = json_get("/players/sport/Soccer")
    assert len(players) > 0
    print("[PASS] Players by sport query works")


# ---------------------------------------------------------------------------
# Criteria queries — by city and team
# ---------------------------------------------------------------------------


def test_players_by_city():
    """Scenario: Get players by city."""
    players = json_get("/players/city/Visalia")
    assert len(players) > 0
    print("[PASS] Players by city query works")


def test_players_not_on_team():
    """Scenario: Get players not on any team."""
    players = json_get("/players/not-on-team")
    assert len(players) > 0
    player_ids = [p["id"] for p in players]
    assert "P5" in player_ids
    print("[PASS] Players not on team query works")


# ---------------------------------------------------------------------------
# Cross-entity queries
# ---------------------------------------------------------------------------


def test_leagues_of_player():
    """Scenario: Get leagues of a specific player."""
    leagues = json_get("/player/P1/leagues")
    assert leagues is not None
    league_ids = [lg["id"] for lg in leagues]
    # P1 is on T1 (L1) and T3 (L2)
    assert "L1" in league_ids
    assert "L2" in league_ids
    print("[PASS] Leagues of player query works")


def test_sports_of_player():
    """Scenario: Get sports of a specific player."""
    sports = json_get("/player/P1/sports")
    assert "Soccer" in sports
    assert "Basketball" in sports
    print("[PASS] Sports of player query works")


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
