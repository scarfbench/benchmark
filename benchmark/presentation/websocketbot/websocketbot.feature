Feature: WebSocket Chat Bot (Duke)
  As a chat user
  I want to interact with Duke the chat bot over WebSocket
  So that I can join a chat room, send messages, and receive bot responses

  Background:
    Given the WebSocket bot application is running

  # ---------------------------------------------------------------------------
  # WebSocket connection
  # ---------------------------------------------------------------------------

  Scenario: Open a WebSocket connection
    When I connect to /websocketbot
    Then the connection should be established
    And the server log should contain "Connection opened"

  Scenario: Close a WebSocket connection
    Given I am connected to /websocketbot
    When I close the connection
    Then the server log should contain "Connection closed"

  # ---------------------------------------------------------------------------
  # Join messages
  # ---------------------------------------------------------------------------

  Scenario: Join the chat room
    Given I am connected to /websocketbot
    When I send a join message with name "Alice"
    Then all connected clients should receive an info message "Alice has joined the chat"
    And Duke should greet me with "Hi there!!"
    And a users list update should be sent to all clients

  Scenario: Users list includes Duke and the joined user
    Given I am connected and joined as "Alice"
    Then the users list should contain "Duke" and "Alice"

  # ---------------------------------------------------------------------------
  # Chat messages
  # ---------------------------------------------------------------------------

  Scenario: Send a chat message to another user
    Given users "Alice" and "Bob" are in the chat
    When "Alice" sends a message "Hello!" to "Bob"
    Then all connected clients should receive the chat message

  Scenario: Send a message to Duke triggers a bot response
    Given I am connected and joined as "Alice"
    When I send a message "How are you?" to "Duke"
    Then Duke should respond with "I'm doing great, thank you!"

  # ---------------------------------------------------------------------------
  # Bot responses
  # ---------------------------------------------------------------------------

  Scenario: Ask Duke how he is doing
    When I send "how are you" to Duke
    Then Duke should respond with "I'm doing great, thank you!"

  Scenario: Ask Duke how old he is
    When I send "how old are you" to Duke
    Then Duke should respond with "I'm" followed by his age and "years old."

  Scenario: Ask Duke when his birthday is
    When I send "when is your birthday" to Duke
    Then Duke should respond with "My birthday is on May 23rd. Thanks for asking!"

  Scenario: Ask Duke about his favorite color
    When I send "what is your favorite color" to Duke
    Then Duke should respond with "My favorite color is blue. What's yours?"

  Scenario: Send an unrecognized message to Duke
    When I send "tell me a joke" to Duke
    Then Duke should respond with "Sorry, I did not understand what you said."
    And the response should suggest asking about how he's doing, his age, or his favorite color

  Scenario: Bot responses are case-insensitive
    When I send "HOW ARE YOU" to Duke
    Then Duke should respond with "I'm doing great, thank you!"

  Scenario: Bot ignores question marks in messages
    When I send "how are you?" to Duke
    Then Duke should respond with "I'm doing great, thank you!"

  # ---------------------------------------------------------------------------
  # Leave the chat
  # ---------------------------------------------------------------------------

  Scenario: User leaves the chat
    Given "Alice" is in the chat
    When "Alice" closes the connection
    Then all clients should receive "Alice has left the chat"
    And the users list should be updated to remove "Alice"

  # ---------------------------------------------------------------------------
  # Message types
  # ---------------------------------------------------------------------------

  Scenario: JoinMessage contains the user's name
    When I send a join message with name "Bob"
    Then the message should be decoded as a JoinMessage with name "Bob"

  Scenario: ChatMessage contains name, target, and message
    When I send a chat message from "Alice" to "Duke" with text "Hello"
    Then the ChatMessage should have name "Alice", target "Duke", and message "Hello"

  Scenario: InfoMessage contains a system notification
    When a user joins the chat
    Then an InfoMessage should be broadcast with the notification text

  Scenario: UsersMessage contains the current user list
    When the users list changes
    Then a UsersMessage should be broadcast with the updated list including "Duke"

  # ---------------------------------------------------------------------------
  # Concurrent sessions
  # ---------------------------------------------------------------------------

  Scenario: Messages are sent to all open sessions
    Given "Alice", "Bob", and "Charlie" are in the chat
    When "Alice" sends a message
    Then "Alice", "Bob", and "Charlie" should all receive the message

  Scenario: User list updates when a new user joins
    Given "Alice" is in the chat
    When "Bob" joins
    Then the users list should contain "Duke", "Alice", and "Bob"
