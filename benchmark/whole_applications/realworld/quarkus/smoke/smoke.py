import os
import pytest
import re
import requests

BASE_URL = "http://localhost:8080/api"

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
        # uses Bearer, not Token
        self.session.headers.update({"Authorization": f"Bearer {token}"})

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


def no_auth_get(path):
    return requests.get(f"{BASE_URL}{path}", headers={"Content-Type": "application/json"})


def no_auth_post(path, json=None):
    return requests.post(
        f"{BASE_URL}{path}", json=json, headers={"Content-Type": "application/json"}
    )


def no_auth_put(path, json=None):
    return requests.put(
        f"{BASE_URL}{path}", json=json, headers={"Content-Type": "application/json"}
    )


def no_auth_delete(path):
    return requests.delete(
        f"{BASE_URL}{path}", headers={"Content-Type": "application/json"}
    )


# -------------------------
# AUTH - Registration
# -------------------------


@pytest.mark.order(1)
def test_register():
    r = session.post(
        "/users",
        json={"user": {"username": USERNAME, "password": PASSWORD, "email": EMAIL}},
    )
    assert r.status_code == 201
    data = r.json()["user"]
    assert data["email"] == EMAIL
    assert data["username"] == USERNAME
    assert data["bio"] is None
    assert data["image"] is None
    assert data["token"]


@pytest.mark.order(2)
def test_register_duplicate_username():
    r = no_auth_post(
        "/users",
        json={
            "user": {
                "username": USERNAME,
                "password": "other123",
                "email": "other@example.com",
            }
        },
    )
    assert r.status_code == 409


@pytest.mark.order(3)
def test_register_duplicate_email():
    r = no_auth_post(
        "/users",
        json={
            "user": {
                "username": "otheruser",
                "password": "other123",
                "email": EMAIL,
            }
        },
    )
    assert r.status_code == 409


@pytest.mark.order(4)
def test_register_blank_username():
    r = no_auth_post(
        "/users",
        json={
            "user": {
                "username": "",
                "password": "pass123",
                "email": "blank@example.com",
            }
        },
    )
    assert r.status_code == 422


@pytest.mark.order(5)
def test_register_blank_email():
    r = no_auth_post(
        "/users",
        json={"user": {"username": "blankuser", "password": "pass123", "email": ""}},
    )
    assert r.status_code == 422


@pytest.mark.order(6)
def test_register_blank_password():
    r = no_auth_post(
        "/users",
        json={
            "user": {
                "username": "blankpass",
                "password": "",
                "email": "blankpass@example.com",
            }
        },
    )
    assert r.status_code == 422


@pytest.mark.order(7)
def test_register_invalid_email():
    r = no_auth_post(
        "/users",
        json={
            "user": {
                "username": "invalidemail",
                "password": "pass123",
                "email": "not-an-email",
            }
        },
    )
    assert r.status_code == 422


# -------------------------
# AUTH - Login
# -------------------------


@pytest.mark.order(8)
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


@pytest.mark.order(9)
def test_login_wrong_password():
    r = no_auth_post(
        "/users/login",
        json={"user": {"email": EMAIL, "password": "wrongpassword"}},
    )
    assert r.status_code == 401


@pytest.mark.order(10)
def test_login_nonexistent_email():
    r = no_auth_post(
        "/users/login",
        json={"user": {"email": "noone@example.com", "password": "secret123"}},
    )
    assert r.status_code == 401


@pytest.mark.order(11)
def test_login_blank_email():
    r = no_auth_post(
        "/users/login",
        json={"user": {"email": "", "password": "secret123"}},
    )
    assert r.status_code == 422


@pytest.mark.order(12)
def test_login_blank_password():
    r = no_auth_post(
        "/users/login",
        json={"user": {"email": EMAIL, "password": ""}},
    )
    assert r.status_code == 422


# -------------------------
# Current User
# -------------------------


@pytest.mark.order(13)
def test_current_user():
    r = session.get("/user")
    assert r.status_code == 200
    user = r.json()["user"]
    assert user["email"] == EMAIL
    assert user["username"] == USERNAME
    assert user["token"]


@pytest.mark.order(14)
def test_current_user_no_auth():
    r = no_auth_get("/user")
    assert r.status_code == 401


@pytest.mark.order(15)
def test_current_user_invalid_token():
    r = requests.get(
        f"{BASE_URL}/user",
        headers={
            "Content-Type": "application/json",
            "Authorization": "Bearer invalid.jwt.token",
        },
    )
    assert r.status_code == 401


