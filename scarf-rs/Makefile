# Makefile
# Usage:
#   make setup
#   make ci
#   make test
#   make coverage
#   make build
#
# Notes:
# - Installs cargo subcommands into ~/.cargo/bin
# - Uses nextest for tests and llvm-cov for coverage

SHELL := /bin/bash
.ONESHELL:
.SHELLFLAGS := -eu -o pipefail -c

CARGO ?= cargo
RUSTUP ?= rustup

WORKSPACE ?= --workspace

# Convenience: pass extra args like:
#   make test NEXTTEST_ARGS="--nocapture"
NEXTTEST_ARGS ?=
LLVM_COV_ARGS ?=

.PHONY: all setup check-rustup check-cargo ensure-components ensure-nextest ensure-llvm-cov \
        fmt clippy build test coverage clean

default: all

setup: check-rustup check-cargo ensure-components ensure-nextest ensure-llvm-cov
	@echo "[OK] Toolchain and tools are ready."

check-rustup:
	@if command -v $(RUSTUP) >/dev/null 2>&1; then
	  echo "[OK] rustup found: $$(rustup --version | head -n1)"; \
	else \
	  echo "[ERROR] rustup not found."; \
	  echo "Install rustup (includes cargo) from:"; \
	  echo "  https://rustup.rs"; \
	  echo "Then re-run: make setup"; \
	  exit 1; \
	fi

check-cargo:
	@if command -v $(CARGO) >/dev/null 2>&1; then \
	  echo "[OK] cargo found: $$(cargo --version | head -n1)"; \
	else \
	  echo "[ERROR] cargo not found."; \
	  echo "If you installed rustup, ensure ~/.cargo/bin is on PATH."; \
	  echo "Install rustup from https://rustup.rs (cargo is included)."; \
	  echo "Then re-run: make setup"; \
	  exit 1; \
	fi

ensure-components: check-rustup
	@echo "[INFO] Ensuring rustfmt + clippy components via rustup..."
	@$(RUSTUP) component add rustfmt clippy >/dev/null
	@echo "[OK] rustfmt + clippy installed."

ensure-nextest: check-cargo
	@if command -v cargo-nextest >/dev/null 2>&1; then \
	  echo "[OK] nextest found: $$(cargo nextest --version | head -n1)"; \
	else \
	  echo "[INFO] Installing nextest (cargo-nextest) ..."; \
	  $(CARGO) install cargo-nextest; \
	  echo "[OK] nextest installed."; \
	fi

ensure-llvm-cov: check-cargo
	@if command -v cargo-llvm-cov >/dev/null 2>&1; then \
	  echo "[OK] llvm-cov found: $$(cargo llvm-cov --version | head -n1)"; \
	else \
	  echo "[INFO] Installing llvm-cov (cargo-llvm-cov) ..."; \
	  $(CARGO) install cargo-llvm-cov; \
	  echo "[OK] llvm-cov installed."; \
	fi

fmt: check-cargo ensure-components
	@echo "[INFO] cargo fmt --check"
	@$(CARGO) fmt --check

clippy: check-cargo ensure-components
	@echo "[INFO] cargo clippy (deny warnings)"
	@$(CARGO) clippy $(WORKSPACE) $(FEATURES) -- -D warnings

build: check-cargo
	@echo "[INFO] cargo build"
	@$(CARGO) build $(WORKSPACE) $(FEATURES)

test: check-cargo ensure-nextest
	@echo "[INFO] cargo nextest run"
	@$(CARGO) nextest run $(WORKSPACE) $(FEATURES) $(NEXTTEST_ARGS)

coverage: check-cargo ensure-llvm-cov ensure-nextest
	@echo "[INFO] cargo llvm-cov nextest (minimum line coverage: $(COVERAGE_MIN)%)"
	@$(CARGO) llvm-cov nextest $(WORKSPACE) $(FEATURES) $(LLVM_COV_ARGS)
	@$(CARGO) llvm-cov nextest $(WORKSPACE) $(FEATURES) --html $(LLVM_COV_ARGS)
	@echo "[OK] Coverage HTML report generated under target/llvm-cov/html/"


all: setup fmt clippy build test coverage
	@echo "[OK] CI pipeline complete."

clean: check-cargo
	@$(CARGO) clean
