#!/usr/bin/env python3
"""Pytest feature coverage for async email sending (Spring Boot).

Split by SMTP availability:
- tests marked `smtp_up` require SMTP server running
- tests marked `smtp_down` require SMTP server unavailable
"""

from __future__ import annotations

import os
import re
import signal
import socket
import subprocess
import sys
import threading
import time
from pathlib import Path
from typing import Callable, List, Optional

import pytest

# -----------------------------------------------------------------------------
# Configuration
# -----------------------------------------------------------------------------

BASE_URL = os.getenv("BASE_URL", "http://localhost:9080/index.xhtml")
ROOT = Path(__file__).parent
MVNW = str(ROOT / "mvnw")
if os.path.exists(MVNW):
    try:
        os.chmod(MVNW, os.stat(MVNW).st_mode | 0o111)
    except OSError:
        pass
MVN_CMD = [MVNW] if os.path.exists(MVNW) else ["mvn"]

START_TIMEOUT = int(os.getenv("START_TIMEOUT", "90"))
SEND_TIMEOUT = int(os.getenv("SEND_TIMEOUT", "30"))
FAST_RESPONSE_THRESHOLD_SEC = float(os.getenv("FAST_RESPONSE_THRESHOLD_SEC", "2.0"))

SMTP_LISTEN_MARKER = "[Test SMTP server listening on port 3025]"
SMTP_PORT_MARKER = "3025"

EMAIL_SELECTOR = 'input[id$="emailInputText"]'
SEND_SELECTOR = 'input[id$="sendButton"]'
STATUS_SELECTOR = 'span[id$="messageStatus"], *[id$="messageStatus"]'


# -----------------------------------------------------------------------------
# Helpers
# -----------------------------------------------------------------------------


class ProcWrapper:
    def __init__(self, name: str, args: List[str]):
        self.name = name
        self.args = args
        self.proc: Optional[subprocess.Popen] = None
        self.lines: List[str] = []
        self._lock = threading.Lock()
        self._thread: Optional[threading.Thread] = None

    def start(self) -> None:
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

    def _pump(self) -> None:
        assert self.proc and self.proc.stdout
        for line in self.proc.stdout:
            ln = line.rstrip("\n")
            with self._lock:
                self.lines.append(ln)
            print(f"[{self.name}] {ln}")

    def stop(self) -> None:
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

    def clear_logs(self) -> None:
        with self._lock:
            self.lines.clear()


def wait_for(
    predicate: Callable[[], bool],
    timeout: int,
    interval: float = 0.25,
    desc: str = "condition",
) -> bool:
    end = time.time() + timeout
    while time.time() < end:
        if predicate():
            return True
        time.sleep(interval)
    raise TimeoutError(f"Timed out waiting for {desc} after {timeout}s")


def wait_for_http(host: str, port: int, timeout: int) -> None:
    def _try() -> bool:
        try:
            with socket.create_connection((host, port), timeout=1):
                return True
        except OSError:
            return False

    wait_for(_try, timeout=timeout, desc=f"HTTP port {port}")


def kill_port(port: int) -> None:
    try:
        if sys.platform.startswith(("linux", "darwin")):
            result = subprocess.run(
                ["lsof", "-ti", f"tcp:{port}"],
                capture_output=True, text=True, check=False,
            )
            pids = [line.strip() for line in result.stdout.splitlines() if line.strip()]
            for pid in pids:
                print(f"[INFO] Killing process {pid} on port {port}")
                os.kill(int(pid), signal.SIGKILL)
    except Exception as e:
        print(f"[WARN] Could not kill processes on port {port}: {e}")


def extract_status(page) -> str:
    page.wait_for_selector(STATUS_SELECTOR, timeout=10000)
    return page.inner_text(STATUS_SELECTOR).strip()


def send_email_via_ui(page, recipient: str) -> float:
    page.goto(BASE_URL)
    page.wait_for_selector(EMAIL_SELECTOR, timeout=15000)
    page.wait_for_selector(SEND_SELECTOR, timeout=15000)

    page.fill(EMAIL_SELECTOR, recipient)
    start = time.time()
    page.click(SEND_SELECTOR)
    _ = extract_status(page)
    return time.time() - start


def wait_for_final_status(page, timeout: int = SEND_TIMEOUT) -> str:
    start = time.time()
    status = extract_status(page)
    while status.startswith("Processing"):
        if time.time() - start > timeout:
            raise TimeoutError("Timeout waiting for async status to complete")
        time.sleep(1.0)
        page.reload()
        status = extract_status(page)
    return status


def smtp_wait_for_delivery(smtp_proc: ProcWrapper, timeout: int = SEND_TIMEOUT) -> str:
    delivery_pattern = re.escape("[Delivering message...]")
    wait_for(
        lambda: smtp_proc.grep(delivery_pattern),
        timeout=timeout,
        desc="SMTP delivery log",
    )
    return smtp_proc.snapshot()


