Feature: Coffee Shop Microservices Application
  As a coffee shop customer
  I want to place orders for drinks and food items
  So that my order is routed to the correct processor and fulfilled asynchronously

  Background:
    Given the orders-service is running on port 8081
    And the barista-service is running on port 8082
    And the kitchen-service is running on port 8083
    And Kafka is running and topics are configured
    And the PostgreSQL database is initialized

  # ---------------------------------------------------------------------------
  # Health checks
  # ---------------------------------------------------------------------------

  Scenario: Orders service health endpoint reports UP
    When I GET http://localhost:8081/health
    Then the response status should be 200
    And the JSON body should contain status "UP"
    And the check name should be "orders-service"

  Scenario: Barista service health endpoint reports UP
    When I GET http://localhost:8082/health
    Then the response status should be 200
    And the JSON body should contain status "UP"
    And the check name should be "barista-service"

  Scenario: Kitchen service health endpoint reports UP
    When I GET http://localhost:8083/health
    Then the response status should be 200
    And the JSON body should contain status "UP"
    And the check name should be "kitchen-service"

  # ---------------------------------------------------------------------------
  # Service status endpoints
  # ---------------------------------------------------------------------------

  Scenario: Barista status endpoint returns ok
    When I GET http://localhost:8082/barista-service/api/status
    Then the response status should be 200
    And the response body should be "barista ok"

  Scenario: Kitchen status endpoint returns ok
    When I GET http://localhost:8083/kitchen-service/api/status
    Then the response status should be 200
    And the response body should be "kitchen ok"

  # ---------------------------------------------------------------------------
  # Order creation
  # ---------------------------------------------------------------------------

  Scenario: Place a valid drink order
    When I POST to /orders-service/api/orders with:
      | customer | Duke  |
      | item     | latte |
      | quantity | 1     |
    Then the response status should be 202
    And the response should contain an "id" field

  Scenario: Place a valid food order
    When I POST to /orders-service/api/orders with:
      | customer | Alice    |
      | item     | sandwich |
      | quantity | 2        |
    Then the response status should be 202
    And the response should contain an "id" field

  Scenario: Order is persisted with status PLACED
    When I place an order for "latte"
    And I GET the order by its ID
    Then the order status should be "PLACED"

  Scenario: Order has created and updated timestamps
    When I place an order
    And I GET the order by its ID
    Then the order should have non-null "created" and "updated" fields

  # ---------------------------------------------------------------------------
  # Input validation
  # ---------------------------------------------------------------------------

  Scenario: Order with blank customer name is rejected
    When I POST an order with customer "" item "latte" quantity 1
    Then the response status should be 400
    And the errors should indicate customer is required

  Scenario: Order with blank item is rejected
    When I POST an order with customer "Duke" item "" quantity 1
    Then the response status should be 400
    And the errors should indicate item is required

  Scenario: Order with quantity less than 1 is rejected
    When I POST an order with customer "Duke" item "latte" quantity 0
    Then the response status should be 400
    And the errors should indicate quantity must be at least 1

  Scenario: Validation error response has correct structure
    When I POST an invalid order
    Then the response should contain an "errors" array
    And each error should have "field" and "message" properties

  # ---------------------------------------------------------------------------
  # Drink routing (to barista)
  # ---------------------------------------------------------------------------

  Scenario Outline: Drink items are routed to the barista service
    When I place an order for "<item>"
    Then the order command should be sent to the "barista-commands" Kafka topic

    Examples:
      | item        |
      | coffee      |
      | latte       |
      | espresso    |
      | cappuccino  |
      | americano   |
      | mocha       |
      | tea         |

  Scenario: Item containing "coffee" (case insensitive) is treated as a drink
    When I place an order for "Iced Coffee Blend"
    Then the order should be routed to the barista service

  # ---------------------------------------------------------------------------
  # Food routing (to kitchen)
  # ---------------------------------------------------------------------------

  Scenario: Non-drink items are routed to the kitchen service
    When I place an order for "sandwich"
    Then the order command should be sent to the "kitchen-commands" Kafka topic

  Scenario: Items not matching any drink keyword go to kitchen
    When I place an order for "croissant"
    Then the order should be routed to the kitchen service

  # ---------------------------------------------------------------------------
  # Kafka message flow
  # ---------------------------------------------------------------------------

  Scenario: Order command message contains target, orderId, item, and quantity
    When I place an order for "latte" quantity 2
    Then the message on barista-commands should contain:
      | field    | value   |
      | target   | BARISTA |
      | item     | latte   |
      | quantity | 2       |

  Scenario: Kitchen command message contains KITCHEN target
    When I place an order for "muffin" quantity 1
    Then the message on kitchen-commands should contain target "KITCHEN"

  # ---------------------------------------------------------------------------
  # Barista processing
  # ---------------------------------------------------------------------------

  Scenario: Barista processes drink order and publishes READY status
    Given a drink order is placed
    When the barista-service consumes the command
    Then it should publish an order update to "order-updates" topic
    And the update should contain status "READY" and from "barista"

  # ---------------------------------------------------------------------------
  # Kitchen processing
  # ---------------------------------------------------------------------------

  Scenario: Kitchen processes food order and publishes READY status
    Given a food order is placed
    When the kitchen-service consumes the command
    Then it should publish an order update to "order-updates" topic
    And the update should contain status "READY" and from "kitchen"

  # ---------------------------------------------------------------------------
  # Order status update (async)
  # ---------------------------------------------------------------------------

  Scenario: Order status is updated to READY after barista processing
    Given I place a drink order for "latte"
    When the barista processes and publishes a READY update
    Then polling GET /orders-service/api/orders/{id} should eventually return status "READY"

  Scenario: Order status is updated to READY after kitchen processing
    Given I place a food order for "sandwich"
    When the kitchen processes and publishes a READY update
    Then polling GET /orders-service/api/orders/{id} should eventually return status "READY"

  Scenario: Malformed order update messages are ignored
    When a malformed message is published to "order-updates"
    Then the orders-service should log an error but continue processing

  # ---------------------------------------------------------------------------
  # Order lifecycle states
  # ---------------------------------------------------------------------------

  Scenario: OrderStatus enum has five values
    Then the valid order statuses should be:
      | PLACED      |
      | IN_PROGRESS |
      | READY       |
      | DELIVERED   |
      | CANCELLED   |

  Scenario: New orders start in PLACED status
    When I place any order
    Then the initial status should be "PLACED"

  # ---------------------------------------------------------------------------
  # Retrieve order
  # ---------------------------------------------------------------------------

  Scenario: Get an order by ID returns full order details
    Given an order exists with ID 1
    When I GET /orders-service/api/orders/1
    Then the response status should be 200
    And the response should contain id, customer, item, quantity, status, created, updated

  # ---------------------------------------------------------------------------
  # End-to-end drink order flow
  # ---------------------------------------------------------------------------

  Scenario: Full drink order lifecycle from placement to READY
    When I POST an order with customer "Raju" item "latte" quantity 1
    Then the response status should be 202
    And the order ID should be returned
    When I poll GET /orders-service/api/orders/{id} for up to 40 seconds
    Then the status should eventually be "READY"

  # ---------------------------------------------------------------------------
  # End-to-end food order flow
  # ---------------------------------------------------------------------------

  Scenario: Full food order lifecycle from placement to READY
    When I POST an order with customer "Raju" item "sandwich" quantity 1
    Then the response status should be 202
    When I poll GET /orders-service/api/orders/{id} for up to 40 seconds
    Then the status should eventually be "READY"

  # ---------------------------------------------------------------------------
  # Web UI
  # ---------------------------------------------------------------------------

  Scenario: Coffee shop homepage loads
    When I navigate to the coffee shop homepage
    Then the page title should contain "coffee"
    And the page should have "About" and "Menu" links

  Scenario: About link is present on the homepage
    When I view the coffee shop homepage
    Then an "About" anchor should be present in the DOM

  Scenario: Menu link is present on the homepage
    When I view the coffee shop homepage
    Then a "Menu" anchor should be present in the DOM

  Scenario: Navigation menu persists across page loads
    When I load the coffee shop homepage
    Then the first nav anchor should be visible

  # ---------------------------------------------------------------------------
  # Database schema
  # ---------------------------------------------------------------------------

  Scenario: Orders table has correct columns
    Then the "orders" table should have columns: id, customer, item, quantity, status, created, updated

  Scenario: Orders table has a status index
    Then the "orders" table should have index "idx_orders_status" on the status column

  Scenario: Quantity column has a CHECK constraint > 0
    Then the quantity column should enforce a value greater than 0

  # ---------------------------------------------------------------------------
  # OpenAPI
  # ---------------------------------------------------------------------------

  Scenario: Orders service exposes OpenAPI UI
    When I GET http://localhost:8081/openapi/ui/
    Then the response status should be 200
    And the page should contain API documentation
