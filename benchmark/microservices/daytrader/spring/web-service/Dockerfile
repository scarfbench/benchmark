FROM maven:3.9.9-eclipse-temurin-8-focal

WORKDIR /app
COPY . .

RUN if [ -f mvnw ]; then chmod +x mvnw; fi

RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/web-service-1.0.1.war"]
