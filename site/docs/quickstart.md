---
hide:
  - toc
---
# Quickstart Guide

This benchmark suite comes with most things needed to run the benchmark applications. Everything is already set up!

## What's Included

Each application comes with:

- **Dockerfile** - Pre-configured container with all dependencies installed
- **justfile** - Simple commands to build and run everything
- **smoke.py or smoke/** - Automated tests to verify the application works

You don't need to install Maven, Java, or any dependencies. Docker handles it all!

## Prerequisites

You only need:

- Docker installed on your machine
- Just command runner (you can install it via Cargo or your package manager)

## Running an Application

### Step 1: Pick an Application

Browse the directory structure and choose any application. For example:

```
business_domain/counter/spring/
dependency_injection/encoder/jakarta/
presentation/mood/quarkus/
```

### Step 2: Navigate to the Application

```bash
cd business_domain/counter/spring
```

### Step 3: Run It!

```bash
just up
```

That's it! The `just up` command will:
1. Build your application
2. Build the Docker container
3. Start everything up

### Step 4: Check the Logs

```bash
just logs
```

### Step 5: Stop When Done

```bash
just down
```

## Common Commands

Every application supports these commands (via the `justfile`):

| Command | What it does |
|---------|-------------|
| `just` | Shows all available commands |
| `just up` | Builds and starts the application |
| `just down` | Stops the application |
| `just logs` | Shows application logs |
| `just build` | Builds the application (Maven) |
| `just docker-build` | Builds the Docker image |
| `just clean` | Removes build artifacts |

## Running Smoke Tests

Most applications include automated tests. To run them:

```bash
# If smoke.py exists
python3 smoke.py

# If smoke/ folder exists
cd smoke && ./verify-all.sh
```

## Example Walkthrough

Let's run the counter application:

```bash
# 1. Go to the application
cd business_domain/counter/spring

# 2. Start it up
just up

# 3. Wait a few seconds, then check logs
just logs

# 4. Open your browser to http://localhost:8080

# 5. When done, stop it
just down
```

## Framework Variations

Each application type comes in three flavors:

- **jakarta/** - Jakarta EE (enterprise Java)
- **quarkus/** - Quarkus (cloud-native Java)
- **spring/** - Spring Boot (popular Java framework)

Pick whichever framework you want to test!

## Troubleshooting

**Port already in use?**
```bash
just down
# Wait a few seconds
just up
```

**Want to rebuild from scratch?**
```bash
just clean
just docker-build
just up
```

**Need to see what's happening?**
```bash
just logs
```

## Quick Reference

```bash
# See all commands
just

# Full workflow: build + start
just up

# Check if it's working
just logs

# Stop everything
just down
```
