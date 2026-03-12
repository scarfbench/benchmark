Feature: RealWorld Conduit API — Blog Platform
  As a blog platform user
  I want to register, write articles, comment, follow authors, and favorite articles
  So that I can participate in a community blogging platform via a REST API

  Background:
    Given the RealWorld application is running
    And the PostgreSQL database is initialized

  # ---------------------------------------------------------------------------
  # User registration
  # ---------------------------------------------------------------------------

  Scenario: Register a new user with valid data
    When I POST to /api/users with:
      """
      {"user": {"username": "duke", "email": "duke@example.com", "password": "secret123"}}
      """
    Then the response status should be 201
    And the response should contain user with email, username, bio, image, and token

  Scenario: Newly registered user has null bio and image
    When I register a new user
    Then the user's bio should be null
    And the user's image should be null

  Scenario: Registration returns a valid JWT token
    When I register a new user
    Then the response should contain a non-empty "token" field
    And the token should be a valid JWT

  Scenario: Registration with duplicate username returns 409
    Given user "duke" already exists
    When I try to register with username "duke"
    Then the response status should be 409
    And the error body should indicate username already exists

  Scenario: Registration with duplicate email returns 409
    Given user with email "duke@example.com" already exists
    When I try to register with email "duke@example.com"
    Then the response status should be 409
    And the error body should indicate email already exists

  Scenario: Registration with blank username returns 422
    When I POST to /api/users with blank username
    Then the response status should be 422

  Scenario: Registration with blank email returns 422
    When I POST to /api/users with blank email
    Then the response status should be 422

  Scenario: Registration with blank password returns 422
    When I POST to /api/users with blank password
    Then the response status should be 422

  Scenario: Registration with invalid email format returns 422
    When I POST to /api/users with email "not-an-email"
    Then the response status should be 422

  Scenario: Username uniqueness is case-insensitive
    Given user "Duke" exists
    When I try to register with username "duke"
    Then the response status should be 409

  Scenario: Email uniqueness is case-insensitive
    Given user with email "Duke@Example.com" exists
    When I try to register with email "duke@example.com"
    Then the response status should be 409

  # ---------------------------------------------------------------------------
  # User login
  # ---------------------------------------------------------------------------

  Scenario: Login with valid credentials
    Given user "duke" exists with password "secret123"
    When I POST to /api/users/login with email and password
    Then the response status should be 200
    And the response should contain user with email, username, bio, image, and token

  Scenario: Login with wrong password returns 401
    When I login with a wrong password
    Then the response status should be 401

  Scenario: Login with non-existent email returns 401
    When I login with email "noone@example.com"
    Then the response status should be 401

  Scenario: Login with blank email returns 422
    When I login with blank email
    Then the response status should be 422

  Scenario: Login with blank password returns 422
    When I login with blank password
    Then the response status should be 422

  Scenario: Each login issues a fresh JWT token
    When I login twice
    Then each response should contain a token (may differ)

  # ---------------------------------------------------------------------------
  # Get current user
  # ---------------------------------------------------------------------------

  Scenario: Get current user with valid token
    Given I am authenticated as "duke"
    When I GET /api/user with Authorization header "Token <jwt>"
    Then the response status should be 200
    And the response should contain the current user's details

  Scenario: Get current user without token returns 401
    When I GET /api/user without an Authorization header
    Then the response status should be 401

  Scenario: Get current user with invalid token returns 401
    When I GET /api/user with an invalid token
    Then the response status should be 401

  # ---------------------------------------------------------------------------
  # Update user
  # ---------------------------------------------------------------------------

  Scenario: Update user email
    Given I am authenticated as "duke"
    When I PUT to /api/user with {"user": {"email": "new@example.com"}}
    Then the response status should be 200
    And the email should be updated to "new@example.com"

  Scenario: Update user username
    Given I am authenticated as "duke"
    When I PUT to /api/user with {"user": {"username": "newduke"}}
    Then the response status should be 200
    And the username should be "newduke"

  Scenario: Update user bio and image
    Given I am authenticated as "duke"
    When I PUT to /api/user with {"user": {"bio": "Hello world", "image": "http://img.com/me.jpg"}}
    Then bio and image should be updated

  Scenario: Update with already-taken username returns 409
    Given users "duke" and "alice" exist
    When I am "duke" and try to update username to "alice"
    Then the response status should be 409

  Scenario: Update with already-taken email returns 409
    Given users "duke" and "alice" exist
    When I am "duke" and try to update email to alice's email
    Then the response status should be 409

  Scenario: Update with all null fields returns 422
    When I PUT to /api/user with {"user": {}}
    Then the response status should be 422

  Scenario: Update without auth returns 401
    When I PUT to /api/user without an Authorization header
    Then the response status should be 401

  # ---------------------------------------------------------------------------
  # Profiles
  # ---------------------------------------------------------------------------

  Scenario: Get a user's profile (unauthenticated)
    Given user "duke" exists
    When I GET /api/profiles/duke without auth
    Then the response status should be 200
    And the profile should contain username, bio, image
    And "following" should be false

  Scenario: Get a user's profile (authenticated, not following)
    Given I am authenticated as "alice" and not following "duke"
    When I GET /api/profiles/duke
    Then "following" should be false

  Scenario: Get a non-existent profile returns 404
    When I GET /api/profiles/nonexistent
    Then the response status should be 404

  # ---------------------------------------------------------------------------
  # Follow / Unfollow
  # ---------------------------------------------------------------------------

  Scenario: Follow a user
    Given I am authenticated as "alice"
    When I POST to /api/profiles/duke/follow
    Then the response status should be 200
    And the profile should have "following": true

  Scenario: Unfollow a user
    Given I am authenticated as "alice" and following "duke"
    When I DELETE /api/profiles/duke/follow
    Then the response status should be 200
    And the profile should have "following": false

  Scenario: Follow requires authentication
    When I POST to /api/profiles/duke/follow without auth
    Then the response status should be 401

  Scenario: Unfollow requires authentication
    When I DELETE /api/profiles/duke/follow without auth
    Then the response status should be 401

  # ---------------------------------------------------------------------------
  # Create article
  # ---------------------------------------------------------------------------

  Scenario: Create an article with valid data
    Given I am authenticated as "duke"
    When I POST to /api/articles with:
      """
      {"article": {"title": "How to train your dragon", "description": "Ever wonder how?", "body": "You have to believe", "tagList": ["dragons", "training"]}}
      """
    Then the response status should be 201
    And the article should have a generated slug
    And the tagList should contain "dragons" and "training"
    And the author username should be "duke"

  Scenario: Article slug is generated from title
    When I create an article with title "How to train your dragon"
    Then the slug should be "how-to-train-your-dragon" or similar slugified form

  Scenario: Duplicate title generates unique slug with UUID suffix
    Given an article with title "How to train your dragon" exists
    When I create another article with the same title
    Then the slug should be different (UUID appended)

  Scenario: Article creation without tagList is valid
    When I create an article without tagList
    Then the response status should be 201
    And the tagList should be empty

  Scenario: Article with blank title returns 422
    When I create an article with blank title
    Then the response status should be 422

  Scenario: Article with blank description returns 422
    When I create an article with blank description
    Then the response status should be 422

  Scenario: Article with blank body returns 422
    When I create an article with blank body
    Then the response status should be 422

  Scenario: Article creation requires authentication
    When I POST to /api/articles without auth
    Then the response status should be 401

  Scenario: Article has createdAt and updatedAt timestamps
    When I create an article
    Then the response should contain createdAt and updatedAt in ISO 8601 format

  # ---------------------------------------------------------------------------
  # Get article
  # ---------------------------------------------------------------------------

  Scenario: Get an article by slug (unauthenticated)
    Given an article with slug "how-to-train-your-dragon" exists
    When I GET /api/articles/how-to-train-your-dragon
    Then the response status should be 200
    And the response should contain slug, title, description, body, tagList, createdAt, updatedAt, author

  Scenario: Get a non-existent article returns 404
    When I GET /api/articles/nonexistent-slug
    Then the response status should be 404

  Scenario: Slug matching is case-insensitive
    Given an article with slug "how-to-train-your-dragon" exists
    When I GET /api/articles/HOW-TO-TRAIN-YOUR-DRAGON
    Then the response status should be 200

  # ---------------------------------------------------------------------------
  # Update article
  # ---------------------------------------------------------------------------

  Scenario: Update an article's title
    Given I am authenticated as "duke" and own an article
    When I PUT to /api/articles/{slug} with {"article": {"title": "New Title"}}
    Then the title should be updated
    And the slug should be regenerated from the new title

  Scenario: Update an article's body only
    When I PUT to /api/articles/{slug} with {"article": {"body": "Updated body"}}
    Then only the body should be updated

  Scenario: Update with all null fields returns 422
    When I PUT to /api/articles/{slug} with {"article": {}}
    Then the response status should be 422

  Scenario: Update a non-existent article returns 404
    When I PUT to /api/articles/nonexistent with valid data
    Then the response status should be 404

  Scenario: Update requires authentication
    When I PUT to /api/articles/{slug} without auth
    Then the response status should be 401

  # ---------------------------------------------------------------------------
  # Delete article
  # ---------------------------------------------------------------------------

  Scenario: Delete own article
    Given I am authenticated as "duke" and own an article
    When I DELETE /api/articles/{slug}
    Then the response status should be 200
    And the article should no longer exist

  Scenario: Delete cascades comments and tag relationships
    Given an article has comments and tags
    When I delete the article
    Then associated comments and tag relationships should be removed

  Scenario: Only the author can delete their article
    Given "duke" owns an article
    When "alice" tries to DELETE the article
    Then the response status should be 404

  Scenario: Delete requires authentication
    When I DELETE /api/articles/{slug} without auth
    Then the response status should be 401

  # ---------------------------------------------------------------------------
  # List articles (global feed)
  # ---------------------------------------------------------------------------

  Scenario: List all articles with default pagination
    When I GET /api/articles
    Then the response status should be 200
    And the response should contain "articles" array and "articlesCount"
    And articles should be ordered by createdAt descending

  Scenario: List articles with limit and offset
    When I GET /api/articles?limit=5&offset=0
    Then the articles array should have at most 5 items

  Scenario: Filter articles by tag
    Given articles tagged with "dragons" exist
    When I GET /api/articles?tag=dragons
    Then all returned articles should have "dragons" in tagList

  Scenario: Filter articles by author
    Given "duke" has published articles
    When I GET /api/articles?author=duke
    Then all returned articles should have author "duke"

  Scenario: Filter articles favorited by a user
    Given "alice" has favorited some articles
    When I GET /api/articles?favorited=alice
    Then all returned articles should be favorited by "alice"

  Scenario: Multiple filters can be combined
    When I GET /api/articles?tag=dragons&author=duke
    Then results should match both filters

  Scenario: articlesCount reflects total matches, not page size
    Given 25 articles exist
    When I GET /api/articles?limit=5
    Then articlesCount should be 25

  Scenario: Tag filter is case-insensitive
    Given articles tagged with "Dragons" exist
    When I GET /api/articles?tag=dragons
    Then tagged articles should be returned

  # ---------------------------------------------------------------------------
  # Article feed (followed authors)
  # ---------------------------------------------------------------------------

  Scenario: Feed returns articles from followed authors
    Given I am authenticated as "alice" and following "duke"
    And "duke" has published articles
    When I GET /api/articles/feed
    Then the response should contain articles by "duke"

  Scenario: Feed does not return articles from unfollowed authors
    Given I am authenticated as "alice" and not following "bob"
    When I GET /api/articles/feed
    Then no articles by "bob" should appear

  Scenario: Feed supports pagination
    When I GET /api/articles/feed?limit=5&offset=0
    Then at most 5 articles should be returned

  Scenario: Feed requires authentication
    When I GET /api/articles/feed without auth
    Then the response status should be 401

  # ---------------------------------------------------------------------------
  # Comments
  # ---------------------------------------------------------------------------

  Scenario: Get comments for an article
    Given an article with comments exists
    When I GET /api/articles/{slug}/comments
    Then the response should contain a "comments" array
    And each comment should have id, body, createdAt, updatedAt, author

  Scenario: Create a comment on an article
    Given I am authenticated as "alice"
    When I POST to /api/articles/{slug}/comments with {"comment": {"body": "Great article!"}}
    Then the response status should be 200
    And the comment should be created with author "alice"

  Scenario: Comment with blank body returns 422
    When I POST a comment with blank body
    Then the response status should be 422

  Scenario: Creating a comment requires authentication
    When I POST to /api/articles/{slug}/comments without auth
    Then the response status should be 401

  Scenario: Delete own comment
    Given I am authenticated as "alice" and own a comment
    When I DELETE /api/articles/{slug}/comments/{id}
    Then the response status should be 200

  Scenario: Only the comment author can delete it
    Given "alice" owns a comment
    When "bob" tries to DELETE the comment
    Then the response status should be 404

  Scenario: Deleting a comment requires authentication
    When I DELETE /api/articles/{slug}/comments/{id} without auth
    Then the response status should be 401

  # ---------------------------------------------------------------------------
  # Favorites
  # ---------------------------------------------------------------------------

  Scenario: Favorite an article
    Given I am authenticated as "alice"
    When I POST to /api/articles/{slug}/favorite
    Then the response status should be 200
    And the article should have "favorited": true
    And "favoritesCount" should be incremented

  Scenario: Favoriting is idempotent
    Given I already favorited the article
    When I POST to /api/articles/{slug}/favorite again
    Then no duplicate favorite should be created
    And favoritesCount should remain the same

  Scenario: Unfavorite an article
    Given I am authenticated as "alice" and have favorited the article
    When I DELETE /api/articles/{slug}/favorite
    Then the response status should be 200
    And "favorited" should be false
    And "favoritesCount" should be decremented

  Scenario: Unfavoriting when not favorited is a no-op
    Given I have not favorited the article
    When I DELETE /api/articles/{slug}/favorite
    Then the response status should be 200
    And favoritesCount should remain unchanged

  Scenario: Favoriting requires authentication
    When I POST to /api/articles/{slug}/favorite without auth
    Then the response status should be 401

  # ---------------------------------------------------------------------------
  # Tags
  # ---------------------------------------------------------------------------

  Scenario: Get all tags
    Given articles with various tags exist
    When I GET /api/tags
    Then the response status should be 200
    And the response should contain a "tags" array of strings

  Scenario: Tags are created on demand when articles are created
    When I create an article with tagList ["newtag"]
    And I GET /api/tags
    Then the tags should include "newtag"

  Scenario: Tags endpoint does not require authentication
    When I GET /api/tags without auth
    Then the response status should be 200

  # ---------------------------------------------------------------------------
  # Authentication and security
  # ---------------------------------------------------------------------------

  Scenario: JWT uses Token prefix (not Bearer)
    Then the Authorization header format should be "Token <jwt_string>"

  Scenario: JWT is signed with HMAC512
    When I decode a JWT token
    Then the algorithm should be HMAC512

  Scenario: Invalid JWT returns 401
    When I send a request with Authorization header "Token invalid.jwt.token"
    Then the response status should be 401

  Scenario: Missing auth on required endpoint returns 401
    When I GET /api/user without any auth header
    Then the response status should be 401

  Scenario: All registered users receive USER role
    When a user registers
    Then their JWT should contain the ROLES claim with "USER"

  # ---------------------------------------------------------------------------
  # Error response format
  # ---------------------------------------------------------------------------

  Scenario: All error responses use standard format
    When an error occurs
    Then the response body should match:
      """
      {"errors": {"body": ["error message"]}}
      """

  Scenario: HTTP status codes are correctly mapped
    Then the following exceptions should map to statuses:
      | exception                       | status |
      | UsernameAlreadyExistsException  | 409    |
      | EmailAlreadyExistsException     | 409    |
      | UserNotFoundException           | 404    |
      | ArticleNotFoundException        | 404    |
      | CommentNotFoundException        | 404    |
      | InvalidPasswordException        | 401    |
      | ModelValidationException        | 422    |
      | UnauthorizedException           | 401    |

  # ---------------------------------------------------------------------------
  # Timestamp format
  # ---------------------------------------------------------------------------

  Scenario: All timestamps follow ISO 8601 with milliseconds
    When I retrieve any entity with timestamps
    Then createdAt and updatedAt should match pattern "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

  # ---------------------------------------------------------------------------
  # Pagination defaults
  # ---------------------------------------------------------------------------

  Scenario: Default pagination is limit 20, offset 0
    When I GET /api/articles without limit or offset parameters
    Then at most 20 articles should be returned
    And offset should default to 0

  # ---------------------------------------------------------------------------
  # Password security
  # ---------------------------------------------------------------------------

  Scenario: Passwords are stored as BCrypt hashes
    When a user registers with password "secret123"
    Then the database should store a BCrypt hash, not plaintext

  Scenario: Login verifies password with BCrypt
    Given a user registered with password "secret123"
    When I login with password "secret123"
    Then authentication should succeed

  # ---------------------------------------------------------------------------
  # Entity relationships
  # ---------------------------------------------------------------------------

  Scenario: Article has many-to-one with User (author)
    Given an article exists
    Then the article should reference its author user

  Scenario: Article has one-to-many with Comment
    Given an article with comments
    Then the article should be associated with its comments

  Scenario: Article has many-to-many with Tag (via TagRelationship)
    Given an article with tags
    Then the article-tag association should be stored in TAG_RELATIONSHIP table

  Scenario: User follow relationship is stored in FOLLOW_RELATIONSHIP table
    When "alice" follows "duke"
    Then a record should exist in FOLLOW_RELATIONSHIP with user=alice and followed=duke

  Scenario: Favorite relationship is stored in FAVORITE_RELATIONSHIP table
    When "alice" favorites an article
    Then a record should exist in FAVORITE_RELATIONSHIP with user=alice and article=article_id
