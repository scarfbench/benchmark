# Spring Boot Bill Payment Application

This is a Spring Boot conversion of the Jakarta EE CDI Bill Payment example. This application demonstrates Spring Boot's dependency injection, event handling, and AOP capabilities.

## Original vs Spring Boot Conversion

### Architecture Changes

| Jakarta EE Feature                     | Spring Boot Equivalent                     |
| -------------------------------------- | ------------------------------------------ |
| `@Named` + `@SessionScoped`            | `@Controller`                              |
| `@Inject`                              | `@Autowired` (via constructor injection)   |
| `@Observes`                            | `@EventListener`                           |
| `Event<PaymentEvent>.fire()`           | `ApplicationEventPublisher.publishEvent()` |
| `@InterceptorBinding` + `@Interceptor` | `@Aspect` + `@Around`                      |
| `@Qualifier`                           | `PaymentType` enum                         |
| JSF XHTML pages                        | Thymeleaf HTML templates                   |
| WAR packaging                          | JAR packaging                              |

### Key Components

1. **BillPaymentApplication**: Main Spring Boot application class
2. **PaymentController**: Web controller handling HTTP requests (replaces JSF managed bean)
3. **PaymentService**: Business service handling payment logic
4. **PaymentEvent**: Event object (similar to original)
5. **PaymentHandler**: Event listener (replaces CDI observer methods)
6. **LoggingAspect**: AOP aspect for method logging (replaces CDI interceptor)
7. **Thymeleaf templates**: Web pages (replaces JSF XHTML)

### Features Demonstrated

- **Dependency Injection**: Constructor-based injection using Spring's IoC container
- **Event Publishing**: Application events using Spring's event system
- **AOP (Aspect-Oriented Programming)**: Method logging using Spring AOP
- **Web Layer**: Spring MVC with Thymeleaf templating
- **Validation**: Bean validation using `@Validated` and `@Digits`

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

## Usage

1. Navigate to http://localhost:8080
2. Enter a payment amount
3. Select Debit Card or Credit Card
4. Click "Pay" to process the payment
5. View the result page
6. Check the console logs for event handling and AOP logging output

## Key Differences from Jakarta EE Version

1. **Standalone Application**: Runs as a standalone Spring Boot application (no application server required)
2. **Embedded Server**: Uses embedded Tomcat server
3. **Simplified Configuration**: Uses Spring Boot auto-configuration
4. **Modern Java**: Uses Java 17 features and modern Spring practices
5. **REST-ready**: Can easily be extended to provide REST endpoints
6. **Production-ready**: Includes Spring Boot Actuator capabilities for monitoring

## Event Flow

1. User submits payment form → `PaymentController.pay()`
2. Controller calls → `PaymentService.processPayment()`
3. Service publishes → `PaymentEvent` 
4. Spring delivers event → `PaymentHandler.handlePaymentEvent()`
5. Handler processes the payment based on type (Credit/Debit)
6. AOP logging intercepts method calls throughout the flow
