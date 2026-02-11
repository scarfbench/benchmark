FROM maven:3.9.12-ibm-semeru-21-noble

USER root
RUN apt-get update && apt-get install -y python3 curl && rm -rf /var/lib/apt/lists/*

# set smoke test env variables
ENV BASE_URL=http://localhost:8080/standalone

WORKDIR /app

# Copy all project files
COPY --chown=1001:1001 . .

RUN chmod +x smoke.py

# Default command matches your local workflow
CMD ["mvn", "clean", "package", "spring-boot:run"]
