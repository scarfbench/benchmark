#!/bin/bash
set -e

# Start PostgreSQL
pg_ctlcluster $(pg_lsclusters -h | head -1 | awk '{print $1, $2}') start

# Create database and user
su - postgres -c "psql -c \"CREATE USER realworld WITH PASSWORD 'realworld';\"" 2>/dev/null || true
su - postgres -c "psql -c \"CREATE DATABASE realworld OWNER realworld;\"" 2>/dev/null || true
su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE realworld TO realworld;\"" 2>/dev/null || true

exec java -jar target/quarkus-app/quarkus-run.jar
