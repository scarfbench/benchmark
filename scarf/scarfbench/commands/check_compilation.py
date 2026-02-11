#!/usr/bin/env python3
"""
check_compilation.py
Check compilation status for all converted applications.

Recursively finds all projects (Maven/Gradle) and attempts to build them,
recording success/failure status in a CSV file and optionally updating the markdown results file.
"""

import argparse
import os
import re
import subprocess
import csv
from collections import defaultdict
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Dict, List, Optional, Tuple

MODEL_MAP = {
    'codex': 'gpt-5',
    'claude': 'claude-sonnet-4.5',
    'gemini': 'gemini-2.5-pro',
    'qwen': 'qwen3-coder-480b',
}


def parse_path_to_components(path: str) -> Optional[Dict[str, str]]:
    """Parse a conversion path to extract cli_tool, model, layer, conversion, and app."""
    path = re.sub(r'/run_\d+.*$', '', path)
    path = re.sub(r'/.agent_out/.*$', '', path)

    patterns = [
        r'.*/agentic(?:2|4)?/([^/]+)/([^/]+)/([^/]+)/?$',  # cli_tool, layer, app+conversion
        r'.*/codex/([^/]+)/([^/]+)/?$',                    # layer, app+conversion (cli_tool is 'codex')
    ]

    for pattern in patterns:
        match = re.search(pattern, path)
        if not match:
            continue

        if 'codex' in pattern:
            cli_tool = 'codex'
            layer, app_conv = match.groups()
        else:
            cli_tool, layer, app_conv = match.groups()


        if '-to-' in app_conv:
            to_idx = app_conv.find('-to-')
            before_to = app_conv[:to_idx]
            after_to = app_conv[to_idx+4:] 
            before_parts = before_to.split('-')
            if len(before_parts) >= 2:
                last_part = before_parts[-1].lower()
                if last_part in ['jakarta', 'quarkus', 'spring']:
                    app_base = '-'.join(before_parts[:-1])
                    conversion = f"{last_part}-to-{after_to}"
                else:
                    app_base = before_to
                    conversion = f"to-{after_to}" 
            else:
                app_base = before_to
                conversion = f"to-{after_to}"  
        else:
            parts = app_conv.split('-')
            if len(parts) >= 2:
                app_base = '-'.join(parts[:-1])
                conversion = parts[-1]
            else:
                app_base = parts[0] if parts else ''
                conversion = ''

        model = MODEL_MAP.get(cli_tool, cli_tool)

        return {
            'cli_tool': cli_tool,
            'model': model,
            'layer': layer,
            'conversion': conversion,
            'app': app_base,
        }

    return None


