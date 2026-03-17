#!/bin/bash

# Make sure a Postgres DB is ready
# !!! This shell script is unfinished but can be a great start to develop

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

# Run Maven commands to package the project
echo "Running mvn package liberty..."
mvn -f ../pom.xml package liberty:create liberty:install-feature liberty:deploy liberty:package -Dinclude=minify,runnable -DskipTests

# Check if the jar file was created successfully
if [ ! -f ../target/realworld-liberty.jar ]; then
    echo "Jar file not found! Build may have failed."
    exit 1
fi

# Start the application
echo "Starting the application..."
java -jar ../target/realworld-liberty.jar > service.log &
SERVICE_PROCESS=$!

# Wait for the application to start
tail -f -n0 service.log | grep -q 'CWWKT0016I:'
echo "Application started"

# Run k6 e2e tests
echo "Running k6 e2e tests..."
k6 run ./api-test.js

# Stop the application after tests complete
kill $SERVICE_PROCESS
rm service.log

echo "Tests completed, application stopped."
