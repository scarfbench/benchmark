FROM eclipse-temurin:11-jdk

USER root
RUN apt-get update && apt-get install -y python3 && rm -rf /var/lib/apt/lists/*

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

RUN useradd -m -u 1001 liberty

# set smoke test env variables
ENV SIMPLE_GREETING_BASE_URL=http://localhost:9080
ENV SIMPLE_GREETING_HOME_URI=/simplegreeting

USER liberty
WORKDIR /app

# Copy all project files
COPY --chown=1001:1001 pom.xml .
COPY --chown=1001:1001 .mvn .mvn
COPY --chown=1001:1001 mvnw .
COPY --chown=1001:1001 src src
COPY --chown=1001:1001 smoke.py .

RUN chmod +x mvnw

EXPOSE 9080

# Default command matches your local workflow
CMD ["./mvnw", "clean", "package", "liberty:run"]

