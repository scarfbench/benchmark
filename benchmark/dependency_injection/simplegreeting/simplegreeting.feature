Feature: Simple Greeting with CDI Qualifier
  As a user
  I want to receive a personalized greeting
  So that the @Informal qualifier selects between formal and informal greeting styles

  Background:
    Given the simple greeting application is running

  # ---------------------------------------------------------------------------
  # Greeting form
  # ---------------------------------------------------------------------------

  Scenario: Greeting form displays a name input field
    When I view the greeting page
    Then I should see a name input field
    And I should see a Submit button

  # ---------------------------------------------------------------------------
  # Informal greeting (default via @Informal qualifier)
  # ---------------------------------------------------------------------------

  Scenario: Greeting with name "Duke" returns informal greeting
    When I enter the name "Duke"
    And I submit the greeting form
    Then the salutation should be "Hi, Duke!"

  Scenario: Greeting with name "Alice" returns informal greeting
    When I enter the name "Alice"
    And I submit the greeting form
    Then the salutation should be "Hi, Alice!"

  Scenario: Greeting with name "World" returns informal greeting
    When I enter the name "World"
    And I submit the greeting form
    Then the salutation should be "Hi, World!"

  Scenario: Greeting with a multi-word name
    When I enter the name "Mary Jane"
    And I submit the greeting form
    Then the salutation should be "Hi, Mary Jane!"

  Scenario Outline: Various names produce correct informal greetings
    When I enter the name "<name>"
    And I submit the greeting form
    Then the salutation should be "Hi, <name>!"

    Examples:
      | name    |
      | Duke    |
      | Alice   |
      | Bob     |
      | Charlie |

  # ---------------------------------------------------------------------------
  # CDI qualifier selection
  # ---------------------------------------------------------------------------

  Scenario: Printer bean injects the @Informal Greeting implementation
    Then the Printer bean should use InformalGreeting
    And InformalGreeting should produce greetings in the format "Hi, NAME!"

  Scenario: Default Greeting without @Informal produces formal greeting
    Given the Greeting bean without @Informal qualifier
    When greet is called with "Duke"
    Then the result should be "Hello, Duke."

  Scenario: InformalGreeting overrides Greeting with exclamation style
    Given the InformalGreeting bean
    When greet is called with "Duke"
    Then the result should be "Hi, Duke!"

  # ---------------------------------------------------------------------------
  # Greeting format differences
  # ---------------------------------------------------------------------------

  Scenario: Formal greeting ends with a period
    Given the default Greeting implementation
    When greet is called with "Test"
    Then the result should end with "."

  Scenario: Informal greeting ends with an exclamation mark
    Given the InformalGreeting implementation
    When greet is called with "Test"
    Then the result should end with "!"
