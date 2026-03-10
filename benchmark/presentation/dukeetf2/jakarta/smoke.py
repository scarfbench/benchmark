"""
Smoke test for DukeETF2 (WebSocket) w/ stdlib only.

Checks:
  1) GET <base>/index.html -> 200
  2) (Soft) GET <base>/resources/css/default.css -> 200 or WARN
  3) WebSocket <ws-base>/dukeetf sends changing frames 5s apart.

Env:
  DUKEETF_BASE (default: http://localhost:9080/dukeetf2-10-SNAPSHOT)
  VERBOSE=1
Exit codes:
  0 ok, 2 index.html fail, 5 WS fail, 9 network/unexpected
"""
import os, sys, time, re, base64, hashlib, socket, ssl
from urllib.parse import urlparse
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import pytest

BASE = os.getenv("DUKEETF_BASE", "http://localhost:9080/dukeetf2-10-SNAPSHOT").rstrip("/")
VERBOSE = os.getenv("VERBOSE") == "1"
HTTP_TIMEOUT = 10
WS_TIMEOUT = 12
SLEEP_SECS = 5.0
_NUM_RE = re.compile(r"\s*(-?\d+(?:\.\d+)?)\s*/\s*(-?\d+)\s*")

def vprint(*args):
    if VERBOSE: print(*args)

def http_request(method: str, url: str, timeout: int = HTTP_TIMEOUT):
    req = Request(url, method=method, headers={})
    try:
        with urlopen(req, timeout=timeout) as resp:
            return (resp.getcode(), resp.read().decode("utf-8","replace")), None
    except HTTPError as e:
        try: body = e.read().decode("utf-8","replace")
        except Exception: body = ""
        return (e.code, body), None
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"

def join(base: str, path: str) -> str:
    if not path: return base
    if base.endswith("/") and path.startswith("/"): return base[:-1] + path
    if (not base.endswith("/")) and (not path.startswith("/")): return base + "/" + path
    return base + path

def must_get_ok(path: str, fail_code: int):
    url = join(BASE, path); vprint("GET", url)
    resp, err = http_request("GET", url)
    if err: pytest.fail(f"[FAIL] {path} -> {err}")
    if resp[0] != 200: pytest.fail(f"[FAIL] GET {path} -> {resp[0]}")
    print(f"[PASS] GET {path} -> 200")

def soft_get_ok(path: str):
    url = join(BASE, path); vprint("GET", url, "(soft)")
    resp, err = http_request("GET", url)
    if err: print(f"[WARN] {path} -> {err}", file=sys.stderr); return
    print(f"[{'PASS' if resp[0]==200 else 'WARN'}] GET {path} -> {resp[0]}")

GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

def http_to_ws_url(http_base: str) -> str:
    ws = http_base.replace("https://","wss://").replace("http://","ws://").rstrip("/")
    return ws if ws.endswith("/dukeetf") else ws + "/dukeetf"

def _ws_connect(url: str, timeout: int = WS_TIMEOUT):
    """Return a connected socket after RFC6455 handshake (text frames only)."""
    pu = urlparse(url)
    scheme, host = pu.scheme, pu.hostname
    port = pu.port or (443 if scheme=="wss" else 80)
    path = pu.path or "/"
    if pu.query: path += "?" + pu.query
    raw = socket.create_connection((host, port), timeout=timeout)
    if scheme == "wss":
        ctx = ssl.create_default_context()
        raw = ctx.wrap_socket(raw, server_hostname=host)
    key = base64.b64encode(os.urandom(16)).decode()
    headers = [
        f"GET {path} HTTP/1.1",
        f"Host: {host}:{port}",
        "Upgrade: websocket",
        "Connection: Upgrade",
        f"Sec-WebSocket-Key: {key}",
        "Sec-WebSocket-Version: 13",
        "", ""
    ]
    raw.sendall("\r\n".join(headers).encode())
    raw.settimeout(timeout)
    resp = b""
    while b"\r\n\r\n" not in resp:
        chunk = raw.recv(4096)
        if not chunk: break
        resp += chunk
    header, leftover = resp.split(b"\r\n\r\n", 1)
    if b" 101 " not in header:
        raise RuntimeError(f"WS handshake failed: {header.decode(errors='replace')}")
    return raw, leftover  

def _ws_recv_text(sock, leftover=b"", timeout: int = WS_TIMEOUT) -> str:
    """Read a single unfragmented text frame."""
    sock.settimeout(timeout)
    buf = bytearray(leftover)
    def need(n):
        while len(buf) < n:
            chunk = sock.recv(4096)
            if not chunk: raise RuntimeError("WS closed while reading")
            buf.extend(chunk)

    need(2)
    b1, b2 = buf[0], buf[1]
    fin = (b1 & 0x80) != 0
    opcode = b1 & 0x0F
    masked = (b2 & 0x80) != 0
    length = (b2 & 0x7F)
    idx = 2
    if length == 126:
        need(idx+2); length = int.from_bytes(buf[idx:idx+2], "big"); idx += 2
    elif length == 127:
        need(idx+8); length = int.from_bytes(buf[idx:idx+8], "big"); idx += 8
    if masked:
        need(idx+4); mask = buf[idx:idx+4]; idx += 4
    else:
        mask = None
    need(idx+length)
    payload = bytes(buf[idx:idx+length])
    del buf[:idx+length]  
    if mask: payload = bytes(b ^ mask[i%4] for i, b in enumerate(payload))
    if opcode == 0x8:  
        raise RuntimeError("WS closed by server")
    if opcode != 0x1 or not fin:
        raise RuntimeError(f"Unsupported WS frame (opcode={opcode}, fin={fin})")
    return payload.decode("utf-8", "replace"), bytes(buf)

def parse_price_volume(msg: str):
    m = _NUM_RE.search(msg or "")
    if not m: return None
    try: return float(m.group(1)), int(m.group(2))
    except Exception: return None

def assert_ws_changes_stdlib():
    ws_url = http_to_ws_url(BASE)
    vprint("WS connect ->", ws_url)
    try:
        sock, leftover = _ws_connect(ws_url, timeout=WS_TIMEOUT)
    except Exception as e:
        pytest.fail(f"[FAIL] WS connect -> {e}")
    try:
        msg1, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
        vprint("WS recv#1:", repr(msg1))
        pv1 = parse_price_volume(msg1)
        if not pv1:
            pytest.fail(f"[FAIL] WS frame not parseable: {msg1!r}")
        time.sleep(SLEEP_SECS)
        msg2, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
        vprint("WS recv#2:", repr(msg2))
        pv2 = parse_price_volume(msg2)
        if not pv2:
            pytest.fail(f"[FAIL] WS frame not parseable: {msg2!r}")
        if pv1 != pv2:
            print(f"[PASS] WS changes over {SLEEP_SECS:.0f}s: {pv1[0]:.2f}/{pv1[1]} -> {pv2[0]:.2f}/{pv2[1]}")
            return
        pytest.fail(f"[FAIL] WS values unchanged after {SLEEP_SECS:.0f}s: {pv1[0]:.2f}/{pv1[1]}")
    finally:
        try: sock.close()
        except Exception: pass

def _run_smoke():
    must_get_ok("/index.html", 2)
    soft_get_ok("/resources/css/default.css")
    assert_ws_changes_stdlib()
    print("[PASS] Smoke sequence complete"); return 0


def test_smoke():
    rc = _run_smoke()
    assert rc == 0, f"Smoke test failed with return code {rc}"


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
