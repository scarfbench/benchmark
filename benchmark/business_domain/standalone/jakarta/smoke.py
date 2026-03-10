#!/usr/bin/env python3
"""Smoke tests for standalone-jakarta."""

import json
import os
import sys
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen

import pytest

GREET_PATH = "/greet"
BASE_URL = os.getenv("BASE_URL", "http://localhost:9080/standalone")


def http_request(method: str, url: str, timeout: int = 10):
    request = Request(url, method=method, headers={"Accept": "application/json"})
    try:
        with urlopen(request, timeout=timeout) as response:
            return (
                response.getcode(),
                response.read().decode("utf-8", "replace"),
                response.headers,
            ), None
    except HTTPError as error:
        return (
            error.code,
            error.read().decode("utf-8", "replace"),
            error.headers,
        ), None
    except (URLError, Exception) as error:
        return None, f"NETWORK-ERROR: {error}"


def get_greet():
    url = BASE_URL.rstrip("/") + GREET_PATH
    response, error = http_request("GET", url)
    assert error is None, error
    status, body, headers = response
    return status, body, headers, json.loads(body)


def test_greet_returns_http_200():
    status, _, _, _ = get_greet()
    assert status == 200


def test_multiple_requests_all_return_http_200():
    statuses = [get_greet()[0] for _ in range(3)]
    assert statuses == [200, 200, 200]


def test_response_body_is_non_empty():
    _, body, _, _ = get_greet()
    assert body.strip()


def test_response_body_is_valid_json():
    _, _, _, payload = get_greet()
    assert isinstance(payload, dict)


def test_json_contains_message_field():
    _, _, _, payload = get_greet()
    assert "message" in payload


def test_message_equals_greetings():
    _, _, _, payload = get_greet()
    assert payload["message"] == "Greetings!"


def test_message_is_string():
    _, _, _, payload = get_greet()
    assert isinstance(payload["message"], str)


def test_repeated_calls_always_return_greetings():
    messages = [get_greet()[3]["message"] for _ in range(5)]
    assert all(message == "Greetings!" for message in messages)


def test_content_type_indicates_json():
    _, _, headers, _ = get_greet()
    assert "json" in headers.get("Content-Type", "").lower()


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
