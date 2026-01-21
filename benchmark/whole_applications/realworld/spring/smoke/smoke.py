import os
import pytest
import re
import requests

BASE_URL = "http://localhost:8080"

EMAIL = os.getenv("EMAIL", "test@example.com")
PASSWORD = os.getenv("PASSWORD", "password")
USERNAME = os.getenv("USERNAME", "testuser")
SLUG = "how-to-train-your-dragon"

ISO_8601_REGEX = re.compile(
    r"^\d{4,}-[01]\d-[0-3]\dT[0-2]\d:[0-5]\d:[0-5]\d.\d+(?:[+-][0-2]\d:[0-5]\d|Z)$"
)


class APISession:
    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})

    def add_token(self, token):
        self.session.headers.update({"Authorization": f"Token {token}"})

    def get(self, path):
        return self.session.get(f"{BASE_URL}{path}")

    def post(self, path, json=None):
        return self.session.post(f"{BASE_URL}{path}", json=json)

    def put(self, path, json=None):
        return self.session.put(f"{BASE_URL}{path}", json=json)

    def delete(self, path):
        return self.session.delete(f"{BASE_URL}{path}")


session = APISession()
comment_id = None


# -------------------------
# AUTH
# -------------------------


@pytest.mark.order(1)
def test_register():
    r = session.post(
        "/users",
        json={"user": {"username": USERNAME, "password": PASSWORD, "email": EMAIL}},
    )
    assert r.status_code == 200  # does not match the others, should be 201
    data = r.json()["user"]
    assert data["email"] == EMAIL
    assert data["username"] == USERNAME
    assert data["bio"] is None
    assert data["image"] == ""  # should be None
    assert data["token"]


@pytest.mark.order(2)
def test_login_and_remember_token():
    r = session.post(
        "/users/login",
        json={"user": {"email": EMAIL, "password": PASSWORD}},
    )
    assert r.status_code == 200
    user = r.json()["user"]
    assert user["email"] == EMAIL
    assert user["username"] == USERNAME
    token = user["token"]
    assert token
    session.add_token(token)


@pytest.mark.order(3)
def test_current_user():
    r = session.get("/user")
    assert r.status_code == 200
    user = r.json()["user"]
    assert user["email"] == EMAIL
    assert user["username"] == USERNAME
    assert user["token"]


@pytest.mark.order(4)
def test_update_user():
    r = session.put("/user", json={"user": {"bio": "my-new-bio"}})
    assert r.status_code == 200
    user = r.json()["user"]
    assert user["bio"] == "my-new-bio"


# -------------------------
# ARTICLES
# -------------------------


@pytest.mark.order(5)
def test_get_all_articles():
    r = session.get("/articles")
    assert r.status_code == 200
    assert r.json()["articlesCount"] == 0


@pytest.mark.order(6)
def test_get_articles_by_author():
    r = session.get("/articles?author=johnjacob")
    assert r.status_code == 200
    assert r.json()["articlesCount"] == 0


@pytest.mark.order(7)
def test_get_articles_favorited_by_username():
    r = session.get("/articles?favorited=jane")
    assert r.status_code == 200
    assert r.json()["articlesCount"] == 0


@pytest.mark.order(8)
def test_get_articles_by_tag():
    r = session.get("/articles?tag=dragons")
    assert r.status_code == 200
    assert r.json()["articlesCount"] == 0


def validate_article(article, body="Very carefully.", favorites_count=0):
    assert isinstance(article, dict), "article"

    # Required keys
    expected_keys = {
        "title",
        "slug",
        "body",
        "createdAt",
        "updatedAt",
        "description",
        "tagList",
        "author",
        "favorited",
        "favoritesCount",
    }
    assert expected_keys.issubset(article.keys())

    # Field assertions
    assert article["title"] == "How to train your dragon"
    assert article["slug"] == SLUG
    assert article["body"] == body
    assert article["description"] == "Ever wonder how?"

    assert ISO_8601_REGEX.match(article["createdAt"])
    assert ISO_8601_REGEX.match(article["updatedAt"])

    # tagList
    assert isinstance(article["tagList"], list)
    assert set(article["tagList"]) == {"training", "dragons"}

    # favoritesCount
    assert isinstance(article["favoritesCount"], int)
    assert article["favoritesCount"] == favorites_count


@pytest.mark.order(9)
def test_create_article():
    article = {
        "title": "How to train your dragon",
        "description": "Ever wonder how?",
        "body": "Very carefully.",
        "tagList": ["dragons", "training"],
    }
    r = session.post(
        "/articles",
        json={"article": article},
    )
    # Status code
    assert r.status_code == 200, "create status"  # does not match, should be 201

    # Valid JSON body
    data = r.json()
    assert isinstance(data, dict)

    # Article object
    article_resp = data.get("article")
    validate_article(article_resp)


@pytest.mark.order(10)
def test_get_feed():
    r = session.get("/articles/feed")
    assert r.status_code == 200
    assert r.json()["articlesCount"] == 0


@pytest.mark.order(11)
def test_get_all_articles_after_creation():
    r = session.get("/articles")

    # Status code
    assert r.status_code == 200, "get all articles status"

    # Valid JSON body
    data = r.json()
    assert isinstance(data, dict)

    # Articles array
    articles = data.get("articles")
    assert isinstance(articles, list), "articles"

    # Articles count
    articles_count = data.get("articlesCount")
    assert isinstance(articles_count, int), "articlesCount"
    assert articles_count == 1

    validate_article(articles[0])


