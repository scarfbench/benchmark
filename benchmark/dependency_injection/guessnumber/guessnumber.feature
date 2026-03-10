Feature: Number Guessing Game
  As a player
  I want to guess a randomly generated number between 0 and 100
  So that I can play a fun number guessing game with narrowing range hints

  Background:
    Given the guess number application is running
    And a new game session has been initialized

  # ---------------------------------------------------------------------------
  # Game initialization
  # ---------------------------------------------------------------------------

  Scenario: New game starts with 10 remaining guesses
    When I view the game page
    Then the remaining guesses should be 10

  Scenario: New game starts with range 0 to 100
    When I view the game page
    Then the minimum should be 0
    And the maximum should be 100

  Scenario: Secret number is between 0 and 100 inclusive
    When a new game is created
    Then the secret number should be between 0 and 100

  # ---------------------------------------------------------------------------
  # Guessing behavior
  # ---------------------------------------------------------------------------

  Scenario: Guessing too high narrows the maximum
    Given the secret number is 42
    When I guess 60
    Then the maximum should be updated to 59
    And the remaining guesses should decrease by 1

  Scenario: Guessing too low narrows the minimum
    Given the secret number is 42
    When I guess 20
    Then the minimum should be updated to 21
    And the remaining guesses should decrease by 1

  Scenario: Guessing the correct number displays "Correct!"
    Given the secret number is 42
    When I guess 42
    Then a "Correct!" message should be displayed

  Scenario: Each guess decrements remaining guesses by one
    When I make 3 guesses
    Then the remaining guesses should be 7

  # ---------------------------------------------------------------------------
  # Range narrowing
  # ---------------------------------------------------------------------------

  Scenario: Multiple guesses progressively narrow the range
    Given the secret number is 50
    When I guess 75
    And I guess 25
    Then the minimum should be 26
    And the maximum should be 74

  Scenario: Range converges on the correct answer
    Given the secret number is 50
    When I guess 60
    And I guess 40
    And I guess 55
    And I guess 45
    Then the minimum should be 46
    And the maximum should be 54

  # ---------------------------------------------------------------------------
  # Validation
  # ---------------------------------------------------------------------------

  Scenario: Guess outside the current range is rejected
    Given the minimum is 20 and the maximum is 80
    When I guess 90
    Then an "Invalid guess" validation error should be displayed

  Scenario: Guess below the current minimum is rejected
    Given the minimum is 30 and the maximum is 70
    When I guess 10
    Then an "Invalid guess" validation error should be displayed

  # ---------------------------------------------------------------------------
  # Producer methods
  # ---------------------------------------------------------------------------

  Scenario: MaxNumber producer method provides value 100
    Then the injected maxNumber should be 100

  Scenario: Random producer method provides a value between 0 and 100
    Then the injected random number should be between 0 and 100

  # ---------------------------------------------------------------------------
  # Game reset
  # ---------------------------------------------------------------------------

  Scenario: Resetting the game restores initial state
    Given I have made several guesses
    When I reset the game
    Then the minimum should be 0
    And the maximum should be 100
    And the remaining guesses should be 10
    And a new secret number should be generated
