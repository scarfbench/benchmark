# --------------------------------------------------------------------
# DayTrader Quarkus - Development Mode
# --------------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21

WORKDIR /app

# Copy Maven wrapper and pom.xml first (better layer caching)
COPY pom.xml ./
COPY mvnw ./
COPY .mvn ./.mvn

# Make mvnw executable and download dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Expose the application port
EXPOSE 8080

# Set environment variables for Quarkus dev mode
ENV QUARKUS_HTTP_HOST=0.0.0.0

# Run Quarkus in dev mode
CMD ["./mvnw", "quarkus:dev", "-Dquarkus.analytics.disabled=true"]