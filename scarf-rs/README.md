# SCARF Commandline Interface

This is a companion CLI tool for the [SCARF Benchmark](../benchmark). It provides a commandline interface to list and test benchmarks, run agents, submit solutions, view and explore leaderboard among other useful tasks.

## Prerequisites
- Rustup ([Instructions](https://rustup.rs)): We'll use this to manage Rust toolchains including `cargo` and other rust testing tools like `llvm-tools`.
- Docker ([Instructions](https://docs.docker.com/get-docker/)) to run benchmarks in isolated environments.
- `Make`: to build and run the projects as specified in the makefiles.
- Git to clone repositories.

## Installation 

To install the SCARF CLI tool, clone the repository and build the project using Cargo:

1. Clone the scarfbench repository
  ```bash
        git clone https://github.com/ibm/scarfbench.git
  ```

2. Navigate into the `scarf-rs` directory and build the project
  ```bash
        cd scarf-rs
        cargo build --release 
        # If you want to specify a different output directory, you can use:
        # cargo build --release --targe-dir $PWD
  ```

The compiled binary will be located in the `target/release` directory.

You may now use the SCARF CLI tool by running the binary directly or adding it to your system's PATH.

### Ex

## Development Instructions

If you are looking at adding features to the scarf cli, here are some guidelines to follow:

### Testing

We use idomatic rust testing practices in that we have two places to put tests:
  
  1. Unit tests lie within the module that is being tested under the `#[cfg(test)]` attribute.
  2. Integration tests (e.g., to test the cli commands) lie in the `tests` directory at the root of the project.

### Development/Test dependencies

1. `cargo clippy`: for linting and code quality checks. Install with `rustup component add clippy`.
2. `cargo fmt`: for code formatting. Install both with `rustup component add rustfmt`.
3. `llvm-tools`: for coverage analysis. Install with `rustup component add llvm-tools-preview && cargo install cargo-llvm-cov`.
4. `nextest`: for running tests. Install with `cargo install cargo-nextest --locked`.
