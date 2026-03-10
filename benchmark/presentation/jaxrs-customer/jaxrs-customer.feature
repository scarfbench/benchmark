Feature: Customer REST CRUD Service
  As a client application
  I want to perform CRUD operations on Customer resources via REST
  So that I can manage customers and their addresses using JAX-RS with JPA

  Background:
    Given the customer REST service is running
    And the database is initialized

  # ---------------------------------------------------------------------------
  # Create customer
  # ---------------------------------------------------------------------------

  Scenario: Create a new customer via POST
    When I POST a customer with:
      | firstname | Duke             |
      | lastname  | Java             |
      | email     | duke@example.com |
      | phone     | 555-1234         |
    Then the response status should be 201
    And the Location header should contain the new customer ID

  Scenario: Created customer includes an address
    When I POST a customer with an address containing street "123 Main St" and city "Anytown"
    Then the customer and address should both be persisted

  # ---------------------------------------------------------------------------
  # Retrieve customer
  # ---------------------------------------------------------------------------

  Scenario: Get a specific customer by ID
    Given customer with ID 1 exists
    When I GET /Customer/1
    Then the response status should be 200
    And the response should contain the customer's firstname, lastname, email, and phone

  Scenario: Get a non-existent customer returns null
    When I GET /Customer/99999
    Then the response body should be empty or null

  Scenario: Get all customers
    Given customers exist in the database
    When I GET /Customer/all
    Then the response should contain a list of all customers
    And the list should be ordered by ID

  Scenario: Get all customers when none exist returns empty list
    Given no customers exist
    When I GET /Customer/all
    Then the response should contain an empty list

  # ---------------------------------------------------------------------------
  # Update customer
  # ---------------------------------------------------------------------------

  Scenario: Update an existing customer via PUT
    Given customer with ID 1 exists
    When I PUT an updated customer to /Customer/1 with lastname "Updated"
    Then the response status should be 303

  Scenario: Update a non-existent customer returns 404
    When I PUT a customer to /Customer/99999
    Then the response status should be 404 or 500

  # ---------------------------------------------------------------------------
  # Delete customer
  # ---------------------------------------------------------------------------

  Scenario: Delete an existing customer
    Given customer with ID 1 exists
    When I DELETE /Customer/1
    Then the customer should be removed from the database
    And the customer's address should also be removed

  Scenario: Delete a non-existent customer returns 404
    When I DELETE /Customer/99999
    Then the response status should be 404

  # ---------------------------------------------------------------------------
  # Content negotiation
  # ---------------------------------------------------------------------------

  Scenario: Customer endpoint supports XML format
    When I GET /Customer/1 with Accept header "application/xml"
    Then the response Content-Type should contain "xml"

  Scenario: Customer endpoint supports JSON format
    When I GET /Customer/1 with Accept header "application/json"
    Then the response Content-Type should contain "json"

  Scenario: POST accepts both XML and JSON
    When I POST a customer in JSON format
    Then the customer should be created successfully
    When I POST a customer in XML format
    Then the customer should be created successfully

  # ---------------------------------------------------------------------------
  # Customer entity structure
  # ---------------------------------------------------------------------------

  Scenario: Customer has required fields
    Then a Customer entity should have id, firstname, lastname, address, email, and phone

  Scenario: Customer has a one-to-one relationship with Address
    Given a customer with an address
    Then the customer's getAddress should return the associated Address entity

  Scenario: Customer ID is auto-generated
    When I create a customer without specifying an ID
    Then the ID should be auto-generated
