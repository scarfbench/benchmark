Feature: Address Book Contact Management
  As a user
  I want to manage contacts in an address book
  So that I can create, view, edit, and delete contacts with validated fields

  Background:
    Given the address book application is running
    And the database is initialized

  # ---------------------------------------------------------------------------
  # Contact creation
  # ---------------------------------------------------------------------------

  Scenario: Create a new contact with all valid fields
    When I create a contact with:
      | firstName   | Duke                  |
      | lastName    | Java                  |
      | email       | duke@example.com      |
      | mobilePhone | (555) 123-4567        |
      | homePhone   | (555) 987-6543        |
      | birthday    | 1995-05-23            |
    Then the contact should be persisted successfully
    And the contact should have a generated ID

  Scenario: Create a contact with only required fields
    When I create a contact with firstName "Alice" and lastName "Smith"
    Then the contact should be persisted successfully

  # ---------------------------------------------------------------------------
  # Contact retrieval
  # ---------------------------------------------------------------------------

  Scenario: View all contacts
    Given contacts exist in the address book
    When I view the contacts list
    Then all contacts should be displayed

  Scenario: View a specific contact by ID
    Given a contact with firstName "Duke" exists
    When I view the contact details
    Then all fields should be displayed correctly

  # ---------------------------------------------------------------------------
  # Email validation
  # ---------------------------------------------------------------------------

  Scenario: Valid email address is accepted
    When I enter email "duke@example.com"
    Then the email should pass validation

  Scenario: Email without @ symbol is rejected
    When I enter email "dukeatexample.com"
    Then a validation error for invalid email should be displayed

  Scenario: Email without domain is rejected
    When I enter email "duke@"
    Then a validation error for invalid email should be displayed

  Scenario: Email with uppercase letters is rejected
    When I enter email "Duke@Example.COM"
    Then a validation error for invalid email should be displayed

  # ---------------------------------------------------------------------------
  # Phone number validation
  # ---------------------------------------------------------------------------

  Scenario: Valid phone number with parentheses is accepted
    When I enter mobile phone "(555) 123-4567"
    Then the phone number should pass validation

  Scenario: Valid phone number with dashes is accepted
    When I enter mobile phone "555-123-4567"
    Then the phone number should pass validation

  Scenario: Phone number with letters is rejected
    When I enter mobile phone "555-ABC-1234"
    Then a validation error for invalid phone number should be displayed

  Scenario: Phone number with wrong digit count is rejected
    When I enter mobile phone "12345"
    Then a validation error for invalid phone number should be displayed

  # ---------------------------------------------------------------------------
  # Birthday validation
  # ---------------------------------------------------------------------------

  Scenario: Past birthday date is accepted
    When I enter birthday "1990-01-15"
    Then the birthday should pass validation

  Scenario: Future birthday date is rejected
    When I enter birthday "2099-01-15"
    Then a validation error should be displayed because birthday must be in the past

  # ---------------------------------------------------------------------------
  # Required fields
  # ---------------------------------------------------------------------------

  Scenario: Contact without firstName is rejected
    When I try to create a contact without a firstName
    Then a validation error for required firstName should be displayed

  Scenario: Contact without lastName is rejected
    When I try to create a contact without a lastName
    Then a validation error for required lastName should be displayed

  # ---------------------------------------------------------------------------
  # Contact editing
  # ---------------------------------------------------------------------------

  Scenario: Edit an existing contact's email
    Given a contact "Duke Java" exists
    When I update the email to "newemail@example.com"
    Then the contact should be updated with the new email

  # ---------------------------------------------------------------------------
  # Contact deletion
  # ---------------------------------------------------------------------------

  Scenario: Delete a contact
    Given a contact "Duke Java" exists
    When I delete the contact
    Then the contact should no longer appear in the contacts list

  # ---------------------------------------------------------------------------
  # Entity structure
  # ---------------------------------------------------------------------------

  Scenario: Contact entity has auto-generated ID
    When I create a contact
    Then the ID should be auto-generated and non-null

  Scenario: Contact equality is based on ID
    Given two contacts with the same ID
    Then they should be considered equal
