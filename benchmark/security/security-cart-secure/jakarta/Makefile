# Jakarta EE Cart Secure EJB Application Justfile
# Run `make` to see available recipes

# Set shell
SHELL := /bin/bash

# Default recipe - shows available commands
default:
	@make help || make --help

# Build the EAR file using Maven
build:
	@echo "Building the Cart EJB EAR file..."
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
	docker build -t security-cart-secure-glassfish:latest .

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

# Restart application
restart:
	@echo "Restarting application..."
	just down
	just up

# Build and deploy (docker-build + up)
deploy: docker-build up
	@echo "Application deployed successfully!"
	@echo ""
	@echo "Cart EJB application is now running."
	@echo "Access GlassFish admin console at: http://localhost:10154/"

# Verify EJB deployment and functionality (complete verification)
verify:
	@echo "Starting verification process..."
	@echo "Step 1: Deploying application silently..."
	@just deploy > /dev/null 2>&1 || (echo "❌ Silent deployment failed" && exit 1)
	@echo "✓ Application deployed successfully"
	@echo ""
	@echo "Step 2: Running verification scripts..."
	@just setup-scripts > /dev/null 2>&1 || echo "Setting up verification scripts..."
	docker-compose exec -T glassfish /tmp/verify-all.sh
	@echo ""
	@echo "Step 3: Cleaning up - dedeploying and releasing ports..."
	@just down > /dev/null 2>&1
	@echo "✓ Application dedeployed and ports released"
	@echo ""
	@echo "🎉 Verification process completed successfully!"

# Interactive verification menu
menu:
	@echo "Starting interactive verification menu..."
	./smoke/interactive-menu.sh

# Setup all verification scripts in container
setup-scripts:
	@echo "Setting up all verification scripts..."
	./smoke/setup-scripts.sh

# Individual verification commands
verify-glassfish:
	@echo "Checking GlassFish server status..."
	docker-compose exec -T glassfish /tmp/check-glassfish-status.sh

verify-apps:
	@echo "Checking deployed applications..."
	docker-compose exec -T glassfish /tmp/list-applications.sh

verify-ejb:
	@echo "Checking EJB container status..."
	docker-compose exec -T glassfish /tmp/check-ejb-container.sh

verify-jndi:
	@echo "Checking EJB JNDI bindings (requires deployed application)..."
	@just _check-deployment
	docker-compose exec -T glassfish /tmp/check-ejb-jndi.sh

verify-security:
	@echo "Checking security configuration..."
	docker-compose exec -T glassfish /tmp/check-security-config.sh

# List deployed applications
list-apps:
	@echo "Listing deployed applications..."
	docker-compose exec -T glassfish asadmin list-applications

# Show EJB JNDI entries
show-jndi:
	@echo "Showing EJB JNDI entries..."
	docker-compose exec -T glassfish asadmin list-jndi-entries --context java:global

# Undeploy from running GlassFish container
undeploy:
	@echo "Undeploying Cart EJB application from GlassFish..."
	docker-compose exec -T glassfish asadmin undeploy cart-secure-ejb-only

# Deploy manually (if autodeploy fails)
manual-deploy:
	@echo "Manually deploying Cart EJB application..."
	docker-compose exec -T glassfish asadmin deploy --name cart-secure-ejb-only /opt/glassfish/glassfish/domains/domain1/autodeploy/cart-secure-ear-10-SNAPSHOT.ear

# Check container logs for errors
check-logs:
	@echo "Checking recent logs for errors..."
	docker-compose logs --tail=50 glassfish | grep -i error || echo "No recent errors found"

# Show Maven dependencies
deps:
	@echo "Showing Maven dependencies..."
	./mvnw dependency:tree

# Package and show EAR contents
package-info:
	@echo "Building and showing EAR contents..."
	./mvnw clean package -q
	@echo "EAR file contents:"
	jar -tf cart-secure-ear/target/cart-secure-ear-10-SNAPSHOT.ear
	@echo ""
	@echo "EAR file size: $(du -h cart-secure-ear/target/cart-secure-ear-10-SNAPSHOT.ear | cut -f1)"

