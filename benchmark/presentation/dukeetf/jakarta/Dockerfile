FROM maven:3.9-eclipse-temurin-21

USER root
RUN apt-get update && apt-get install -y python3 curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --chown=1001:1001 pom.xml .
COPY --chown=1001:1001 src src
COPY --chown=1001:1001 smoke.py .

CMD ["mvn", "clean", "package", "liberty:run"]