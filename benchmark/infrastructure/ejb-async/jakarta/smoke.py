#!/usr/bin/env python3
""" """

from __future__ import annotations

import os
import re
import socket
import subprocess
import sys
import threading
import time
from pathlib import Path
from typing import List, Optional

RECIPIENT = os.getenv("RECIPIENT", "someone@email.com")
BASE_URL = os.getenv("BASE_URL", "http://localhost:9080/async-war/")
START_TIMEOUT = int(os.getenv("START_TIMEOUT", "90"))
SEND_TIMEOUT = int(os.getenv("SEND_TIMEOUT", "30"))
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
            # Optional: echo for visibility
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


def run_playwright(recipient: str):
    from playwright.sync_api import sync_playwright

    with sync_playwright() as pw:
        browser = pw.chromium.launch()
        page = browser.new_page()
        page.goto(BASE_URL)
        # JSF component IDs include form prefix; use suffix match
        email_selector = 'input[id$="emailInputText"]'
        send_selector = 'input[id$="sendButton"]'
        page.wait_for_selector(email_selector, timeout=15000)
        page.fill(email_selector, recipient)
        page.click(send_selector)
        # Redirect to response.xhtml - wait for status text element
        status_selector = 'span[id$="messageStatus"], *[id$="messageStatus"]'
        page.wait_for_selector(status_selector, timeout=10000)
        start = time.time()
        status = page.inner_text(status_selector).strip()
        while status.startswith("Processing"):
            if time.time() - start > SEND_TIMEOUT:
                raise RuntimeError("Timeout waiting for async status to complete")
            time.sleep(1.0)
            page.reload()
            page.wait_for_selector(status_selector, timeout=5000)
            status = page.inner_text(status_selector).strip()
        if status != "Sent":
            raise RuntimeError(f"Unexpected final status: {status}")
        browser.close()
        return status


def main():
    start_smtp = os.getenv("SKIP_START_SMTP") != "1"
    start_app = os.getenv("SKIP_START_APP") != "1"
    smtp_proc = None
    app_proc = None
    rc = 0
    try:
        if start_smtp:
            smtp_proc = ProcWrapper(
                "async-smtpd",
                [MVNW, "-q", "-pl", "async-smtpd", "compile", "exec:java"],
            )
            smtp_proc.start()
            wait_for(
                lambda: smtp_proc.grep(re.escape(SMTP_LISTEN_MARKER)),
                START_TIMEOUT,
                desc="SMTP listen",
            )
        if start_app:
            app_proc = ProcWrapper(
                "async-war", [MVNW, "-q", "-pl", "async-war", "liberty:run"]
            )
            app_proc.start()
            # Wait for port 9080 instead of parsing logs for robustness
            wait_for_http("localhost", 9080, START_TIMEOUT)
        status = run_playwright(RECIPIENT)
        print(f"[INFO] UI reported status: {status}")
        # Wait until SMTP server indicates message delivered
        if smtp_proc:
            # Wait for the SMTP server to log the delivery line. The previous pattern
            # was over-escaped (started with \\[]) so it never matched and timed out.
            delivery_pattern = re.escape("[Delivering message...]")
            try:
                wait_for(
                    lambda: smtp_proc.grep(delivery_pattern),
                    SEND_TIMEOUT,
                    desc="SMTP delivery",
                )
            except TimeoutError:
                # Augment the exception with recent SMTP log lines for easier debugging
                recent = "\n".join(smtp_proc.snapshot().splitlines()[-25:])
                raise TimeoutError(
                    f"Timed out waiting for SMTP delivery after {SEND_TIMEOUT}s. Recent SMTP log:\n{recent}"
                )
            output = smtp_proc.snapshot()
            missing = [s for s in EXPECT_SUBSTRINGS if s not in output]
            if missing:
                print(
                    "[ERROR] Missing expected substrings in SMTP output:",
                    missing,
                    file=sys.stderr,
                )
                print(output)
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
