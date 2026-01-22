#!/usr/bin/env python3
import os
import re
import sys
import time
import signal
import socket
import threading
import subprocess
from pathlib import Path
from typing import Optional, List

# -------------------- Config --------------------

RECIPIENT = os.getenv("RECIPIENT", "someone@email.com")
BASE_URL = os.getenv("BASE_URL", "http://localhost:9080/index.xhtml")
START_TIMEOUT = int(os.getenv("START_TIMEOUT", "90"))
SEND_TIMEOUT = int(os.getenv("SEND_TIMEOUT", "30"))
STATUS_STRICT = os.getenv("STATUS_STRICT", "0") == "1"
ROOT = Path(__file__).parent
MVNW = str(ROOT / "mvnw")

EXPECT_SUBSTRINGS = [
    "[Delivering message...]",
    "Subject: Test message from async example",
    "X-Mailer: Jakarta Mail",
    "This is a test message from the async example of the Jakarta EE Tutorial.",
]

SMTP_LISTEN_MARKER = "[Test SMTP server listening on port 3025]"
SMTP_CLIENT_MARKER = "[Client connected]"

# -------------------- Proc Wrapper --------------------

class ProcWrapper:
    def __init__(self, name: str, args: List[str]):
        self.name = name
        self.args = args
        self.proc: Optional[subprocess.Popen] = None
        self.lines: List[str] = []
        self._lock = threading.Lock()
        self._thread: Optional[threading.Thread] = None

    def start(self):
        self.proc = subprocess.Popen(
            self.args,
            cwd=str(ROOT),
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            bufsize=1,
            universal_newlines=True,
        )
        self._thread = threading.Thread(target=self._pump, daemon=True)
        self._thread.start()

    def _pump(self):
        assert self.proc and self.proc.stdout
        for line in self.proc.stdout:
            with self._lock:
                self.lines.append(line.rstrip("\n"))
            print(f"[{self.name}] {line.rstrip()}")

    def stop(self):
        if self.proc and self.proc.poll() is None:
            self.proc.terminate()
            try:
                self.proc.wait(timeout=10)
            except subprocess.TimeoutExpired:
                self.proc.kill()

    def grep(self, pattern: str) -> bool:
        rx = re.compile(pattern)
        with self._lock:
            return any(rx.search(l) for l in self.lines)

    def snapshot(self) -> str:
        with self._lock:
            return "\n".join(self.lines)

# -------------------- Helpers --------------------

def wait_for(predicate, timeout: int, interval: float = 0.5, desc: str = "condition"):
    end = time.time() + timeout
    while time.time() < end:
        if predicate():
            return True
        time.sleep(interval)
    raise TimeoutError(f"Timed out waiting for {desc} after {timeout}s")

def wait_for_http(host: str, port: int, timeout: int):
    def _try():
        try:
            with socket.create_connection((host, port), timeout=1):
                return True
        except OSError:
            return False
    wait_for(_try, timeout=timeout, desc=f"HTTP port {port}")

def kill_port(port: int):
    """Find and kill any process listening on the given TCP port."""
    try:
        if sys.platform.startswith(("linux", "darwin")):
            result = subprocess.run(
                ["lsof", "-ti", f"tcp:{port}"],
                capture_output=True, text=True, check=False
            )
            pids = [line.strip() for line in result.stdout.splitlines() if line.strip()]
            for pid in pids:
                print(f"[INFO] Killing process {pid} on port {port}")
                os.kill(int(pid), signal.SIGKILL)
        elif sys.platform.startswith("win"):
            result = subprocess.run(
                ["netstat", "-ano"], capture_output=True, text=True, check=False
            )
            for line in result.stdout.splitlines():
                if f":{port} " in line and "LISTENING" in line:
                    pid = line.strip().split()[-1]
                    print(f"[INFO] Killing process {pid} on port {port}")
                    subprocess.run(["taskkill", "/PID", pid, "/F"])
        else:
            print(f"[WARN] Kill on port {port} not implemented for this platform")
    except Exception as e:
        print(f"[WARN] Could not kill processes on port {port}: {e}")

# -------------------- UI Driver --------------------

