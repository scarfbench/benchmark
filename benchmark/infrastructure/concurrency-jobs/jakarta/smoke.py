#!/usr/bin/env python3
"""Smoke test for Jakarta EE jobs application on Open Liberty.

Checks:
  1) Discover reachable JobService base path.
  2) GET <base>/token returns 200 and a token starting with '123X5-'.
  3) POST <base>/process?jobID=1 with header X-REST-API-Key submits job (HTTP 200, contains 'successfully submitted').
  4) POST <base>/process?jobID=2 WITHOUT header also succeeds (HTTP 200, contains 'successfully submitted').

Exit codes:
  0 success, non-zero on first failure encountered.
"""
import os
import re
import sys
import time
import pytest
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

TOKEN_PATH = "/token"
PROCESS_PATH = "/process"
API_HEADER = "X-REST-API-Key"
VERBOSE = os.getenv("VERBOSE") == "1"

CANDIDATES = [
    os.getenv("JOBS_BASE_URL"),
    "http://localhost:9080/jobs/webapi/JobService/",
    "http://localhost:10011/jobs/webapi/JobService/"
]


def vprint(msg: str):
    if VERBOSE:
        print(msg)


def http_request(
    method: str,
    url: str,
    data: bytes | None = None,
    headers: dict | None = None,
    timeout: int = 10,
):
    req = Request(url, data=data, method=method, headers=headers or {})
    try:
        with urlopen(req, timeout=timeout) as resp:
            status = resp.getcode()
            body = resp.read().decode("utf-8", "replace")
    except HTTPError as e:
        status = e.code
        body = e.read().decode("utf-8", "replace")
    except (URLError, Exception) as e:  # network failure
        return None, f"NETWORK-ERROR: {e}"
    return (status, body), None


def try_get_token(base: str):
    url = base.rstrip("/") + TOKEN_PATH
    vprint(f"Attempt GET {url}")
    resp, err = http_request("GET", url)
    if err:
        vprint(f"Fail: {err}")
        return None
    status, body = resp
    if status != 200:
        vprint(f"Unexpected status {status}")
        return None
    token = body.strip()
    if not token.startswith("123X5-"):
        vprint(f"Token format mismatch: {token}")
        return None
    return token


def discover_base() -> str:
    for cand in CANDIDATES:
        if not cand:
            continue
        token = try_get_token(cand)
        if token:
            print(f"[INFO] Base discovered: {cand}")
            return cand
    # fallback to first non-empty candidate even if no token
    for cand in CANDIDATES:
        if cand:
            print(f"[WARN] No base validated, using fallback {cand}")
            return cand
    print("[ERROR] No base URL candidates available", file=sys.stderr)
    pytest.fail("No base URL candidates available")


def assert_token(base: str):
    token = try_get_token(base)
    if not token:
        print("[FAIL] Could not obtain token", file=sys.stderr)
        pytest.fail("Could not obtain token")
    print(f"[PASS] GET token -> {token}")
    return token


def submit_job(base: str, job_id: int, token: str | None):
    url = f"{base.rstrip('/')}{PROCESS_PATH}?jobID={job_id}"
    headers = {"Content-Type": "text/plain"}
    label = "auth" if token else "no-auth"
    if token:
        headers[API_HEADER] = token
    resp, err = http_request("POST", url, data=b"", headers=headers)
    if err:
        print(f"[FAIL] POST {label} {url}: {err}", file=sys.stderr)
        pytest.fail(f"POST {label} {url}: {err}")
    status, body = resp
    body_stripped = body.strip()
    if status != 200 or "successfully submitted" not in body_stripped:
        print(
            f"[FAIL] POST {label} status/body mismatch: {status} :: {body_stripped}",
            file=sys.stderr,
        )
        pytest.fail(f"POST {label} status/body mismatch: {status} :: {body_stripped}")
    print(f"[PASS] POST {label} job {job_id} -> {status}")


@pytest.fixture(scope="module")
def base_url():
    return discover_base()


def test_token(base_url):
    token = try_get_token(base_url)
    assert token is not None, "Could not obtain token"
    assert token.startswith("123X5-"), f"Token format mismatch: {token}"
    print(f"[PASS] GET token -> {token}")


def test_submit_job_with_auth(base_url):
    token = try_get_token(base_url)
    assert token is not None, "Could not obtain token for auth submit"
    submit_job(base_url, 1, token)


def test_submit_job_no_auth(base_url):
    submit_job(base_url, 2, None)


def test_tokens_are_unique(base_url):
    """Each token request should return a different token."""
    token1 = try_get_token(base_url)
    token2 = try_get_token(base_url)
    assert token1 is not None, "First token request failed"
    assert token2 is not None, "Second token request failed"
    assert token1 != token2, f"Tokens should be unique, got '{token1}' twice"


def test_submit_with_invalid_token(base_url):
    """Invalid token should still allow job submission (falls to low priority)."""
    url = f"{base_url.rstrip('/')}{PROCESS_PATH}?jobID=3"
    headers = {"Content-Type": "text/plain", API_HEADER: "invalid-token"}
    resp, err = http_request("POST", url, data=b"", headers=headers)
    assert err is None, f"Request failed: {err}"
    status, body = resp
    assert status == 200
    assert "successfully submitted" in body.strip()


def test_job_response_contains_job_id(base_url):
    """Response body should reference the submitted job ID."""
    url = f"{base_url.rstrip('/')}{PROCESS_PATH}?jobID=42"
    headers = {"Content-Type": "text/plain"}
    resp, err = http_request("POST", url, data=b"", headers=headers)
    assert err is None, f"Request failed: {err}"
    status, body = resp
    assert status == 200
    assert "42" in body.strip(), f"Expected job ID 42 in response: {body.strip()}"


def test_token_has_content_after_prefix(base_url):
    """Token should have meaningful content after the '123X5-' prefix."""
    token = try_get_token(base_url)
    assert token is not None
    assert len(token) > len("123X5-"), "Token should have content after prefix"


def test_submit_multiple_sequential_jobs(base_url):
    """Multiple jobs can be submitted sequentially."""
    for job_id in [10, 11, 12]:
        url = f"{base_url.rstrip('/')}{PROCESS_PATH}?jobID={job_id}"
        headers = {"Content-Type": "text/plain"}
        resp, err = http_request("POST", url, data=b"", headers=headers)
        assert err is None, f"Job {job_id} request failed: {err}"
        status, body = resp
        assert status == 200
        assert "successfully submitted" in body.strip()


def main():
    start = time.time()
    base = discover_base()
    token = assert_token(base)
    submit_job(base, 1, token)
    submit_job(base, 2, None)
    elapsed = time.time() - start
    print(f"[PASS] Smoke sequence complete in {elapsed:.2f}s")
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
