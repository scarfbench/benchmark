---
title: "Quickstart Guide"
---

# Quickstart Guide

This guide will help you get started with:

- Preparing your agent for evaluation
- Running your agent with the benchmark applications
- Submitting the results

## Table of Contents
- [Introduction](#introduction)
- [Preparing Your Agent](#preparing-your-agent)
- [Running the Benchmark](#running-the-benchmark)
- [Submitting Results](#submitting-results)

## Introduction

In order to use ScarfBench, we have a command line helper tool called [`scarf`](https://github.com/scarfbench/scarf). It provides a commandline interface to run agents, submit solutions, view and explore leaderboard among other useful tasks.

For the purposes of this guide, we will focus on using the `scarf` tool to run agents and submit solutions. If you haven't already, you can follow the instructions [here](https://github.com/scarfbench/scarfbench-cli?tab=readme-ov-file#installation) to install it.

Ensure you have the `scarf` tool installed and configured before proceeding. You can verify the installation by running:

```
❯ scarf --help

  ███████╗  ██████╗  █████╗  ██████╗  ███████╗ ██████╗  ███████╗ ███╗   ██╗  ██████╗ ██╗  ██╗
  ██╔════╝ ██╔════╝ ██╔══██╗ ██╔══██╗ ██╔════╝ ██╔══██╗ ██╔════╝ ████╗  ██║ ██╔════╝ ██║  ██║
  ███████╗ ██║      ███████║ ██████╔╝ █████╗   ██████╔╝ █████╗   ██╔██╗ ██║ ██║      ███████║
  ╚════██║ ██║      ██╔══██║ ██╔══██╗ ██╔══╝   ██╔══██╗ ██╔══╝   ██║╚██╗██║ ██║      ██╔══██║
  ███████║ ╚██████╗ ██║  ██║ ██║  ██║ ██║      ██████╔╝ ███████╗ ██║ ╚████║ ╚██████╗ ██║  ██║
  ╚══════╝  ╚═════╝ ╚═╝  ╚═╝ ╚═╝  ╚═╝ ╚═╝      ╚═════╝  ╚══════╝ ╚═╝  ╚═══╝  ╚═════╝ ╚═╝  ╚═╝

  ScarfBench CLI: The command line helper tool for scarf bench
  
  Usage: scarf [OPTIONS] <COMMAND>
  
  Commands:
  bench  A series of subcommands to run on the benchmark applications.
  eval   Subcommands to run evaluation over the benchmark
  help   Print this message or the help of the given subcommand(s)
  
  Options:
  -v, --verbose...  Increase verbosity (-v, -vv, -vvv).
  -h, --help        Print help
  -V, --version     Print version
```

## Preparing Your Agent

This section describes how to structure an **agent implementation** so it can be executed by the `scarf` CLI during evaluation runs.

An *agent* is an opaque executable harness from Scarf's point of view.

!!! tip "Note"
    `scarf` does not attempt to interpret your code or prompts. It only knows how to run your agent (based on the `agent.toml` you have specified and the entrypoint contained within) and where results should go (based on the `--eval-out` flag).

  
### Agent Directory Structure

```text
agents/<agent-name>/
├── agent.toml <-   REQUIRED
├── run.sh     <-   OPTIONAL/RECOMMENDED wrapper script to wrap your agent's main executable 
├                   must be specified as the entrypoint in `agent.toml`)
└── README.md  <-   OPTIONAL (Documentation for your agent)
```

!!! tip "Some remarks on the structure"
    1. `scarf` treats the agent implementation as opaque.
    2. The only required contract is:
        - a metadata file (`agent.toml`)
        - an executable entrypoint (`run.sh`)
        - all other files are agent-defined, unconstrained, and private to your implementation.

### `agent.toml` file

The `agent.toml` file is a required configuration file that provides metadata about your agent. It should include the following fields:

#### Minimal example

```toml
name = "example-application-migrator-agent"
entrypoint = ["run.sh"]
```

#### Fields

| Field | Required | Description |
|------|----------|-------------|
| `name` | yes | Logical name of the agent (used in run metadata and reporting) |
| `entrypoint` | yes | Command (relative to agent directory) used to run the agent |

!!! tip "Note" 
    The `scarf` CLI executes the entrypoint exactly as specified relative to the agent directory. For example, if your entrypoint is `run.sh`, `scarf` executes `/path/to/agent-dir/example-agent/run.sh` when running your agent.

### `run.sh` — Agent Entrypoint

`run.sh` is the executable that `scarf eval run` commands runs to execute your agent.

`scarf` sets the following environment variables before calling your `run.sh`:

```bash
SCARF_WORK_DIR       # Output/work directory. Do not write outside this directory.
SCARF_FRAMEWORK_FROM # Source framework.
SCARF_FRAMEWORK_TO   # Target framework.
```

### Writing a Codex-based agent (`agents/codex-cli`)

Here, we provide an example of what `run.sh` could look like for a migration agent based on OpenAI's `codex` CLI. A similar structure can be used for other LLM-based agents.

#### 1) Recommended directory layout

```text
agents/codex-cli/
├── agent.toml
├── run.sh
└── skills/
   ├── spring-to-quarkus/
   │   └── SKILL.md
   ├── spring-to-jakarta/
   │   └── SKILL.md
   ├── quarkus-to-spring/
   │   └── SKILL.md
   ├── quarkus-to-jakarta/
   │   └── SKILL.md
   ├── jakarta-to-spring/
   │   └── SKILL.md
   └── jakarta-to-quarkus/
      └── SKILL.md
```

Each `SKILL.md` can contain migration instructions for exactly one conversion pair.

#### 2) `agent.toml` for Codex

Use an entrypoint that points to your shell wrapper:

```toml
name        = "codex-framework-migration"
description = "Sample implementation of a framework-migration agent for ScarfBench."
entrypoint  = "./run.sh"
```

#### 3) What `run.sh` should do

The `agents/codex-cli/run.sh` implementation follows a robust flow that you can copy for your own Codex-based agent:

1. Resolve script-local paths (for example, `skills/`).
2. Read required env vars:
  - `SCARF_WORK_DIR` (with optional fallback support if needed)
  - `SCARF_FRAMEWORK_FROM`
  - `SCARF_FRAMEWORK_TO`
3. Validate all required values and fail fast with clear stderr messages.
4. Normalize framework names (for example, map aliases like `springboot` -> `spring`).
5. Build the conversion key `${from}-to-${to}` and verify `skills/<pair>/SKILL.md` exists.
6. Verify the `codex` CLI is installed and available in `PATH`.
7. Prepare managed helper files inside `SCARF_WORK_DIR`:
  - Create a local `.agent/skills` symlink to the selected skill directory.
  - Create/update `AGENTS.md` so Codex can discover the active skill.
  - Backup any existing `AGENTS.md` and restore it on exit.
8. Run `codex exec` in headless mode against `SCARF_WORK_DIR` with a migration prompt.
9. Always clean up temporary links/files with a trap handler.

#### 4) Example Codex invocation pattern

Use a workspace-restricted execution mode and set cwd to the scarf work directory:

```bash
codex -a never exec \
  --sandbox workspace-write \
  --skip-git-repo-check \
  -C "$SCARF_WORK_DIR" \
  "$PROMPT"
```

This keeps writes constrained to the evaluation workspace and makes execution deterministic for batch runs.

#### 5) Best practices for Codex agents in ScarfBench

- Keep framework-specific logic in `skills/<pair>/SKILL.md`, not hardcoded into prompt strings.
- Keep `run.sh` focused on orchestration: validation, routing, setup, invocation, cleanup.
- Normalize aliases so scorer-provided framework names do not break pair resolution.
- Print concise diagnostics to stderr and use non-zero exits for invalid inputs.
- Ensure no writes happen outside `$SCARF_WORK_DIR`.
- Keep the public wrapper minimal; private internals can remain outside this repository.

