FROM maven:3.9-eclipse-temurin-21

USER root
RUN apt-get update && apt-get install -y python3 && rm -rf /var/lib/apt/lists/*

# Install uv
COPY --from=ghcr.io/astral-sh/uv:latest /uv /bin/uv

RUN useradd -m -u 1001 quarkus

USER quarkus
WORKDIR /app

# Create JWT keys
RUN openssl genpkey -algorithm RSA -out privateKey.pem -pkeyopt rsa_keygen_bits:2048 \
  && openssl rsa -pubout -in privateKey.pem -out publicKey.pem

# Copy all project files
COPY --chown=1001:1001 pom.xml .
COPY --chown=1001:1001 src src

# Copy smoke test project (uv project with pyproject.toml)
COPY --chown=1001:1001 smoke ./smoke

EXPOSE 8080

# Default command matches your local workflow
CMD ["mvn", "clean", "package", "-DskipTests", "quarkus:run"]