# -------------------------
# Update User
# -------------------------


@pytest.mark.order(16)
def test_update_user():
    r = session.put("/user", json={"user": {"bio": "my-new-bio"}})
    assert r.status_code == 200
    user = r.json()["user"]
    assert user["bio"] == "my-new-bio"


@pytest.mark.order(17)
def test_update_user_bio_and_image():
    r = session.put(
        "/user",
        json={"user": {"bio": "Hello world", "image": "http://img.com/me.jpg"}},
    )
    assert r.status_code == 200
    user = r.json()["user"]
    assert user["bio"] == "Hello world"
    assert user["image"] == "http://img.com/me.jpg"


@pytest.mark.order(18)
def test_update_user_no_auth():
    r = no_auth_put("/user", json={"user": {"bio": "hacked"}})
    assert r.status_code == 401


# -------------------------
# ARTICLES - empty state
# -------------------------


@pytest.mark.order(19)
def test_get_all_articles():
    r = session.get("/articles")
    assert r.status_code == 200
    assert r.json()["articlesCount"] == 0


@pytest.mark.order(20)
def test_get_articles_by_author():
    r = session.get("/articles?author=johnjacob")
    assert r.status_code == 200
    assert r.json()["articlesCount"] == 0


@pytest.mark.order(21)
def test_get_articles_favorited_by_username():
    r = session.get("/articles?favorited=jane")
    assert r.status_code == 200
    assert r.json()["articlesCount"] == 0


@pytest.mark.order(22)
def test_get_articles_by_tag():
    r = session.get("/articles?tag=dragons")
    assert r.status_code == 200
    assert r.json()["articlesCount"] == 0


def validate_article(article, body="Very carefully.", favorites_count=0):
    assert isinstance(article, dict), "article"

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

    assert article["title"] == "How to train your dragon"
    assert article["slug"] == SLUG
    assert article["body"] == body
    assert article["description"] == "Ever wonder how?"

    assert ISO_8601_REGEX.match(article["createdAt"])
    assert ISO_8601_REGEX.match(article["updatedAt"])

    assert isinstance(article["tagList"], list)
    assert set(article["tagList"]) == {"training", "dragons"}

    assert isinstance(article["favoritesCount"], int)
    assert article["favoritesCount"] == favorites_count


# -------------------------
# ARTICLES - create
# -------------------------


@pytest.mark.order(23)
def test_create_article():
    article = {
        "title": "How to train your dragon",
        "description": "Ever wonder how?",
        "body": "Very carefully.",
        "tagList": ["dragons", "training"],
    }
    r = session.post("/articles", json={"article": article})
    assert r.status_code == 201, "create status"
    data = r.json()
    assert isinstance(data, dict)
    article_resp = data.get("article")
    validate_article(article_resp)


@pytest.mark.order(24)
def test_create_article_no_auth():
    r = no_auth_post(
        "/articles",
        json={
            "article": {"title": "No auth", "description": "test", "body": "test"}
        },
    )
    assert r.status_code == 401


@pytest.mark.order(25)
def test_create_article_blank_title():
    r = session.post(
        "/articles",
        json={"article": {"title": "", "description": "desc", "body": "body"}},
    )
    assert r.status_code == 422


@pytest.mark.order(26)
def test_create_article_blank_description():
    r = session.post(
        "/articles",
        json={"article": {"title": "Title", "description": "", "body": "body"}},
    )
    assert r.status_code == 422


@pytest.mark.order(27)
def test_create_article_blank_body():
    r = session.post(
        "/articles",
        json={"article": {"title": "Title", "description": "desc", "body": ""}},
    )
    assert r.status_code == 422


# -------------------------
# ARTICLES - feed
# -------------------------


@pytest.mark.order(28)
def test_get_feed():
    r = session.get("/articles/feed")
    assert r.status_code == 200
    assert r.json()["articlesCount"] == 0


@pytest.mark.order(29)
def test_feed_no_auth():
    r = no_auth_get("/articles/feed")
    assert r.status_code == 401


# -------------------------
# ARTICLES - list / get
# -------------------------


