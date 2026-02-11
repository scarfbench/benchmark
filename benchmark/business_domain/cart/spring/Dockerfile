FROM maven:3.9.12-ibm-semeru-21-noble

USER root
RUN apt-get update && apt-get install -y python3 curl && rm -rf /var/lib/apt/lists/*
WORKDIR /app

# Copy all project files
COPY --chown=1001:1001 . .

RUN chmod +x smoke.py

# Build and install modules, then run the packaged jar
CMD ["bash", "-lc", "mvn -pl cart-web -am clean install && java -jar cart-web/target/cart-web.jar"]
