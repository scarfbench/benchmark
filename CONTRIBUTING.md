# Contributing to SCARFBench

This guide explains how to contribute a new application to the SCARFBench benchmark suite.

## Table of Contents

- [Getting Started](#getting-started)
- [Contribution Workflow](#contribution-workflow)
- [Overview](#overview)
- [Required Files](#required-files)
- [1. Dockerfile](#1-dockerfile)
- [2. Justfile](#2-justfile)
- [3. Smoke Test (smoke.py)](#3-smoke-test-smokepy)
- [Standard Patterns](#standard-patterns)
- [Checklist](#checklist)

## Getting Started

### Prerequisites

Before contributing, ensure you have:
- **Git** installed
- **Docker** installed and running
- **just** command runner installed ([justfile.guide](https://just.systems/))
- Basic familiarity with the three frameworks (Jakarta EE, Quarkus, Spring Boot)

### Forking the Repository

1. **Fork** the repository on GitHub by clicking the "Fork" button at the top right of the repository page
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/<your-username>/scarfbench.git
   cd scarfbench
   ```
3. **Add upstream** remote to keep your fork synchronized:
   ```bash
   git remote add upstream https://github.com/original-repo/scarfbench.git
   ```
4. **Create a branch** for your contribution:
   ```bash
   git checkout -b add-<application-name>
   ```

## Contribution Workflow

Follow these steps to contribute a new benchmark application:

### 1. Choose Application Category

Determine which category your application belongs to:
- `business_domain/` - Core business logic patterns
- `dependency_injection/` - DI and configuration patterns
- `infrastructure/` - Infrastructure services (concurrency, messaging, etc.)
- `integration/` - External system integration (batch, messaging, web services)
- `persistence/` - Database and persistence patterns
- `presentation/` - Web UI and REST API patterns
- `security/` - Authentication and authorization patterns
- `whole_applications/` - Complete, production-like applications

### 2. Create Directory Structure

Create your application directory under the appropriate category:
```bash
mkdir -p benchmark/<category>/<your-app-name>/{jakarta,quarkus,spring}
```

### 3. Implement Framework Variants

For each framework (Jakarta, Quarkus, Spring):
1. Implement the application using framework-specific patterns
2. Create `Dockerfile` (see [Dockerfile section](#1-dockerfile))
3. Create `justfile` (see [Justfile section](#2-justfile))
4. Create smoke tests - either `smoke.py` or `smoke/` folder (see [Smoke Test section](#3-smoke-test-smokepy))

### 4. Verify All Implementations

For each framework, run the complete test sequence:
```bash
cd benchmark/<category>/<your-app-name>/<framework>
just build      # Build Docker image
just up         # Start container
just test       # Run smoke tests (should PASS)
just logs       # Verify application logs
just down       # Clean up
```

Repeat for all three frameworks: `jakarta`, `quarkus`, and `spring`.

### 5. Document Your Application

Create a `README.md` in your application directory:
```bash
benchmark/<category>/<your-app-name>/README.md
```

Include:
- **Purpose**: What the application demonstrates
- **Key Features**: Technologies and patterns showcased
- **Framework Implementations**: Brief notes on each variant
- **Testing**: What the smoke tests validate

### 6. Submit Pull Request

Once all implementations are working:

1. **Commit your changes**:
   ```bash
   git add benchmark/<category>/<your-app-name>
   git commit -m "Add <application-name> benchmark for <category>"
   ```

2. **Push to your fork**:
   ```bash
   git push origin add-<application-name>
   ```

3. **Create Pull Request** on GitHub:
   - Go to your fork on GitHub
   - Click "New Pull Request"
   - Fill out the PR template (automatically populated)
   - Ensure all checklist items are completed
   - Submit for review

4. **Respond to feedback**: Maintainers may request changes or improvements

### 7. Keep Your Fork Updated

Sync with upstream before starting new contributions:
```bash
git fetch upstream
git checkout main
git merge upstream/main
git push origin main
```

## Overview

Each benchmark application in SCARFBench should support three frameworks:
- **Jakarta EE** (using Open Liberty)
- **Quarkus**
- **Spring Boot**

In addition to the source application files, each framework implementation requires three standard files:
1. `Dockerfile` - Container definition
2. `justfile` - Build and run automation
3. `smoke.py` - Automated smoke tests

**Note on Technology Choices**: This guide demonstrates examples using **Python**, **uv**, and **Playwright** for smoke tests. However, these are not strict requirements. You are free to implement smoke tests in any language (Shell, JavaScript, Go, Java, etc.) as long as your Dockerfile and justfile's `test` target work correctly. See [Alternative Smoke Test Implementations](#alternative-smoke-test-implementations) for examples in other languages.

This guide uses the following applications as reference standards: 

1. [`benchmark/infrastructure/concurrency-jobs`](benchmark/infrastructure/concurrency-jobs) for basic REST/HTTP applications
2. [`benchmark/infrastructure/ejb-async`](benchmark/infrastructure/ejb-async) for applications requiring browser automation and UI testing
3. [`benchmark/whole_applications/daytrader`](benchmark/whole_applications/daytrader) for complex, multi-framework applications with extensive smoke tests

## Required Files

### 1. Dockerfile

The Dockerfile creates a containerized environment for building and running your application.

#### Standard Structure (Basic Pattern)

For applications with simple REST/HTTP API testing:

```dockerfile
FROM maven:3.9-eclipse-temurin-21

USER root
RUN apt-get update && apt-get install -y python3 curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy all project files
COPY pom.xml .
COPY src src
COPY smoke.py .

# Default command matches your local workflow
# Framework-specific goal to be replaced accordingly
# | Framework             | Maven Goal                    |
# |-----------------------|-------------------------------|
# | Jakarta/OpenLiberty   | liberty:run                 |
# | Quarkus               | quarkus:dev                 |
# | Spring                | spring-boot:run             |
CMD ["mvn", "clean", "package", "<framework-specific-goal>"]
```

#### Playwright Pattern (For UI/Browser Testing)

For applications requiring browser automation and UI testing (reference: [`benchmark/infrastructure/ejb-async`](benchmark/infrastructure/ejb-async)):

```dockerfile
FROM maven:3.9-eclipse-temurin-21

# Run everything as root (needed for Playwright installation)
USER root

# Install Python and needed tools (add python3-venv for virtual environment support)
RUN apt-get update && apt-get install -y python3 python3-venv python3-pip curl && rm -rf /var/lib/apt/lists/*

# Create and activate virtual environment for Python dependencies (PEP 668 safe)
RUN python3 -m venv /opt/venv
ENV PATH="/opt/venv/bin:$PATH"

# Shared browsers path so Chromium is cached once
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
RUN mkdir -p /ms-playwright && chmod 755 /ms-playwright

# Install Playwright and Chromium dependencies inside venv
RUN pip install --no-cache-dir --upgrade pip setuptools wheel \
 && pip install --no-cache-dir playwright==1.47.0 \
 && playwright install --with-deps chromium

WORKDIR /app

# Copy Maven wrapper first (cache efficient)
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Copy root pom and module poms for dependency layer caching
COPY pom.xml ./
# If you have multiple modules, copy each module's pom separately:
# COPY module1/pom.xml module1/pom.xml
# COPY module2/pom.xml module2/pom.xml
RUN ./mvnw -q -DskipTests dependency:go-offline || true

# Copy full module sources
COPY src src
# If you have multiple modules:
# COPY module1 module1
# COPY module2 module2

# Copy unified smoke test (includes Playwright logic)
COPY smoke.py .
RUN chmod +x smoke.py

# For Playwright tests, CMD can run smoke.py directly or the Maven goal
# Framework-specific goal to be replaced accordingly
# | Framework             | Maven Goal                    |
# |-----------------------|-------------------------------|
# | Jakarta/OpenLiberty   | liberty:run                 |
# | Quarkus               | quarkus:dev                 |
# | Spring                | spring-boot:run             |
CMD ["mvn", "clean", "package", "<framework-specific-goal>"]
```

#### Important Notes

1. **Python3 and curl**: Always install these for smoke tests
2. **User ID 1001**: Standard non-root user for security (basic pattern only)
3. **Root User for Playwright**: Playwright installation requires root privileges; use root user when browser automation is needed
4. **Virtual Environment**: Use Python venv (`/opt/venv`) for Playwright to comply with PEP 668
5. **File Ownership**: Use `--chown=1001:1001` for all COPY commands in basic pattern; omit in Playwright pattern (root owns everything)
6. **smoke.py**: Must be copied into the container
7. **No Port Exposure**: Do NOT use `EXPOSE` directive - tests run internally via `docker exec`, not through exposed ports
8. **Maven Wrapper**: Ensure `mvnw` is executable with `chmod +x`
9. **Layer Caching**: For Playwright pattern, copy pom.xml files first to cache dependencies separately from source code
10. **Browser Path**: Set `PLAYWRIGHT_BROWSERS_PATH=/ms-playwright` to share browser binaries across builds

### 2. Justfile

The justfile provides standard commands for building, running, testing, and managing your application.

#### Standard Structure

```just
### <Application Name> (<Framework>) Justfile
APP_NAME       := "<app-name>-<framework>"
IMAGE_NAME     := "<app-name>-<framework>:latest"
SUCCESS_PATTERN := "<framework-specific-pattern>"

build:
	docker build -f Dockerfile -t {{IMAGE_NAME}} .
	@echo "[INFO] Built image: {{IMAGE_NAME}}"

rebuild:
	docker build --no-cache -f Dockerfile -t {{IMAGE_NAME}} .
	@echo "[INFO] Rebuilt image (no cache): {{IMAGE_NAME}}"

up: build
    ### The below section will look for exisiting containers and if so, remove them. 
	@if docker ps --all --quiet --filter name=^/{{APP_NAME}}$ | grep -q .; then \
		echo "[INFO] Removing existing container"; \
		docker rm -f {{APP_NAME}} >/dev/null; \
	fi
    
    ### Run the container in detached mode
	docker run -d --name {{APP_NAME}} {{IMAGE_NAME}}
	@echo "[INFO] Started {{APP_NAME}}, waiting for app to start..."

	### Check for build failure
	@until docker logs {{APP_NAME}} 2>&1 | grep -q "BUILD FAILURE"; do \
		if docker logs {{APP_NAME}} 2>&1 | grep -q "{{SUCCESS_PATTERN}}"; then \
			break; \
		fi; \
		sleep 1; \
	done
	@if docker logs {{APP_NAME}} 2>&1 | grep -q "BUILD FAILURE"; then \
		echo "[ERROR] Build failed in container:"; \
		docker logs {{APP_NAME}} 2>&1; \
		exit 1; \
	fi

	### Wait for success pattern
	@until docker logs {{APP_NAME}} 2>&1 | grep -q "{{SUCCESS_PATTERN}}"; do sleep 1; done
	@echo "[INFO] App started and ready."

logs:
	docker logs -f {{APP_NAME}}

down:
	- docker rm -f {{APP_NAME}}
	@echo "[INFO] Container removed (if it existed)"

test: up
    @docker exec {{APP_NAME}} sh -c 'python3 /app/smoke.py'

local:
	./mvnw clean package <framework-goal>
```

#### Framework-Specific Startup Patterns

The `SUCCESS_PATTERN` variable should be set based on the framework:

| Framework | Success Pattern | Example |
|-----------|-----------------|---------|
| Jakarta | `CWWKF0011I` | `SUCCESS_PATTERN := "CWWKF0011I"` |
| Quarkus | `started in.*Listening on:` | `SUCCESS_PATTERN := "started in.*Listening on:"` |
| Spring Boot | `Tomcat started on port\|Started .* in .* seconds` | `SUCCESS_PATTERN := "Tomcat started on port"` |

**Note**: These patterns are used in extended grep (`grep -E`), so use regex syntax where needed.

#### Startup Wait Loop Explained

The `up` target uses two separate checks:

1. **Build Failure Check**: Polls logs looking for `BUILD FAILURE` (a standard Maven failure message)
   - While checking, also looks for the success pattern
   - If success pattern found first, breaks out of the failure check
   - If `BUILD FAILURE` found: displays full logs and exits with error

2. **Success Pattern Wait**: If no build failure, continues waiting for the success pattern
   - Loops until the framework-specific success pattern appears in logs
   - Pattern indicates the application is fully started and ready


#### Port Notes

Applications bind to internal ports (usually `9080` or `8080`) for communication within the container. Since tests run internally via `docker exec`, **no port mapping to the host is needed**. The justfile does not expose ports, and containers run without `-p` flags.

#### Required Targets

All justfiles must include these standard targets:

- `build`: Build the Docker image
- `rebuild`: Rebuild without cache (for troubleshooting)
- `up`: Start container, wait for readiness
- `logs`: Stream container logs
- `down`: Stop and remove container
- `test`: Run smoke tests
- `local`: Run locally without Docker

### 3. Smoke Test (smoke.py or custom smoke tests)

The smoke test validates your application's core functionality. While examples below use Python, **you are free to write smoke tests in any language** - the only requirements are:

1. **Dockerfile** must exist and be able to run the tests
2. **justfile** must have a `test` target that executes the tests
3. Tests must exit with code `0` on success, non-zero on failure

This allows flexibility to use:
- Python with pytest/Playwright (recommended for complex UI testing)
- Shell scripts with curl (lightweight HTTP testing)
- JavaScript with Node.js (if you prefer JavaScript)
- Go, Java, or any other language
- Combinations of multiple test frameworks

The key requirement is that `just test` must work inside the container.

#### Standard Structure

```python
#!/usr/bin/env python3
"""Smoke test for <Application Name> (<Framework>).

Checks:
  1) Discover reachable base path
  2) <Test 1 description>
  3) <Test 2 description>
  ...

Exit codes:
  0 success, non-zero on first failure encountered.
"""
import os
import sys
import time
from urllib.request import Request, urlopen
from urllib.error import HTTPError, URLError

# Configuration
VERBOSE = os.getenv("VERBOSE") == "1"

# Base URL candidates (try in order)
CANDIDATES = [
    os.getenv("<APP>_BASE_URL"),  # Environment variable override
    "http://localhost:<internal-port>/<path>/",  # Container internal
]

def vprint(msg: str):
    """Print only if VERBOSE=1"""
    if VERBOSE:
        print(msg)

def http_request(
    method: str,
    url: str,
    data: bytes | None = None,
    headers: dict | None = None,
    timeout: int = 10,
):
    """Make HTTP request, return (status, body) or None on network error"""
    req = Request(url, data=data, method=method, headers=headers or {})
    try:
        with urlopen(req, timeout=timeout) as resp:
            status = resp.getcode()
            body = resp.read().decode("utf-8", "replace")
    except HTTPError as e:
        status = e.code
        body = e.read().decode("utf-8", "replace")
    except (URLError, Exception) as e:
        return None, f"NETWORK-ERROR: {e}"
    return (status, body), None

def discover_base() -> str:
    """Try each candidate URL, return first working one"""
    for cand in CANDIDATES:
        if not cand:
            continue
        # Try to validate candidate
        if validate_candidate(cand):
            print(f"[INFO] Base discovered: {cand}")
            return cand
    # Fallback
    for cand in CANDIDATES:
        if cand:
            print(f"[WARN] No base validated, using fallback {cand}")
            return cand
    print("[ERROR] No base URL candidates available", file=sys.stderr)
    sys.exit(2)

def validate_candidate(base: str) -> bool:
    """Implement validation logic for your app"""
    # Example: Try a health check endpoint
    pass

def main():
    start = time.time()
    base = discover_base()
    
    # Run your test sequence here
    # Example:
    # test_endpoint_1(base)
    # test_endpoint_2(base)
    
    elapsed = time.time() - start
    print(f"[PASS] Smoke sequence complete in {elapsed:.2f}s")
    return 0

if __name__ == "__main__":
    sys.exit(main())
```

#### Key Components

1. **Docstring**: Describe what the smoke test validates
2. **URL Discovery**: Try multiple URL candidates (env var, localhost variations, container ports)
3. **HTTP Helper**: Reusable function for making requests
4. **Verbose Logging**: Support `VERBOSE=1` environment variable
5. **Exit Codes**: Use specific exit codes for different failure types
6. **Pass/Fail Messages**: Clear `[PASS]`/`[FAIL]` prefixes
7. **Timing**: Report total test execution time

#### Playwright-Based Testing (Advanced)

For applications requiring browser automation and UI testing, use Playwright. Example structure:

```python
#!/usr/bin/env python3
"""Smoke test with Playwright for UI validation.

Checks:
  1) Start application server
  2) Launch browser and navigate to UI
  3) Interact with UI elements
  4) Validate UI behavior and responses

Exit codes:
  0 success, non-zero on failure.
"""
import os
import sys
import time
import subprocess
from playwright.sync_api import sync_playwright, TimeoutError as PlaywrightTimeout

VERBOSE = os.getenv("VERBOSE") == "1"
BASE_URL = "http://localhost:9080"

def vprint(msg: str):
    if VERBOSE:
        print(msg)

def start_server():
    """Start the application server"""
    vprint("[INFO] Starting application server...")
    # Example: Start Liberty server
    proc = subprocess.Popen(
        ["./mvnw", "clean", "package", "liberty:run"],
        stdout=subprocess.PIPE,
        stderr=subprocess.STDOUT,
        text=True
    )
    
    # Wait for server to be ready
    for line in proc.stdout:
        vprint(line.rstrip())
        if "CWWKF0011I" in line:  # Liberty started
            print("[INFO] Server started successfully")
            break
    
    return proc

def test_ui_with_playwright(base_url: str):
    """Test UI using Playwright browser automation"""
    print("[INFO] Launching browser...")
    
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        page = browser.new_page()
        
        try:
            # Navigate to application
            vprint(f"[INFO] Navigating to {base_url}")
            page.goto(base_url, wait_until="networkidle", timeout=30000)
            
            # Example: Check page title
            title = page.title()
            if "Expected Title" not in title:
                print(f"[FAIL] Unexpected page title: {title}", file=sys.stderr)
                return False
            print(f"[PASS] Page title: {title}")
            
            # Example: Fill form and submit
            page.fill("#inputField", "test value")
            page.click("#submitButton")
            
            # Wait for response
            page.wait_for_selector(".result", timeout=10000)
            result = page.text_content(".result")
            
            if "expected result" not in result.lower():
                print(f"[FAIL] Unexpected result: {result}", file=sys.stderr)
                return False
            print(f"[PASS] Form submission successful: {result}")
            
            return True
            
        except PlaywrightTimeout as e:
            print(f"[FAIL] Timeout: {e}", file=sys.stderr)
            return False
        except Exception as e:
            print(f"[FAIL] Error: {e}", file=sys.stderr)
            return False
        finally:
            browser.close()

def main():
    start = time.time()
    
    # Start server
    server_proc = start_server()
    
    try:
        # Run UI tests
        success = test_ui_with_playwright(BASE_URL)
        
        if not success:
            return 1
        
        elapsed = time.time() - start
        print(f"[PASS] Smoke sequence complete in {elapsed:.2f}s")
        return 0
        
    finally:
        # Clean up
        if server_proc:
            vprint("[INFO] Stopping server...")
            server_proc.terminate()
            server_proc.wait()

if __name__ == "__main__":
    sys.exit(main())
```

#### Playwright Testing Notes

1. **Browser Setup**: Use `chromium.launch(headless=True)` for CI/CD compatibility
2. **Timeouts**: Set appropriate timeouts for page loads and element waits
3. **Wait Strategies**: Use `wait_until="networkidle"` for page loads, `wait_for_selector` for elements
4. **Error Handling**: Always use try/finally to ensure browser cleanup
5. **Server Management**: Start application server before tests, terminate after
6. **Selectors**: Use stable selectors (IDs, data attributes) instead of classes
7. **Screenshots**: Capture on failure for debugging: `page.screenshot(path="error.png")`
8. **Virtual Display**: In Docker, browsers run headless automatically

#### Best Practices

1. **Test Real Functionality**: Don't just check if the server responds - validate actual behavior
2. **Multiple Scenarios**: Test with and without authentication, different inputs, etc.
3. **Meaningful Assertions**: Check status codes AND response content
4. **Clear Error Messages**: Include URL, status, and response body in failures
5. **Early Exit**: Exit on first failure with specific exit code
6. **Timeout Handling**: Use reasonable timeouts (10s default)
7. **Network Resilience**: Handle both HTTP errors and network failures

#### Example Test Pattern

```python
def test_endpoint(base: str, description: str, path: str, 
                  method: str = "GET", expected_status: int = 200,
                  expected_content: str = None, headers: dict = None):
    """Reusable test function"""
    url = f"{base.rstrip('/')}{path}"
    resp, err = http_request(method, url, headers=headers)
    
    if err:
        print(f"[FAIL] {description}: {err}", file=sys.stderr)
        sys.exit(1)
    
    status, body = resp
    body_stripped = body.strip()
    
    if status != expected_status:
        print(f"[FAIL] {description}: Expected {expected_status}, got {status}", 
              file=sys.stderr)
        sys.exit(1)
    
    if expected_content and expected_content not in body_stripped:
        print(f"[FAIL] {description}: Expected content '{expected_content}' not found",
              file=sys.stderr)
        sys.exit(1)
    
    print(f"[PASS] {description}")
```

#### Smoke Test Folder Structure (For Complex Applications)

For large, multi-framework applications (reference: [`benchmark/whole_applications/daytrader`](benchmark/whole_applications/daytrader)), organize smoke tests in a dedicated folder with multiple test files and a dependency manifest:

**Folder Structure:**
```
<application>/<framework>/smoke/
├── smoke.py              # Main entry point with pytest fixtures
├── test_*.py             # Individual test modules
├── playwright-test-cases.md  # Documentation of test scenarios
├── pyproject.toml        # Python dependencies (pytest, pytest-playwright, etc.)
├── pytest.ini            # Pytest configuration
├── conftest.py           # Pytest fixtures and configuration
└── README.md             # Test documentation
```

**pyproject.toml** (using `uv` for modern dependency management - recommended):
```toml
[project]
name = "smoke"
version = "0.1.0"
description = "Smoke tests for <Application>"
requires-python = ">=3.11"
dependencies = [
    "playwright>=1.47.0",
    "pytest>=8.0.0,<9.0.0",
    "pytest-playwright>=0.7.0",
]

[tool.pytest.ini_options]
testpaths = ["."]
python_files = ["test_*.py", "smoke.py"]

# Optional: specify script entry point
[project.scripts]
smoke = "smoke:main"
```

**conftest.py** (pytest fixtures):
```python
import os
import pytest
from playwright.sync_api import sync_playwright

BASE_URL = os.getenv("BASE_URL", "http://localhost:9080/app")

@pytest.fixture
def page():
    """Provide a Playwright page fixture for all tests"""
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context()
        page = context.new_page()
        yield page
        browser.close()

@pytest.fixture
def base_url():
    """Provide base URL to all tests"""
    return BASE_URL
```

**smoke.py** (example with pytest markers):
```python
import pytest
from playwright.sync_api import Page, expect

BASE_URL = "http://localhost:9080/app"

@pytest.mark.smoke
def test_home_page_loads(page: Page) -> None:
    """Test that home page loads successfully"""
    page.goto(BASE_URL, wait_until="domcontentloaded")
    expect(page).to_have_title("Application Title")

@pytest.mark.smoke
def test_navigation_links(page: Page) -> None:
    """Test that main navigation links are present"""
    page.goto(BASE_URL, wait_until="domcontentloaded")
    links = page.get_by_role("link")
    assert links.count() > 0

@pytest.mark.smoke
def test_login_form(page: Page) -> None:
    """Test login functionality"""
    page.goto(f"{BASE_URL}/login", wait_until="domcontentloaded")
    page.fill('input[name="username"]', "testuser")
    page.fill('input[name="password"]', "password")
    page.click('button[type="submit"]')
    # Assert successful login
    assert page.url != f"{BASE_URL}/login"
```

**Dockerfile Integration** (using `uv` for fast, reproducible test environments - recommended):
```dockerfile
FROM maven:3.9-eclipse-temurin-21

USER root
RUN apt-get update && apt-get install -y python3 curl && rm -rf /var/lib/apt/lists/*

# Install uv for fast Python dependency management
RUN curl -sSL https://astral.sh/uv/install.sh | sh
ENV PATH="$HOME/.cargo/bin:$PATH"

WORKDIR /app

# Copy application files
COPY pom.xml .
COPY src src
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Copy entire smoke test folder (includes pyproject.toml, uv.lock, test files)
COPY smoke smoke

CMD ["./mvnw", "clean", "package", "<framework-specific-goal>"]
```

**justfile Integration** (using `uv` with Playwright - recommended for complex apps):
```just
### <Application Name> (<Framework>) Justfile
APP_NAME        := "<app-name>-<framework>"
IMAGE_NAME      := "<app-name>-<framework>:latest"
APP_PORT        := "9080"

build:
	docker build -f Dockerfile -t {{IMAGE_NAME}} .
	@echo "[INFO] Built image: {{IMAGE_NAME}}"

rebuild:
	docker build --no-cache -f Dockerfile -t {{IMAGE_NAME}} .
	@echo "[INFO] Rebuilt image (no cache): {{IMAGE_NAME}}"

up: build
	- docker rm -f {{APP_NAME}} 2>/dev/null || true
	docker run -d --name {{APP_NAME}} {{IMAGE_NAME}}
	@echo "[INFO] Started {{APP_NAME}} (internal port {{APP_PORT}})..."
	@echo "[INFO] Waiting for application to start..."
	@until docker logs {{APP_NAME}} 2>&1 | grep -q "<startup-pattern>"; do sleep 1; done
	@echo "[INFO] Application started, port {{APP_PORT}} is ready."

logs:
	docker logs -f {{APP_NAME}}

down:
	- docker rm -f {{APP_NAME}}
	@echo "[INFO] Container {{APP_NAME}} removed (if it existed)."

# Run Playwright-based smoke tests inside the running container with uv
smoke: up
	@echo "[INFO] Running smoke tests inside container {{APP_NAME}}..."
	docker exec {{APP_NAME}} bash -lc "cd smoke && uv sync && uv run playwright install chromium && uv run pytest -v --tb=short"

test: smoke

local:
	./mvnw clean package <framework-specific-goal>
```

**Key Benefits of Folder Structure:**

1. **Scalability**: Organize tests into multiple files as test suite grows
2. **Modularity**: Separate concerns (auth tests, API tests, UI tests, etc.)
3. **pytest Features**: Use markers, fixtures, and parameterization
4. **Dependencies**: Centralized management in `pyproject.toml` with `uv.lock` for reproducibility
5. **Documentation**: Each test file and test case is self-documenting
6. **CI/CD Friendly**: pytest integrates with most CI/CD systems
7. **Fast Dependency Sync**: `uv sync` is significantly faster than `pip install`

**Running Tests Locally (with `uv` - recommended):**
```bash
cd smoke
uv sync                       # Install dependencies from pyproject.toml
uv run pytest -v              # Run all tests
uv run pytest -m smoke       # Run only smoke tests
uv run pytest test_login.py  # Run specific test file
uv run pytest -v --tb=short  # Run with short traceback
VERBOSE=1 uv run pytest -v   # Run with verbose output
```

**Running Tests Locally (with pip - legacy approach):**
```bash
cd smoke
pip install -e .              # Install from pyproject.toml
pytest -v                     # Run all tests
pytest -m smoke              # Run only smoke tests
```

#### Alternative Smoke Test Implementations

While Python with pytest is recommended for complex applications, here are other valid approaches:

**Shell Script Example** (simple HTTP testing):
```bash
#!/bin/bash
# smoke.sh - Lightweight smoke test using curl

set -e
BASE_URL="http://localhost:9080/app"

echo "[INFO] Testing home page..."
curl -sf "$BASE_URL/" > /dev/null || exit 1

echo "[INFO] Testing API endpoint..."
curl -sf "$BASE_URL/api/status" | grep -q "ok" || exit 1

echo "[PASS] Smoke tests complete"
exit 0
```

**JavaScript/Node.js Example** (with Jest):
```javascript
// smoke.test.js
const axios = require('axios');

const BASE_URL = 'http://localhost:9080/app';

describe('Smoke Tests', () => {
  test('home page loads', async () => {
    const response = await axios.get(BASE_URL);
    expect(response.status).toBe(200);
    expect(response.data).toContain('app');
  });

  test('API endpoint responds', async () => {
    const response = await axios.get(`${BASE_URL}/api/status`);
    expect(response.data.status).toBe('ok');
  });
});
```

**justfile Integration** (shell script example):
```just
smoke: up
	@echo "[INFO] Running smoke tests..."
	docker exec {{APP_NAME}} bash -c "cd /app && bash smoke.sh"

test: smoke
```

**Key Point**: The implementation language doesn't matter, as long as:
- The Dockerfile includes necessary runtime and dependencies
- The `test` target in justfile successfully runs the tests within the container
- Tests exit with appropriate exit codes (0 = success)

## Standard Patterns

### Directory Structure

For **simple applications** (single smoke.py):
```
benchmark/<category>/<application-name>/
├── jakarta/
│   ├── Dockerfile
│   ├── justfile
│   ├── smoke.py
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── .mvn/
│   └── src/
├── quarkus/
│   ├── Dockerfile
│   ├── justfile
│   ├── smoke.py
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── .mvn/
│   └── src/
└── spring/
    ├── Dockerfile
    ├── justfile
    ├── smoke.py
    ├── pom.xml
    ├── mvnw
    ├── mvnw.cmd
    ├── .mvn/
    └── src/
```

For **complex applications** (smoke folder with pytest):
```
benchmark/<category>/<application-name>/
├── jakarta/
│   ├── Dockerfile
│   ├── justfile
│   ├── pom.xml
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── .mvn/
│   ├── src/
│   └── smoke/                      # Folder instead of single file
│       ├── smoke.py
│       ├── test_home.py
│       ├── test_auth.py
│       ├── test_api.py
│       ├── conftest.py
│       ├── pytest.ini
│       ├── pyproject.toml
│       ├── playwright-test-cases.md
│       └── README.md
├── quarkus/
│   ├── ... (same structure)
│   └── smoke/
│       └── ... (same structure)
└── spring/
    ├── ... (same structure)
    └── smoke/
        └── ... (same structure)
```

### Naming Conventions

1. **Container Names**: `<app>-<framework>` (e.g., `jobs-jakarta`, `jobs-quarkus`)
2. **Image Names**: `<app>-<framework>:latest`
3. **Variables**: Use uppercase with underscores (e.g., `APP_NAME`, `IMAGE_NAME`)
4. **Paths**: Use consistent endpoint paths across frameworks

### Common Variables

These should appear in every justfile:

```just
APP_NAME       := "<app>-<framework>"
IMAGE_NAME     := "<app>-<framework>:latest"
```

## Checklist

Before submitting your contribution, verify:

### Dockerfile
- [ ] Uses appropriate JDK base image for framework
- [ ] Installs `python3` and `curl`
- [ ] **Basic pattern**: Creates non-root user with UID 1001
- [ ] **Playwright pattern**: Runs as root and installs Python venv, pip, and Playwright
- [ ] **Playwright pattern**: Sets up `PLAYWRIGHT_BROWSERS_PATH` and installs Chromium
- [ ] Copies all necessary files with correct ownership (use `--chown=1001:1001` for basic pattern)
- [ ] Makes `mvnw` executable
- [ ] Does NOT include `EXPOSE` directive (tests run internally)
- [ ] Uses correct Maven goal in CMD
- [ ] **Playwright pattern**: Implements layer caching for pom.xml files

### Justfile
- [ ] All variables defined at top (APP_NAME, IMAGE_NAME)
- [ ] `build` target works
- [ ] `rebuild` target works (no cache)
- [ ] `up` target waits for correct startup pattern
- [ ] `logs` target streams container logs
- [ ] `down` target cleans up container
- [ ] `test` target runs smoke tests successfully
- [ ] `local` target runs app without Docker

### Smoke Test (smoke.py)
- [ ] Has descriptive docstring
- [ ] Includes shebang `#!/usr/bin/env python3`
- [ ] Has URL discovery with multiple candidates
- [ ] Supports `VERBOSE=1` environment variable
- [ ] Tests real application functionality (not just health checks)
- [ ] Uses clear `[PASS]`/`[FAIL]` messages
- [ ] Exits with code 0 on success, non-zero on failure
- [ ] Reports execution time
- [ ] Handles network errors gracefully
- [ ] Works both inside container and from host
- [ ] **Playwright tests**: Properly manages browser lifecycle (launch/close)
- [ ] **Playwright tests**: Uses appropriate timeouts and wait strategies
- [ ] **Playwright tests**: Handles server startup and shutdown

### Testing
- [ ] `just build` succeeds for all frameworks
- [ ] `just up` starts container and waits for ready state
- [ ] `just test` passes all smoke tests
- [ ] `just logs` shows application logs
- [ ] `just down` cleans up successfully
- [ ] `just local` runs application locally
- [ ] All three frameworks (jakarta, quarkus, spring) work identically
- [ ] **Playwright tests**: Browser tests pass in headless mode
- [ ] **Playwright tests**: Tests work both inside container and from host

### Documentation
- [ ] Add README.md explaining the application's purpose
- [ ] Document any special configuration or requirements
- [ ] Document if Playwright is used for UI testing
- [ ] Update main benchmark documentation if needed

## Tips and Troubleshooting

### Debugging

**Test startup pattern**: `docker logs <container> 2>&1 | grep -q "<pattern>"`

**Run smoke test manually**: `docker exec <container> python3 /app/smoke.py`

**Verbose output**: `VERBOSE=1 python3 smoke.py`

**Playwright debugging**: Set `headless=False` and use `page.pause()`

### Common Issues

**Container won't start**: Check logs with `docker logs <container>`

**Smoke test fails**: Run with `VERBOSE=1` to see detailed request/response

**Startup timeout**: Increase sleep or check startup pattern regex

**Permission denied**: Verify file ownership matches user UID 1001 (basic pattern only)

**Playwright browser won't launch**: Ensure you're running as root in Dockerfile or install system dependencies

**Playwright timeout**: Increase timeout values or check if page is actually loading

**Python package conflicts**: Use virtual environment with `python3 -m venv` (required for Playwright)

**Chromium not found**: Verify `playwright install chromium` ran successfully and `PLAYWRIGHT_BROWSERS_PATH` is set

## Quick Start

```bash
# 1. Create structure
mkdir -p benchmark/<category>/<app>/{jakarta,quarkus,spring}

# 2. Implement first framework
cd benchmark/<category>/<app>/jakarta
# ... create Dockerfile, justfile, smoke.py

# 3. Test
just build && just up && just test

# 4. Adapt to other frameworks
# 5. Submit PR
```

## Reference Implementations

- **Basic pattern**: [`benchmark/infrastructure/concurrency-jobs`](benchmark/infrastructure/concurrency-jobs)
- **Playwright pattern**: [`benchmark/infrastructure/ejb-async`](benchmark/infrastructure/ejb-async)
- **Complex multi-test**: [`benchmark/whole_applications/daytrader`](benchmark/whole_applications/daytrader)
