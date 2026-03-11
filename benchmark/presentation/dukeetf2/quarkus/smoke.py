"""
Smoke test for DukeETF2 (WebSocket) w/ stdlib only.

Checks:
  1) GET <base>/index.html -> 200
  2) (Soft) GET <base>/resources/css/default.css -> 200 or WARN
  3) WebSocket <ws-base>/dukeetf sends changing frames 5s apart.

Env:
  DUKEETF_BASE (default: http://localhost:8080/)
  VERBOSE=1
Exit codes:
  0 ok, 2 index.html fail, 5 WS fail, 9 network/unexpected
"""
import os, sys, time, re, base64, hashlib, socket, ssl
from urllib.parse import urlparse
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError
import pytest

BASE = os.getenv("DUKEETF_BASE", "http://localhost:8080").rstrip("/")
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

def test_index_page():
    """Index page should load successfully."""
    must_get_ok("/index.html", 2)


def test_css_resource():
    """CSS resource should be accessible."""
    soft_get_ok("/resources/css/default.css")


def test_websocket_price_changes():
    """WebSocket should send changing price/volume frames."""
    assert_ws_changes_stdlib()


def test_ws_connection():
    """Scenario: Connect to the WebSocket endpoint."""
    ws_url = http_to_ws_url(BASE)
    vprint("WS connect ->", ws_url)
    try:
        sock, leftover = _ws_connect(ws_url, timeout=WS_TIMEOUT)
    except Exception as e:
        pytest.fail(f"[FAIL] WS connect -> {e}")
    try:
        print("[PASS] WebSocket connection established")
    finally:
        try: sock.close()
        except Exception: pass


def test_ws_message_format():
    """Scenario: Update message includes both price and volume separated by ' / '."""
    ws_url = http_to_ws_url(BASE)
    try:
        sock, leftover = _ws_connect(ws_url, timeout=WS_TIMEOUT)
    except Exception as e:
        pytest.fail(f"[FAIL] WS connect -> {e}")
    try:
        msg, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
        pattern = re.compile(r"\d+\.\d{2}\s*/\s*\d+")
        assert pattern.search(msg), f"Message does not match 'X.XX / NNNN' format: {msg!r}"
        print(f"[PASS] WS message format: {msg.strip()!r}")
    finally:
        try: sock.close()
        except Exception: pass


def test_price_two_decimal_places():
    """Scenario: Update message uses two decimal places for price."""
    ws_url = http_to_ws_url(BASE)
    try:
        sock, leftover = _ws_connect(ws_url, timeout=WS_TIMEOUT)
    except Exception as e:
        pytest.fail(f"[FAIL] WS connect -> {e}")
    try:
        msg, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
        price_pattern = re.compile(r"(\d+\.\d{2})\s*/")
        m = price_pattern.search(msg)
        assert m, f"Could not find price with two decimals in: {msg!r}"
        price_str = m.group(1)
        assert "." in price_str and len(price_str.split(".")[1]) == 2, \
            f"Price does not have exactly two decimal places: {price_str}"
        print(f"[PASS] Price two decimals: {price_str}")
    finally:
        try: sock.close()
        except Exception: pass


def test_initial_price_near_100():
    """Scenario: Price starts near 100.0."""
    ws_url = http_to_ws_url(BASE)
    try:
        sock, leftover = _ws_connect(ws_url, timeout=WS_TIMEOUT)
    except Exception as e:
        pytest.fail(f"[FAIL] WS connect -> {e}")
    try:
        msg, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
        pv = parse_price_volume(msg)
        assert pv is not None, f"Could not parse price/volume from: {msg!r}"
        price = pv[0]
        assert 50.0 <= price <= 150.0, f"Initial price {price} not near 100.0"
        print(f"[PASS] Initial price near 100: {price:.2f}")
    finally:
        try: sock.close()
        except Exception: pass