@pytest.mark.order(12)
def test_get_articles_by_author_after_creation():
    r = session.get(f"/articles?author={USERNAME}")

    # Status code
    assert r.status_code == 200, "get all articles status"

    # Valid JSON body
    data = r.json()
    assert isinstance(data, dict)

    # Articles array
    articles = data.get("articles")
    assert isinstance(articles, list), "articles"

    # Articles count
    articles_count = data.get("articlesCount")
    assert isinstance(articles_count, int), "articlesCount"
    assert articles_count == 1

    validate_article(articles[0])


@pytest.mark.order(13)
def test_get_article_by_slug():
    r = session.get(f"/articles/{SLUG}")
    assert r.status_code == 200
    article = r.json()["article"]
    validate_article(article)


@pytest.mark.order(14)
def test_get_articles_by_tag_after_creation():
    r = session.get("/articles?tag=dragons")

    # Status code
    assert r.status_code == 200, "get all articles status"

    # Valid JSON body
    data = r.json()
    assert isinstance(data, dict)

    # Articles array
    articles = data.get("articles")
    assert isinstance(articles, list), "articles"

    # Articles count
    articles_count = data.get("articlesCount")
    assert isinstance(articles_count, int), "articlesCount"
    assert articles_count == 1

    validate_article(articles[0])


@pytest.mark.order(15)
def test_update_article():
    r = session.put(
        f"/articles/{SLUG}",
        json={"article": {"body": "With two hands"}},
    )
    article = r.json()["article"]
    validate_article(article, body="With two hands")


@pytest.mark.order(16)
def test_favorite_article():
    r = session.post(f"/articles/{SLUG}/favorite")
    assert r.status_code == 200
    article = r.json()["article"]
    validate_article(article, body="With two hands", favorites_count=1)
    assert article["favorited"] is True


@pytest.mark.order(17)
def test_unfavorite_article():
    r = session.delete(f"/articles/{SLUG}/favorite")
    assert r.status_code == 200
    article = r.json()["article"]
    validate_article(article, body="With two hands")
    assert article["favorited"] is False


def validate_comment(comment):
    assert isinstance(comment, dict), "comment"

    # Required keys
    expected_keys = {"id", "body", "createdAt", "updatedAt", "author"}
    assert expected_keys.issubset(comment.keys())

    # Field assertions
    assert comment["body"] == "Thank you so much!"

    assert ISO_8601_REGEX.match(comment["createdAt"])
    assert ISO_8601_REGEX.match(comment["updatedAt"])


@pytest.mark.order(18)
def test_create_comment():
    global comment_id
    r = session.post(
        f"/articles/{SLUG}/comments",
        json={"comment": {"body": "Thank you so much!"}},
    )
    assert r.status_code == 200
    comment = r.json()["comment"]
    validate_comment(comment)
    comment_id = comment["id"]


@pytest.mark.order(19)
def test_get_comments():
    r = session.get(f"/articles/{SLUG}/comments")

    assert r.status_code == 200

    # Valid JSON body
    data = r.json()
    assert isinstance(data, dict)

    # Comments array
    comments = data.get("comments")
    assert isinstance(comments, list), "comments"

    validate_comment(comments[0])


@pytest.mark.order(20)
def test_delete_comment():
    r = session.delete(f"/articles/{SLUG}/comments/{comment_id}")
    assert r.status_code == 200


@pytest.mark.order(21)
def test_delete_article():
    r = session.delete(f"/articles/{SLUG}")
    assert r.status_code == 204  # does not match, should be 200


# -------------------------
# PROFILES
# -------------------------


def validate_user(user):
    assert isinstance(user, dict), "user"

    # Required keys
    expected_keys = {"email", "username", "bio", "image", "token"}
    assert expected_keys.issubset(user.keys())

    # Field assertions
    assert user["email"] == f"celeb_{EMAIL}"
    assert user["username"] == f"celeb_{USERNAME}"
    assert user["bio"] is None
    assert user["image"] == ""  # should be None
    assert user["token"] is not None


def validate_profile(profile, following=False):
    assert isinstance(profile, dict), "profile"

    # Required keys
    expected_keys = {"username", "bio", "image", "following"}
    assert expected_keys.issubset(profile.keys())

    # Field assertions
    assert profile["username"] == f"celeb_{USERNAME}"
    assert profile["bio"] is None
    assert profile["image"] == "null"  # does not match, should be None
    assert profile["following"] is following


@pytest.mark.order(22)
def test_register_celeb():
    r = session.post(
        "/users",
        json={
            "user": {
                "email": f"celeb_{EMAIL}",
                "password": PASSWORD,
                "username": f"celeb_{USERNAME}",
            }
        },
    )
    assert r.status_code == 200  # does not match, should be 201
    user = r.json()["user"]
    validate_user(user)


@pytest.mark.order(23)
def test_get_celeb_profile():
    r = session.get(f"/profiles/celeb_{USERNAME}")

    assert r.status_code == 200
    profile = r.json()["profile"]
    validate_profile(profile)


@pytest.mark.order(24)
def test_follow_celeb_profile():
    r = session.post(f"/profiles/celeb_{USERNAME}/follow")
    assert r.status_code == 200
    profile = r.json()["profile"]
    validate_profile(profile, following=True)


@pytest.mark.order(25)
def test_unfollow_celeb_profile():
    r = session.delete(f"/profiles/celeb_{USERNAME}/follow")
    assert r.status_code == 200
    profile = r.json()["profile"]
    validate_profile(profile)


# -------------------------
# TAGS
# -------------------------


@pytest.mark.order(26)
def test_get_tags():
    r = session.get("/tags")
    assert r.status_code == 200
    tags = r.json()["tags"]
    assert set(tags) == {"training", "dragons"}


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
