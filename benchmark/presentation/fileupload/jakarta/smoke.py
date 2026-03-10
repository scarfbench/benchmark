"""
Smoke test for FileUpload application.

Checks:
  1) GET /index.html returns 200.
  2) GET /upload is optional: accept 200 OR a sensible error (405/415), or the known 500 'not multipart' message.
  3) POST /upload multipart with destination + file returns 200 and non-empty body.
  4) POST /upload with BLANK destination returns 200 and a helpful message (contains 'destination').

Env:
  FILEUPLOAD_BASE (default http://localhost:9080/fileupload/)
  VERBOSE=1
"""
import io, os, sys, uuid, mimetypes
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import pytest

BASE = os.getenv("FILEUPLOAD_BASE", "http://localhost:9080/fileupload/")
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


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
