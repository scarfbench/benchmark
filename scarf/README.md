# Running and Processing Benchmark Results

This directory contains tools and documentation for running the ScarfBench conversion benchmark on your own solution and processing the results through the complete evaluation pipeline.

## Quick Start

All tools are available through the unified `scarfbench` CLI:

```bash
# Install dependencies
pip install -r requirements.txt

# Run using Python module (recommended)
python -m scarfbench --help

# Or use the entry point script
./bin/scarfbench --help

# Or add bin/ to your PATH
export PATH=$PATH:$(pwd)/bin
scarfbench --help

# Run a specific command
python -m scarfbench setup --help
python -m scarfbench run --help
python -m scarfbench process --help
```

## Overview

The benchmark evaluation process consists of several stages:

1. **Conversion**: Run your agent/solution to convert applications between frameworks
2. **Compilation**: Verify that converted applications compile successfully
3. **Execution**: Build Docker containers and verify applications run
4. **Smoke Testing**: Verify applications respond to HTTP requests (coming soon)

Each stage produces results that are tracked in a markdown table (`conversions-example.md`) and eventually aggregated into a CSV summary (`conversions.csv`).

## Prerequisites

Before running the benchmark, ensure you have:

- **Python 3.9+** installed
- **Required Python packages** (install from `requirements.txt` in this directory):
  ```bash
  pip install -r requirements.txt
  ```
