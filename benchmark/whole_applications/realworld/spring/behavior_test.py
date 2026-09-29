#!/usr/bin/env python3
"""Shared HTTP behavior contract for the three RealWorld references."""

import json
import datetime
import os
import time
import urllib.error
import urllib.parse
import urllib.request
import uuid


configured_origin = os.environ.get("APP_ORIGIN") or os.environ.get("BASE_URL") or "http://localhost:8080"
parsed_origin = urllib.parse.urlsplit(configured_origin)
ORIGIN = parsed_origin.scheme + "://" + parsed_origin.netloc
API = ORIGIN + "/api"
NONCE = uuid.uuid4().hex[:12]


def request(method, path, payload=None, token=None):
    headers = {}
    data = None
    if payload is not None:
        data = json.dumps(payload).encode("utf-8")
        headers["Content-Type"] = "application/json"
    if token:
        headers["Authorization"] = "Token " + token
    req = urllib.request.Request(API + path, data=data, headers=headers, method=method)
    try:
        response = urllib.request.urlopen(req, timeout=10)
    except urllib.error.HTTPError as error:
        response = error
    with response:
        body = response.read()
        if not body:
            return response.status, {}
        try:
            return response.status, json.loads(body)
        except ValueError:
            return response.status, {"raw": body.decode("utf-8", "replace")[:200]}


def check(method, path, payload=None, token=None, statuses=(200,)):
    status, body = request(method, path, payload, token)
    assert status in statuses, f"{method} {path}: HTTP {status}, {body}"
    return body


def denied(method, path, payload=None, token=None):
    status, body = request(method, path, payload, token)
    assert status in (401, 403, 404), f"{method} {path}: expected access denial, got {status}, {body}"


def slugs(body):
    return [article["slug"] for article in body["articles"]]


