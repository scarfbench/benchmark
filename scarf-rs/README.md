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
- **Python** - If you want to install `scarf` with pip (optional)

### Clone the repository

```bash
git clone https://github.com/ibm/scarfbench.git
cd scarfbench
```

### Install with `pip`

You can install the SCARF CLI using `pip` for easier management:

```bash
pip install -U ./scarf
```

Note: Depending on your os, pip may be called `pip3`.

### Install with `cargo`

If you have `cargo`, you can install with 

```bash
cargo install --path ./scarf
```

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

After installation, you can use the SCARF CLI to interact with the SCARF Benchmark. Here are some common commands:

### 1. List Benchmarks
```bash
❯ ./target/release/scarf bench list --help
List the application(s) in the benchmark.

Usage: scarf bench list [OPTIONS] --benchmark-dir <ROOT>

Options:
      --benchmark-dir <ROOT>    Path to the root of the scarf benchmark.
  -v, --verbose...     Increase verbosity (-v, -vv, -vvv). If RUST_LOG is set, it takes precedence.
      --layer <LAYER>  Application layer to list.
  -h, --help           Print help
```

This should give you something like below
```bash
❯ ./target/release/scarf bench list --benchmark-dir /home/rkrsn/workspace/scarfbench/benchmark --layer business_domain
┌─────────────────┬──────────────┬───────────┬─────────────────────────────────────────────────────────────────────────────────┐
│ Layer           ┆ Application  ┆ Framework ┆ Path                                                                            │
╞═════════════════╪══════════════╪═══════════╪═════════════════════════════════════════════════════════════════════════════════╡
│ business_domain ┆ cart         ┆ jakarta   ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/cart/jakarta         │
│ business_domain ┆ cart         ┆ quarkus   ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/cart/quarkus         │
│ business_domain ┆ cart         ┆ spring    ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/cart/spring          │
│ business_domain ┆ converter    ┆ jakarta   ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/converter/jakarta    │
│ business_domain ┆ converter    ┆ quarkus   ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/converter/quarkus    │
│ business_domain ┆ converter    ┆ spring    ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/converter/spring     │
│ business_domain ┆ counter      ┆ jakarta   ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/counter/jakarta      │
│ business_domain ┆ counter      ┆ quarkus   ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/counter/quarkus      │
│ business_domain ┆ counter      ┆ spring    ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/counter/spring       │
│ business_domain ┆ helloservice ┆ jakarta   ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/helloservice/jakarta │
│ business_domain ┆ helloservice ┆ quarkus   ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/helloservice/quarkus │
│ business_domain ┆ helloservice ┆ spring    ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/helloservice/spring  │
│ business_domain ┆ standalone   ┆ jakarta   ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/standalone/jakarta   │
│ business_domain ┆ standalone   ┆ quarkus   ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/standalone/quarkus   │
│ business_domain ┆ standalone   ┆ spring    ┆ /home/rkrsn/workspace/scarfbench/benchmark/business_domain/standalone/spring    │
└─────────────────┴──────────────┴───────────┴─────────────────────────────────────────────────────────────────────────────────┘
```

### 2. Test Benchmark Layer(s)

You can use the `scarf bench test` command to test specific benchmark layers or the whole benchmark. Here are some examples:

```bash
❯ ./target/release/scarf bench test --help
Run regression tests (with `make test`) on the benchmark application(s).

Usage: scarf bench test [OPTIONS] --benchmark-dir <ROOT>

Options:
      --benchmark-dir <ROOT>    Path to the root of the scarf benchmark.
  -v, --verbose...     Increase verbosity (-v, -vv, -vvv). If RUST_LOG is set, it takes precedence.
      --layer <LAYER>  Application layer to test.
      --dry-run        Use dry run instead of full run.
  -h, --help           Print help
```

For example, to test the `persistence` layer:

```bash
❯ ./target/release/scarf bench test --benchmark-dir /home/rkrsn/workspace/scarfbench/benchmark --layer persistence
```

This will run `make tests` in all the apps in `persistence` layer and provide a summary of the results.

```bash
┌─────────────────────────────────────────────────────────────────────────────┬─────────┐
│ Application Path                                                            ┆ Result  │
╞═════════════════════════════════════════════════════════════════════════════╪═════════╡
│ /home/rkrsn/workspace/scarfbench/benchmark/persistence/order/jakarta        ┆ Failure │
│ /home/rkrsn/workspace/scarfbench/benchmark/persistence/roster/spring        ┆ Success │
│ /home/rkrsn/workspace/scarfbench/benchmark/persistence/order/quarkus        ┆ Failure │
│ /home/rkrsn/workspace/scarfbench/benchmark/persistence/roster/quarkus       ┆ Success │
│ /home/rkrsn/workspace/scarfbench/benchmark/persistence/roster/jakarta       ┆ Success │
│ /home/rkrsn/workspace/scarfbench/benchmark/persistence/address-book/spring  ┆ Success │
│ /home/rkrsn/workspace/scarfbench/benchmark/persistence/address-book/quarkus ┆ Success │
│ /home/rkrsn/workspace/scarfbench/benchmark/persistence/address-book/jakarta ┆ Success │
│ /home/rkrsn/workspace/scarfbench/benchmark/persistence/order/spring         ┆ Success │
└─────────────────────────────────────────────────────────────────────────────┴─────────┘
```


## Development

### Development Dependencies

Use `make setup` to install and verify tooling (`rustup`, `rustfmt`, `clippy`, `cargo-nextest`, `cargo-llvm-cov`). You can also install them manually:

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
- **Integration tests**: Place in `tests/` (not currently present) for CLI-level coverage. 
  - For intergation tests, use descriptive names for test files, e.g., `cli_tests.rs`.

### Building and Testing

A [Makefile](Makefile) is provided to streamline development tasks. Run `make help` to see available commands. You can run `make help` to see all available targets:

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

Run the full pipeline with:
```bash
make
```

To build a release binary:
```bash
cargo build --release
./target/release/scarf --help
```
