FROM maven:3.9.12-ibm-semeru-21-noble

USER root
# install uv
RUN apt-get update && apt-get install -y --no-install-recommends curl lsof python3-venv python3-full     && curl -LsSf https://astral.sh/uv/install.sh | sh

ENV PATH="/root/.local/bin:$PATH"

# Shared browsers path so Chromium is cached once
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright
RUN mkdir -p /ms-playwright && chmod 755 /ms-playwright

# create venv and install
RUN uv venv /opt/venv     && uv pip install --python /opt/venv/bin/python playwright==1.47.0 pytest

ENV PATH="/opt/venv/bin:$PATH"
RUN playwright install --with-deps chromium

# Install ActiveMQ Artemis (required as AMQP broker for JMS messaging)
ENV ARTEMIS_VERSION=2.31.2
ENV ARTEMIS_HOME=/opt/artemis
RUN curl -L -o /tmp/artemis.tar.gz "https://archive.apache.org/dist/activemq/activemq-artemis/${ARTEMIS_VERSION}/apache-artemis-${ARTEMIS_VERSION}-bin.tar.gz" \
    && mkdir -p ${ARTEMIS_HOME} \
    && tar xzf /tmp/artemis.tar.gz -C ${ARTEMIS_HOME} --strip-components=1 \
    && rm /tmp/artemis.tar.gz \
    && ${ARTEMIS_HOME}/bin/artemis create /var/lib/artemis-broker \
       --user quarkus --password quarkus --allow-anonymous --no-web --silent

# Set working directory
WORKDIR /app

# Copy all the assets
COPY . .

RUN if [ -f mvnw ]; then chmod +x mvnw; fi && if [ -f smoke.py ]; then chmod +x smoke.py; fi

# Build the application at image build time
RUN mvn clean install -DskipTests

# Start Artemis broker in background, then run Quarkus
CMD /var/lib/artemis-broker/bin/artemis-service start && mvn quarkus:run
