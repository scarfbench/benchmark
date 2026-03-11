Feature: DayTrader Online Stock Trading Brokerage
  As a stock trader
  I want to buy and sell stocks, manage my portfolio, and track market activity
  So that I can trade equities through an online brokerage system

  Background:
    Given the DayTrader application is running on port 9080
    And the database is populated with users and stock quotes

  # ---------------------------------------------------------------------------
  # Authentication — login
  # ---------------------------------------------------------------------------

  Scenario: Login with valid credentials
    When I POST to /daytrader/app with action "login" uid "uid:0" and passwd "xxx"
    Then I should be redirected to the home page
    And an HTTP session should be established with uidBean "uid:0"

  Scenario: Login with invalid password fails
    When I POST to /daytrader/app with action "login" uid "uid:0" and passwd "wrong"
    Then I should see the login page with an error

  Scenario: Login increments the login count
    Given user "uid:0" has login count N
    When I login as "uid:0"
    Then the login count should be N + 1

  Scenario: Login updates the last login timestamp
    When I login as "uid:0"
    Then the lastLogin field should be updated to the current time

  # ---------------------------------------------------------------------------
  # Authentication — logout
  # ---------------------------------------------------------------------------

  Scenario: Logout invalidates the session
    Given I am logged in as "uid:0"
    When I POST to /daytrader/app with action "logout"
    Then the HTTP session should be invalidated
    And I should see the welcome page

  Scenario: Logout increments the logout count
    Given user "uid:0" has logout count M
    When I logout
    Then the logout count should be M + 1

  # ---------------------------------------------------------------------------
  # Authentication — session required
  # ---------------------------------------------------------------------------

  Scenario: Unauthenticated request to a protected action redirects to login
    When I GET /daytrader/app?action=home without a session
    Then I should be redirected to the welcome/login page

  # ---------------------------------------------------------------------------
  # Registration
  # ---------------------------------------------------------------------------

  Scenario: Register a new user with valid data
    When I register with:
      | user id           | newuser1            |
      | passwd            | pass123             |
      | confirm passwd    | pass123             |
      | Full Name         | New User            |
      | Credit Card Number| 1234-5678-9012-3456 |
      | money             | 10000.00            |
      | email             | new@example.com     |
      | snail mail        | 123 Main St         |
    Then the account should be created
    And I should be automatically logged in

  Scenario: Registered user has opening balance
    When I register with opening balance 50000.00
    Then the account balance should be 50000.00
    And the opening balance should be 50000.00

  Scenario: Registration requires password confirmation to match
    When I register with password "abc" and confirm password "xyz"
    Then the registration should fail

  # ---------------------------------------------------------------------------
  # Home page
  # ---------------------------------------------------------------------------

  Scenario: Home page displays account summary
    Given I am logged in as "uid:0"
    When I navigate to action "home"
    Then I should see account ID, creation date, login count, session date
    And I should see cash balance, number of holdings, holdings total
    And I should see sum of cash and holdings, opening balance, and gain/loss

  # ---------------------------------------------------------------------------
  # Portfolio
  # ---------------------------------------------------------------------------

  Scenario: Portfolio page shows all holdings
    Given I am logged in and have stock holdings
    When I navigate to action "portfolio"
    Then I should see a table with columns: holdingID, purchase date, symbol, quantity, purchase price, current price, purchase basis, market value, gain/loss%
    And each holding should have a "sell" link

  Scenario: Portfolio shows totals row
    When I view my portfolio
    Then the bottom row should show total purchase basis, total market value, and total gain/loss

  # ---------------------------------------------------------------------------
  # Buying stocks
  # ---------------------------------------------------------------------------

  Scenario: Buy shares of a stock
    Given I am logged in as "uid:0"
    When I buy 100 shares of "s:0"
    Then a new buy order should be created
    And the order type should be "buy"
    And the order fee should be $24.95
    And my account balance should decrease by (100 * share price + $24.95)
    And a new holding should be created

  Scenario: Buy order shows confirmation
    When I buy shares
    Then I should see the order confirmation page with orderID, status, creation date, type "buy", symbol, and quantity

  Scenario: Buying updates the stock's price and volume
    When I buy 100 shares of "s:0"
    Then the quote for "s:0" should have increased volume
    And the price should be updated by a random factor

  # ---------------------------------------------------------------------------
  # Selling stocks
  # ---------------------------------------------------------------------------

  Scenario: Sell a holding
    Given I own holding with holdingID 1
    When I sell holdingID 1
    Then a new sell order should be created
    And the order type should be "sell"
    And the order fee should be $24.95
    And my account balance should increase by (quantity * current price - $24.95)
    And the holding should be removed

  Scenario: Sell order shows confirmation
    When I sell a holding
    Then I should see the order confirmation with type "sell"

  # ---------------------------------------------------------------------------
  # Order processing modes
  # ---------------------------------------------------------------------------

  Scenario: Synchronous order processing completes immediately
    Given order processing mode is "Sync"
    When I buy shares
    Then the order status should be "completed" immediately

  Scenario: Asynchronous order processing queues to JMS
    Given order processing mode is "Async"
    When I buy shares
    Then the order should be queued to the TradeBrokerQueue
    And the DTBroker3MDB should process and complete the order

  Scenario: Async 2-Phase uses XA transactions
    Given order processing mode is "Async_2Phase"
    When I buy shares
    Then the order should use two-phase XA commit

  # ---------------------------------------------------------------------------
  # Order status lifecycle
  # ---------------------------------------------------------------------------

  Scenario: Order statuses follow the correct lifecycle
    Then valid order statuses should include: "open", "processing", "completed", "alertcompleted", "cancelled", "closed"

  Scenario: Closed orders become completed after user acknowledges
    Given I have "closed" orders
    When I visit the account page
    Then the closed orders should transition to "completed"

  # ---------------------------------------------------------------------------
  # Order alerts
  # ---------------------------------------------------------------------------

  Scenario: Completed order alert banner appears when DisplayOrderAlerts is enabled
    Given DisplayOrderAlerts is enabled
    And I have recently completed orders with status "closed"
    When I visit any authenticated page
    Then I should see a blinking order alert banner

  # ---------------------------------------------------------------------------
  # Quotes
  # ---------------------------------------------------------------------------

  Scenario: Look up a single stock quote
    Given I am logged in
    When I request quotes for "s:0"
    Then I should see the quote with symbol, company name, volume, price, open, low, high, change

  Scenario: Look up multiple stock quotes
    When I request quotes for "s:0,s:1,s:2"
    Then I should see 3 quote rows

  Scenario: Quote page shows buy form with quantity field
    When I view quotes for "s:0"
    Then I should see a buy form with a quantity input and "Buy" button

  # ---------------------------------------------------------------------------
  # REST API — Quotes
  # ---------------------------------------------------------------------------

  Scenario: GET /rest/quotes/{symbols} returns JSON
    When I GET /daytrader/rest/quotes/s:0,s:1
    Then the response should be JSON
    And it should contain 2 QuoteDataBean objects

  Scenario: POST /rest/quotes returns quotes for form-encoded symbols
    When I POST to /daytrader/rest/quotes with symbols "s:0"
    Then the response should contain the quote for "s:0"

  # ---------------------------------------------------------------------------
  # REST API — SSE broadcast
  # ---------------------------------------------------------------------------

  Scenario: SSE endpoint streams quote price changes
    When I connect to GET /daytrader/rest/broadcastevents with Accept text/event-stream
    Then I should receive Server-Sent Events with recent quote price changes

  # ---------------------------------------------------------------------------
  # Market summary
  # ---------------------------------------------------------------------------

  Scenario: Market summary shows TSIA and volume
    When I navigate to action "mksummary"
    Then I should see the Trade Stock Index Average (TSIA)
    And I should see the total trading volume

  Scenario: Market summary shows top 5 gainers and top 5 losers
    When I view the market summary
    Then I should see a top 5 gainers table
    And I should see a top 5 losers table

  Scenario: Market summary is cached for the configured interval
    Given marketSummaryInterval is 20 seconds
    When I request the market summary twice within 20 seconds
    Then both requests should return the same cached data

  # ---------------------------------------------------------------------------
  # Account management
  # ---------------------------------------------------------------------------

  Scenario: View account page shows profile and order history
    Given I am logged in
    When I navigate to action "account"
    Then I should see my profile information
    And I should see the 5 most recent orders

  Scenario: Show all orders displays complete history
    When I navigate to action "account" with showAllOrders
    Then I should see all orders for my account

  Scenario: Update profile changes user information
    Given I am logged in
    When I update my profile with fullname "Updated Name" and email "new@mail.com"
    Then my profile should be updated

  # ---------------------------------------------------------------------------
  # Quote price dynamics
  # ---------------------------------------------------------------------------

  Scenario: Stock price is updated by random factor on trade
    When a buy or sell order is executed for symbol "s:5"
    Then the price should change by a random factor within ±10%

  Scenario: Penny stock protection kicks in at $0.01
    Given stock "s:5" has price $0.01
    When a trade occurs
    Then the price should be multiplied by 600

  Scenario: Maximum stock price is capped at $400
    Given stock "s:5" has price above $400
    When a trade occurs
    Then a 0.5x split factor should be applied

  # ---------------------------------------------------------------------------
  # Financial calculations
  # ---------------------------------------------------------------------------

  Scenario: Gain is computed as current minus open
    Given current value is 150.00 and open value is 100.00
    Then gain should be 50.00

  Scenario: Gain percent is computed correctly
    Given current value is 150.00 and open value is 100.00
    Then gain percent should be 50.0%

  Scenario: Holdings total sums purchase price times quantity
    Given holdings:
      | purchasePrice | quantity |
      | 25.00         | 100      |
      | 50.00         | 50       |
    Then the holdings total should be 5000.00

  # ---------------------------------------------------------------------------
  # Configuration (admin)
  # ---------------------------------------------------------------------------

  Scenario: Config page displays current settings
    When I GET /daytrader/config
    Then I should see the current DayTrader configuration

  Scenario: Update runtime configuration
    When I POST to /daytrader/config with action "updateConfig" and MaxUsers 1000
    Then the MaxUsers setting should be updated to 1000

  Scenario: Reset trade clears recent data and returns run stats
    When I POST to /daytrader/config with action "resetTrade"
    Then recent orders should be deleted
    And I should see RunStats with trade/user/order counts

  Scenario: Build database populates users and quotes
    When I POST to /daytrader/config with action "buildDB"
    Then the database should be populated with the configured number of users and quotes

  Scenario: Build DB tables creates schema
    When I POST to /daytrader/config with action "buildDBTables"
    Then all required database tables should be created

  # ---------------------------------------------------------------------------
  # Default configuration values
  # ---------------------------------------------------------------------------

  Scenario: Default configuration values
    Then the following defaults should be active:
      | parameter                       | default |
      | MAX_USERS                       | 15000   |
      | MAX_QUOTES                      | 10000   |
      | MAX_HOLDINGS                    | 10      |
      | marketSummaryInterval           | 20      |
      | orderFee                        | 24.95   |
      | publishQuotePriceChange         | true    |
      | listQuotePriceChangeFrequency   | 100     |
      | displayOrderAlerts              | true    |

  # ---------------------------------------------------------------------------
  # Scenario workload servlet
  # ---------------------------------------------------------------------------

  Scenario: Scenario servlet drives random trading operations
    When I GET /daytrader/scenario
    Then it should execute a random mix of operations including home, quote, buy, sell, portfolio, account, logout, register

  Scenario: Standard workload mix percentages
    Then the workload should distribute operations approximately as:
      | operation      | percentage |
      | Home           | 20         |
      | Quote          | 40         |
      | Portfolio      | 12         |
      | Account        | 10         |
      | Buy            | 4          |
      | Sell           | 4          |
      | Update Account | 4          |
      | Logout         | 4          |
      | Register       | 2          |

  # ---------------------------------------------------------------------------
  # Entity structure
  # ---------------------------------------------------------------------------

  Scenario: AccountDataBean entity has required fields
    Then an Account entity should have accountID, loginCount, logoutCount, lastLogin, creationDate, balance, openBalance, profile

  Scenario: QuoteDataBean entity has required fields
    Then a Quote entity should have symbol, companyName, volume, price, open1, low, high, change1

  Scenario: OrderDataBean entity has required fields
    Then an Order entity should have orderID, orderType, orderStatus, openDate, completionDate, quantity, price, orderFee

  Scenario: HoldingDataBean entity has required fields
    Then a Holding entity should have holdingID, quantity, purchasePrice, purchaseDate, account, quote

  Scenario: AccountProfileDataBean entity has required fields
    Then a Profile entity should have userID, passwd, fullName, address, email, creditCard

  # ---------------------------------------------------------------------------
  # Pre-seeded data
  # ---------------------------------------------------------------------------

  Scenario: Pre-seeded users follow uid:N pattern
    Then users should have IDs in format "uid:0" through "uid:14999" with password "xxx"

  Scenario: Pre-seeded stock quotes follow s:N pattern
    Then quotes should have symbols "s:0" through "s:9999"

  # ---------------------------------------------------------------------------
  # JMS messaging
  # ---------------------------------------------------------------------------

  Scenario: TradeBrokerQueue processes async orders
    When an order is queued to the TradeBrokerQueue
    Then DTBroker3MDB should receive and complete the order

  Scenario: TradeStreamerTopic broadcasts quote price changes
    Given publishQuotePriceChange is enabled
    When a trade updates a quote price
    Then a message should be published to the TradeStreamerTopic

  # ---------------------------------------------------------------------------
  # WebSocket / SSE for market data
  # ---------------------------------------------------------------------------

  Scenario: Quote price change events are broadcast via CDI
    When a quote price changes
    Then a @QuotePriceChange CDI event should fire asynchronously
    And connected SSE clients should receive the update

  # ---------------------------------------------------------------------------
  # Welcome page
  # ---------------------------------------------------------------------------

  Scenario: Welcome page displays login form
    When I GET /daytrader/app
    Then I should see a login form with uid and passwd fields
    And I should see a "Log in" button
    And I should see a "Register With DayTrader" link