def assert_contains(output: str, needle: str) -> None:
    assert needle in output, f"Expected to find '{needle}' in SMTP output."


# -----------------------------------------------------------------------------
# Fixtures
# -----------------------------------------------------------------------------


@pytest.fixture(scope="function")
def smtp_server(request) -> Optional[ProcWrapper]:
    needs_smtp = request.node.get_closest_marker("smtp_up") is not None
    if not needs_smtp:
        yield None
        return

    proc = ProcWrapper(
        "async-smtpd",
        [*MVN_CMD, "-q", "-pl", "async-smtpd", "compile", "exec:java"],
    )
    proc.start()
    wait_for(
        lambda: proc.grep(re.escape(SMTP_LISTEN_MARKER)),
        timeout=START_TIMEOUT,
        desc="SMTP server listen marker",
    )
    try:
        yield proc
    finally:
        proc.stop()


@pytest.fixture(scope="session")
def app_server():
    skip = os.getenv("SKIP_START_APP") == "1"
    if skip:
        yield ProcWrapper("async-service", [])
        return

    kill_port(9080)
    proc = ProcWrapper(
        "async-service",
        [*MVN_CMD, "-q", "-pl", "async-service", "spring-boot:run"],
    )
    proc.start()
    wait_for_http("localhost", 9080, START_TIMEOUT)
    yield proc
    proc.stop()


@pytest.fixture(scope="function")
def browser_page(app_server, smtp_server):
    from playwright.sync_api import sync_playwright

    with sync_playwright() as pw:
        browser = pw.chromium.launch()
        page = browser.new_page()
        try:
            yield page
        finally:
            browser.close()


@pytest.fixture(scope="function")
def clean_smtp_logs(smtp_server: Optional[ProcWrapper]):
    if smtp_server:
        smtp_server.clear_logs()
    yield
    if smtp_server:
        smtp_server.clear_logs()


# -----------------------------------------------------------------------------
# Feature: Email form
# -----------------------------------------------------------------------------


@pytest.mark.smtp_up
def test_email_form_displays_input_and_send_button(browser_page):
    page = browser_page
    page.goto(BASE_URL)
    page.wait_for_selector(EMAIL_SELECTOR, timeout=15000)
    page.wait_for_selector(SEND_SELECTOR, timeout=15000)
    assert page.is_visible(EMAIL_SELECTOR), "Email input field should be visible."
    assert page.is_visible(SEND_SELECTOR), "Send button should be visible."


# -----------------------------------------------------------------------------
# Feature: Sending emails
# -----------------------------------------------------------------------------


@pytest.mark.smtp_up
def test_send_email_to_valid_address_eventually_sent(
    browser_page, smtp_server, clean_smtp_logs
):
    page = browser_page
    send_email_via_ui(page, "duke@example.com")
    final_status = wait_for_final_status(page)
    assert final_status == "Sent"
    output = smtp_wait_for_delivery(smtp_server)
    assert_contains(output, "[Delivering message...]")


@pytest.mark.smtp_up
def test_email_is_sent_to_correct_recipient(browser_page, smtp_server, clean_smtp_logs):
    page = browser_page
    recipient = "alice@example.com"
    send_email_via_ui(page, recipient)
    final_status = wait_for_final_status(page)
    assert final_status == "Sent"
    output = smtp_wait_for_delivery(smtp_server)
    assert_contains(output, recipient)


@pytest.mark.smtp_up
def test_email_has_correct_subject(browser_page, smtp_server, clean_smtp_logs):
    page = browser_page
    send_email_via_ui(page, "duke@example.com")
    final_status = wait_for_final_status(page)
    assert final_status == "Sent"
    output = smtp_wait_for_delivery(smtp_server)
    assert_contains(output, "Subject: Test message from async example")


@pytest.mark.smtp_up
def test_email_has_x_mailer_header(browser_page, smtp_server, clean_smtp_logs):
    page = browser_page
    send_email_via_ui(page, "duke@example.com")
    final_status = wait_for_final_status(page)
    assert final_status == "Sent"
    output = smtp_wait_for_delivery(smtp_server)
    assert_contains(output, "X-Mailer: Jakarta Mail")


@pytest.mark.smtp_up
def test_email_body_contains_test_message(browser_page, smtp_server, clean_smtp_logs):
    page = browser_page
    send_email_via_ui(page, "duke@example.com")
    final_status = wait_for_final_status(page)
    assert final_status == "Sent"
    output = smtp_wait_for_delivery(smtp_server)
    assert_contains(
        output,
        "This is a test message from the async example of the Jakarta EE Tutorial.",
    )


