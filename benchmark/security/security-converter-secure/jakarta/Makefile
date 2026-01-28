# Jakarta EE Security API Project Justfile
# Run `make` to see available recipes

# Set shell
SHELL := /bin/bash

# Default recipe - shows available commands
default:
	@make help || make --help

# Build the WAR file using Maven
build:
	@echo "Building the converter-secure WAR file..."
	./mvnw clean package

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	./mvnw clean

# Compile without packaging
compile:
	@echo "Compiling Java sources..."
	./mvnw compile

# Run tests
test:
	@echo "Running tests..."
	./mvnw test

# Build Docker image
docker-build:
	@echo "Building Docker image..."
	just build
	docker build -t converter-secure-glassfish7:latest .

# Start application with Docker-Compose
up:
	@echo "Starting application with Docker-Compose..."
	docker-compose up -d

# Stop Docker-Compose services
down:
	@echo "Stopping Docker-Compose services..."
	docker-compose down

# View Docker-Compose logs
logs:
	@echo "Viewing application logs..."
	docker-compose logs -f

# Show application status
status:
	@echo "Checking application status..."
	docker-compose ps

# Open application in browser
open:
	@echo "Opening application in browser..."
	open http://localhost:10089/converter-secure/

# Restart application
restart:
	@echo "Restarting application..."
	just down
	just up

# Build and deploy (docker-build + up)
deploy: docker-build up
	@echo "Application deployed successfully!"
	@echo ""
	@echo "Access the application at: http://localhost:10089/converter-secure/"
	@echo "Access GlassFish admin console at: http://localhost:10155/"

# Undeploy from running GlassFish container
undeploy:
	@echo "Undeploying application from GlassFish..."
	docker-compose exec glassfish asadmin undeploy converter-secure

# Check container logs for errors
check-logs:
	@echo "Checking recent logs for errors..."
	docker-compose logs --tail=50 glassfish | grep -i error || echo "No recent errors found"

# Show Maven dependencies
deps:
	@echo "Showing Maven dependencies..."
	./mvnw dependency:tree

# Format code (if applicable)
format:
	@echo "Formatting code..."
	./mvnw spotless:apply || echo "No formatter configured"

# Run security check
security:
	@echo "Running security check..."
	./mvnw org.owasp:dependency-check-maven:check || echo "No security check plugin configured"

# Package and show WAR contents
package-info:
	@echo "Building and showing WAR contents..."
	./mvnw clean package -q
	@echo "WAR file contents:"
	jar -tf target/converter-secure-10-SNAPSHOT.war | head -20
	@echo "..."
	@echo "WAR file size: $(du -h target/converter-secure-10-SNAPSHOT.war | cut -f1)"

# Help command with more detailed descriptions
help:
	@echo "Available commands:"
	@echo ""
	@echo "Build & Test:"
	@echo "  build         - Build the WAR file using Maven"
	@echo "  clean         - Clean build artifacts"
	@echo "  compile       - Compile Java sources only"
	@echo "  test          - Run unit tests"
	@echo ""
	@echo "Docker & Deployment:"
	@echo "  docker-build  - Build Docker image"
	@echo "  up            - Start application with Docker-Compose"
	@echo "  down          - Stop Docker-Compose services"
	@echo "  restart       - Restart the application"
	@echo "  logs          - View application logs"
	@echo "  status        - Show container status"
	@echo ""
	@echo "Development:"
	@echo "  open          - Open application in browser"
	@echo "  deploy        - Deploy WAR to running container"
	@echo "  undeploy      - Undeploy from running container"
	@echo "  check-logs    - Check for recent errors"
	@echo "  deps          - Show Maven dependencies"
	@echo "  package-info  - Show WAR contents and size"
