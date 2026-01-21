---
hide:
  - toc
---

# Installing ScarfBench

ScarfBench is a suite of self-contained Java applications intended to be run locally (typically via Docker) for benchmarking and evaluation.

## Prerequisites

- **Docker** (Docker Desktop is fine)
- **just** (command runner)
- **Git**

Optional (only for running local smoke scripts outside containers):

- **Python 3**

## Install `just`

=== "macOS (Homebrew)"

    ```bash
    brew install just
    ```

=== "Other platforms"

    See the upstream install options: https://github.com/casey/just

## Get the repository

```bash
git clone https://github.com/ibm/scarfbench
cd scarfbench
```

## Run an application

Each benchmark app directory contains a `justfile` and a `Dockerfile`.

Example:

```bash
cd business_domain/counter/spring
just up
```

Common commands:

```bash
just            # list available commands
just logs       # view logs
just down       # stop
just clean      # clean build artifacts
```

## Troubleshooting

- **Port already in use**: run `just down`, then retry `just up`.
- **Docker build issues**: try `just clean` then `just up`.
