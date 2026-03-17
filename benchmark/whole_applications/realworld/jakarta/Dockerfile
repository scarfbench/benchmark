FROM maven:3.9.12-ibm-semeru-21-noble

USER root
# install uv and PostgreSQL
RUN apt-get update && apt-get install -y --no-install-recommends curl python3-venv python3-full postgresql postgresql-client \
    && curl -LsSf https://astral.sh/uv/install.sh | sh

ENV PATH="/root/.local/bin:$PATH"

# Shared browsers path so Chromium is cached once
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
RUN mkdir -p /ms-playwright && chmod 755 /ms-playwright

# create venv and install
RUN uv venv /opt/venv     && uv pip install --python /opt/venv/bin/python playwright==1.47.0 pytest

ENV PATH="/opt/venv/bin:$PATH"
RUN playwright install --with-deps chromium

# Set working directory
WORKDIR /app

# Copy all the assets
COPY . .

RUN if [ -f mvnw ]; then chmod +x mvnw; fi && if [ -f smoke.py ]; then chmod +x smoke.py; fi && if [ -f test.sh ]; then chmod +x test.sh; fi

CMD set -e && \
    pg_ctlcluster $(pg_lsclusters -h | head -1 | awk '{print $1, $2}') start && \
    su - postgres -c "psql -c \"CREATE USER postgres_user WITH PASSWORD 'S3cret';\"" 2>/dev/null || true && \
    su - postgres -c "psql -c \"CREATE DATABASE postgres_db OWNER postgres_user;\"" 2>/dev/null || true && \
    su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE postgres_db TO postgres_user;\"" 2>/dev/null || true && \
    exec mvn clean liberty:run