- **Docker** (or a compatible runtime such as **OrbStack**) installed and running for execution and smoke testing
- **Maven or Gradle** (for compilation testing)
- **Access to a CLI and model** for running conversions—for example Gemini or Claude, or your own CLI/model. Your agent/solution CLI tool must be configured and accessible.

  **Example — Gemini CLI** (you can use your own CLI and model instead):

  1. Install the Gemini CLI (or your preferred CLI).
  2. Export environment variables, for example:
     ```bash
     export GOOGLE_GEMINI_BASE_URL=your-host-url
     export GEMINI_API_KEY=your-litellm-api-key
     ```
  3. Use that CLI in your YAML `command` (see [Step 1: Configure Your Solution](#step-1-configure-your-solution)).

## Running the Benchmark

### Step 1: Configure Your Solution

Create a YAML configuration file (see `short-example.yaml` and `long-example.yaml` for examples) that specifies:

- **`command`**: The CLI command to execute for each conversion (use `{prompt}` as placeholder for prompt content, `{working_dir}` for the working directory)
- **`runs`**: Number of runs per conversion (typically 3 for pass@k evaluation)
- **`timeout`**: Optional timeout in seconds for each conversion
- **`before`** and **`after`**: Framework names for template substitution (e.g., "Jakarta" → "Spring")
- **`seeds`**: Source directories to copy from and output directories to create
- **`conversions`**: List of conversions to run in format `"output_dir | prompt_file"`

**Important**: The directory name immediately after `agentic/` in your output paths will be used as your **solution name** (cli-tool) in the results. For example, if your path is `agentic/yoursolution/whole_applications/...`, then `yoursolution` will be the solution name.

**Model Name Mapping**: The system automatically maps known solution names to their model names:
- `claude` → `claude-sonnet-4.5`
- `codex` → `gpt-5`
- `gemini` → `gemini-2.5-pro`
- `qwen` → `qwen3-coder-480b`

For custom solution names not in this list, the solution name itself will be used as the model name. To specify a custom model name for your solution, you can either:
1. Use a naming convention: name your solution directory as `solutionname-modelname` (e.g., `myagent-gpt-4`), or
2. Modify the `MODEL_MAP` in `scarfbench/commands/process_results.py` to add your solution's model mapping

Example configuration:

```yaml
command: cd {working_dir}; your-agent --model your-model --prompt {prompt}
runs: 3
timeout: 300
before: Jakarta
after: Spring

seeds:
- source: whole_applications/jakarta/cargotracker
  output: agentic/yoursolution/whole_applications/cargotracker-jakarta-to-spring
  exclude_files: [smoke.py, justfile, Dockerfile, .idea/]

conversions:
- agentic/yoursolution/whole_applications/cargotracker-jakarta-to-spring | ../prompts/your_prompt.txt
```

In this example, `yoursolution` will appear as the `cli-tool` in the results table, and if it's not in the known model map, it will also be used as the `model` name.
Note: using the Gemini CLI as an example would look like:
``` 
command: cd {working_dir}; gemini --model gemini-2.5-flash --prompt {prompt}
```

### Step 2: Setup Run Directories

Use the `setup` command to create run directories by copying from source applications:

```bash
# Using the unified CLI (recommended)
python -m scarfbench setup -c your_config.yaml

# Or using the entry point script
./bin/scarfbench setup -c your_config.yaml
```

This creates `run_1`, `run_2`, `run_3` directories for each conversion specified in your config.

### Step 3: Run Conversions

Execute your agent on all configured conversions:

```bash
# Using the unified CLI (recommended)
python -m scarfbench run -c your_config.yaml --results-json results.json

# Or using the entry point script
./bin/scarfbench run -c your_config.yaml --results-json results.json
```

This will:
- Execute your command in each `run_*` directory
- Stream output in real-time
- Save output to `.agent_out/stdout.txt` in each run directory
- Track success/failure for each run
- Optionally write structured JSON results file (recommended)

The script outputs a summary showing which runs completed successfully and which failed. **Recommended**: Use `--results-json` to generate structured results that can be processed more reliably.

## Processing Results

After running conversions, use the consolidated `process_results.py` script to handle all post-processing tasks in one step.

### Consolidated Post-Processing

**Command**: `scarfbench process` (or `process_results.py`)

This command consolidates all post-processing tasks:
1. Updates `conversions-example.md` with conversion status
2. Optionally deletes failed run directories
3. Optionally generates YAML files for rerunning failed conversions
4. Produces a summary report

```bash

python -m scarfbench process \
  --results-json results.json \
  --results-md conversions-example.md

# Full workflow: Update MD, delete failed runs, generate rerun YAMLs
python -m scarfbench process \
  --results-json results.json \
  --results-md conversions-example.md \
  --conversions-dir agentic \
  --delete-failed \
  --generate-yamls \
  --yaml-output-dir rerun_yamls

```

**Options**:
- `--results-json`: Path to JSON results file from `run_agent.py` (preferred)
- `--results-output`: Path to `run_agent.py` output file (fallback if JSON not available)
- `--results-md`: Path to results markdown file (default: `conversions-example.md`)
- `--base-dir`: Base directory containing conversion outputs (default: `.`)
- `--conversions-dir`: Directory name containing conversions (e.g., `agentic` or `agentic2`, default: `agentic`)
- `--yaml-output-dir`: Directory to write rerun YAML files (default: `rerun_yamls`)
- `--delete-failed`: Delete directories for failed runs
- `--generate-yamls`: Generate YAML files for failed conversions
- `--dry-run`: Show what would be done without making changes

The script:
- Parses conversion results from JSON (preferred) or output file
- Updates the `converted` column in the markdown table with ✅/❌ symbols
- Creates new rows for conversions that don't exist in the markdown yet
- Can delete failed run directories to save space
- Generates YAML configuration files for rerunning failed conversions
- Produces a summary report with statistics

### Stage 4: Check Compilation

**Command**: `scarfbench compile` (or `check_compilation.py`)

This command attempts to compile all converted applications, records results in a CSV file, and optionally updates the markdown results file.

```bash
# Compile all projects
python -m scarfbench compile \
  --conversions-dir agentic \
  --result-file results_compile.csv \
  --results-md conversions-example.md \
  --max-workers 4 \
  --timeout 600

# Rerun only failed compilations
python -m scarfbench compile \
  --conversions-dir agentic \
  --result-file results_compile.csv \
  --results-md conversions-example.md \
  --only-failures conversions-example.md \
  --max-workers 4
```

**Options**:
- `--conversions-dir`: Directory containing all conversion outputs (required, e.g., `agentic`)
- `--result-file`: Path to output CSV file (required)
- `--results-md`: Path to results markdown file to update (optional, e.g., `conversions-example.md`)
- `--only-failures`: Path to markdown file to read failures from. Only rerun projects that show ❌ in the compiled column. When used, merges new results with existing CSV instead of overwriting.
- `--max-workers`: Number of parallel compilation jobs (default: 4)
- `--timeout`: Timeout per compilation in seconds (default: 600)
- `--dry-run`: Show what would be done without making changes

The script:
- **Only processes `run_X` level directories**: Finds projects directly in `run_1`, `run_2`, etc. folders, not in subdirectories (e.g., `run_1/cart-appclient` is ignored)
- Recursively finds all `pom.xml` and `build.gradle` files at the run level
- Attempts to build each project (Maven: `mvn clean package`, Gradle: `gradle build`)
- Records success/failure status in CSV format with improved error capture (checks both stdout and stderr)
- If `--results-md` is provided, updates the `compiled automatic` column in the markdown file with ✅/❌ symbols for each run
- If `--only-failures` is used:
  - Reads the markdown file to find entries with ❌ in the compiled column
  - Only rebuilds those specific failed run directories
  - Merges new results with existing CSV (updating only the rerun entries, preserving all other results)

### Stage 5: Check Execution (Docker)

**Command**: `scarfbench docker` (or `check_docker.py`)

This command builds Docker containers for successfully compiled applications, verifies they run, and updates the markdown results file.

```bash
python -m scarfbench docker \
  --results-file conversions-example.md \
  --result-file results_docker.csv \
  --base-dir . \
  --conversions-dir agentic \
  --max-workers 128
```

**Options**:
- `--results-file`: Path to results markdown file to read/update (default: `conversions-example.md`)
- `--result-file`: Path to output CSV file for Docker results (optional, e.g., `results_docker.csv`)
- `--base-dir`: Base directory containing Dockerfile templates (default: `.`)
- `--conversions-dir`: Directory containing conversions (default: `agentic`)
- `--max-workers`: Maximum parallel workers (default: 128)
- `--build-timeout`: Timeout for Docker builds in seconds (default: 600)
- `--startup-wait`: Seconds to wait after container starts (default: 2)
- `--smoke-wait`: Seconds to wait before smoke testing (default: 480)
- `--smoke-attempts`: Number of smoke test attempts (default: 5)
- `--smoke-delay`: Delay between smoke test attempts in seconds (default: 2.0)
- `--skip-existing`: Skip runs that already have 🟢 status
- `--dry-run`: Show what would be done without making changes

**Note**: This script expects Dockerfile templates (`jakarta_Dockerfile`, `spring_Dockerfile`, `quarkus_Dockerfile`) to be in the `--base-dir` directory.

The script:
- **Only processes `run_X` level directories**: Validates that paths are at the run level (e.g., `run_1`, `run_2`), not in subdirectories
- Reads `conversions-example.md` to find successfully compiled runs
- Creates appropriate Dockerfiles based on conversion type (jakarta/spring/quarkus)
- Builds Docker images for each run
- Starts containers and verifies they're running
- **Writes detailed output files** to `evaluation-outputs/` directory structure:
  - `docker_build.out`: Docker build logs and errors
  - `docker_run.out`: Container execution logs and errors
  - `smoke.out`: Smoke test results and container logs
- **Automatically updates the `ran` column** in the markdown file with status symbols:
  - 🟢: Successfully ran and passed smoke test
  - 🔨: Docker build or execution failed
  - 🚫: Container started but smoke test failed
  - ⏭️: Skipped (compilation failed)

**Output Directory Structure**: The `docker` command creates an `evaluation-outputs/` directory in the `--base-dir` with the following structure:
```
evaluation-outputs/
  └── {cli_tool}/
      └── {layer}/
          └── {app}-{conversion}/
              └── run_{num}/
                  ├── docker_build.out
                  ├── docker_run.out
                  └── smoke.out
```

These output files contain detailed logs and error messages for debugging Docker build, execution, and smoke test failures.

**Configuration options**:
- `--build-timeout`: Timeout for Docker builds (default: 600s)
- `--startup-wait`: Seconds to wait after container starts (default: 2s)
- `--smoke-wait`: Seconds to wait before smoke testing (default: 480s)
- `--smoke-attempts`: Number of smoke test attempts (default: 5)
- `--skip-existing`: Skip runs that already have 🟢 status

### Stage 6: Generate CSV Summary

**Command**: `scarfbench csv` (or `md_to_csv.py`)

Convert the markdown results table to a CSV summary with aggregated percentages. This command updates the CSV file, preserving existing rows for other solutions while updating rows for the current solution.

```bash
python -m scarfbench csv conversions-example.md conversions.csv --org "Your Company" --link "https://yourcompany.com"
```

**Options**:
- `input`: Input markdown file (required)
- `output`: Output CSV file (required)
- `--org`: Organization name (required, e.g., "Your Company")
- `--link`: Website/link URL (optional, e.g., "https://yourcompany.com")

The script:
- Parses the markdown table
- Aggregates results by `(model, conversion)` pairs for `whole_applications` layer
- Computes percentage pass rates for:
  - `translate`: From `converted` column (✅ count / total)
  - `compile`: From `compiled automatic` column (✅ count / total)
  - `run`: From `ran` column (🟢 count / total, where 🔨🚫⏭️❌ are failures)
- **Updates existing CSV**: Preserves `org`, `date`, `status`, and `link` for existing rows matching the same solution
- **Adds new rows**: For new (solution, from, to) combinations with the provided `--org`
- **Preserves other solutions**: Keeps rows for different solutions/models unchanged
- Outputs CSV with columns: `solution,org,date,status,link,layer,from,to,compile,run,translate`

## Complete Workflow Example

Here's a complete example workflow:

```bash
# 1. Setup and run conversions
python -m scarfbench setup -c my_config.yaml
python -m scarfbench run -c my_config.yaml --results-json results.json

# 2. Process conversion results (consolidated workflow)
python -m scarfbench process \
  --results-json results.json \
  --results-md conversions-example.md \
  --conversions-dir agentic \
  --delete-failed \
  --generate-yamls

# 3. Check compilation (updates markdown automatically)
python -m scarfbench compile \
  --conversions-dir agentic \
  --result-file results_compile.csv \
  --results-md conversions-example.md \
  --max-workers 4 \
  --timeout 600

# 3a. Rerun only failed compilations (optional, after initial run)
python -m scarfbench compile \
  --conversions-dir agentic \
  --result-file results_compile.csv \
  --results-md conversions-example.md \
  --only-failures conversions-example.md \
  --max-workers 4

# 4. Check execution (updates markdown automatically)
python -m scarfbench docker \
  --results-file conversions-example.md \
  --result-file results_docker.csv \
  --base-dir . \
  --conversions-dir agentic \
  --max-workers 128

# 5. Generate final CSV
python -m scarfbench csv conversions-example.md conversions.csv --org "Your Company" --link "https://yourcompany.com"
```

## Understanding Results

### Markdown Table Format

The `conversions-example.md` file contains a table with columns:

- **cli-tool**: The solution name, extracted from the directory name after `agentic/` in your output paths (e.g., "claude", "codex", "gemini", "yoursolution")
- **model**: The model name, automatically mapped from known solution names or the solution name itself if not in the mapping (e.g., "claude-sonnet-4.5", "gpt-5", "your-custom-model")
- **layer**: Application category (e.g., "whole_applications", "business_domain")
- **conversion**: Conversion type (e.g., "jakarta-to-spring", "quarkus-to-jakarta")
- **app**: Application name (e.g., "cargotracker", "coffee-shop")
- **orig-exists**: ✅ if source application exists
- **converted**: Status symbols for each run (✅ = success, ❌ = failure)
- **compiled automatic**: Compilation status for each run
- **ran**: Execution status for each run (🟢 = success, 🔨 = build failed, 🚫 = smoke failed, ⏭️ = skipped)
- **smoke**: Smoke test status (coming soon)

**Note on Solution and Model Names**: The solution name (cli-tool) is automatically extracted from your output directory structure. For example, if your output path is `agentic/myagent/whole_applications/...`, then `myagent` will be the solution name. The model name is then determined by:
1. Checking if the solution name exists in the built-in model mapping (claude, codex, gemini, qwen)
2. If not found, using the solution name itself as the model name
3. To specify a custom model name, you can modify `MODEL_MAP` in `scarfbench/commands/process_results.py` or use a naming convention like `solutionname-modelname` in your directory structure

### CSV Summary Format

The final CSV contains aggregated results:

- **solution**: Model name
- **from/to**: Source and target frameworks
- **compile**: Percentage of successful compilations
- **run**: Percentage of successful executions
- **translate**: Percentage of successful conversions

Percentages are calculated as: `(✅ count) / (✅ count + ❌ count) * 100`

## Custom Solution and Model Names

### Solution Name (cli-tool)

The solution name is automatically extracted from your output directory structure. The directory name immediately after `agentic/` (or `agentic2/` for dependency injection) becomes your solution name.

**Example**: If your output path is:
```
agentic/myagent/whole_applications/cargotracker-jakarta-to-spring
```

Then `myagent` will be the solution name (cli-tool) in the results table.

### Model Name Mapping

The system automatically maps known solution names to their model names:

| Solution Name | Model Name |
|--------------|------------|
| `claude` | `claude-sonnet-4.5` |
| `codex` | `gpt-5` |
| `gemini` | `gemini-2.5-pro` |
| `qwen` | `qwen3-coder-480b` |

### Specifying a Custom Model Name

If your solution name is not in the list above, the solution name itself will be used as the model name. To specify a different model name, you have two options:

#### Option 1: Modify MODEL_MAP (Recommended)

Edit `scarfbench/commands/process_results.py` and add your solution to the `MODEL_MAP` dictionary:

```python
MODEL_MAP = {
    'codex': 'gpt-5',
    'claude': 'claude-sonnet-4.5',
    'gemini': 'gemini-2.5-pro',
    'qwen': 'qwen3-coder-480b',
    'myagent': 'my-custom-model-name',  # Add your mapping here
}
```

#### Option 2: Use Naming Convention

Name your solution directory using the format `solutionname-modelname`. For example:
- Directory: `agentic/myagent-gpt-4/...`
- Solution name: `myagent-gpt-4`
- Model name: `myagent-gpt-4` (used as-is)

**Note**: This approach will use the full directory name as both the solution and model name, which may not be ideal if you want them to differ.

## Troubleshooting

### Conversion Failures

- Check `.agent_out/stdout.txt` in each run directory for detailed error messages
- Verify your CLI command is correct in the YAML config
- Check timeout settings if conversions are timing out
- Review `combined_output.txt` for failure reasons

### Compilation Failures

- Check `results_compile.csv` for specific error messages
- Verify Maven/Gradle is properly configured
- Check Java version compatibility
- Review build logs in each project directory

### Docker Execution Failures

- Check `docker_build.out` and `docker_run.out` in output directories
- Verify Docker is running and has sufficient resources
- Check port conflicts if containers fail to start
- Review container logs: `docker logs <container_name>`

## Next Steps

- **Smoke Testing**: The smoke test stage is still under development
- **Parallelization**: Adjust `MAX_WORKERS` and `--max-workers` based on your system resources


