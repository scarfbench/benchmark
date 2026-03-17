# Spring Boot Encoder Application

This is a Spring Boot conversion of the Jakarta EE Encoder example. This application demonstrates Spring Boot's dependency injection conditioned by the active profile.

## Original vs Spring Boot Conversion

TODO

## Building and Running

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Build
```bash
mvn clean compile
```

### Run
```bash
mvn spring-boot:run
```

The application will be available at: http://localhost:8080

### Package
```bash
mvn clean package
```

This creates an executable JAR in the `target/` directory.
