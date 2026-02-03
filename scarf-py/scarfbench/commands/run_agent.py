#!/usr/bin/env python3
"""
run_agent.py
Run agent conversions based on YAML configuration file.
Reads CLI command and conversions from config.yaml and executes them.
"""

import argparse
import json
import os
import shlex
import subprocess
import sys
import threading
import time
import yaml
from pathlib import Path
from queue import Queue, Empty
from typing import Dict, List, Tuple, Optional

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

def reader_thread(pipe, queue):
    """Reader thread to non-blockingly read from subprocess pipe."""
    try:
        for line in iter(pipe.readline, ''):
            queue.put(line)
        queue.put(None)  # Signal end of stream
    except Exception:
        queue.put(None)

def parse_conversion(conversion: str) -> Optional[Tuple[str, str]]:
    """
    Parse a conversion string in format "output_dir | prompt_file".
    Returns (output_dir, prompt_file) tuple or None if invalid.
    """
    if not conversion or conversion.strip().startswith("#"):
        return None
    parts = conversion.split("|", 1)
    if len(parts) == 2:
        output_dir = parts[0].strip()
        prompt_file = parts[1].strip()
        if output_dir and prompt_file:
            return (output_dir, prompt_file)
    return None

def execute_command(command: str, working_dir: str, prompt_file: str, before: str, after: str, timeout_seconds: Optional[int] = None) -> Tuple[bool, str]:
    """
    Execute the CLI command with the given prompt file.
    Returns (success, output) tuple.
    """
    try:
        # Read the prompt file content
        prompt_path = Path(prompt_file)
        if not prompt_path.is_file():
            return False, f"Prompt file not found: {prompt_file}"
        
        with open(prompt_path, 'r', encoding='utf-8') as f:
            prompt_content = f.read().strip()

        prompt_content = prompt_content.replace("{{ before }}", before).replace("{{ after }}", after)
        # Replace {prompt} placeholder with the actual prompt content
        # Quote the prompt content to handle spaces and special characters properly
        quoted_prompt_content = shlex.quote(prompt_content)
        # Get absolute path for working directory to ensure system uses the correct directory
        abs_working_dir = str(Path(working_dir).resolve())
        formatted_command = command.format(prompt=quoted_prompt_content, working_dir=abs_working_dir)
        
        # Split command into parts for subprocess using shlex for proper handling of quoted arguments
        cmd_parts = shlex.split(formatted_command)
        
        # Execute the command with real-time streaming and file output
        print(f"    {DIM}Executing command...{RESET}")
        if timeout_seconds:
            print(f"    {DIM}Timeout: {timeout_seconds}s (will move on if no output for {timeout_seconds}s){RESET}")
        print(f"    {DIM}{'='*50}{RESET}")
        
        # Create .agent_out directory if it doesn't exist
        agent_out_dir = Path(working_dir) / ".agent_out"
        agent_out_dir.mkdir(exist_ok=True)
        stdout_file = agent_out_dir / "stdout.txt"
        
        try:
            process = subprocess.Popen(
                formatted_command,
                cwd=working_dir,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True,
                bufsize=1,
                universal_newlines=True,
                shell=True
            )
            
            # Start a reader thread to non-blockingly read the process output
            q = Queue()
            thread = threading.Thread(target=reader_thread, args=[process.stdout, q])
            thread.daemon = True
            thread.start()
            
            # Stream output in real-time and save to file
            output_lines = []
            start_time = time.time()
            last_output_time = start_time
            timeout_occurred = False
            
            with open(stdout_file, 'w', encoding='utf-8') as f:
                while process.poll() is None:
                    # Check for timeout
                    if timeout_seconds and (time.time() - last_output_time) > timeout_seconds:
                        print(f"    {YELLOW}⚠️  No output for {timeout_seconds}s, timing out...{RESET}")
                        process.terminate()
                        timeout_occurred = True
                        break
                    
                    # Read from the queue without blocking
                    try:
                        while True:  # Drain the queue of all available lines
                            line = q.get_nowait()
                            if line is None:  # End of stream signal
                                break
                            # Print to console
                            print(f"    {line.strip()}")
                            # Save to file
                            f.write(line)
                            f.flush()
                            output_lines.append(line.strip())
                            last_output_time = time.time()  # Update last output time
                    except Empty:
                        time.sleep(0.1)  # Wait a bit to avoid busy-waiting
                
                # Process any final output in the queue
                while not q.empty():
                    try:
                        line = q.get_nowait()
                        if line is None:
                            break
                        print(f"    {line.strip()}")
                        f.write(line)
                        f.flush()
                        output_lines.append(line.strip())
                    except Empty:
                        break
                f.flush()
            
            if timeout_occurred:
                try:
                    return_code = process.wait(timeout=1)
                except subprocess.TimeoutExpired:
                    print(f"    {YELLOW}Process did not terminate gracefully. Sending SIGKILL...{RESET}")
                    process.kill()
                    return_code = process.wait()
                success = False
                output_lines.append(f"[TIMEOUT] Process killed after {timeout_seconds}s of no output")
            else:
                return_code = process.wait()
                success = return_code == 0
            
            print(f"    {DIM}{'='*50}{RESET}")
            print(f"    {DIM}Output saved to: {stdout_file}{RESET}")
            return success, '\n'.join(output_lines)
            
        except Exception as e:
            return False, f"Error executing command: {e}"
        
    except Exception as e:
        return False, f"Error executing command: {e}"

