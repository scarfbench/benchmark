# Multi-stage Dockerfile for Spring Boot Gradle application

# Build stage
FROM eclipse-temurin:11-jdk-jammy AS builder

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build files
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY test.gradle .
COPY lombok.config .

# Make gradlew executable
RUN chmod +x gradlew

# Copy source code
COPY src src

# Build the application (skip tests for faster build, you can remove --exclude-task test if you want to run tests)
RUN ./gradlew build --exclude-task test --no-daemon

# Runtime stage
FROM eclipse-temurin:11-jre-jammy

# Install curl for health checks (optional) and python
RUN apt-get update && apt-get install -y python3 curl && rm -rf /var/lib/apt/lists/*

# Install uv
COPY --from=ghcr.io/astral-sh/uv:latest /uv /bin/uv

# Create a non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Copy smoke test project (uv project with pyproject.toml)
COPY --chown=spring:spring smoke ./smoke

# Change ownership to spring user
RUN chown spring:spring app.jar

# Create home and cache directory
RUN mkdir -p /home/spring/.cache \
    && chown -R spring:spring /home/spring

ENV HOME=/home/spring

# Switch to non-root user
USER spring

# Expose the port the app runs on
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