@pytest.mark.smtp_up
def test_email_body_contains_formatted_date_time(
    browser_page, smtp_server, clean_smtp_logs
):
    page = browser_page
    send_email_via_ui(page, "duke@example.com")
    final_status = wait_for_final_status(page)
    assert final_status == "Sent"
    output = smtp_wait_for_delivery(smtp_server)

    date_time_patterns = [
        r"\b\d{4}-\d{2}-\d{2}\b.*\b\d{1,2}:\d{2}(:\d{2})?\b",
        r"\b\d{1,2}/\d{1,2}/\d{4}\b.*\b\d{1,2}:\d{2}\s?(AM|PM|am|pm)?\b",
        r"\b(Mon|Tue|Wed|Thu|Fri|Sat|Sun)\b.*\b\d{1,2}:\d{2}(:\d{2})?\b",
    ]
    assert any(
        re.search(p, output, flags=re.IGNORECASE | re.MULTILINE)
        for p in date_time_patterns
    ), "Expected email body/log to contain a formatted date and time."


# -----------------------------------------------------------------------------
# Feature: Asynchronous behavior
# -----------------------------------------------------------------------------


@pytest.mark.smtp_up
def test_send_message_behavior_returns_eventual_sent_proxy(
    browser_page, smtp_server, clean_smtp_logs
):
    page = browser_page
    elapsed = send_email_via_ui(page, "duke@example.com")
    assert elapsed < SEND_TIMEOUT, (
        "UI action should return before full async completion."
    )

    final_status = wait_for_final_status(page)
    assert final_status == "Sent"

    output = smtp_wait_for_delivery(smtp_server)
    assert_contains(output, "[Delivering message...]")


@pytest.mark.smtp_up
def test_ui_not_blocked_during_email_sending(
    browser_page, smtp_server, clean_smtp_logs
):
    page = browser_page
    elapsed = send_email_via_ui(page, "duke@example.com")
    assert elapsed <= FAST_RESPONSE_THRESHOLD_SEC, (
        f"Expected click->response in <= {FAST_RESPONSE_THRESHOLD_SEC}s, got {elapsed:.3f}s"
    )
    final_status = wait_for_final_status(page)
    assert final_status == "Sent"


# -----------------------------------------------------------------------------
# Feature: SMTP configuration
# -----------------------------------------------------------------------------


@pytest.mark.smtp_up
def test_emails_are_sent_via_smtp_port_3025(browser_page, smtp_server):
    page = browser_page
    send_email_via_ui(page, "duke@example.com")
    final_status = wait_for_final_status(page)
    assert final_status == "Sent"

    output = smtp_wait_for_delivery(smtp_server)
    # Use full snapshot (including listen marker) to verify port 3025.
    full_output = smtp_server.snapshot()
    assert SMTP_LISTEN_MARKER in full_output or SMTP_PORT_MARKER in full_output, (
        "Expected SMTP transport/logging to indicate port 3025."
    )


# -----------------------------------------------------------------------------
# Feature: Error handling
# -----------------------------------------------------------------------------


@pytest.mark.smtp_down
def test_messaging_error_returns_error_status(app_server):
    from playwright.sync_api import sync_playwright

    with sync_playwright() as pw:
        browser = pw.chromium.launch()
        page = browser.new_page()
        try:
            page.goto(BASE_URL)
            page.wait_for_selector(EMAIL_SELECTOR, timeout=15000)
            page.fill(EMAIL_SELECTOR, "duke@example.com")
            page.click(SEND_SELECTOR)

            status = wait_for_final_status(page, timeout=SEND_TIMEOUT)
            assert "Encountered an error" in status, (
                f"Expected error status, got: {status}"
            )
        finally:
            browser.close()


@pytest.mark.smtp_down
def test_messaging_error_is_logged(app_server):
    from playwright.sync_api import sync_playwright

    with sync_playwright() as pw:
        browser = pw.chromium.launch()
        page = browser.new_page()
        try:
            page.goto(BASE_URL)
            page.wait_for_selector(EMAIL_SELECTOR, timeout=15000)
            page.fill(EMAIL_SELECTOR, "duke@example.com")
            page.click(SEND_SELECTOR)
            _ = wait_for_final_status(page, timeout=SEND_TIMEOUT)
        finally:
            browser.close()

    error_marker = "Error sending mail"
    app_log = ROOT / "app.log"

    def _check_logs() -> bool:
        if app_server.proc is not None:
            return app_server.grep(re.escape(error_marker))
        # Inside Docker the CMD tees output to /app/app.log.
        try:
            return error_marker in app_log.read_text(errors="replace")
        except FileNotFoundError:
            return False

    wait_for(_check_logs, timeout=SEND_TIMEOUT, desc="app error log marker")


# -----------------------------------------------------------------------------
# Entrypoint
# -----------------------------------------------------------------------------


def main() -> int:
    return pytest.main(
        [
            __file__,
            "-v",
            "-m",
            os.getenv("PYTEST_MARK_EXPR", "smtp_up or smtp_down"),
        ]
    )


if __name__ == "__main__":
    raise SystemExit(main())