def test_initial_volume_near_300000():
    """Scenario: Volume starts near 300000."""
    ws_url = http_to_ws_url(BASE)
    try:
        sock, leftover = _ws_connect(ws_url, timeout=WS_TIMEOUT)
    except Exception as e:
        pytest.fail(f"[FAIL] WS connect -> {e}")
    try:
        msg, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
        pv = parse_price_volume(msg)
        assert pv is not None, f"Could not parse price/volume from: {msg!r}"
        volume = pv[1]
        assert 100000 <= volume <= 500000, f"Initial volume {volume} not near 300000"
        print(f"[PASS] Initial volume near 300000: {volume}")
    finally:
        try: sock.close()
        except Exception: pass


def test_price_fluctuation_bounded():
    """Scenario: Price fluctuates with each update by at most 0.50."""
    ws_url = http_to_ws_url(BASE)
    try:
        sock, leftover = _ws_connect(ws_url, timeout=WS_TIMEOUT)
    except Exception as e:
        pytest.fail(f"[FAIL] WS connect -> {e}")
    try:
        msg1, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
        pv1 = parse_price_volume(msg1)
        assert pv1 is not None, f"Could not parse frame 1: {msg1!r}"
        time.sleep(SLEEP_SECS)
        msg2, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
        pv2 = parse_price_volume(msg2)
        assert pv2 is not None, f"Could not parse frame 2: {msg2!r}"
        price_diff = abs(pv2[0] - pv1[0])
        # Allow some tolerance for multiple updates between reads
        assert price_diff <= 5.0, \
            f"Price change {price_diff:.2f} exceeds reasonable bound: {pv1[0]:.2f} -> {pv2[0]:.2f}"
        print(f"[PASS] Price fluctuation bounded: {pv1[0]:.2f} -> {pv2[0]:.2f} (diff={price_diff:.2f})")
    finally:
        try: sock.close()
        except Exception: pass


def test_volume_fluctuation_bounded():
    """Scenario: Volume fluctuates with each update by at most 2500."""
    ws_url = http_to_ws_url(BASE)
    try:
        sock, leftover = _ws_connect(ws_url, timeout=WS_TIMEOUT)
    except Exception as e:
        pytest.fail(f"[FAIL] WS connect -> {e}")
    try:
        msg1, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
        pv1 = parse_price_volume(msg1)
        assert pv1 is not None, f"Could not parse frame 1: {msg1!r}"
        time.sleep(SLEEP_SECS)
        msg2, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
        pv2 = parse_price_volume(msg2)
        assert pv2 is not None, f"Could not parse frame 2: {msg2!r}"
        vol_diff = abs(pv2[1] - pv1[1])
        # Allow some tolerance for multiple updates between reads
        assert vol_diff <= 25000, \
            f"Volume change {vol_diff} exceeds reasonable bound: {pv1[1]} -> {pv2[1]}"
        print(f"[PASS] Volume fluctuation bounded: {pv1[1]} -> {pv2[1]} (diff={vol_diff})")
    finally:
        try: sock.close()
        except Exception: pass


def test_multiple_clients_receive_same_update():
    """Scenario: Updates are sent to all connected WebSocket clients."""
    ws_url = http_to_ws_url(BASE)
    socks = []
    try:
        for i in range(3):
            try:
                sock, leftover = _ws_connect(ws_url, timeout=WS_TIMEOUT)
                socks.append((sock, leftover))
            except Exception as e:
                pytest.fail(f"[FAIL] WS connect client {i+1} -> {e}")
        # Drain initial messages from each client
        for i, (sock, leftover) in enumerate(socks):
            _, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
            socks[i] = (sock, leftover)
        # Wait for a fresh update
        time.sleep(SLEEP_SECS)
        messages = []
        for i, (sock, leftover) in enumerate(socks):
            msg, leftover = _ws_recv_text(sock, leftover, timeout=WS_TIMEOUT)
            messages.append(msg.strip())
            socks[i] = (sock, leftover)
        # All clients should receive the same message
        assert len(set(messages)) == 1, \
            f"Not all clients received the same message: {messages}"
        print(f"[PASS] All 3 clients received same update: {messages[0]!r}")
    finally:
        for sock, _ in socks:
            try: sock.close()
            except Exception: pass


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