@pytest.mark.order(30)
def test_get_all_articles_after_creation():
    r = session.get("/articles")
    assert r.status_code == 200, "get all articles status"
    data = r.json()
    assert isinstance(data, dict)
    articles = data.get("articles")
    assert isinstance(articles, list), "articles"
    articles_count = data.get("articlesCount")
    assert isinstance(articles_count, int), "articlesCount"
    assert articles_count == 1
    validate_article(articles[0])


@pytest.mark.order(31)
def test_get_articles_by_author_after_creation():
    r = session.get(f"/articles?author={USERNAME}")
    assert r.status_code == 200, "get all articles status"
    data = r.json()
    assert isinstance(data, dict)
    articles = data.get("articles")
    assert isinstance(articles, list), "articles"
    articles_count = data.get("articlesCount")
    assert isinstance(articles_count, int), "articlesCount"
    assert articles_count == 1
    validate_article(articles[0])


@pytest.mark.order(32)
def test_get_article_by_slug():
    r = session.get(f"/articles/{SLUG}")
    assert r.status_code == 200
    article = r.json()["article"]
    validate_article(article)


@pytest.mark.order(33)
def test_get_nonexistent_article():
    r = session.get("/articles/nonexistent-slug")
    assert r.status_code == 404


@pytest.mark.order(34)
def test_get_articles_by_tag_after_creation():
    r = session.get("/articles?tag=dragons")
    assert r.status_code == 200, "get all articles status"
    data = r.json()
    assert isinstance(data, dict)
    articles = data.get("articles")
    assert isinstance(articles, list), "articles"
    articles_count = data.get("articlesCount")
    assert isinstance(articles_count, int), "articlesCount"
    assert articles_count == 1
    validate_article(articles[0])


@pytest.mark.order(35)
def test_list_articles_with_limit():
    r = session.get("/articles?limit=5&offset=0")
    assert r.status_code == 200
    data = r.json()
    assert isinstance(data["articles"], list)
    assert len(data["articles"]) <= 5


# -------------------------
# ARTICLES - update
# -------------------------


@pytest.mark.order(36)
def test_update_article():
    r = session.put(
        f"/articles/{SLUG}",
        json={"article": {"body": "With two hands"}},
    )
    article = r.json()["article"]
    validate_article(article, body="With two hands")


@pytest.mark.order(37)
def test_update_article_no_auth():
    r = no_auth_put(
        f"/articles/{SLUG}",
        json={"article": {"body": "hacked"}},
    )
    assert r.status_code == 401


@pytest.mark.order(38)
def test_update_nonexistent_article():
    r = session.put(
        "/articles/nonexistent-slug",
        json={"article": {"body": "updated"}},
    )
    assert r.status_code == 404


# -------------------------
# FAVORITES
# -------------------------


@pytest.mark.order(39)
def test_favorite_article():
    r = session.post(f"/articles/{SLUG}/favorite")
    assert r.status_code == 200
    article = r.json()["article"]
    validate_article(article, body="With two hands", favorites_count=1)
    assert article["favorited"] is True


@pytest.mark.order(40)
def test_favorite_idempotent():
    r = session.post(f"/articles/{SLUG}/favorite")
    assert r.status_code == 200
    article = r.json()["article"]
    assert article["favoritesCount"] >= 1


@pytest.mark.order(41)
def test_unfavorite_article():
    r = session.delete(f"/articles/{SLUG}/favorite")
    assert r.status_code == 200
    article = r.json()["article"]
    assert article["favorited"] is False


@pytest.mark.order(42)
def test_unfavorite_when_not_favorited():
    r = session.delete(f"/articles/{SLUG}/favorite")
    assert r.status_code == 200


@pytest.mark.order(43)
def test_favorite_no_auth():
    r = no_auth_post(f"/articles/{SLUG}/favorite")
    assert r.status_code == 401


# -------------------------
# COMMENTS
# -------------------------


def validate_comment(comment):
    assert isinstance(comment, dict), "comment"
    expected_keys = {"id", "body", "createdAt", "updatedAt", "author"}
    assert expected_keys.issubset(comment.keys())
    assert comment["body"] == "Thank you so much!"
    assert ISO_8601_REGEX.match(comment["createdAt"])
    assert ISO_8601_REGEX.match(comment["updatedAt"])


@pytest.mark.order(44)
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


@pytest.mark.order(45)
def test_get_comments():
    r = session.get(f"/articles/{SLUG}/comments")
    assert r.status_code == 200
    data = r.json()
    assert isinstance(data, dict)
    comments = data.get("comments")
    assert isinstance(comments, list), "comments"
    validate_comment(comments[0])


