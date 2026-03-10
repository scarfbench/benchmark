Feature: Shopping Cart Management
  As a customer
  I want to manage a shopping cart
  So that I can add, view, and remove books from my cart

  Background:
    Given the cart service is running and healthy

  # ---------------------------------------------------------------------------
  # Health
  # ---------------------------------------------------------------------------

  Scenario: Health endpoint reports the service is UP
    When I request GET /health
    Then the response status should be 200
    And the JSON body should contain status "UP"

  # ---------------------------------------------------------------------------
  # Cart initialization
  # ---------------------------------------------------------------------------

  Scenario: Initialize a new cart session with valid customer data
    Given no cart exists for the current session
    When I POST to /initialize with customerName "Duke DeUrl" and customerId "123"
    Then the response status should be 200
    And the JSON response body should contain a "message" field

  Scenario: Initialize a cart with a numeric customer ID
    When I POST to /initialize with customerName "Alice" and customerId "456"
    Then the response status should be 200
    And the JSON response body should contain a "message" field

  # ---------------------------------------------------------------------------
  # Adding books
  # ---------------------------------------------------------------------------

  Scenario: Add a single book to an initialized cart
    Given a cart is initialized for "Duke DeUrl" with ID "123"
    When I POST to /books/Infinite%20Jest
    Then the response status should be 200
    And the JSON response should contain title "Infinite Jest"

  Scenario: Add multiple books to the cart
    Given a cart is initialized for "Duke DeUrl" with ID "123"
    When I add the following books to the cart:
      | title              |
      | Infinite Jest      |
      | Bel Canto          |
      | Kafka on the Shore |
    Then each add response should return status 200
    And each response should echo back the correct book title

  Scenario: Add a book whose title contains spaces
    Given a cart is initialized
    When I add "Kafka on the Shore" to the cart
    Then the response status should be 200
    And the JSON response should contain title "Kafka on the Shore"

  # ---------------------------------------------------------------------------
  # Viewing books
  # ---------------------------------------------------------------------------

  Scenario: Retrieve books from an empty cart
    Given a cart is initialized and contains no books
    When I GET /books
    Then the response status should be 200
    And the JSON body should contain a "books" list
    And the JSON body should report count 0

  Scenario: Retrieve books after adding items
    Given a cart is initialized with 3 books
    When I GET /books
    Then the response status should be 200
    And the JSON body should contain a "books" list
    And the JSON body should report count 3

  Scenario: Books response always contains both "books" and "count" fields
    Given a cart is initialized with at least one book
    When I GET /books
    Then the JSON body should contain a "books" field
    And the JSON body should contain a "count" field

  # ---------------------------------------------------------------------------
  # Removing books
  # ---------------------------------------------------------------------------

  Scenario: Remove an existing book from the cart
    Given a cart contains "Bel Canto"
    When I DELETE /books/Bel%20Canto
    Then the response status should be 200
    And the JSON response should contain title "Bel Canto"

  Scenario: Cart count decreases by one after a remove
    Given a cart contains 3 books including "Bel Canto"
    When I DELETE /books/Bel%20Canto
    Then a subsequent GET /books should report count 2

  Scenario: Remove a book that does not exist returns 404
    Given a cart is initialized
    When I DELETE /books/Gravity%27s%20Rainbow which is not in the cart
    Then the response status should be 404
    And the JSON response should contain an "error" field

  # ---------------------------------------------------------------------------
  # Clearing the cart
  # ---------------------------------------------------------------------------

  Scenario: Clear the entire cart
    Given a cart contains multiple books
    When I DELETE the cart root
    Then the response status should be 200
    And the JSON response should contain a "message" field

  Scenario: Cart is empty after being cleared
    Given a cart has been cleared
    When I GET /books
    Then the response status should be 200
    And the JSON body should report count 0

  # ---------------------------------------------------------------------------
  # Session persistence
  # ---------------------------------------------------------------------------

  Scenario: Books added in one request persist for subsequent requests
    Given a cart is initialized
    When I add "Infinite Jest" to the cart
    And I add "Bel Canto" to the cart
    Then a GET /books request should return both titles

  Scenario: Re-initializing and adding a new book works correctly
    Given a cart was previously cleared
    When I POST to /initialize for a new customer
    And I add "The Great Gatsby" to the cart
    Then a GET /books should report count 1
