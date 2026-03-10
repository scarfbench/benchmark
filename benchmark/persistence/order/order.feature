Feature: Order Management System
  As a user
  I want to manage customer orders with line items, parts, and vendors
  So that I can create orders, add line items, search vendors, and calculate totals

  Background:
    Given the order management application is running
    And the database is populated with initial data

  # ---------------------------------------------------------------------------
  # Order creation
  # ---------------------------------------------------------------------------

  Scenario: Create a new customer order
    When I create an order with:
      | orderId       | 1001            |
      | status        | P               |
      | discount      | 10              |
      | shipmentInfo  | Express         |
    Then the order should be persisted successfully
    And the order should have a lastUpdate timestamp

  Scenario: New order starts with an empty line items collection
    When I create a new order
    Then the order should have 0 line items

  # ---------------------------------------------------------------------------
  # Order listing
  # ---------------------------------------------------------------------------

  Scenario: List all customer orders
    Given orders exist in the database
    When I view the orders list
    Then all orders should be displayed ordered by orderId

  Scenario: View line items for a specific order
    Given order 1001 has line items
    When I view the line items for order 1001
    Then all line items for that order should be displayed

  # ---------------------------------------------------------------------------
  # Order deletion
  # ---------------------------------------------------------------------------

  Scenario: Remove a customer order
    Given order 1001 exists
    When I remove order 1001
    Then order 1001 should no longer exist in the database

  # ---------------------------------------------------------------------------
  # Line items
  # ---------------------------------------------------------------------------

  Scenario: Add a line item to an existing order
    Given order 1001 exists with available parts
    When I add a line item with part number "P001" revision 1 and quantity 1
    Then the line item should be added to order 1001
    And the order line item count should increase by 1

  Scenario: Line item next ID is based on collection size
    Given an order has 3 line items
    Then getNextId should return 4

  # ---------------------------------------------------------------------------
  # Order amount calculation
  # ---------------------------------------------------------------------------

  Scenario: Calculate order amount with no discount
    Given an order with discount 0 and line items totaling $100.00
    When I calculate the order amount
    Then the amount should be 100.00

  Scenario: Calculate order amount with 10% discount
    Given an order with discount 10 and line items totaling $100.00
    When I calculate the order amount
    Then the amount should be 90.00

  Scenario: Calculate order amount with multiple line items
    Given an order with discount 5 and line items:
      | quantity | price |
      | 2        | 25.00 |
      | 3        | 10.00 |
    When I calculate the order amount
    Then the amount should be 76.00

  # ---------------------------------------------------------------------------
  # Vendor search
  # ---------------------------------------------------------------------------

  Scenario: Find vendors by partial name
    Given vendors "Acme Corp" and "Acme Industries" exist
    When I search for vendors with name "Acme"
    Then the results should include both "Acme Corp" and "Acme Industries"

  Scenario: Vendor search with no matches returns empty results
    When I search for vendors with name "NonExistent"
    Then the results should be empty

  # ---------------------------------------------------------------------------
  # Entity relationships
  # ---------------------------------------------------------------------------

  Scenario: CustomerOrder has one-to-many relationship with LineItems
    Given an order with line items
    Then the order's getLineItems should return the associated line items

  Scenario: LineItem references a VendorPart
    Given a line item exists
    Then the line item should reference a VendorPart with a price

  Scenario: Part has a composite key of partNumber and revision
    Given a part with number "P001" and revision 1
    Then the part key should combine partNumber and revision

  # ---------------------------------------------------------------------------
  # Parts listing
  # ---------------------------------------------------------------------------

  Scenario: List all available parts
    When I view all parts
    Then all parts in the database should be displayed