def run_conversion(output_dir: str, prompt_file: str, command: str, before: str, after: str, run_number: int, timeout_seconds: Optional[int] = None) -> Tuple[bool, List[Tuple[str, int, bool]]]:
    """
    Run a single conversion with the given parameters.
    Returns (all_successful, run_results) where run_results is a list of (run_dir_path, run_num, success) tuples.
    """
    print(f"  {DIM}└─{RESET} Run {run_number}: {BLUE}{output_dir}{RESET}")
    print(f"    Prompt: {BLUE}{prompt_file}{RESET}")
    
    # Read prompt content for display
    prompt_path = Path(prompt_file)
    if not prompt_path.is_file():
        print(f"    {RED}❌ Prompt file not found:{RESET} {prompt_file}")
        return False, []
    
    try:
        with open(prompt_path, 'r', encoding='utf-8') as f:
            prompt_content = f.read().strip()
        # Show first 300 characters of prompt content
        preview = prompt_content[:300] + "..." if len(prompt_content) > 300 else prompt_content
        print(f"    Command: {DIM}{command.format(prompt=prompt_file, working_dir='<run_dir>')}{RESET}")
        print(f"    Prompt preview: {DIM}{preview}{RESET}")
    except Exception as e:
        print(f"    {RED}❌ Error reading prompt file:{RESET} {e}")
        return False
    
    # Check if output directory exists
    output_path = Path(output_dir)
    if not output_path.is_dir():
        print(f"    {RED}❌ Output directory not found:{RESET} {output_dir}")
        return False, []
    
    # Find all run_* directories
    run_dirs = [d for d in output_path.iterdir() if d.is_dir() and d.name.startswith('run_')]
    run_dirs.sort()  # Sort to ensure consistent order
    
    if not run_dirs:
        print(f"    {YELLOW}⚠️  No run_* directories found in {output_dir}. Skipping.{RESET}")
        return True  # Not a failure, just nothing to do
    
    print(f"    {DIM}Found {len(run_dirs)} run directories: {[d.name for d in run_dirs]}{RESET}")
    
    # Execute the command in each run directory
    all_successful = True
    run_results = []
    for run_dir in run_dirs:
        print(f"    {DIM}└─ Executing in {str(run_dir)}...{RESET}")
        success, output = execute_command(command, str(run_dir), prompt_file, before, after, timeout_seconds)
        
        # Extract run number from directory name
        run_num = int(run_dir.name.split('_')[1]) if '_' in run_dir.name else 0
        run_results.append((str(run_dir), run_num, success))
        
        if success:
            print(f"      {GREEN}✅ {run_dir.name} completed successfully.{RESET}")
        else:
            print(f"      {RED}❌ {run_dir.name} failed:{RESET}")
            if output.strip():
                error_lines = output.strip().split('\n')
                for line in error_lines[:2]:  # Show first 2 lines of error
                    print(f"        {RED}{line}{RESET}")
            all_successful = False
    
    if all_successful:
        print(f"    {GREEN}✅ All runs completed successfully.{RESET}")
    else:
        print(f"    {RED}❌ Some runs failed.{RESET}")
    
    return all_successful, run_results

