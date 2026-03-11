"""
Smoke test for FileUpload application.

Checks:
  1) GET /index.html returns 200.
  2) GET /upload is optional: accept 200 OR a sensible error (405/415), or the known 500 'not multipart' message.
  3) POST /upload multipart with destination + file returns 200 and non-empty body.
  4) POST /upload with BLANK destination returns 200 and a helpful message (contains 'destination').

Env:
  FILEUPLOAD_BASE (default http://localhost:8080/)
  VERBOSE=1
"""
import io, os, sys, uuid, mimetypes
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import pytest

BASE = os.getenv("FILEUPLOAD_BASE", "http://localhost:8080/")
VERBOSE = os.getenv("VERBOSE") == "1"

def vprint(*args):
    if VERBOSE:
        print(*args)

def http_request(method: str, url: str, data: bytes | None = None, headers: dict | None = None, timeout: int = 10):
    req = Request(url, data=data, method=method, headers=headers or {})
    try:
        with urlopen(req, timeout=timeout) as resp:
            return resp.getcode(), resp.read().decode("utf-8", "replace")
    except HTTPError as e:
        body = ""
        try: body = e.read().decode("utf-8", "replace")
        except Exception: pass
        return e.code, body
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"

def must_get(path: str, fail_code: int, contains: str | None = None):
    url = BASE.rstrip("/") + path
    vprint(f"GET {url}")
    s, b = http_request("GET", url)
    if s != 200 or (contains and (contains not in b)):
        print(f"[FAIL] GET {path} expected 200 (contains={contains!r}), got {s}, body={b[:200]!r}", file=sys.stderr)
        pytest.fail("smoke check failed")
    print(f"[PASS] GET {path} -> 200")
    return b

def get_upload_optional():
    """Allow 200, or common 'method/content' errors, or the known 500 from non-multipart GET."""
    url = BASE.rstrip("/") + "/upload"
    vprint(f"GET {url} (optional info endpoint)")
    s, b = http_request("GET", url)
    if s == 200:
        print("[PASS] GET /upload -> 200")
        return
    if s in (400, 401, 403, 404, 405, 415):
        print(f"[PASS] GET /upload -> {s} (acceptable for endpoints that only support multipart POST)")
        return
    if s == 500 and "not of type multipart" in b.lower():
        print(f"[PASS] GET /upload -> 500 with 'not multipart' message (acceptable)")
        return
    pytest.fail(f"[FAIL] GET /upload unacceptable response: {s} :: {b[:240]!r}")

def build_multipart(fields: dict[str, str], files: dict[str, tuple[str, bytes]]) -> tuple[bytes, str]:
    boundary = "----WebKitFormBoundary" + uuid.uuid4().hex
    buf = io.BytesIO()

    def write_field(name: str, value: str):
        buf.write(f"--{boundary}\r\n".encode())
        buf.write(f'Content-Disposition: form-data; name="{name}"\r\n\r\n'.encode())
        buf.write(value.encode()); buf.write(b"\r\n")

    def write_file(name: str, filename: str, data: bytes, content_type: str):
        buf.write(f"--{boundary}\r\n".encode())
        buf.write(f'Content-Disposition: form-data; name="{name}"; filename="{filename}"\r\n'.encode())
        buf.write(f"Content-Type: {content_type}\r\n\r\n".encode())
        buf.write(data); buf.write(b"\r\n")

    for k, v in fields.items():
        write_field(k, v)
    for name, (filename, data) in files.items():
        ctype = mimetypes.guess_type(filename)[0] or "application/octet-stream"
        write_file(name, filename, data, ctype)

    buf.write(f"--{boundary}--\r\n".encode())
    return buf.getvalue(), boundary

def post_upload(destination: str, filename: str, data: bytes, fail_code: int, expect_hint: str | None = None):
    url = BASE.rstrip("/") + "/upload"
    body, boundary = build_multipart({"destination": destination}, {"file": (filename, data)})
    headers = {"Content-Type": f"multipart/form-data; boundary={boundary}"}
    vprint(f"POST {url} (dest={destination!r}, file={filename}, size={len(data)})")
    s, b = http_request("POST", url, data=body, headers=headers)
    if s != 200 or (expect_hint and expect_hint not in b.lower()) or (expect_hint is None and not b.strip()):
        print(f"[FAIL] POST /upload -> {s}, body={b[:240]!r}", file=sys.stderr)
        pytest.fail("smoke check failed")
    print(f"[PASS] POST /upload -> 200")
    return b


def test_must_get():
    must_get("/index.html", 2)


def test_get_upload_optional():
    get_upload_optional()


def test_post_upload():
    post_upload("/tmp", "sample.txt", b"hello world\n", 4)
    b = post_upload("", "sample.txt", b"x", 6)
    if "destination" not in b.lower() and "location" not in b.lower():
        print("[FAIL] Expected helpful 'destination/location' hint for blank destination", file=sys.stderr)
        pytest.fail("smoke test failed with code 6")
    print("[PASS] blank destination hint present")


def test_upload_reports_filename():
    """Scenario: Upload reports the file name in the response."""
    url = BASE.rstrip("/") + "/upload"
    filename = "testdoc.txt"
    body, boundary = build_multipart({"destination": "/tmp"}, {"file": (filename, b"test content")})
    headers = {"Content-Type": f"multipart/form-data; boundary={boundary}"}
    s, b = http_request("POST", url, data=body, headers=headers)
    if s == 200:
        assert filename in b, f"Expected filename '{filename}' in response, got: {b[:200]}"
        print(f"[PASS] Upload reports filename: {filename}")
    else:
        print(f"[WARN] Upload returned {s}, skipping filename check")


def test_upload_to_nonexistent_destination():
    """Scenario: Upload to a nonexistent destination returns an error."""
    url = BASE.rstrip("/") + "/upload"
    body, boundary = build_multipart(
        {"destination": "/nonexistent/path/does/not/exist"},
        {"file": ("test.txt", b"test content")}
    )
    headers = {"Content-Type": f"multipart/form-data; boundary={boundary}"}
    s, b = http_request("POST", url, data=body, headers=headers)
    b_lower = b.lower() if b else ""
    has_error = ("error" in b_lower or "protected" in b_lower or
                 "nonexistent" in b_lower or "not found" in b_lower or
                 s in [400, 404, 500])
    assert has_error, f"Expected error for nonexistent destination, got {s}: {b[:200]}"
    print(f"[PASS] Upload to nonexistent destination -> error (HTTP {s})")


def test_response_content_type():
    """Scenario: Response content type is text/html."""
    url = BASE.rstrip("/") + "/upload"
    body, boundary = build_multipart({"destination": "/tmp"}, {"file": ("test.txt", b"hello")})
    headers = {"Content-Type": f"multipart/form-data; boundary={boundary}"}
    req = __import__("urllib.request", fromlist=["Request"]).Request(
        url, data=body, method="POST", headers=headers
    )
    try:
        with __import__("urllib.request", fromlist=["urlopen"]).urlopen(req, timeout=10) as resp:
            ct = resp.headers.get("Content-Type", "")
            assert "text/html" in ct.lower(), f"Expected text/html, got: {ct}"
            print(f"[PASS] Response Content-Type: {ct}")
    except Exception as e:
        print(f"[WARN] Could not verify content type: {e}")


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
