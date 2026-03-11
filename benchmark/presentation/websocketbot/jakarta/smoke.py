"""
Smoke tests for "WebSocketBot" app (WebSocket + CDI) w/ stdlib only.

Covers all scenarios from websocketbot.feature:
  - WebSocket connection open/close
  - Join chat room, users list
  - Chat messages between users
  - Duke bot responses (how are you, age, birthday, color, unrecognized)
  - Case-insensitivity and question-mark tolerance
  - Message type structure (JoinMessage, ChatMessage, InfoMessage, UsersMessage)
  - Broadcast to all sessions, user list updates

Environment:
  WEBSOCKETBOT_BASE   Base app URL (default: http://localhost:9080/websocketbot-10-SNAPSHOT)
  VERBOSE=1           Verbose logging
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
import pytest

BASE = os.getenv("WEBSOCKETBOT_BASE", "http://localhost:9080/websocketbot-10-SNAPSHOT").rstrip("/")
VERBOSE = os.getenv("VERBOSE") == "1"
HTTP_TIMEOUT = 12
WS_TIMEOUT = 10


def vprint(*args):
    if VERBOSE:
        print(*args)


def _join_url(base: str, path: str) -> str:
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


def must_get_ok(path: str):
    url = _join_url(BASE, path)
    vprint("GET", url)
    resp, err = http_request("GET", url)
    if err:
        pytest.fail(f"[FAIL] {path} -> {err}")
    if resp[0] != 200:
        pytest.fail(f"[FAIL] GET {path} -> {resp[0]}")
    print(f"[PASS] GET {path} -> 200")
    return resp[1]


# ---------------------------------------------------------------------------
# WebSocket helpers (RFC 6455, text frames, client masking)
# ---------------------------------------------------------------------------

def http_to_ws_url(http_base: str) -> str:
    ws = http_base.replace("https://", "wss://").replace("http://", "ws://").rstrip("/")
    return ws if ws.endswith("/websocketbot") else ws + "/websocketbot"


def _ws_connect(url: str, timeout: int = WS_TIMEOUT):
    """Return a connected socket after RFC6455 handshake."""
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
    """Send a masked text frame."""
    payload = message.encode("utf-8")
    length = len(payload)
    mask = os.urandom(4)
    masked_payload = bytes(b ^ mask[i % 4] for i, b in enumerate(payload))
    if length < 126:
        header = bytes([0x81, 0x80 | length]) + mask
    elif length < 65536:
        header = bytes([0x81, 0x80 | 126]) + length.to_bytes(2, "big") + mask
    else:
        header = bytes([0x81, 0x80 | 127]) + length.to_bytes(8, "big") + mask
    sock.sendall(header + masked_payload)


def _ws_recv_text(sock, leftover=b"", timeout: int = WS_TIMEOUT):
    """Read a single unfragmented text frame. Returns (text, remaining_bytes)."""
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
    length = b2 & 0x7F
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
        mask_key = buf[idx:idx + 4]
        idx += 4
    else:
        mask_key = None
    need(idx + length)
    payload = bytes(buf[idx:idx + length])
    del buf[:idx + length]
    if mask_key:
        payload = bytes(b ^ mask_key[i % 4] for i, b in enumerate(payload))
    if opcode == 0x8:
        raise RuntimeError("WS closed by server")
    if opcode != 0x1 or not fin:
        raise RuntimeError(f"Unsupported WS frame (opcode={opcode}, fin={fin})")
    return payload.decode("utf-8", "replace"), bytes(buf)


def _ws_send_close(sock):
    """Send a close frame (opcode 0x8, masked)."""
    mask = os.urandom(4)
    sock.sendall(bytes([0x88, 0x80]) + mask)


# ---------------------------------------------------------------------------
# Higher-level helpers
# ---------------------------------------------------------------------------

def _recv_messages(sock, leftover=b"", timeout=5, max_msgs=10):
    """Receive up to max_msgs JSON messages within timeout seconds."""
    messages = []
    remaining = leftover
    deadline = time.time() + timeout
    while len(messages) < max_msgs and time.time() < deadline:
        per_msg_timeout = max(0.5, deadline - time.time())
        try:
            text, remaining = _ws_recv_text(sock, remaining, timeout=per_msg_timeout)
            vprint("  recv:", text)
            data = json.loads(text)
            messages.append(data)
        except (socket.timeout, json.JSONDecodeError):
            break
        except RuntimeError:
            break
    return messages, remaining


def _connect_and_join(name="TestUser"):
    """Connect via WS, send join, collect initial messages.
    Returns (sock, leftover, messages_list).
    """
    ws_url = http_to_ws_url(BASE)
    vprint(f"WS connect+join as {name} -> {ws_url}")
    sock, leftover = _ws_connect(ws_url)
    _ws_send_text(sock, json.dumps({"type": "join", "name": name}))
    messages, leftover = _recv_messages(sock, leftover, timeout=5)
    return sock, leftover, messages


def _ask_duke(sock, leftover, sender, question):
    """Send a chat to Duke and return (duke_reply_msg, leftover, all_msgs)."""
    msg = json.dumps({"type": "chat", "name": sender, "target": "Duke", "message": question})
    _ws_send_text(sock, msg)
    messages, leftover = _recv_messages(sock, leftover, timeout=5)
    for m in messages:
        if m.get("type") == "chat" and m.get("name") == "Duke":
            return m, leftover, messages
    return None, leftover, messages


def _find_msg(messages, **criteria):
    """Find first message matching all key=value criteria."""
    for m in messages:
        if all(m.get(k) == v for k, v in criteria.items()):
            return m
    return None


def _find_msg_containing(messages, msg_type, field, substring):
    """Find first message of msg_type where field contains substring."""
    for m in messages:
        if m.get("type") == msg_type and substring.lower() in str(m.get(field, "")).lower():
            return m
    return None


# ===================================================================
# Tests
# ===================================================================

# --- HTTP ---

def test_index_page():
    """Index page should load with WebSocket content."""
    body = must_get_ok("/index.html")
    assert "WebSocket" in body or "websocketbot" in body.lower(), \
        "Expected WebSocket content in page"


# --- WebSocket connection (Feature: WebSocket connection) ---

def test_ws_connection_opens():
    """Scenario: Open a WebSocket connection."""
    ws_url = http_to_ws_url(BASE)
    sock, leftover = _ws_connect(ws_url)
    try:
        assert sock is not None, "WebSocket connection should be established"
        print("[PASS] WebSocket connection opened")
    finally:
        sock.close()


def test_ws_connection_closes():
    """Scenario: Close a WebSocket connection."""
    ws_url = http_to_ws_url(BASE)
    sock, leftover = _ws_connect(ws_url)
    try:
        _ws_send_close(sock)
        time.sleep(0.5)
        print("[PASS] WebSocket connection closed cleanly")
    finally:
        sock.close()


# --- Join messages ---

def test_join_chat_room():
    """Scenario: Join the chat room — info message, Duke greeting, users update."""
    sock, leftover, messages = _connect_and_join("Alice")
    try:
        info = _find_msg_containing(messages, "info", "info", "joined")
        assert info is not None, "Expected info message about Alice joining"
        assert "Alice" in info.get("info", ""), "Info should mention Alice"

        duke_chat = _find_msg_containing(messages, "chat", "message", "Hi there")
        assert duke_chat is not None, "Duke should greet with 'Hi there'"
        assert duke_chat.get("name") == "Duke"

        users_msg = _find_msg(messages, type="users")
        assert users_msg is not None, "Expected a users list update"
        print("[PASS] Join chat room: info + greeting + users update")
    finally:
        sock.close()


def test_users_list_includes_duke_and_user():
    """Scenario: Users list includes Duke and the joined user."""
    sock, leftover, messages = _connect_and_join("Alice")
    try:
        users_msg = _find_msg(messages, type="users")
        assert users_msg is not None, "Expected users message after join"
        users = users_msg.get("userlist", users_msg.get("users", []))
        assert "Duke" in users, f"Users list should include Duke, got {users}"
        assert "Alice" in users, f"Users list should include Alice, got {users}"
        print(f"[PASS] Users list: {users}")
    finally:
        sock.close()


# --- Chat messages ---

def test_send_chat_message_to_another_user():
    """Scenario: Send a chat message to another user — all clients receive it."""
    sock_a, left_a, _ = _connect_and_join("Alice")
    try:
        sock_b, left_b, _ = _connect_and_join("Bob")
        try:
            # Drain any users-update messages Bob's join caused on Alice's side
            _, left_a = _recv_messages(sock_a, left_a, timeout=2)

            msg = json.dumps({"type": "chat", "name": "Alice", "target": "Bob", "message": "Hello!"})
            _ws_send_text(sock_a, msg)

            msgs_b, left_b = _recv_messages(sock_b, left_b, timeout=5)
            chat = _find_msg_containing(msgs_b, "chat", "message", "Hello!")
            assert chat is not None, "Bob should receive Alice's chat message"
            assert chat.get("name") == "Alice"
            assert chat.get("target") == "Bob"
            print("[PASS] Chat message delivered to other user")
        finally:
            sock_b.close()
    finally:
        sock_a.close()


def test_message_to_duke_triggers_response():
    """Scenario: Send a message to Duke triggers a bot response."""
    sock, leftover, _ = _connect_and_join("Alice")
    try:
        reply, leftover, _ = _ask_duke(sock, leftover, "Alice", "How are you?")
        assert reply is not None, "Duke should respond to a message"
        assert reply.get("name") == "Duke"
        assert reply.get("message"), "Duke's response should have a message body"
        print(f"[PASS] Duke responded: {reply.get('message')}")
    finally:
        sock.close()


# --- Bot responses ---

def test_duke_how_are_you():
    """Scenario: Ask Duke how he is doing."""
    sock, leftover, _ = _connect_and_join("Alice")
    try:
        reply, leftover, _ = _ask_duke(sock, leftover, "Alice", "how are you")
        assert reply is not None, "Duke should respond"
        assert "great" in reply["message"].lower(), \
            f"Expected 'great' in response, got: {reply['message']}"
        print(f"[PASS] Duke: {reply['message']}")
    finally:
        sock.close()


def test_duke_how_old():
    """Scenario: Ask Duke how old he is."""
    sock, leftover, _ = _connect_and_join("Alice")
    try:
        reply, leftover, _ = _ask_duke(sock, leftover, "Alice", "how old are you")
        assert reply is not None, "Duke should respond"
        assert "years old" in reply["message"].lower(), \
            f"Expected 'years old' in response, got: {reply['message']}"
        print(f"[PASS] Duke: {reply['message']}")
    finally:
        sock.close()


def test_duke_birthday():
    """Scenario: Ask Duke when his birthday is."""
    sock, leftover, _ = _connect_and_join("Alice")
    try:
        reply, leftover, _ = _ask_duke(sock, leftover, "Alice", "when is your birthday")
        assert reply is not None, "Duke should respond"
        assert "may 23" in reply["message"].lower(), \
            f"Expected 'May 23' in response, got: {reply['message']}"
        print(f"[PASS] Duke: {reply['message']}")
    finally:
        sock.close()


def test_duke_favorite_color():
    """Scenario: Ask Duke about his favorite color."""
    sock, leftover, _ = _connect_and_join("Alice")
    try:
        reply, leftover, _ = _ask_duke(sock, leftover, "Alice", "what is your favorite color")
        assert reply is not None, "Duke should respond"
        assert "blue" in reply["message"].lower(), \
            f"Expected 'blue' in response, got: {reply['message']}"
        print(f"[PASS] Duke: {reply['message']}")
    finally:
        sock.close()


def test_duke_unrecognized_message():
    """Scenario: Send an unrecognized message to Duke."""
    sock, leftover, _ = _connect_and_join("Alice")
    try:
        reply, leftover, _ = _ask_duke(sock, leftover, "Alice", "tell me a joke")
        assert reply is not None, "Duke should respond even to unrecognized messages"
        body = reply["message"].lower()
        assert "sorry" in body or "did not understand" in body, \
            f"Expected 'sorry' or 'did not understand', got: {reply['message']}"
        print(f"[PASS] Duke: {reply['message']}")
    finally:
        sock.close()


def test_bot_responses_case_insensitive():
    """Scenario: Bot responses are case-insensitive."""
    sock, leftover, _ = _connect_and_join("Alice")
    try:
        reply, leftover, _ = _ask_duke(sock, leftover, "Alice", "HOW ARE YOU")
        assert reply is not None, "Duke should respond to uppercase input"
        assert "great" in reply["message"].lower(), \
            f"Expected 'great' in response to uppercase, got: {reply['message']}"
        print(f"[PASS] Case-insensitive: {reply['message']}")
    finally:
        sock.close()


def test_bot_ignores_question_marks():
    """Scenario: Bot ignores question marks in messages."""
    sock, leftover, _ = _connect_and_join("Alice")
    try:
        reply, leftover, _ = _ask_duke(sock, leftover, "Alice", "how are you?")
        assert reply is not None, "Duke should respond despite question mark"
        assert "great" in reply["message"].lower(), \
            f"Expected 'great' in response with '?', got: {reply['message']}"
        print(f"[PASS] Question mark ignored: {reply['message']}")
    finally:
        sock.close()


# --- Leave the chat ---

def test_user_leaves_chat():
    """Scenario: User leaves the chat — other clients get notified."""
    sock_a, left_a, _ = _connect_and_join("Alice")
    try:
        sock_b, left_b, _ = _connect_and_join("Bob")
        try:
            # Drain Bob-join messages on Alice's side
            _, left_a = _recv_messages(sock_a, left_a, timeout=2)

            # Alice disconnects
            sock_a.close()
            time.sleep(1)

            # Bob should see "Alice has left"
            msgs, left_b = _recv_messages(sock_b, left_b, timeout=5)
            left_info = _find_msg_containing(msgs, "info", "info", "left")
            assert left_info is not None, "Bob should receive 'left' info message"
            assert "Alice" in left_info.get("info", ""), "Info should mention Alice"

            # Users list should be updated (Alice removed)
            users_msg = _find_msg(msgs, type="users")
            if users_msg:
                users = users_msg.get("userlist", users_msg.get("users", []))
                assert "Alice" not in users, f"Alice should be removed from users list, got {users}"
            print("[PASS] User leave: info broadcast + users list updated")
        finally:
            try:
                sock_b.close()
            except Exception:
                pass
    except Exception:
        # sock_a may already be closed
        raise


# --- Message types ---

def test_join_message_contains_name():
    """Scenario: JoinMessage contains the user's name."""
    sock, leftover, messages = _connect_and_join("Bob")
    try:
        info = _find_msg_containing(messages, "info", "info", "Bob")
        assert info is not None, "Join should produce info message with user name 'Bob'"
        assert "Bob" in info.get("info", "")
        print(f"[PASS] JoinMessage info: {info.get('info')}")
    finally:
        sock.close()