# ---------- Main ----------
def main():
    """Main execution function."""
    parser = argparse.ArgumentParser(description="Run agent conversions based on YAML configuration.")
    parser.add_argument("-c", "--config", default="config.yaml",
                        help="Path to YAML configuration file (default: config.yaml)")
    parser.add_argument("-n", "--num-runs", type=int, default=None,
                        help="Number of runs to execute (overrides config file)")
    parser.add_argument("--dry-run", action="store_true",
                        help="Show what would be executed without running commands")
    parser.add_argument("--results-json", type=str, default=None,
                        help="Path to write JSON results file (optional)")
    args = parser.parse_args()

    # Load configuration
    config = load_config(args.config)
    config_file = Path(args.config).resolve()
    config_dir = config_file.parent  # Directory containing the config file
    
    # Extract configuration values
    command = config.get('command', '')
    default_runs = config.get('runs', 1)
    timeout_seconds = config.get('timeout', None)
    before = config.get('before', '')
    after = config.get('after', '')
    conversions = config.get('conversions', [])
    
    # Use command line override for number of runs if provided
    num_runs = args.num_runs if args.num_runs is not None else default_runs
    
    if not command:
        print(f"{RED}❌ No command specified in configuration file.{RESET}")
        sys.exit(1)
    
    if not conversions:
        print(f"{RED}❌ No conversions specified in configuration file.{RESET}")
        sys.exit(1)
    
    # Parse conversions - output_dir comes from the part before | in conversions
    valid_conversions = []
    for conversion in conversions:
        parsed = parse_conversion(conversion)
        if parsed:
            output_dir, prompt_file = parsed
            # Resolve paths relative to config file directory
            resolved_output_dir = str((config_dir / output_dir).resolve())
            resolved_prompt_file = str((config_dir / prompt_file).resolve())
            valid_conversions.append((resolved_output_dir, resolved_prompt_file))
    
    total_conversions = len(valid_conversions)
    total_runs = total_conversions * num_runs
    
    print(f"🧣 {BOLD}Running agent conversions{RESET}")
    print(f"Command: {BOLD}{command}{RESET}")
    print(f"Runs per conversion: {BOLD}{num_runs}{RESET}")
    print(f"Total conversions: {BOLD}{total_conversions}{RESET}")
    print(f"Total runs: {BOLD}{total_runs}{RESET}")
    hr()
    
    if args.dry_run:
        print(f"{YELLOW}🔍 DRY RUN MODE - No commands will be executed{RESET}")
        hr()
    
    successful_runs = 0
    failed_runs = 0
    all_results = []  # Store all run results for JSON output
    
    for idx, (output_dir, prompt_file) in enumerate(valid_conversions, start=1):
        print(f"{DIM}#{idx}/{total_conversions}{RESET} {BOLD}{output_dir}{RESET} | {prompt_file}")
        
        if args.dry_run:
            print(f"  {DIM}└─{RESET} Would run: {BLUE}{output_dir}{RESET}")
            # Read prompt content for dry run display
            try:
                prompt_path = Path(prompt_file)
                if prompt_path.is_file():
                    with open(prompt_path, 'r', encoding='utf-8') as f:
                        prompt_content = f.read().strip()
                    print(f"    Command: {DIM}{command.format(prompt=prompt_file, working_dir='<run_dir>')}{RESET}")
                    # Show first 300 characters of prompt content
                    preview = prompt_content[:300] + "..." if len(prompt_content) > 300 else prompt_content
                    print(f"    Prompt preview: {DIM}{preview}{RESET}")
                    
                    # Check for run directories in dry run
                    output_path = Path(output_dir)
                    if output_path.is_dir():
                        run_dirs = [d for d in output_path.iterdir() if d.is_dir() and d.name.startswith('run_')]
                        run_dirs.sort()
                        if run_dirs:
                            print(f"    Found {len(run_dirs)} run directories: {[d.name for d in run_dirs]}")
                            for run_dir in run_dirs:
                                print(f"      {DIM}└─ Would execute in {run_dir.name}{RESET}")
                        else:
                            print(f"    {YELLOW}⚠️  No run_* directories found{RESET}")
                    else:
                        print(f"    {RED}❌ Output directory not found:{RESET} {output_dir}")
                else:
                    print(f"    Command: {DIM}{command.format(prompt=prompt_file, working_dir='<run_dir>')}{RESET}")
                    print(f"    {RED}❌ Prompt file not found:{RESET} {prompt_file}")
            except Exception as e:
                print(f"    Command: {DIM}{command.format(prompt=prompt_file, working_dir='<run_dir>')}{RESET}")
                print(f"    {RED}❌ Error reading prompt file:{RESET} {e}")
            successful_runs += 1
        else:
            success, run_results = run_conversion(output_dir, prompt_file, command, before, after, 1, timeout_seconds)
            # Store results for JSON output
            for run_dir_path, run_num, run_success in run_results:
                all_results.append({
                    'output_dir': output_dir,
                    'run_dir': run_dir_path,
                    'run_num': run_num,
                    'success': run_success,
                    'prompt_file': prompt_file
                })
            if success:
                successful_runs += 1
            else:
                failed_runs += 1
        
        hr()
    
    # Summary
    print()
    hr()
    print(f"📊 {BOLD}EXECUTION SUMMARY{RESET}")
    hr()
    print(f"Total conversions:     {BOLD}{total_conversions}{RESET}")
    print(f"Runs per conversion:   {BOLD}{num_runs}{RESET}")
    print(f"Total runs:            {BOLD}{total_runs}{RESET}")
    print(f"✅ Successful runs:     {GREEN}{successful_runs}{RESET}")
    print(f"❌ Failed runs:         {RED}{failed_runs}{RESET}")
    
    if args.dry_run:
        print(f"{YELLOW}🔍 This was a dry run - no actual commands were executed{RESET}")
    
    # Write JSON results if requested
    if args.results_json and not args.dry_run:
        results_data = {
            'config_file': str(args.config),
            'total_conversions': total_conversions,
            'total_runs': total_runs,
            'successful_runs': successful_runs,
            'failed_runs': failed_runs,
            'results': all_results
        }
        json_path = Path(args.results_json)
        json_path.parent.mkdir(parents=True, exist_ok=True)
        with open(json_path, 'w', encoding='utf-8') as f:
            json.dump(results_data, f, indent=2)
        print(f"\n{GREEN}✅ Results written to: {json_path}{RESET}")

if __name__ == "__main__":
    main()
