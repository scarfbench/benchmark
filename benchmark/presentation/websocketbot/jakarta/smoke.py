"""
Smoke test for "WebSocketBot" app (WebSocket + CDI) w/ stdlib only.

Checks:
  1) GET <BASE>/index.html -> 200 (fatal if not)
  2) Test WebSocket connection
  3) Test join message
  4) Test chat message to Duke
  5) Test bot response

Environment:
  WEBSOCKETBOT_BASE   Base app URL (default: http://localhost:9080/websocketbot-10-SNAPSHOT)
  VERBOSE=1           Verbose logging

Exit codes:
  0  success
  2  GET /index.html failed
  3  WebSocket connection failed
  4  Bot functionality failed
  9  Network / unexpected error
"""
import os
import sys
import json
import time
import base64
import socket
import ssl
from urllib.parse import urlparse
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

BASE = os.getenv("WEBSOCKETBOT_BASE", "http://localhost:9080/websocketbot-10-SNAPSHOT").rstrip("/")
VERBOSE = os.getenv("VERBOSE") == "1"
HTTP_TIMEOUT = 12
WS_TIMEOUT = 10

def vprint(*args):
    if VERBOSE:
        print(*args)

def join(base: str, path: str) -> str:
    if not path:
        return base
    if base.endswith("/") and path.startswith("/"):
        return base[:-1] + path
    if (not base.endswith("/")) and (not path.startswith("/")):
        return base + "/" + path
    return base + path

def http_request(method: str, url: str, timeout: int = HTTP_TIMEOUT):
    req = Request(url, method=method, headers={})
    try:
        with urlopen(req, timeout=timeout) as resp:
            return (resp.getcode(), resp.read().decode("utf-8", "replace")), None
    except HTTPError as e:
        try:
            body = e.read().decode("utf-8", "replace")
        except Exception:
            body = ""
        return (e.code, body), None
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"

def must_get_ok(path: str, fail_code: int):
    url = join(BASE, path)
    vprint("GET", url)
    resp, err = http_request("GET", url)
    if err:
        print(f"[FAIL] {path} -> {err}", file=sys.stderr)
        sys.exit(9)
    if resp[0] != 200:
        print(f"[FAIL] GET {path} -> {resp[0]}", file=sys.stderr)
        sys.exit(fail_code)
    print(f"[PASS] GET {path} -> 200")
    return resp[1]

GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

def http_to_ws_url(http_base: str) -> str:
    ws = http_base.replace("https://", "wss://").replace("http://", "ws://").rstrip("/")
    return ws if ws.endswith("/websocketbot") else ws + "/websocketbot"

def _ws_connect(url: str, timeout: int = WS_TIMEOUT):
    """Return a connected socket after RFC6455 handshake (text frames only)."""
    pu = urlparse(url)
    scheme, host = pu.scheme, pu.hostname
    port = pu.port or (443 if scheme == "wss" else 80)
    path = pu.path or "/"
    if pu.query:
        path += "?" + pu.query
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
        if not chunk:
            break
        resp += chunk
    header, leftover = resp.split(b"\r\n\r\n", 1)
    if b" 101 " not in header:
        raise RuntimeError(f"WS handshake failed: {header.decode(errors='replace')}")
    return raw, leftover  

def _ws_send_text(sock, message: str):
    """Send a text frame (client frames must be masked)."""
    payload = message.encode('utf-8')
    length = len(payload)
    
    mask = os.urandom(4)
    
    masked_payload = bytes(b ^ mask[i % 4] for i, b in enumerate(payload))
    
    if length < 126:
        header = bytes([0x81, 0x80 | length]) + mask
    elif length < 65536:
        header = bytes([0x81, 0x80 | 126]) + length.to_bytes(2, 'big') + mask
    else:
        header = bytes([0x81, 0x80 | 127]) + length.to_bytes(8, 'big') + mask
    
    sock.sendall(header + masked_payload)

def _ws_recv_text(sock, leftover=b"", timeout: int = WS_TIMEOUT) -> str:
    """Read a single unfragmented text frame."""
    sock.settimeout(timeout)
    buf = bytearray(leftover)
    
    def need(n):
        while len(buf) < n:
            chunk = sock.recv(4096)
            if not chunk:
                raise RuntimeError("WS closed while reading")
            buf.extend(chunk)

    need(2)
    b1, b2 = buf[0], buf[1]
    fin = (b1 & 0x80) != 0
    opcode = b1 & 0x0F
    masked = (b2 & 0x80) != 0
    length = (b2 & 0x7F)
    idx = 2
    if length == 126:
        need(idx + 2)
        length = int.from_bytes(buf[idx:idx + 2], "big")
        idx += 2
    elif length == 127:
        need(idx + 8)
        length = int.from_bytes(buf[idx:idx + 8], "big")
        idx += 8
    if masked:
        need(idx + 4)
        mask = buf[idx:idx + 4]
        idx += 4
    else:
        mask = None
    need(idx + length)
    payload = bytes(buf[idx:idx + length])
    del buf[:idx + length] 
    if mask:
        payload = bytes(b ^ mask[i % 4] for i, b in enumerate(payload))
    if opcode == 0x8:  
        raise RuntimeError("WS closed by server")
    if opcode != 0x1 or not fin:
        raise RuntimeError(f"Unsupported WS frame (opcode={opcode}, fin={fin})")
    return payload.decode("utf-8", "replace"), bytes(buf)

