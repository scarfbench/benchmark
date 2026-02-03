# DayTrader Quarkus Smoke Tests

Playwright-based smoke tests for the DayTrader Quarkus application.

## Prerequisites

- Python 3.11+
- [uv](https://github.com/astral-sh/uv) (recommended) or pip
- DayTrader Quarkus app running on `http://localhost:8080` (or custom port)

## Quick Start

### Using uv (recommended)

```bash
# Navigate to smoke directory
cd smoke

# Install dependencies and sync
uv sync

# Install Playwright browsers
uv run playwright install chromium

# Run all smoke tests
uv run pytest smoke.py -v

# Run with custom port (e.g., 8090)
DAYTRADER_PORT=8090 uv run pytest smoke.py -v

# Run specific test
uv run pytest smoke.py::test_login_with_valid_credentials -v

# Run with headed browser (see the browser)
uv run pytest smoke.py -v --headed

# Run with slowmo to see what's happening
uv run pytest smoke.py -v --headed --slowmo=500
```

### Using pip

```bash
cd smoke
python -m venv .venv
source .venv/bin/activate  # On Windows: .venv\Scripts\activate
pip install -r requirements.txt
playwright install chromium
pytest smoke.py -v

# With custom port
DAYTRADER_PORT=8090 pytest smoke.py -v
```

## Test Categories

| Category | Tests | Description |
|----------|-------|-------------|
| Home Page | 3 | Basic page load and static resources |
| Login/Logout | 4 | Authentication flow |
| Navigation | 1 | Post-login navigation links |
| Quotes | 4 | Stock quote viewing |
| Portfolio | 2 | Portfolio viewing |
| Account | 2 | Account details |
| Trading | 2 | Buy/sell operations |
| REST API | 2 | REST endpoint verification |
| Error Handling | 2 | 404 and invalid action handling |
| Performance | 2 | Basic performance sanity |

## Configuration

The base URL defaults to `http://localhost:8080`. To test against a different URL:

```bash
# Set environment variable
export BASE_URL=http://your-server:8080

# Or modify BASE_URL in smoke.py
```

## Running in Docker

```bash
# Build the test image
docker build -f Dockerfile.test -t daytrader-smoke-tests .

# Run tests against running app
docker run --network host daytrader-smoke-tests
```

## Troubleshooting

### Tests failing with connection errors
- Ensure DayTrader Quarkus is running on port 8080
- Check: `curl http://localhost:8080/`

### Login tests failing
- Database may not be populated
- Application auto-populates on startup, but verify users exist

### Playwright browser issues
```bash
# Reinstall browsers
uv run playwright install chromium --with-deps
```