def run_playwright(recipient: str):
    from re import compile as _re
    from playwright.sync_api import sync_playwright

    with sync_playwright() as pw:
        browser = pw.chromium.launch()
        page = browser.new_page()

        # Normalize URL (avoid trailing slash for .xhtml)
        url = BASE_URL[:-1] if BASE_URL.endswith("/") else BASE_URL
        page.goto(url)

        print(f"[DEBUG] Page title: {page.title()}")
        print(f"[DEBUG] Page URL: {page.url}")
        try:
            page.screenshot(path="debug_screenshot.png")
            print("[DEBUG] Screenshot saved as debug_screenshot.png")
        except Exception as e:
            print(f"[DEBUG] Could not save screenshot: {e}")

        content = page.content()
        print(f"[DEBUG] Page content length: {len(content)}")
        if len(content) < 1000:
            print(f"[DEBUG] Full page content: {content}")
        else:
            print(f"[DEBUG] Page content preview: {content[:500]}...")

        # Robust selectors for JSF-generated IDs
        selectors_to_try = [
            'input[id$="emailInputText"]',
            'input[id*="emailInputText"]',
            'input[type="text"]',
            '#emailForm\\:emailInputText',
            'input[id="emailForm:emailInputText"]',
        ]

        found_selector = None
        for selector in selectors_to_try:
            try:
                page.wait_for_selector(selector, timeout=2000)
                found_selector = selector
                print(f"[DEBUG] Found input using selector: {selector}")
                break
            except Exception:
                print(f"[DEBUG] Selector failed: {selector}")
                continue

        if not found_selector:
            inputs = page.query_selector_all('input')
            print(f"[DEBUG] Found {len(inputs)} input elements:")
            for i, inp in enumerate(inputs):
                print(f"[DEBUG] Input {i}: id='{inp.get_attribute('id')}' "
                      f"type='{inp.get_attribute('type')}' name='{inp.get_attribute('name')}'")
            raise RuntimeError("Could not find email input field with any selector")

        # Fill and submit
        page.fill(found_selector, recipient)
        send_selector = 'input[id$="sendButton"]'

        # Handle redirect to response.xhtml if it happens
        try:
            with page.expect_navigation(url=_re(r".*/response\.xhtml$"), timeout=10000):
                page.click(send_selector)
        except Exception:
            # No redirect; just click and continue
            page.click(send_selector)

        # Try to read status from UI if present
        status = "UNKNOWN"
        status_selector = 'span[id$="messageStatus"], *[id$="messageStatus"]'
        try:
            page.wait_for_selector(status_selector, timeout=5000)
            start = time.time()
            status = page.inner_text(status_selector).strip()
            while status.startswith("Processing"):
                if time.time() - start > SEND_TIMEOUT:
                    raise RuntimeError("Timeout waiting for async status to complete")
                time.sleep(1.0)
                page.reload()
                page.wait_for_selector(status_selector, timeout=3000)
                status = page.inner_text(status_selector).strip()
        except Exception:
            if STATUS_STRICT:
                raise RuntimeError("UI status element not found and STATUS_STRICT=1")
            # Fallback: attempt to parse "Status:" line anywhere on the page
            try:
                body = page.inner_text("body")
                m = re.search(r"Status:\s*(.+)", body)
                if m:
                    status = m.group(1).strip()
            except Exception:
                pass  # leave as UNKNOWN

        browser.close()
        return status

# -------------------- Main --------------------

def main():
    start_smtp = os.getenv("SKIP_START_SMTP") != "1"
    start_app  = os.getenv("SKIP_START_APP")  != "1"
    smtp_proc: Optional[ProcWrapper] = None
    app_proc:  Optional[ProcWrapper] = None
    rc = 0

    # Free up 9080
    kill_port(9080)

    try:
        # Start SMTP
        if start_smtp:
            smtp_proc = ProcWrapper("async-smtpd", [MVNW, "-q", "-pl", "async-smtpd", "compile", "exec:java"])
            smtp_proc.start()
            wait_for(lambda: smtp_proc.grep(re.escape(SMTP_LISTEN_MARKER)),
                     START_TIMEOUT, desc="SMTP listen")

        # Start app
        if start_app:
            app_proc = ProcWrapper("async-service", [MVNW, "-q", "-pl", "async-service", "spring-boot:run"])
            app_proc.start()
            wait_for_http("localhost", 9080, START_TIMEOUT)

        # Drive UI
        status = run_playwright(RECIPIENT)
        print(f"[INFO] UI reported status: {status}")

        # Validate SMTP delivery
        if smtp_proc:
            delivery_pattern = re.escape("[Delivering message...]")
            try:
                wait_for(lambda: smtp_proc.grep(delivery_pattern),
                         SEND_TIMEOUT, desc="SMTP delivery")
            except TimeoutError:
                recent = "\n".join(smtp_proc.snapshot().splitlines()[-25:])
                raise TimeoutError(f"Timed out waiting for SMTP delivery after {SEND_TIMEOUT}s.\n"
                                   f"Recent SMTP log:\n{recent}")

            # Normalize QP soft breaks and whitespace
            output = smtp_proc.snapshot()
            output_norm = re.sub(r"=\r?\n", "", output)     # quoted-printable soft break
            output_norm = re.sub(r"\s+", " ", output_norm)  # collapse whitespace

            missing = [s for s in EXPECT_SUBSTRINGS if s not in output_norm]
            if missing:
                print("[ERROR] Missing expected substrings in SMTP output:", missing, file=sys.stderr)
                print(output)  # raw for debugging
                rc = 2
            else:
                print("[PASS] SMTP output contains all expected substrings")
        else:
            print("[WARN] SMTP process not started; skipped delivery validation")

    except Exception as e:
        print(f"[FAIL] {e}", file=sys.stderr)
        rc = 1
    finally:
        if app_proc:
            app_proc.stop()
        if smtp_proc:
            smtp_proc.stop()

    return rc

if __name__ == "__main__":
    sys.exit(main())