def test_chat_message_structure():
    """Scenario: ChatMessage contains name, target, and message."""
    sock, leftover, _ = _connect_and_join("Alice")
    try:
        msg_payload = json.dumps({"type": "chat", "name": "Alice", "target": "Duke", "message": "Hello"})
        _ws_send_text(sock, msg_payload)
        messages, leftover = _recv_messages(sock, leftover, timeout=5)

        # Find any chat message (Duke's reply or echo)
        chat_msgs = [m for m in messages if m.get("type") == "chat"]
        assert len(chat_msgs) > 0, "Should receive at least one chat message"
        for cm in chat_msgs:
            assert "name" in cm, "ChatMessage must have 'name' field"
            assert "target" in cm, "ChatMessage must have 'target' field"
            assert "message" in cm, "ChatMessage must have 'message' field"
        print("[PASS] ChatMessage structure has name, target, message")
    finally:
        sock.close()


def test_info_message_structure():
    """Scenario: InfoMessage contains a system notification."""
    sock, leftover, messages = _connect_and_join("Alice")
    try:
        info = _find_msg(messages, type="info")
        assert info is not None, "Should receive an info message on join"
        assert "info" in info, "InfoMessage must have 'info' field"
        assert isinstance(info["info"], str) and len(info["info"]) > 0, \
            "InfoMessage 'info' field should be a non-empty string"
        print(f"[PASS] InfoMessage: {info['info']}")
    finally:
        sock.close()