def main():
    for attempt in range(90):
        try:
            status, body = request("GET", "/tags")
            if status == 200 and isinstance(body.get("tags"), list):
                break
        except (OSError, ValueError):
            pass
        time.sleep(2)
    else:
        raise AssertionError("RealWorld API did not become ready at " + API)

    alice = "acbalice" + NONCE
    bob = "acbbob" + NONCE
    password = "ContractPassword123!"
    tokens = {}
    for username in (alice, bob):
        user = check("POST", "/users", {"user": {
            "username": username, "email": username + "@example.test", "password": password
        }}, statuses=(200, 201))["user"]
        assert user["username"] == username and user["token"]
        tokens[username] = user["token"]
    a, b = tokens[alice], tokens[bob]
    check("POST", "/users/login", {"user": {
        "email": alice + "@example.test", "password": password
    }})
    denied("POST", "/users/login", {"user": {
        "email": alice + "@example.test", "password": "wrong"
    }})
    assert check("GET", "/user", token=a)["user"]["username"] == alice
    denied("GET", "/user")
    updated = check("PUT", "/user", {"user": {"bio": "Contract author"}}, a)["user"]
    assert updated["bio"] == "Contract author"
    assert check("GET", "/user", token=a)["user"]["bio"] == "Contract author"
    status, _ = request("POST", "/articles", {"article": {
        "title": "Invalid " + NONCE, "description": "Missing body", "tagList": []
    }}, a)
    assert status == 422, f"invalid article should return HTTP 422, got {status}"

    titles = ["Contract " + NONCE + " " + str(i) for i in range(3)]
    tag = "contract" + NONCE
    articles = []
    for title in titles:
        article = check("POST", "/articles", {"article": {
            "title": title, "description": "Initial description",
            "body": "A useful article body", "tagList": [tag]
        }}, a, statuses=(200, 201))["article"]
        assert article["title"] == title and article["author"]["username"] == alice
        assert article["body"] == "A useful article body" and tag in article["tagList"]
        articles.append(article["slug"])
    primary = articles[0]
    assert check("GET", "/articles/" + primary)["article"]["title"] == titles[0]
    updated = check("PUT", "/articles/" + primary, {"article": {
        "description": "Updated description"
    }}, a)["article"]
    assert updated["description"] == "Updated description"
    assert check("GET", "/articles/" + primary)["article"]["description"] == "Updated description"
    old_slug = articles[2]
    new_title = "Retitled Contract " + NONCE
    retitled = check("PUT", "/articles/" + old_slug, {"article": {
        "title": new_title
    }}, a)["article"]
    assert retitled["title"] == new_title and retitled["slug"] != old_slug
    assert check("GET", "/articles/" + retitled["slug"])["article"]["title"] == new_title
    assert request("GET", "/articles/" + old_slug)[0] == 404
    articles[2] = retitled["slug"]
    denied("POST", "/articles", {"article": {
        "title": "No token", "description": "No token", "body": "No token", "tagList": []
    }})
    denied("PUT", "/articles/" + primary, {"article": {"body": "Unauthorized"}}, b)
    denied("DELETE", "/articles/" + primary, token=b)
    assert check("GET", "/articles/" + primary)["article"]["body"] == "A useful article body"

    author_query = "/articles?author=" + urllib.parse.quote(alice)
    author_list = check("GET", author_query)
    assert set(articles) <= set(slugs(author_list))
    assert author_list["articlesCount"] >= 3
    created_at = [datetime.datetime.fromisoformat(item["createdAt"].replace("Z", "+00:00"))
                  for item in author_list["articles"]]
    assert created_at == sorted(created_at, reverse=True), "articles are not newest first"
    tag_list = check("GET", "/articles?tag=" + tag)
    assert set(articles) <= set(slugs(tag_list))
    assert tag in check("GET", "/tags")["tags"]
    page0 = check("GET", author_query + "&limit=1&offset=0")
    page1 = check("GET", author_query + "&limit=1&offset=1")
    assert len(slugs(page0)) == len(slugs(page1)) == 1
    assert slugs(page0)[0] != slugs(page1)[0]
    assert page0["articlesCount"] >= 3 and page1["articlesCount"] >= 3
    page2 = check("GET", author_query + "&limit=2&offset=1")
    assert len(slugs(page2)) == 2 and slugs(page2)[0] == slugs(page1)[0]

    assert check("POST", "/profiles/" + alice + "/follow", token=b)["profile"]["following"]
    assert primary in slugs(check("GET", "/articles/feed", token=b))
    favored = check("POST", "/articles/" + primary + "/favorite", token=b)["article"]
    assert favored["favorited"] and favored["favoritesCount"] >= 1
    assert primary in slugs(check("GET", "/articles?favorited=" + bob))
    comment = check("POST", "/articles/" + primary + "/comments", {
        "comment": {"body": "A real comment from " + bob}
    }, b, statuses=(200, 201))["comment"]
    comment_id = str(comment["id"])
    assert comment["author"]["username"] == bob
    comments_path = "/articles/" + primary + "/comments"
    comments = check("GET", comments_path)["comments"]
    assert any(str(item["id"]) == comment_id and item["body"] == comment["body"] for item in comments)
    denied("DELETE", comments_path + "/" + comment_id, token=a)
    assert any(str(item["id"]) == comment_id for item in check("GET", comments_path)["comments"])
    check("DELETE", comments_path + "/" + comment_id, token=b, statuses=(200, 204))
    assert all(str(item["id"]) != comment_id for item in check("GET", comments_path)["comments"])
    unfavored = check("DELETE", "/articles/" + primary + "/favorite", token=b)["article"]
    assert not unfavored["favorited"]
    assert primary not in slugs(check("GET", "/articles?favorited=" + bob))
    assert not check("DELETE", "/profiles/" + alice + "/follow", token=b)["profile"]["following"]
    assert primary not in slugs(check("GET", "/articles/feed", token=b))

    for slug in articles:
        check("DELETE", "/articles/" + slug, token=a, statuses=(200, 204))
        status, _ = request("GET", "/articles/" + slug)
        assert status == 404, f"deleted article {slug} returned HTTP {status}"
    print("PASS: RealWorld shared behavior contract")


if __name__ == "__main__":
    main()
