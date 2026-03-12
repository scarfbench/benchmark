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
RUN uv venv /opt/venv \
    && uv pip install --python /opt/venv/bin/python playwright==1.47.0 pytest

ENV PATH="/opt/venv/bin:$PATH"
RUN playwright install --with-deps chromium

# Initialize PostgreSQL database at build time
RUN pg_lsclusters | tail -1 | awk '{print $1, $2}' > /tmp/pgver.txt \
    && PG_VERSION=$(cut -d' ' -f1 /tmp/pgver.txt) \
    && PG_CLUSTER=$(cut -d' ' -f2 /tmp/pgver.txt) \
    && pg_ctlcluster ${PG_VERSION} ${PG_CLUSTER} start \
    && su - postgres -c "psql -c \"CREATE USER petclinic_jakartaee WITH PASSWORD 'petclinic_jakartaeepwd';\"" \
    && su - postgres -c "psql -c \"CREATE DATABASE petclinic_jakartaee_liberty OWNER petclinic_jakartaee;\"" \
    && su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE petclinic_jakartaee_liberty TO petclinic_jakartaee;\"" \
    && pg_ctlcluster ${PG_VERSION} ${PG_CLUSTER} stop

# Set working directory
WORKDIR /app

# Copy all the assets
COPY . .

RUN if [ -f mvnw ]; then chmod +x mvnw; fi && if [ -f smoke.py ]; then chmod +x smoke.py; fi

CMD pg_lsclusters | tail -1 | awk '{print $1, $2}' > /tmp/pgver.txt \
    && PG_VERSION=$(cut -d' ' -f1 /tmp/pgver.txt) \
    && PG_CLUSTER=$(cut -d' ' -f2 /tmp/pgver.txt) \
    && pg_ctlcluster ${PG_VERSION} ${PG_CLUSTER} start \
    && mvn clean liberty:run
