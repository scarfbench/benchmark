Feature: ToDo List with Producer Field for EntityManager
  As a user
  I want to manage a to-do list backed by JPA
  So that my tasks are persisted using an EntityManager provided via a CDI producer field

  Background:
    Given the producer fields application is running
    And the database is initialized

  # ---------------------------------------------------------------------------
  # Creating to-do items
  # ---------------------------------------------------------------------------

  Scenario: Create a new to-do item
    When I create a to-do item with text "Buy groceries"
    Then the to-do item should be persisted in the database
    And the to-do item should have a generated ID
    And the to-do item should have a creation timestamp

  Scenario: Create multiple to-do items
    When I create to-do items with text:
      | taskText          |
      | Buy groceries     |
      | Walk the dog      |
      | Read a book       |
    Then all three to-do items should be persisted

  Scenario: To-do item stores the correct task text
    When I create a to-do item with text "Clean the house"
    Then the to-do item taskText should be "Clean the house"

  # ---------------------------------------------------------------------------
  # Listing to-do items
  # ---------------------------------------------------------------------------

  Scenario: List all to-do items ordered by creation time
    Given the following to-do items exist:
      | taskText        |
      | First task      |
      | Second task     |
      | Third task      |
    When I retrieve all to-do items
    Then the items should be ordered by timeCreated ascending

  Scenario: List is empty when no items exist
    When I retrieve all to-do items
    Then the result should be an empty list

  # ---------------------------------------------------------------------------
  # Producer field for EntityManager
  # ---------------------------------------------------------------------------

  Scenario: EntityManager is injected via @UserDatabase producer field
    Then the RequestBean should have a non-null EntityManager
    And the EntityManager should be qualified with @UserDatabase

  Scenario: EntityManager supports persist operations
    When I create a to-do item with text "Test persistence"
    Then the item should be retrievable from the database

  # ---------------------------------------------------------------------------
  # ToDo entity structure
  # ---------------------------------------------------------------------------

  Scenario: ToDo entity has auto-generated ID
    When I create a to-do item
    Then the to-do ID should be auto-generated and non-null

  Scenario: ToDo entity has a creation timestamp
    When I create a to-do item with text "Timestamp test"
    Then the timeCreated field should reflect the current time

  Scenario: ToDo entity equality is based on ID
    Given two to-do items with the same ID
    Then they should be considered equal

  Scenario: ToDo entity toString includes the ID
    Given a to-do item with ID 1
    Then its toString should be "entity.ToDo[id=1]"
