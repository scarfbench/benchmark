# SCARF Commandline Interface

This is a companion CLI tool for the [SCARF Benchmark](../benchmark). It provides a commandline interface to list and test benchmarks, run agents, submit solutions, view and explore leaderboard among other useful tasks.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
  - [Prerequisites](#prerequisites)
  - [Build from Source](#build-from-source)
- [Usage](#usage)
- [Development](#development)
  - [Development Dependencies](#development-dependencies)
  - [Testing](#testing)
  - [Building and Testing](#building-and-testing)

## Features

- List available benchmarks
- Test and validate benchmarks
- Run agents on benchmark problems
- Submit solutions
- View and explore leaderboards
- Isolated benchmark execution using Docker

## Installation

### Prerequisites

Before installing the SCARF CLI, ensure you have the following tools installed:

- **Rustup** ([Installation Guide](https://rustup.rs)) - Manages Rust toolchains including `cargo` and `llvm-tools`
- **Docker** ([Installation Guide](https://docs.docker.com/get-docker/)) - Runs benchmarks in isolated environments
- **Make** - Builds and runs projects as specified in makefiles
- **Git** - Clones repositories

### Build from Source

1. **Clone the repository:**
   ```bash
   git clone https://github.com/ibm/scarfbench.git
   cd scarfbench/scarf-rs
   ```

2. **Build the project:**
   ```bash
   cargo build --release
   ```
   
   The compiled binary will be located in `target/release/scarf`.

3. **Run the CLI:**
   ```bash
   ./target/release/scarf --help
   ```
   
   Optionally, add the binary to your system's PATH for easier access.

## Usage

(Add usage examples and commands here as they become available)

## Development

### Development Dependencies

If you're contributing to the SCARF CLI, install these additional tools:

1. **Clippy** - Linting and code quality checks
   ```bash
   rustup component add clippy
   ```

2. **Rustfmt** - Code formatting
   ```bash
   rustup component add rustfmt
   ```

3. **LLVM Coverage Tools** - Coverage analysis
   ```bash
   rustup component add llvm-tools-preview
   cargo install cargo-llvm-cov
   ```

4. **Nextest** - Advanced test runner
   ```bash
   cargo install cargo-nextest --locked
   ```

### Testing

The project follows idiomatic Rust testing practices:

- **Unit tests**: Located within each module under the `#[cfg(test)]` attribute
- **Integration tests**: Located in the `tests/` directory at the project root for testing CLI commands

### Building and Testing

A [Makefile](Makefile) is provided to streamline development tasks. Run `make help` to see available commands:

```bash
make help
```

**Available targets:**

| Target     | Description                                                      |
|------------|------------------------------------------------------------------|
| `all`      | Run full pipeline (setup → fmt → clippy → build → test → coverage) |
| `setup`    | Check/install rustup, cargo, components, nextest, llvm-cov       |
| `fmt`      | Run `cargo fmt --all`                                            |
| `clippy`   | Run `cargo clippy` with warnings denied                          |
| `build`    | Run `cargo build`                                                |
| `test`     | Run tests using `cargo nextest`                                  |
| `coverage` | Run coverage using `cargo llvm-cov` + nextest                    |
| `clean`    | Run `cargo clean`                                                |
| `help`     | Show help message                                                |


Just run `make` to execute the full development pipeline:
```bash
# Run the full development pipeline
make
```
