FROM maven:3.9.9-eclipse-temurin-17-focal

WORKDIR /app
COPY . .

RUN mvn clean package -DskipTests

CMD ["sh", "-c", "java -jar target/*.jar"]
