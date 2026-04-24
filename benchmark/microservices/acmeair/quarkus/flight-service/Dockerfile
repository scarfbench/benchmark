FROM maven:3.9.9-eclipse-temurin-11-focal

WORKDIR /build

# Copy parent POM and install it
COPY pom.xml /build/parent-pom.xml
RUN mvn install -f /build/parent-pom.xml -N -DskipTests 2>/dev/null || true

# Copy and build service
WORKDIR /app
COPY flight-service/ .

RUN mvn clean package -DskipTests -Dexec.skip=true

CMD ["java", "-jar", "target/quarkus-app/quarkus-run.jar"]
