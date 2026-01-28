FROM eclipse-temurin:17-jdk

# Run everything as root (per request)
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

WORKDIR /app

# Copy Maven wrapper first (cache efficient)
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw

# Copy root pom and module poms for dependency layer caching
COPY pom.xml ./
COPY async-smtpd/pom.xml async-smtpd/pom.xml
COPY async-war/pom.xml async-war/pom.xml
RUN ./mvnw -q -DskipTests dependency:go-offline || true

# Copy full module sources
COPY async-smtpd async-smtpd
COPY async-war async-war

# Copy unified smoke test (includes Playwright logic)
COPY smoke.py .
RUN chmod +x smoke.py

EXPOSE 9080
CMD ["python3", "smoke.py"]
