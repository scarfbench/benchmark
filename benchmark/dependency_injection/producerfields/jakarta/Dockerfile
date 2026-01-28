# Multi-stage build: Build stage
FROM eclipse-temurin:11-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy source code and Maven wrapper
COPY . .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:11-jre

# set smoke test env variables
ENV PRODUCER_FIELDS_BASE_URL=http://localhost:8080
ENV PRODUCER_FIELDS_HOME_URI=/producerfields

# Install Python and needed tools
RUN apt-get update && apt-get install -y python3 curl unzip && rm -rf /var/lib/apt/lists/*

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

# Set GlassFish version
ENV GLASSFISH_VERSION=7.0.16

# Download and install GlassFish
RUN curl -L -o /tmp/glassfish.zip https://github.com/eclipse-ee4j/glassfish/releases/download/${GLASSFISH_VERSION}/glassfish-${GLASSFISH_VERSION}.zip && \
    unzip /tmp/glassfish.zip -d /opt && \
    rm /tmp/glassfish.zip && \
    mv /opt/glassfish7 /opt/glassfish

# Set environment variables
ENV GLASSFISH_HOME=/opt/glassfish
ENV PATH=$PATH:$GLASSFISH_HOME/bin

# Configure the default domain to use port 8080 (it already exists)
# We don't need to create a new domain, just use the existing one

# Expose ports (default domain1 ports)
EXPOSE 8080 4848 8181

# Copy the WAR file from builder stage
COPY --from=builder /app/target/producerfields.war $GLASSFISH_HOME/glassfish/domains/domain1/autodeploy/

COPY --from=builder /app/smoke.py /opt/smoke-test/smoke.py

# Start GlassFish
CMD ["asadmin", "start-domain", "--verbose", "domain1"]
