#!/usr/bin/env python3
"""
setup.py
Spawn multiple "run" directories for ALL valid conversion lines in the conversions file.
No agent runs. No Docker. For each valid line:
  - Parse "output_dir | prompts/whatever.txt"
  - Derive source_dir from output_dir (whole_applications/{framework}/{app})
  - Create output_dir/run_1, output_dir/run_2, etc. by copying from source_dir.
"""

import argparse
import fnmatch
import os
import shutil
import sys
import yaml
from pathlib import Path
from typing import Tuple, Optional, Dict, List

# ---------- Styling ----------
def is_tty() -> bool:
    """Check if the script is running in a TTY context."""
    return sys.stdout.isatty()

BOLD = "\033[1m" if is_tty() else ""
DIM = "\033[2m" if is_tty() else ""
RESET = "\033[0m" if is_tty() else ""
RED = "\033[31m" if is_tty() else ""
GREEN = "\033[32m" if is_tty() else ""
YELLOW = "\033[33m" if is_tty() else ""
BLUE = "\033[34m" if is_tty() else ""
MAG = "\033[35m" if is_tty() else ""

def hr():
    """Prints a horizontal rule to the console."""
    print("=" * 60)

# ---------- Helpers ----------
def trim(s: str) -> str:
    """Removes leading/trailing whitespace."""
    return s.strip()

def sanitize_field(s: str) -> str:
    """Remove CR, trim whitespace, strip surrounding quotes, strip trailing comma."""
    s = s.replace("\r", "")
    s = trim(s)
    if (s.startswith('"') and s.endswith('"')) or (s.startswith("'") and s.endswith("'")):
        s = s[1:-1]
    s = s.lstrip('\'"').rstrip('\'"')
    if s.endswith(","):
        s = s[:-1]
    return s

def load_config(config_path: str) -> Dict:
    """Load and parse the YAML configuration file."""
    config_file = Path(config_path)
    if not config_file.is_file():
        print(f"{RED}❌ Configuration file not found:{RESET} {config_file}")
        sys.exit(1)
    
    try:
        with open(config_file, 'r', encoding='utf-8') as f:
            config = yaml.safe_load(f)
        return config
    except yaml.YAMLError as e:
        print(f"{RED}❌ Error parsing YAML file:{RESET} {e}")
        sys.exit(1)
    except Exception as e:
        print(f"{RED}❌ Error reading configuration file:{RESET} {e}")
        sys.exit(1)

def parse_seed(seed) -> Optional[Tuple[str, str, List[str]]]:
    """
    Parse a seed entry which can be either:
    - String format: "source_dir | output_dir"
    - Dict format: {"source": "source_dir", "output": "output_dir", "exclude_files": [...]}
    Returns (source_dir, output_dir, exclude_files) tuple or None if invalid.
    """
    if isinstance(seed, str):
        # Legacy string format
        if not seed or seed.strip().startswith("#"):
            return None
        parts = seed.split("|", 1)
        if len(parts) != 2:
            return None
        left = parts[0].strip()
        right = parts[1].strip()
        if not left or not right:
            return None
        return (left, right, [])
    elif isinstance(seed, dict):
        # New dict format
        source = seed.get('source', '').strip()
        output = seed.get('output', '').strip()
        exclude_files = seed.get('exclude_files', [])
        if not source or not output:
            return None
        return (source, output, exclude_files)
    return None


def should_exclude(path: Path, exclude_patterns: List[str]) -> bool:
    """
    Check if a path should be excluded based on the exclude patterns.
    Supports glob patterns and directory names.
    """
    path_str = str(path)
    for pattern in exclude_patterns:
        # Check if the path matches the pattern
        if fnmatch.fnmatch(path_str, pattern) or fnmatch.fnmatch(path.name, pattern):
            return True
        # Check if any parent directory matches
        for parent in path.parents:
            if fnmatch.fnmatch(str(parent), pattern) or fnmatch.fnmatch(parent.name, pattern):
                return True
    return False

