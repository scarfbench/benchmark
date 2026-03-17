# Order Management Application - Spring Boot Version

This is a Spring Boot conversion of the Jakarta EE Order Management application, maintaining the original JSF frontend while using Spring Boot for the backend services.

## Features

- **Order Management**: Create, view, and delete customer orders
- **Line Item Management**: Add parts to orders as line items
- **Part Management**: Manage parts with bill of materials (BOM) relationships
- **Vendor Management**: Search and manage vendor information
- **Vendor Part Management**: Associate parts with vendors and pricing

## Technology Stack

- **Spring Boot 3.2.0** - Main framework
- **Spring Data JPA** - Data access layer
- **JSF 4.0** - Web framework (maintaining original frontend)
- **H2 Database** - In-memory database for development
- **Maven** - Build tool

## Project Structure

```
src/main/java/com/example/orderspring/
├── entity/           # JPA entities (Part, CustomerOrder, LineItem, etc.)
├── repository/       # Spring Data JPA repositories
├── service/          # Business logic services
├── web/             # JSF managed beans
├── config/          # Spring configuration classes
└── OrderApplication.java  # Main Spring Boot application

src/main/webapp/
├── *.xhtml          # JSF pages (copied from Jakarta version)
├── resources/css/   # CSS stylesheets
└── WEB-INF/        # Web configuration files
```

## Key Changes from Jakarta EE Version

1. **Dependency Injection**: Replaced `@EJB` with `@Autowired` and Spring's dependency injection
2. **Persistence**: Replaced JTA with Spring's `@Transactional` annotations
3. **Managed Beans**: Converted JSF managed beans to Spring `@Component` beans
4. **Configuration**: Replaced `persistence.xml` with Spring Boot's `application.properties`
5. **Database**: Added H2 database for development (replaces Payara's default datasource)

## Running the Application

1. **Prerequisites**:
   - Java 17 or higher
   - Maven 3.6 or higher

2. **Build and Run**:
   ```bash
   cd persistence/spring/order
   mvn clean install
   mvn spring-boot:run
   ```

3. **Access the Application**:
   - Main application: http://localhost:8080/order.xhtml
   - H2 Database Console: http://localhost:8080/h2-console
     - JDBC URL: `jdbc:h2:mem:testdb`
     - Username: `sa`
     - Password: (empty)

## Application Features

### Order Management
- View all orders in a table format
- Create new orders with status, discount, and shipping information
- Delete existing orders
- Navigate to line items for each order

### Line Item Management
- View line items for a specific order
- Add parts to orders as line items
- Each line item includes quantity and vendor part information

### Part Management
- View all available parts
- Parts can have bill of materials (BOM) relationships
- Each part can be associated with a vendor part

### Vendor Management
- Search vendors by partial name
- View vendor information and contact details

## Database Schema

The application uses the same database schema as the original Jakarta EE version:
- `PERSISTENCE_ORDER_PART` - Parts table
- `PERSISTENCE_ORDER_CUSTOMERORDER` - Orders table
- `PERSISTENCE_ORDER_LINEITEM` - Line items table
- `PERSISTENCE_ORDER_VENDOR` - Vendors table
- `PERSISTENCE_ORDER_VENDOR_PART` - Vendor parts table

## Configuration

The application is configured through `application.properties`:
- Database connection settings
- JPA/Hibernate configuration
- JSF configuration
- Logging levels

## Development Notes

- The application automatically creates sample data on startup
- All original JSF pages and functionality are preserved
- The UI remains exactly the same as the Jakarta EE version
- Spring Boot provides embedded server, so no external application server is needed
