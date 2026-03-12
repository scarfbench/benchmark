#!/bin/bash
set -e

# Start PostgreSQL
pg_ctlcluster $(pg_lsclusters -h | head -1 | awk '{print $1, $2}') start

# Create database and user
su - postgres -c "psql -c \"CREATE USER postgres_user WITH PASSWORD 'S3cret';\"" 2>/dev/null || true
su - postgres -c "psql -c \"CREATE DATABASE postgres_db OWNER postgres_user;\"" 2>/dev/null || true
su - postgres -c "psql -c \"GRANT ALL PRIVILEGES ON DATABASE postgres_db TO postgres_user;\"" 2>/dev/null || true

exec mvn clean liberty:run
