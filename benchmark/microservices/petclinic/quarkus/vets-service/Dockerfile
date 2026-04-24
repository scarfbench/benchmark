FROM maven:3.9.9-eclipse-temurin-17-focal

WORKDIR /build

COPY pom.xml /build/parent-pom.xml
RUN mvn install -f /build/parent-pom.xml -N -DskipTests 2>/dev/null || true

WORKDIR /app
COPY vets-service/ .

RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/quarkus-app/quarkus-run.jar"]
