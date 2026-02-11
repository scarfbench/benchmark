# --------------------------------------------------------------------
# Base image: Maven + JDK 21 for Quarkus
# --------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21

# We'll work as root for package installs (this is the default in this image)
USER root

# --------------------------------------------------------------------
# System deps: Python, Chromium + driver, curl (for uv installer), and Playwright dependencies
# --------------------------------------------------------------------
RUN apt-get clean && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y --no-install-recommends \
    python3 python3-venv python3-pip \
    curl \
    chromium chromium-driver \
    libglib2.0-0t64 \
    libnspr4 \
    libnss3 \
    libatk1.0-0t64 \
    libatk-bridge2.0-0t64 \
    libcups2t64 \
    libxcb1 \
    libxkbcommon0 \
    libatspi2.0-0t64 \
    libx11-6 \
    libxcomposite1 \
    libxdamage1 \
    libxext6 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libcairo2 \
    libpango-1.0-0 \
    libasound2t64 \
    && rm -rf /var/lib/apt/lists/*

# --------------------------------------------------------------------
# Install uv (Python package/project manager)
# --------------------------------------------------------------------
RUN curl -LsSf https://astral.sh/uv/install.sh | sh && \
    ln -s /root/.cargo/bin/uv /usr/local/bin/uv

# --------------------------------------------------------------------
# App + smoke project
# --------------------------------------------------------------------
WORKDIR /workspace

# Copy Maven wrapper and project files
COPY pom.xml ./
COPY src ./src

# Copy smoke test project (uv project with pyproject.toml)
COPY smoke ./smoke

# Optionally pre-resolve Python deps so 'uv run' is fast later:
# RUN cd smoke && uv sync

# Build the Quarkus app once during image build
# (use -DskipTests if you want it faster)
RUN mvn -B -DskipTests clean package

# --------------------------------------------------------------------
# Default command: run the Quarkus app via Maven
# Note: Quarkus requires an external Artemis broker on port 5672
# You can then exec into the container to run:
#   cd smoke && uv run pytest smoke.py
# --------------------------------------------------------------------
CMD ["mvn", "quarkus:run"]