# Show EJB information
ejb-info:
	@echo "EJB Information:"
	@echo "=================="
	@echo "EJB Name: CartBean"
	@echo "Business Interface: jakarta.tutorial.cartsecure.ejb.Cart"
	@echo "JNDI Name: java:global/cart-secure-ejb-only/jakarta.examples.tutorial.security.cart-secure-cart-secure-ejb-10-SNAPSHOT/CartBean"
	@echo "Security Role: TutorialUser"
	@echo ""
	@echo "Available Methods:"
	@echo "  - initialize(String person, String id)"
	@echo "  - addBook(String title) [@RolesAllowed('TutorialUser')]"
	@echo "  - removeBook(String title) [@RolesAllowed('TutorialUser')]"
	@echo "  - getContents() [@RolesAllowed('TutorialUser')]"
	@echo "  - remove() [@RolesAllowed('TutorialUser')]"

# Test EJB functionality (requires GlassFish client libraries)
test-ejb:
	@echo "To test the EJB functionality, you need to:"
	@echo "1. Create a client application with proper dependencies"
	@echo "2. Include GlassFish client libraries in classpath"
	@echo "3. Use JNDI to lookup: java:global/cart-secure-ejb-only/.../CartBean"
	@echo "4. See TestCartClient.java for example implementation"

# Show all application and container information
info:
	@echo "=== Cart Secure EJB Application Information ==="
	@echo ""
	@just ejb-info
	@echo ""
	@echo "Container Status:"
	@just status
	@echo ""
	@echo "Deployed Applications:"
	@just list-apps

# Help command with detailed descriptions
help:
	@echo "Cart Secure EJB Application Commands:"
	@echo "====================================="
	@echo ""
	@echo "Build & Test:"
	@echo "  build         - Build the EAR file using Maven"
	@echo "  clean         - Clean build artifacts"
	@echo "  compile       - Compile Java sources only"
	@echo "  test          - Run unit tests"
	@echo ""
	@echo "Docker & Deployment:"
	@echo "  docker-build  - Build Docker image"
	@echo "  up            - Start application with Docker-Compose"
	@echo "  down          - Stop Docker-Compose services"
	@echo "  restart       - Restart the application"
	@echo "  deploy        - Build and deploy complete application"
	@echo ""
	@echo "EJB Management:"
	@echo "  verify        - Run complete EJB verification (all checks)"
	@echo "  menu          - Interactive verification menu"
	@echo "  verify-glassfish - Check GlassFish server status only"
	@echo "  verify-apps   - Check deployed applications only"
	@echo "  verify-ejb    - Check EJB container status only"
	@echo "  verify-jndi   - Check EJB JNDI bindings only"
	@echo "  verify-security - Check security configuration only"
	@echo "  list-apps     - List deployed applications in GlassFish"
	@echo "  show-jndi     - Show EJB JNDI entries"
	@echo "  manual-deploy - Manually deploy EAR to GlassFish"
	@echo "  undeploy      - Undeploy application from GlassFish"
	@echo ""
	@echo "Information & Debugging:"
	@echo "  info          - Show complete application information"
	@echo "  ejb-info      - Show EJB details and methods"
	@echo "  logs          - View application logs"
	@echo "  check-logs    - Check for recent errors"
	@echo "  status        - Show container status"
	@echo "  package-info  - Show EAR contents and size"
	@echo "  deps          - Show Maven dependencies"
	@echo ""
	@echo "Testing & Setup:"
	@echo "  test-ejb      - Instructions for testing EJB functionality"
	@echo "  setup-scripts - Setup all verification scripts in container"
	@echo ""
	@echo "Note: This is an EJB application, not a web application."
	@echo "It provides business logic via Enterprise Java Beans accessible through JNDI."

# Internal helper to check deployment prerequisites
_check-deployment:
	@echo "Verifying container is running..."
	@docker-compose ps -q glassfish > /dev/null || just _deployment-error "Container not running. Run: just up"
	@echo "✓ Container is running"
	@echo "Checking application deployment..."
	@docker-compose exec -T glassfish asadmin list-applications --terse 2>/dev/null | grep -q "cart-secure" || just _deployment-error "Cart application not deployed"
	@echo "✓ Cart application is deployed"

# Helper to show deployment error message
_deployment-error MESSAGE:
	@echo "❌ $(MESSAGE)!"
	@echo ""
	@echo "REQUIRED: Deploy the application first:"
	@echo "  just deploy       # Complete build and deploy"
	@echo "  just manual-deploy # Deploy existing EAR"
	@echo ""
	@echo "Then verify deployment:"
	@echo "  just list-apps"
	@echo ""
	@exit 1
