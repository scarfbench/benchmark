FROM maven:3.9.12-ibm-semeru-21-noble

USER root
# install uv
RUN apt-get update && apt-get install -y --no-install-recommends curl lsof python3-venv python3-full \
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

WORKDIR /app

# Cache Maven dependencies separately from source (multi-module)
COPY pom.xml .
COPY cart-ejb/pom.xml cart-ejb/
COPY cart-web/pom.xml cart-web/
RUN mvn dependency:go-offline -q

# Build application
COPY . .
RUN mvn install -pl cart-web -am -DskipTests -q

COPY test.sh .
RUN chmod +x test.sh

CMD ["java", "-jar", "cart-web/target/quarkus-app/quarkus-run.jar"]
