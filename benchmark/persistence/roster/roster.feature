Feature: Sports Roster Management
  As a sports league administrator
  I want to manage leagues, teams, and players
  So that I can organize sports rosters and query them with criteria queries

  Background:
    Given the roster application is running
    And the database is seeded with canonical leagues

  # ---------------------------------------------------------------------------
  # League management
  # ---------------------------------------------------------------------------

  Scenario: Canonical leagues are seeded on startup
    When I retrieve all leagues
    Then I should see leagues including:
      | id | name      | sport        |
      | L1 | Mountain  | Soccer       |
      | L2 | Valley    | Basketball   |
      | L3 | Foothills | Soccer       |
      | L4 | Alpine    | Snowboarding |

  Scenario: Create a summer league
    When I create a league with id "L5" name "Coastal" and sport "Swimming"
    Then the league should be persisted as a SummerLeague

  Scenario: Create a winter league
    When I create a league with id "L6" name "Nordic" and sport "Skiing"
    Then the league should be persisted as a WinterLeague

  Scenario: Invalid sport throws IncorrectSportException
    When I try to create a league with sport "Cricket"
    Then an IncorrectSportException should be thrown

  Scenario: Summer sports include soccer, swimming, basketball, and baseball
    When I create leagues with sports "Soccer", "Swimming", "Basketball", "Baseball"
    Then all should be persisted as SummerLeague instances

  Scenario: Winter sports include hockey, skiing, and snowboarding
    When I create leagues with sports "Hockey", "Skiing", "Snowboarding"
    Then all should be persisted as WinterLeague instances

  Scenario: Remove a league
    Given league "L5" exists
    When I remove league "L5"
    Then league "L5" should no longer exist

  # ---------------------------------------------------------------------------
  # Team management
  # ---------------------------------------------------------------------------

  Scenario: Create a team in a league
    When I create team "T1" named "Eagles" in city "Denver" in league "L1"
    Then the team should be persisted
    And the team should be associated with league "L1"

  Scenario: Get team details by ID
    Given team "T1" exists
    When I retrieve team "T1"
    Then I should see the team name and city

  Scenario: Get teams of a league
    Given league "L1" has teams "T1" and "T2"
    When I get teams of league "L1"
    Then I should see both teams

  Scenario: Remove a team drops all player associations
    Given team "T1" has players
    When I remove team "T1"
    Then the team should be deleted
    And the players should no longer reference that team

  # ---------------------------------------------------------------------------
  # Player management
  # ---------------------------------------------------------------------------

  Scenario: Create a new player
    When I create player "P1" named "Duke" at position "forward" with salary 50000.0
    Then the player should be persisted with the correct details

  Scenario: Add a player to a team
    Given player "P1" and team "T1" exist
    When I add player "P1" to team "T1"
    Then the player should be on team "T1"
    And the team should contain player "P1"

  Scenario: Drop a player from a team
    Given player "P1" is on team "T1"
    When I drop player "P1" from team "T1"
    Then the player should no longer be on team "T1"

  Scenario: Remove a player from the system
    Given player "P1" exists on multiple teams
    When I remove player "P1"
    Then the player should be deleted
    And all team associations should be removed

  # ---------------------------------------------------------------------------
  # Criteria queries - by position
  # ---------------------------------------------------------------------------

  Scenario: Get players by position
    Given players at position "forward" exist
    When I query players by position "forward"
    Then only players with position "forward" should be returned

  # ---------------------------------------------------------------------------
  # Criteria queries - by salary
  # ---------------------------------------------------------------------------

  Scenario: Get players with salary higher than a named player
    Given player "Duke" has salary 50000 and player "Alice" has salary 30000
    When I query players with salary higher than "Alice"
    Then "Duke" should be in the results

  Scenario: Get players by salary range
    Given players with various salaries exist
    When I query players with salary between 40000 and 60000
    Then only players within that range should be returned

  # ---------------------------------------------------------------------------
  # Criteria queries - by league and sport
  # ---------------------------------------------------------------------------

  Scenario: Get players by league ID
    Given players are on teams in league "L1"
    When I query players by league "L1"
    Then only players in league "L1" should be returned

  Scenario: Get players by sport
    When I query players by sport "Soccer"
    Then only players in Soccer leagues should be returned

  # ---------------------------------------------------------------------------
  # Criteria queries - by city and team
  # ---------------------------------------------------------------------------

  Scenario: Get players by city
    When I query players by city "Denver"
    Then only players on teams in Denver should be returned

  Scenario: Get players not on any team
    Given some players are not assigned to any team
    When I query players not on a team
    Then only unassigned players should be returned

  # ---------------------------------------------------------------------------
  # Cross-entity queries
  # ---------------------------------------------------------------------------

  Scenario: Get leagues of a specific player
    Given player "P1" is on teams in leagues "L1" and "L2"
    When I query leagues of player "P1"
    Then I should see leagues "L1" and "L2"

  Scenario: Get sports of a specific player
    Given player "P1" plays in Soccer and Basketball leagues
    When I query sports of player "P1"
    Then I should see "Soccer" and "Basketball"
