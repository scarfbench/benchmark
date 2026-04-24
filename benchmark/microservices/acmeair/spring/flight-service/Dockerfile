FROM maven:3.9.9-eclipse-temurin-8-focal

WORKDIR /app
COPY . .

RUN if [ -f mvnw ]; then chmod +x mvnw; fi

RUN mvn clean package -DskipTests

CMD ["sh", "-c", "java -jar target/*.jar"]