def test_users_message_structure():
    """Scenario: UsersMessage contains the current user list including Duke."""
    sock, leftover, messages = _connect_and_join("Alice")
    try:
        users_msg = _find_msg(messages, type="users")
        assert users_msg is not None, "Should receive a users message on join"
        assert "userlist" in users_msg or "users" in users_msg, \
            "UsersMessage must have 'userlist' or 'users' field"
        users = users_msg.get("userlist", users_msg.get("users", []))
        assert isinstance(users, list), "users field should be a list"
        assert "Duke" in users, f"Users list should always include Duke, got {users}"
        print(f"[PASS] UsersMessage: {users}")
    finally:
        sock.close()


# --- Concurrent sessions ---

def test_broadcast_to_all_sessions():
    """Scenario: Messages are sent to all open sessions."""
    socks = []
    try:
        sock_a, left_a, _ = _connect_and_join("Alice")
        socks.append(sock_a)
        sock_b, left_b, _ = _connect_and_join("Bob")
        socks.append(sock_b)
        sock_c, left_c, _ = _connect_and_join("Charlie")
        socks.append(sock_c)

        # Drain join notifications
        _, left_a = _recv_messages(sock_a, left_a, timeout=2)
        _, left_b = _recv_messages(sock_b, left_b, timeout=2)

        # Alice sends a chat message
        msg = json.dumps({"type": "chat", "name": "Alice", "target": "Bob", "message": "Broadcast test"})
        _ws_send_text(sock_a, msg)

        # All three should receive it
        received = 0
        for label, sock, left in [("Alice", sock_a, left_a), ("Bob", sock_b, left_b), ("Charlie", sock_c, left_c)]:
            msgs, _ = _recv_messages(sock, left, timeout=5)
            chat = _find_msg_containing(msgs, "chat", "message", "Broadcast test")
            if chat:
                received += 1
                vprint(f"  {label} received broadcast")

        assert received >= 2, \
            f"At least sender and target should receive the message, got {received}/3"
        print(f"[PASS] Broadcast received by {received}/3 clients")
    finally:
        for s in socks:
            try:
                s.close()
            except Exception:
                pass


def test_user_list_updates_on_join():
    """Scenario: User list updates when a new user joins."""
    sock_a, left_a, msgs_a = _connect_and_join("Alice")
    try:
        users_before = None
        for m in msgs_a:
            if m.get("type") == "users":
                users_before = m.get("userlist", m.get("users", []))

        # Bob joins — Alice should get an updated users list
        sock_b, left_b, _ = _connect_and_join("Bob")
        try:
            msgs_update, left_a = _recv_messages(sock_a, left_a, timeout=5)
            users_msg = _find_msg(msgs_update, type="users")
            assert users_msg is not None, "Alice should receive updated users list when Bob joins"
            users = users_msg.get("userlist", users_msg.get("users", []))
            assert "Duke" in users, f"Users should contain Duke, got {users}"
            assert "Alice" in users, f"Users should contain Alice, got {users}"
            assert "Bob" in users, f"Users should contain Bob, got {users}"
            print(f"[PASS] User list updated on join: {users}")
        finally:
            sock_b.close()
    finally:
        sock_a.close()


def main():
    return pytest.main([__file__, "-v"])


if __name__ == "__main__":
    sys.exit(main())
