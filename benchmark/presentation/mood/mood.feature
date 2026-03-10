Feature: Mood Servlet with TimeOfDayFilter
  As a user
  I want to see Duke's mood based on the time of day
  So that the TimeOfDayFilter sets the mood attribute before the servlet renders it

  Background:
    Given the mood application is running

  # ---------------------------------------------------------------------------
  # Mood display
  # ---------------------------------------------------------------------------

  Scenario: Mood page displays Duke's current mood
    When I GET /report
    Then the response status should be 200
    And the response should contain "Duke's mood is:"
    And the response should display a mood value

  Scenario: Mood page contains an image for the mood
    When I GET /report
    Then the response should contain an img tag with a mood-related image

  Scenario: Response is an HTML page
    When I GET /report
    Then the response Content-Type should be "text/html;charset=UTF-8"
    And the response should contain "<html"
    And the response should contain "</html>"

  # ---------------------------------------------------------------------------
  # TimeOfDayFilter mood mapping
  # ---------------------------------------------------------------------------

  Scenario: Late night hours (23, 0-6) set mood to "sleepy"
    Given the current hour is 3
    When the TimeOfDayFilter processes a request
    Then the mood attribute should be "sleepy"

  Scenario: Meal hours (7, 13, 18) set mood to "hungry"
    Given the current hour is 7
    When the TimeOfDayFilter processes a request
    Then the mood attribute should be "hungry"

  Scenario: Active hours (8-10, 12, 14, 16-17) set mood to "alert"
    Given the current hour is 9
    When the TimeOfDayFilter processes a request
    Then the mood attribute should be "alert"

  Scenario: Mid-morning and mid-afternoon (11, 15) set mood to "in need of coffee"
    Given the current hour is 11
    When the TimeOfDayFilter processes a request
    Then the mood attribute should be "in need of coffee"

  Scenario: Evening hours (19-21) set mood to "thoughtful"
    Given the current hour is 20
    When the TimeOfDayFilter processes a request
    Then the mood attribute should be "thoughtful"

  Scenario: Late evening (22) sets mood to "lethargic"
    Given the current hour is 22
    When the TimeOfDayFilter processes a request
    Then the mood attribute should be "lethargic"

  # ---------------------------------------------------------------------------
  # Mood images
  # ---------------------------------------------------------------------------

  Scenario: Sleepy mood shows snooze image
    Given the mood is "sleepy"
    When I GET /report
    Then the response should contain "duke.snooze.gif"

  Scenario: Alert mood shows waving image
    Given the mood is "alert"
    When I GET /report
    Then the response should contain "duke.waving.gif"

  Scenario: Hungry mood shows cookies image
    Given the mood is "hungry"
    When I GET /report
    Then the response should contain "duke.cookies.gif"

  Scenario: Lethargic mood shows hands-on-hips image
    Given the mood is "lethargic"
    When I GET /report
    Then the response should contain "duke.handsOnHips.gif"

  Scenario: Thoughtful mood shows pensive image
    Given the mood is "thoughtful"
    When I GET /report
    Then the response should contain "duke.pensive.gif"

  Scenario: Default mood shows thumbs-up image
    Given no specific mood is set
    When I GET /report
    Then the response should contain "duke.thumbsup.gif"

  # ---------------------------------------------------------------------------
  # Filter configuration
  # ---------------------------------------------------------------------------

  Scenario: Filter is configured with init parameter mood="awake"
    Then the TimeOfDayFilter should have init parameter "mood" set to "awake"

  Scenario: Filter applies to all URL patterns
    Then the TimeOfDayFilter should be mapped to "/*"

  # ---------------------------------------------------------------------------
  # Response structure
  # ---------------------------------------------------------------------------

  Scenario: Page title is "Servlet MoodServlet"
    When I GET /report
    Then the response should contain "<title>Servlet MoodServlet</title>"

  Scenario: Page shows the context path
    When I GET /report
    Then the response should contain "Servlet MoodServlet at"
