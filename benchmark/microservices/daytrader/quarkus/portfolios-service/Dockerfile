FROM maven:3.9.9-eclipse-temurin-17-focal

WORKDIR /app
COPY . .

RUN if [ -f mvnw ]; then chmod +x mvnw; fi

RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/quarkus-app/quarkus-run.jar"]
