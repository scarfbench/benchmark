def pytest_configure(config):
    config.addinivalue_line(
        "markers",
        "smtp_up: tests that require SMTP server running on port 3025",
    )
    config.addinivalue_line(
        "markers",
        "smtp_down: tests that require SMTP server to be unavailable",
    )
