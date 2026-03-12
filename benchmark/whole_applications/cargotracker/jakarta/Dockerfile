FROM maven:3.9.9-ibm-semeru-11-focal

# We’ll work as root for package installs (this is the default in this image)
USER root

# --------------------------------------------------------------------
# System deps: Python, Chromium + driver, curl (for uv installer), and Playwright dependencies
# --------------------------------------------------------------------
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    python3 python3-venv python3-pip \
    curl \
    chromium-browser chromium-chromedriver \
    libglib2.0-0 \
    libnspr4 \
    libnss3 \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libcups2 \
    libxcb1 \
    libxkbcommon0 \
    libatspi2.0-0 \
    libx11-6 \
    libxcomposite1 \
    libxdamage1 \
    libxext6 \
    libxfixes3 \
    libxrandr2 \
    libgbm1 \
    libcairo2 \
    libpango-1.0-0 \
    libasound2 \
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

# Copy Maven project
COPY pom.xml .
COPY src ./src

# Copy smoke test project (assumed to be a uv project with pyproject.toml)
COPY smoke ./smoke

# Pre-download Maven deps and build once to warm the cache
RUN mvn -B -DskipTests package

# Optionally pre-resolve Python deps so 'uv run' is fast later:
# RUN cd smoke && uv sync

# --------------------------------------------------------------------
# Default command: run the app via Maven Cargo
# You can then exec into the container to run:
#   uv run smoke/smoke.py
# --------------------------------------------------------------------
CMD ["mvn", "clean", "-DskipTests", "package", "cargo:run"]
