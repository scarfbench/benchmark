Feature: EJB Interceptor for Greeting Name Lowercasing
  As a user
  I want to enter a name for a greeting
  So that the interceptor automatically lowercases the name before it is stored

  Background:
    Given the EJB interceptor application is running

  # ---------------------------------------------------------------------------
  # Greeting form
  # ---------------------------------------------------------------------------

  Scenario: Greeting form displays a name input field
    When I view the greeting page
    Then I should see a name input field
    And I should see a Submit button

  # ---------------------------------------------------------------------------
  # Core interceptor behavior
  # ---------------------------------------------------------------------------

  Scenario: Name is lowercased by the interceptor
    When I enter name "DUKE"
    And I submit the greeting form
    Then the stored name should be "duke"

  Scenario: Mixed-case name is lowercased
    When I enter name "Alice"
    And I submit the greeting form
    Then the stored name should be "alice"

  Scenario: Already lowercase name remains unchanged
    When I enter name "bob"
    And I submit the greeting form
    Then the stored name should be "bob"

  Scenario: Name with spaces is lowercased
    When I enter name "Mary Jane"
    And I submit the greeting form
    Then the stored name should be "mary jane"

  Scenario Outline: Various names are lowercased correctly
    When I enter name "<input>"
    And I submit the greeting form
    Then the stored name should be "<expected>"

    Examples:
      | input       | expected    |
      | DUKE        | duke        |
      | Alice       | alice       |
      | Bob         | bob         |
      | HELLO WORLD | hello world |
      | JoHn        | john        |

  # ---------------------------------------------------------------------------
  # Interceptor mechanics
  # ---------------------------------------------------------------------------

  Scenario: HelloInterceptor intercepts the setName method
    When setName is called with "Test"
    Then the HelloInterceptor modifyGreeting method should be invoked
    And the parameter should be converted to lowercase before proceeding

  Scenario: Interceptor modifies the parameters array
    When the interceptor receives parameter "UPPER"
    Then ctx.getParameters()[0] should be "upper"
    And ctx.proceed() should be called with the modified parameters

  # ---------------------------------------------------------------------------
  # Bean state
  # ---------------------------------------------------------------------------

  Scenario: getName returns the lowercased value after setName
    When I set the name to "DUKE"
    Then getName should return "duke"

  Scenario: HelloBean is stateless
    When I set the name on one request
    Then the bean state should not persist across requests in a different session
