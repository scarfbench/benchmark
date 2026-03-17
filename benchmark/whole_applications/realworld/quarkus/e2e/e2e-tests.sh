#!/bin/bash

# Make sure docker is running

# Function to clean up and stop the PostgreSQL container
cleanup() {
    echo "Stopping PostgreSQL container..."
    docker stop postgres_test_container
    docker rm postgres_test_container
    echo "PostgreSQL container stopped and removed."

    echo "Cleaning up environment variables..."
    unset QUARKUS_DATASOURCE_JDBC_URL
    unset QUARKUS_DATASOURCE_USERNAME
    unset QUARKUS_DATASOURCE_PASSWORD
    echo "Environment variables cleaned up."
}

# Trap any exit or interrupt signals to ensure cleanup
trap cleanup EXIT

# Check if mvn is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven (mvn) is not installed. Please install it and try again."
    exit 1
fi

# Check if k6 is installed
if ! command -v k6 &> /dev/null; then
    echo "k6 is not installed. Please install it and try again."
    exit 1
fi

# Run Maven clean install to build the project
echo "Running mvn clean install..."
mvn -f ../pom.xml clean install

# Check if the jar file was created successfully
if [ ! -f ../target/quarkus-app/quarkus-run.jar ]; then
    echo "Jar file not found! Build may have failed."
    exit 1
fi

# Run a PostgreSQL 17 container with the specified environment variable
echo "Starting PostgreSQL 17 container..."
docker run --name postgres_test_container -e POSTGRES_PASSWORD=123456 -p 5432:5432 -d postgres:17

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to start..."
until docker exec postgres_test_container pg_isready > /dev/null 2>&1; do
    sleep 1
done
echo "PostgreSQL is ready."

# Set environment variables for the application
export QUARKUS_DATASOURCE_JDBC_URL="jdbc:postgresql://localhost:5432/postgres"
export QUARKUS_DATASOURCE_USERNAME="postgres"
export QUARKUS_DATASOURCE_PASSWORD="123456"

# Start the application
echo "Starting the application..."
java -jar ../target/quarkus-app/quarkus-run.jar > service.log &
SERVICE_PROCESS=$!

# Wait for the application to start
tail -f -n0 service.log | grep -q 'Listening on'
echo "Application started"

# Run k6 e2e tests
echo "Running k6 e2e tests..."
k6 run ./api-test.js

# Stop the application after tests complete
kill $SERVICE_PROCESS
rm service.log

echo "Tests completed, application stopped."
