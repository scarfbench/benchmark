FROM eclipse-temurin:11-jdk

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
 && pip install --no-cache-dir playwright==1.47.0 pytest \
 && playwright install --with-deps chromium

 RUN useradd -m -u 1001 liberty

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