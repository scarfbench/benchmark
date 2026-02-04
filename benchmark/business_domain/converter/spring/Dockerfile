FROM maven:3.9.12-ibm-semeru-21-noble

USER root
RUN apt-get update && apt-get install -y python3 curl && rm -rf /var/lib/apt/lists/*

# Install uv
COPY --from=ghcr.io/astral-sh/uv:latest /uv /bin/uv

# Create virtual environment
RUN uv venv /venv
ENV PATH="/venv/bin:$PATH"

# Shared browsers path so Chromium is cached once
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
RUN mkdir -p /ms-playwright && chmod 755 /ms-playwright
# Install Playwright and Chromium dependencies
RUN uv pip install --no-cache-dir playwright==1.47.0 pytest && playwright install --with-deps chromium
# set smoke test env variables
ENV CONVERTER_BASE_URL=http://localhost:8080
ENV CONVERTER_HOME_URI=/converter

WORKDIR /app

# Copy all project files
COPY --chown=1001:1001 . .

RUN chmod +x smoke.py

# Default command matches your local workflow
CMD ["mvn", "clean", "package", "spring-boot:run"]