def update_results_md(
    results_md_file: str,
    compilation_results: Dict[Tuple[str, str, str, str, str], List[bool]],
    dry_run: bool = False
) -> int:
    """Update results.md with compilation results. Returns number of updates made."""
    md_path = Path(results_md_file)
    
    if not md_path.exists():
        print(f"⚠️  Results MD file not found: {results_md_file}")
        return 0
    
    with open(md_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()

    updated_lines = []
    updates_made = 0

    for line in lines:
        if not line.strip().startswith('|') or line.strip().startswith('|---'):
            updated_lines.append(line)
            continue

        parts = [p.strip() for p in line.split('|')]
        if len(parts) < 7:
            updated_lines.append(line)
            continue

        cli_tool = parts[1]
        model = parts[2]
        layer = parts[3]
        conversion = parts[4]
        app = parts[5]

        key = (cli_tool, model, layer, conversion, app)

        if key in compilation_results:
            compiled_status = ''.join('✅' if s else '❌' for s in compilation_results[key])
            while len(parts) <= 8:
                parts.append('')
            parts[8] = compiled_status
            updates_made += 1

        updated_lines.append('|'.join(parts) + '\n')

    if not dry_run and updates_made > 0:
        with open(md_path, 'w', encoding='utf-8') as f:
            f.writelines(updated_lines)
        print(f"✅ Updated {updates_made} rows in {results_md_file}")
    elif dry_run:
        print(f"[DRY RUN] Would update {updates_made} rows in {results_md_file}")

    return updates_made


def run_command(cmd, cwd=None):
    """Run a shell command and return exit code and error message (from stderr or stdout)."""
    try:
        result = subprocess.run(
            cmd, 
            stdout=subprocess.PIPE, 
            stderr=subprocess.PIPE, 
            text=True, 
            timeout=600,
            cwd=cwd
        )
        error_msg = result.stderr.strip() if result.stderr.strip() else result.stdout.strip()
        if result.returncode != 0:
            return result.returncode, error_msg
        return result.returncode, ""
    except subprocess.TimeoutExpired:
        return 1, f"Command timed out after 600 seconds"
    except Exception as e:
        return 1, str(e)


def parse_failed_entries_from_md(md_file: str) -> set:
    """Parse markdown file to find entries with failures in the compiled column.
    Returns a set of tuples: (cli_tool, model, layer, conversion, app, run_num)"""
    failed_entries = set()
    
    try:
        with open(md_file, 'r', encoding='utf-8') as f:
            lines = f.readlines()
    except FileNotFoundError:
        print(f"⚠️  Markdown file not found: {md_file}")
        return failed_entries
    
    for line in lines:
        if not line.strip().startswith('|') or line.strip().startswith('|---'):
            continue
        
        parts = [p.strip() for p in line.split('|')]
        if len(parts) < 9:
            continue
        
        cli_tool = parts[1]
        model = parts[2]
        layer = parts[3]
        conversion = parts[4]
        app = parts[5]
        compiled_str = parts[8] if len(parts) > 8 else ""
        
        if '❌' in compiled_str:
            for i, char in enumerate(compiled_str):
                if char == '❌':
                    run_num = i + 1
                    failed_entries.add((cli_tool, model, layer, conversion, app, run_num))
    
    return failed_entries


def find_projects(root, failed_entries=None):
    """Recursively find all directories containing pom.xml or build.gradle.
    Only includes projects at the run_X level, not in subdirectories.
    If failed_entries is provided, only includes projects matching those entries."""
    projects = []
    root_path = Path(root)
    if not root_path.exists():
        print(f"Warning: Root directory does not exist: {root}")
        return projects
    
    include_paths = None
    if failed_entries:
        include_paths = set()
        for cli_tool, model, layer, conversion, app, run_num in failed_entries:
            app_conv = f"{app}-{conversion}"
            path = root_path / cli_tool / layer / app_conv / f"run_{run_num}"
            abs_path = str(path.resolve())
            rel_path = str(path)
            include_paths.add(abs_path)
            include_paths.add(rel_path)
    
    for dirpath, _, filenames in os.walk(root):
        dir_path = Path(dirpath)
        last_component = dir_path.name
        if re.match(r'^run_\d+$', last_component):
            if include_paths:
                dir_abs = str(dir_path.resolve())
                dir_rel = str(dir_path)
                if dir_abs not in include_paths and dir_rel not in include_paths:
                    continue
            
            if "pom.xml" in filenames:
                projects.append((dirpath, "Maven"))
            elif "build.gradle" in filenames or "build.gradle.kts" in filenames:
                projects.append((dirpath, "Gradle"))
    return projects


def build_project(project, conversions_dir):
    """Build a single project and return results."""
    build_dir, build_sys = project
    status = 1
    error_msg = ""

    try:
        if build_sys == "Maven":
            print(f"[Thread] Building Maven project: {build_dir}")
            status, error_msg = run_command([
                "mvn", "clean", "package", "-Dmaven.test.skip=true", "-Dmaven.repo.local=.m2repo"
            ], cwd=build_dir)
            if status != 0:
                print(f"[Thread] Retrying Maven build without custom repo: {build_dir}")
                status, error_msg = run_command([
                    "mvn", "clean", "package", "-Dmaven.test.skip=true"
                ], cwd=build_dir)
        else:
            print(f"[Thread] Building Gradle project: {build_dir}")
            gradle_cmd = ["./gradlew", "build", "--exclude-task", "test"] if os.path.isfile(os.path.join(build_dir, "gradlew")) \
                         else ["gradle", "build", "--exclude-task", "test"]
            status, error_msg = run_command(gradle_cmd, cwd=build_dir)
    except Exception as e:
        error_msg = f"Exception during build: {str(e)}"
        status = 1

    if status != 0 and error_msg:
        build_path = Path(build_dir)
        conversions_path = Path(conversions_dir)
        
        try:
            rel_path = build_path.relative_to(conversions_path)
            
            eval_outputs_dir = Path("evaluation-outputs") / rel_path
            eval_outputs_dir.mkdir(parents=True, exist_ok=True)
            
            error_file = "mvn_error.txt" if build_sys == "Maven" else "gradle_error.txt"
            error_path = eval_outputs_dir / error_file
            
            with open(error_path, 'w', encoding='utf-8') as f:
                f.write(f"Build System: {build_sys}\n")
                f.write(f"Build Directory: {build_dir}\n")
                f.write(f"Status: Failed\n")
                f.write("=" * 80 + "\n")
                f.write("Error Output:\n")
                f.write("=" * 80 + "\n")
                f.write(error_msg)
            print(f"[Thread] Saved error output to: {error_path}")
        except Exception as e:
            print(f"[Thread] Warning: Could not save error output: {str(e)}")

    result_text = "Success" if status == 0 else "Failure"
    return [build_dir, build_sys, result_text, error_msg if error_msg else "No error"]


def main():
    parser = argparse.ArgumentParser(
        description="Check compilation status for all converted applications"
    )
    parser.add_argument(
        "--conversions-dir",
        required=True,
        help="Directory containing all conversion outputs to check (e.g., agentic)"
    )
    parser.add_argument(
        "--result-file",
        required=True,
        help="Path to output CSV file (e.g., results_compile.csv)"
    )
    parser.add_argument(
        "--max-workers",
        type=int,
        default=4,
        help="Number of parallel compilation jobs (default: 4)"
    )
    parser.add_argument(
        "--timeout",
        type=int,
        default=600,
        help="Timeout per compilation in seconds (default: 600)"
    )
    parser.add_argument(
        "--results-md",
        help="Path to results markdown file to update (e.g., whole_app_conversions.md)"
    )
    parser.add_argument(
        "--only-failures",
        help="Path to markdown file to read failures from. Only rerun projects that show ❌ in the compiled column."
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show what would be done without making changes"
    )
    args = parser.parse_args()
    
    conversions_dir = Path(args.conversions_dir).resolve()
    result_file = Path(args.result_file)
    
    failed_entries = None
    if args.only_failures:
        print(f"Reading failures from: {args.only_failures}")
        failed_entries = parse_failed_entries_from_md(args.only_failures)
        print(f"Found {len(failed_entries)} failed run(s) to rerun")
        if not failed_entries:
            print("⚠️  No failures found in markdown file. Exiting.")
            return
    
    print(f"🔍 Searching for projects in: {conversions_dir}")
    projects = find_projects(str(conversions_dir), failed_entries=failed_entries)
    print(f"📦 Found {len(projects)} project(s) to build.")
    
    if not projects:
        print("⚠️  No projects found. Exiting.")
        return
    
    print(f"🔨 Building projects (max {args.max_workers} parallel workers)...")
    results = []
    with ThreadPoolExecutor(max_workers=args.max_workers) as executor:
        futures = {executor.submit(build_project, project, str(conversions_dir)): project for project in projects}
        for future in as_completed(futures):
            results.append(future.result())
    
    results.sort(key=lambda x: x[0])
    
    if args.only_failures and result_file.exists():
        print(f"Reading existing results from: {result_file}")
        existing_results = {}
        with open(result_file, mode="r", newline="") as file:
            reader = csv.DictReader(file)
            for row in reader:
                existing_results[row['Path']] = [row['Path'], row['Build System'], row['Status'], row['Error']]
        
        updated_count = 0
        new_paths = {result[0] for result in results}
        for result in results:
            if result[0] in existing_results:
                updated_count += 1
            existing_results[result[0]] = result
        
        results = sorted(existing_results.values(), key=lambda x: x[0])
        print(f"📝 Merged results: {len(results)} total entries (updated {updated_count} entries)")
    
    result_file.parent.mkdir(parents=True, exist_ok=True)
    with open(result_file, mode="w", newline="") as file:
        writer = csv.writer(file)
        writer.writerow(["Path", "Build System", "Status", "Error"]) 
        writer.writerows(results)
    
    success_count = sum(1 for r in results if r[2] == "Success")
    failure_count = len(results) - success_count
    print(f"\n📊 Build Summary")
    print(f"   ✅ Successful: {success_count}")
    print(f"   ❌ Failed: {failure_count}")
    print(f"   📄 Results saved to: {result_file}")
    
    if args.results_md:
        print(f"\n📝 Processing compilation results for markdown update...")
        compilation_results = defaultdict(lambda: {})
        
        for result in results:
            build_dir, build_sys, status, error = result
            success = (status == "Success")
            
            if re.search(r'/run_\d+/.+', build_dir):
                continue
            
            components = parse_path_to_components(build_dir)
            if not components:
                continue
            
            run_match = re.search(r'/run_(\d+)(?:/|$)', build_dir)
            if run_match:
                run_num = int(run_match.group(1))
            else:
                run_num = 1
            
            key = (
                components['cli_tool'],
                components['model'],
                components['layer'],
                components['conversion'],
                components['app']
            )
            compilation_results[key][run_num] = success
        
        final_results = {}
        for key, runs_dict in compilation_results.items():
            max_run = max(runs_dict.keys()) if runs_dict else 0
            runs_list = [runs_dict.get(i, False) for i in range(1, max_run + 1)]
            final_results[key] = runs_list
        
        if final_results:
            update_results_md(args.results_md, final_results, dry_run=args.dry_run)
        else:
            print(f"⚠️  No compilation results could be parsed for markdown update")


if __name__ == "__main__":
    main()