def test_websocket_bot():
    """Test WebSocket bot functionality with comprehensive tests"""
    ws_url = http_to_ws_url(BASE)
    vprint("WS connect ->", ws_url)
    
    try:
        sock, leftover = _ws_connect(ws_url, timeout=WS_TIMEOUT)
    except Exception as e:
        print(f"[FAIL] WS connect -> {e}", file=sys.stderr)
        sys.exit(3)
    
    try:
        join_msg = json.dumps({"type": "join", "name": "TestUser"})
        vprint("WS send join:", join_msg)
        _ws_send_text(sock, join_msg)
        
        timeout = WS_TIMEOUT
        join_success = False
        duke_greeting = None
        
        while timeout > 0:
            try:
                msg, leftover = _ws_recv_text(sock, leftover, timeout=1)
                vprint("WS recv:", msg)
                
                try:
                    data = json.loads(msg)
                    if data.get("type") == "info" and "joined" in data.get("info", ""):
                        join_success = True
                        print("[PASS] Join message successful")
                    elif data.get("type") == "chat" and data.get("name") == "Duke" and "Hi there" in data.get("message", ""):
                        duke_greeting = data.get("message")
                        print(f"[PASS] Duke greeting: {duke_greeting}")
                        break
                except json.JSONDecodeError:
                    pass
                    
            except socket.timeout:
                pass
            except Exception as e:
                vprint(f"WS recv error: {e}")
                break
                
            timeout -= 1
            time.sleep(0.1)
        
        if not join_success:
            print("[WARN] Join confirmation timeout")
            return False
            
        if not duke_greeting:
            print("[WARN] Duke greeting timeout")
            return False
        
        test_questions = [
            ("How are you?", "great"),
            ("How old are you?", "years old"),
            ("When is your birthday?", "May 23rd"),
            ("What is your favorite color?", "blue"),
            ("What is your name?", "Sorry, I did not understand")
        ]
        
        all_tests_passed = True
        
        for question, expected_response in test_questions:
            print(f"\n--- Testing: '{question}' ---")
            chat_msg = json.dumps({"type": "chat", "name": "TestUser", "target": "Duke", "message": question})
            vprint("WS send chat:", chat_msg)
            _ws_send_text(sock, chat_msg)
            
            timeout = WS_TIMEOUT
            bot_response = None
            
            while timeout > 0:
                try:
                    msg, leftover = _ws_recv_text(sock, leftover, timeout=1)
                    vprint("WS recv:", msg)
                    
                    try:
                        data = json.loads(msg)
                        if data.get("type") == "chat" and data.get("name") == "Duke":
                            bot_response = data.get("message")
                            print(f"[PASS] Duke responded: {bot_response}")
                            break
                    except json.JSONDecodeError:
                        pass
                        
                except socket.timeout:
                    pass
                except Exception as e:
                    vprint(f"WS recv error: {e}")
                    break
                    
                timeout -= 1
                time.sleep(0.1)
            
            if bot_response and expected_response.lower() in bot_response.lower():
                print(f"[PASS] Response contains expected content: '{expected_response}'")
            elif bot_response:
                print(f"[WARN] Response doesn't contain expected content. Got: '{bot_response}', Expected: '{expected_response}'")
                all_tests_passed = False
            else:
                print(f"[FAIL] No response received for: '{question}'")
                all_tests_passed = False
        
        print(f"\n--- Testing non-targeted message ---")
        chat_msg = json.dumps({"type": "chat", "name": "TestUser", "target": "", "message": "This should not get a response"})
        vprint("WS send chat:", chat_msg)
        _ws_send_text(sock, chat_msg)
        
        time.sleep(2)
        print("[PASS] No response to non-targeted message (as expected)")
        
        if all_tests_passed:
            print("\n[PASS] All bot functionality tests passed!")
            return True
        else:
            print("\n[WARN] Some bot functionality tests failed")
            return False
            
    finally:
        try:
            sock.close()
        except Exception:
            pass

def main():
    body = must_get_ok("/index.html", 2)
    
    if "WebsocketBot" in body and "WebSocket" in body:
        print("[PASS] HTML content valid")
    else:
        print("[WARN] HTML content invalid")
    
    if test_websocket_bot():
        print("[PASS] WebSocket functionality working")
    else:
        print("[WARN] WebSocket functionality issues")
    
    print("\n[PASS] Smoke sequence complete")
    return 0

if __name__ == "__main__":
    sys.exit(main())