def copy_tree_with_exclusions(src: Path, dst: Path, exclude_patterns: List[str] = None):
    """
    Copies the source directory tree to the destination with exclusions.
    If the destination already exists, it is removed first to ensure a clean seed.
    """
    if exclude_patterns is None:
        exclude_patterns = []
    
    if dst.exists():
        shutil.rmtree(dst)
    
    def ignore_func(dir_path, names):
        """Function to determine which files/directories to ignore during copy."""
        ignored = []
        current_path = Path(dir_path)
        
        for name in names:
            item_path = current_path / name
            if should_exclude(item_path, exclude_patterns):
                ignored.append(name)
        
        return ignored
    
    shutil.copytree(src, dst, ignore=ignore_func)

def copy_tree(src: Path, dst: Path):
    """
    Legacy copy_tree function for backward compatibility.
    Copies the source directory tree to the destination.
    If the destination already exists, it is removed first to ensure a clean seed.
    """
    if dst.exists():
        shutil.rmtree(dst)
    shutil.copytree(src, dst)

# ---------- Main ----------
def main():
    """Main execution function."""
    parser = argparse.ArgumentParser(description="Spawn multiple run directories for all conversions.")
    parser.add_argument("-c", "--config", default="config.yaml",
                        help="Path to YAML configuration file (default: config.yaml)")
    parser.add_argument("-n", "--num-runs", type=int, default=None,
                        help="Number of runs to create for each conversion (overrides config file)")
    args = parser.parse_args()

    # Load configuration
    config = load_config(args.config)
    
    # Extract configuration values
    default_runs = config.get('runs', 5)
    seeds = config.get('seeds', [])
    
    # Use command line override for number of runs if provided
    num_runs = args.num_runs if args.num_runs is not None else default_runs
    
    if not seeds:
        print(f"{RED}❌ No seeds specified in configuration file.{RESET}")
        sys.exit(1)
    
    # Parse seeds
    pairs = [p for p in (parse_seed(s) for s in seeds) if p is not None]
    total_lines = len(pairs)
    total_ops = total_lines * num_runs

    print(f"🧣 {BOLD}Spawning {num_runs} runs for each conversion (no agent run){RESET}")
    print(f"Total valid lines: {BOLD}{total_lines}{RESET} | Total runs to create: {BOLD}{total_ops}{RESET}")
    hr()

    runs_created = 0
    failures = 0
    skipped_sources = 0

    for idx, (source_dir, output_dir, exclude_files) in enumerate(pairs, start=1):
        output_dir_p = Path(output_dir)
        source_dir_p = Path(source_dir)

        print(f"{DIM}#{idx}/{total_lines}{RESET} {BOLD}{output_dir}{RESET} <- {source_dir}")
        print(f"  Source: {BLUE}{source_dir_p}{RESET}")
        if exclude_files:
            print(f"  Exclude: {YELLOW}{', '.join(exclude_files)}{RESET}")

        if not source_dir_p.is_dir():
            print(f"  {YELLOW}⚠️  Source directory does not exist. Skipping {num_runs} runs.{RESET}")
            skipped_sources += 1
            failures += num_runs
            hr()
            continue

        output_dir_p.mkdir(parents=True, exist_ok=True)
        for run_num in range(1, num_runs + 1):
            run_dir = output_dir_p / f"run_{run_num}"
            print(f"  {DIM}└─{RESET} Run {run_num}/{num_runs}: {BLUE}{run_dir}{RESET}")

            try:
                if exclude_files:
                    copy_tree_with_exclusions(source_dir_p, run_dir, exclude_files)
                else:
                    copy_tree(source_dir_p, run_dir)
                print(f"    {GREEN}✅ Seeded successfully.{RESET}")
                runs_created += 1
            except Exception as e:
                print(f"    {RED}❌ Failed to create run directory:{RESET} {e}")
                failures += 1

        hr()

    # Summary
    print()
    hr()
    print(f"📊 {BOLD}SETUP SUMMARY{RESET}")
    hr()
    print(f"Total lines processed:  {BOLD}{total_lines}{RESET}")
    print(f"Total runs attempted:   {BOLD}{total_ops}{RESET}")
    print(f"✅ Runs created:         {GREEN}{runs_created}{RESET}")
    print(f"❌ Failures:             {RED}{failures}{RESET}")
    if skipped_sources > 0:
        print(f"⚠️  Missing source dirs:  {YELLOW}{skipped_sources}{RESET}")
    print(f"{DIM}(No agent was run. This script only prepares run directories.){RESET}")

if __name__ == "__main__":
    main()