@pytest.mark.order(46)
def test_create_comment_blank_body():
    r = session.post(
        f"/articles/{SLUG}/comments",
        json={"comment": {"body": ""}},
    )
    assert r.status_code == 422


@pytest.mark.order(47)
def test_create_comment_no_auth():
    r = no_auth_post(
        f"/articles/{SLUG}/comments",
        json={"comment": {"body": "No auth comment"}},
    )
    assert r.status_code == 401


@pytest.mark.order(48)
def test_delete_comment_no_auth():
    r = no_auth_delete(f"/articles/{SLUG}/comments/{comment_id}")
    assert r.status_code == 401


@pytest.mark.order(49)
def test_delete_comment():
    r = session.delete(f"/articles/{SLUG}/comments/{comment_id}")
    assert r.status_code == 200


# -------------------------
# ARTICLES - delete
# -------------------------


@pytest.mark.order(50)
def test_delete_article_no_auth():
    r = no_auth_delete(f"/articles/{SLUG}")
    assert r.status_code == 401


@pytest.mark.order(51)
def test_delete_article():
    r = session.delete(f"/articles/{SLUG}")
    assert r.status_code == 200


@pytest.mark.order(52)
def test_deleted_article_not_found():
    r = session.get(f"/articles/{SLUG}")
    assert r.status_code == 404


# -------------------------
# PROFILES
# -------------------------


def validate_user(user):
    assert isinstance(user, dict), "user"
    expected_keys = {"email", "username", "bio", "image", "token"}
    assert expected_keys.issubset(user.keys())
    assert user["email"] == f"celeb_{EMAIL}"
    assert user["username"] == f"celeb_{USERNAME}"
    assert user["bio"] is None
    assert user["image"] is None
    assert user["token"] is not None


def validate_profile(profile, following=False):
    assert isinstance(profile, dict), "profile"
    expected_keys = {"username", "bio", "image", "following"}
    assert expected_keys.issubset(profile.keys())
    assert profile["username"] == f"celeb_{USERNAME}"
    assert profile["bio"] is None
    assert profile["image"] is None
    assert profile["following"] is following


@pytest.mark.order(53)
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
    assert r.status_code == 201
    user = r.json()["user"]
    validate_user(user)


@pytest.mark.order(54)
def test_get_celeb_profile():
    r = session.get(f"/profiles/celeb_{USERNAME}")
    assert r.status_code == 200
    profile = r.json()["profile"]
    validate_profile(profile)


@pytest.mark.order(55)
def test_get_celeb_profile_unauthenticated():
    r = no_auth_get(f"/profiles/celeb_{USERNAME}")
    assert r.status_code == 200
    profile = r.json()["profile"]
    assert profile["username"] == f"celeb_{USERNAME}"
    assert profile["following"] is False


@pytest.mark.order(56)
def test_get_nonexistent_profile():
    r = session.get("/profiles/nonexistent")
    assert r.status_code == 404


@pytest.mark.order(57)
def test_follow_celeb_profile():
    r = session.post(f"/profiles/celeb_{USERNAME}/follow")
    assert r.status_code == 200
    profile = r.json()["profile"]
    validate_profile(profile, following=True)


@pytest.mark.order(58)
def test_unfollow_celeb_profile():
    r = session.delete(f"/profiles/celeb_{USERNAME}/follow")
    assert r.status_code == 200
    profile = r.json()["profile"]
    validate_profile(profile)


@pytest.mark.order(59)
def test_follow_no_auth():
    r = no_auth_post(f"/profiles/celeb_{USERNAME}/follow")
    assert r.status_code == 401


@pytest.mark.order(60)
def test_unfollow_no_auth():
    r = no_auth_delete(f"/profiles/celeb_{USERNAME}/follow")
    assert r.status_code == 401


# -------------------------
# TAGS
# -------------------------


@pytest.mark.order(61)
def test_get_tags():
    r = session.get("/tags")
    assert r.status_code == 200
    tags = r.json()["tags"]
    assert set(tags) == {"training", "dragons"}


@pytest.mark.order(62)
def test_get_tags_no_auth():
    r = no_auth_get("/tags")
    assert r.status_code == 200
    tags = r.json()["tags"]
    assert isinstance(tags, list)


if __name__ == "__main__":
    pytest.main(["-v", "smoke.py"])
