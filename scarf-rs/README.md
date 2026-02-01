# SCARF Commandline Interface

This is a companion CLI tool for the [SCARF Benchmark](../benchmark). It provides a commandline interface to list and test benchmarks, run agents, submit solutions, view and explore leaderboard among other useful tasks.

## Prerequisites
- Rustup ([Instructions](https://rustup.rs)) to manage Rust toolchains.
- Docker ([Instructions](https://docs.docker.com/get-docker/)) to run benchmarks in isolated environments.
- Make to build and run the projects.
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
        cargo build --release --targe-dir $PWD
  ```

The compiled binary will be located in the `target/release` directory.

You may now use the SCARF CLI tool by running the binary directly or adding it to your system's PATH.

### Ex
