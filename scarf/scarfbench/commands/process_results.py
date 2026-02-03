#!/usr/bin/env python3
"""
process_results.py
Consolidated post-processing script for conversion results.

This script:
1. Reads conversion results from run_agent.py (JSON format)
2. Updates whole_app_conversions.md with conversion status
3. Optionally deletes failed run directories
4. Generates YAML files for rerunning failed conversions
5. Produces a summary report
"""

import argparse
import json
import re
import shutil
import sys
from collections import defaultdict
from pathlib import Path
from typing import Dict, List, Optional, Tuple

# Styling
def is_tty() -> bool:
    return sys.stdout.isatty()

BOLD = "\033[1m" if is_tty() else ""
DIM = "\033[2m" if is_tty() else ""
RESET = "\033[0m" if is_tty() else ""
RED = "\033[31m" if is_tty() else ""
GREEN = "\033[32m" if is_tty() else ""
YELLOW = "\033[33m" if is_tty() else ""
BLUE = "\033[34m" if is_tty() else ""

MODEL_MAP = {
    'codex': 'gpt-5',
    'claude': 'claude-sonnet-4.5',
    'gemini': 'gemini-2.5-pro',
    'qwen': 'qwen3-coder-480b',
}

CONVERSION_MAP = {
    'jakarta-to-quarkus': {'before': 'Jakarta', 'after': 'Quarkus'},
    'jakarta-to-spring': {'before': 'Jakarta', 'after': 'Spring'},
    'quarkus-to-jakarta': {'before': 'Quarkus', 'after': 'Jakarta'},
    'quarkus-to-spring': {'before': 'Quarkus', 'after': 'Spring'},
    'spring-to-jakarta': {'before': 'Spring', 'after': 'Jakarta'},
    'spring-to-quarkus': {'before': 'Spring', 'after': 'Quarkus'},
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


def load_results_json(results_file: str) -> Dict[Tuple[str, str, str, str, str], List[bool]]:
    """Load results from JSON file produced by run_agent.py and convert to internal format."""
    try:
        with open(results_file, 'r', encoding='utf-8') as f:
            data = json.load(f)
    except FileNotFoundError:
        print(f"{RED}❌ Results file not found:{RESET} {results_file}")
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"{RED}❌ Error parsing JSON:{RESET} {e}")
        sys.exit(1)
    
    results = defaultdict(lambda: {})
    
    for result in data.get('results', []):
        output_dir = result.get('output_dir', '')
        run_dir = result.get('run_dir', '')
        run_num = result.get('run_num', 0)
        success = result.get('success', False)
        
        components = parse_path_to_components(output_dir)
        if not components:
            components = parse_path_to_components(run_dir)
        
        if not components:
            continue
        
        key = (
            components['cli_tool'],
            components['model'],
            components['layer'],
            components['conversion'],
            components['app']
        )
        results[key][run_num] = success
    
    final_results = {}
    for key, runs_dict in results.items():
        max_run = max(runs_dict.keys()) if runs_dict else 0
        runs_list = [runs_dict.get(i, False) for i in range(1, max_run + 1)]
        final_results[key] = runs_list
    
    return final_results


def parse_results_from_output(output_file: str) -> Dict[Tuple[str, str, str, str, str], List[bool]]:
    """
    Parse run_agent.py output file to extract conversion results.
    Fallback method if JSON results aren't available.
    """
    results = defaultdict(lambda: {})

    try:
        with open(output_file, 'r', encoding='utf-8', errors='ignore') as f:
            content = f.read()

        output_pattern = re.compile(
            r'Output saved to:\s*(\S+)\s*\n\s*(✅|❌)\s*(run_(\d+))\s*(?:completed successfully|failed:)',
            re.MULTILINE
        )

        for match in output_pattern.finditer(content):
            output_path = match.group(1)
            status_symbol = match.group(2)
            run_id = match.group(3)
            run_num = int(match.group(4))

            base_path = re.sub(r'/run_\d+/.agent_out/stdout\.txt$', '', output_path)
            base_path = re.sub(r'/.agent_out/stdout\.txt$', '', base_path)
            components = parse_path_to_components(base_path)
            if not components:
                continue

            key = (
                components['cli_tool'],
                components['model'],
                components['layer'],
                components['conversion'],
                components['app']
            )
            results[key][run_num] = (status_symbol == '✅')

    except FileNotFoundError:
        print(f"{YELLOW}⚠️  Output file not found:{RESET} {output_file}")
    except Exception as e:
        print(f"{YELLOW}⚠️  Error parsing output file:{RESET} {e}")

    final_results = {}
    for key, runs_dict in results.items():
        max_run = max(runs_dict.keys()) if runs_dict else 0
        runs_list = [runs_dict.get(i, False) for i in range(1, max_run + 1)]
        final_results[key] = runs_list

    return final_results


def update_results_md(
    results_md_file: str,
    conversion_results: Dict[Tuple[str, str, str, str, str], List[bool]],
    dry_run: bool = False
) -> int:
    """Update results.md with conversion results. Returns number of updates made."""
    md_path = Path(results_md_file)
    
    if not md_path.exists():
        print(f"{YELLOW}⚠️  Results MD file not found. Creating new file...{RESET}")
        lines = [
            "|cli-tool|model|layer|conversion|app|orig-exists|converted|compiled automatic|ran|smoke|\n",
            "|--------|-----|-----|----------|---|-----------|---------|------------------|---|-----|\n"
        ]
    else:
        with open(md_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()

    updated_lines = []
    updates_made = 0
    existing_keys = set()

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
        existing_keys.add(key)

        if key in conversion_results:
            converted_status = ''.join('✅' if s else '❌' for s in conversion_results[key])
            if len(parts) > 7:
                parts[7] = converted_status
                updates_made += 1
            else:
                while len(parts) <= 7:
                    parts.append('')
                parts[7] = converted_status
                updates_made += 1

        updated_lines.append('|'.join(parts) + '\n')

    new_keys = set(conversion_results.keys()) - existing_keys
    for key in sorted(new_keys):
        cli_tool, model, layer, conversion, app = key
        converted_status = ''.join('✅' if s else '❌' for s in conversion_results[key])
        parts = ['', cli_tool, model, layer, conversion, app, '✅', converted_status, '', '', '']
        updated_lines.append('|'.join(parts) + '\n')
        updates_made += 1

    if not dry_run:
        with open(md_path, 'w', encoding='utf-8') as f:
            f.writelines(updated_lines)
        print(f"{GREEN}✅ Updated {updates_made} rows in {results_md_file}{RESET}")
    else:
        print(f"{DIM}[DRY RUN] Would update {updates_made} rows in {results_md_file}{RESET}")

    return updates_made


def delete_failed_runs(
    conversion_results: Dict[Tuple[str, str, str, str, str], List[bool]],
    base_dir: Path,
    conversions_dir: str,
    dry_run: bool = False
) -> Tuple[int, List[str]]:
    """Delete directories for failed runs. Returns (deleted_count, failed_paths)."""
    failed_paths = []
    
    for key, runs in conversion_results.items():
        cli_tool, model, layer, conversion, app = key
        
        app_dir = base_dir / conversions_dir / cli_tool / layer / f"{app}-{conversion}"
        
        for run_num, success in enumerate(runs, start=1):
            if not success:
                run_dir = app_dir / f"run_{run_num}"
                if run_dir.exists():
                    failed_paths.append(str(run_dir))
    
    if not failed_paths:
        print(f"{GREEN}✅ No failed runs to delete.{RESET}")
        return 0, []
    
    print(f"\n📋 Found {len(failed_paths)} failed run directory(ies):")
    for path in sorted(failed_paths)[:10]:  
        print(f"  {DIM}- {path}{RESET}")
    if len(failed_paths) > 10:
        print(f"  {DIM}... and {len(failed_paths) - 10} more{RESET}")
    
    if dry_run:
        print(f"\n{DIM}[DRY RUN] Would delete {len(failed_paths)} directories{RESET}")
        return 0, failed_paths
    
    deleted_count = 0
    for path_str in failed_paths:
        path = Path(path_str)
        try:
            if path.exists():
                shutil.rmtree(path)
                deleted_count += 1
        except Exception as e:
            print(f"{RED}❌ Error deleting {path}: {e}{RESET}")
    
    print(f"\n{GREEN}✅ Deleted {deleted_count} failed run directory(ies){RESET}")
    return deleted_count, failed_paths


def generate_rerun_yamls(
    conversion_results: Dict[Tuple[str, str, str, str, str], List[bool]],
    base_dir: Path,
    output_dir: Path,
    conversions_dir: str
) -> int:
    """Generate YAML files for failed conversions that need to be rerun."""
    failed_groups = defaultdict(lambda: defaultdict(set))
    
    for key, runs in conversion_results.items():
        cli_tool, model, layer, conversion, app = key
        
        if not all(runs):
            failed_groups[(cli_tool, model, conversion)][layer].add(app)
    
    if not failed_groups:
        print(f"{GREEN}✅ No failed conversions to generate YAMLs for.{RESET}")
        return 0
    
    def get_source_path(layer: str, app: str, conversion: str) -> str:
        """Determine source path based on conversion type."""
        before = conversion.split('-to-')[0]
        
        if layer == 'whole_applications':
            return f"../../bench/whole_applications/{before}/{app}"
        elif layer == 'business_domain':
            return f"../../bench/business_domain/{app}/{before}"
        elif layer == 'dependency_injection':
            if app in ['producermethods', 'simplegreeting']:
                return f"../../bench/dependency_injection/{before}/{app}"
            else:
                return f"../../bench/dependency_injection/{app}/{before}"
        elif layer in ['infrastructure', 'security', 'integration', 'persistence', 'presentation']:
            return f"../../bench/{layer}/{before}/{app}"
        else:
            return f"../../bench/{layer}/{before}/{app}"
    
    def get_output_path(cli_tool: str, layer: str, app: str, conversion: str) -> str:
        """Determine output path using provided conversions_dir."""
        tool_dir_map = {
            'claude': 'claude',
            'codex': 'codex',
            'gemini': 'gemini',
            'qwen': 'qwen'
        }
        tool_dir = tool_dir_map.get(cli_tool, cli_tool)
        
        return f"{conversions_dir}/{tool_dir}/{layer}/{app}-{conversion}"
    
    def get_command(cli_tool: str) -> str:
        """Get command template for CLI tool."""
        if cli_tool == 'claude':
            return "cd {working_dir}; claude --model aws/claude-sonnet-4-5 --output-format stream-json --print --verbose --tools default --add-dir {working_dir} --permission-mode bypassPermissions  {prompt}"
        elif cli_tool == 'codex':
            return "codex exec {prompt} --skip-git-repo-check --sandbox workspace-write -C {working_dir}"
        elif cli_tool == 'gemini':
            return "cd {working_dir}; gemini {prompt} --model gemini-2.5-pro --yolo --debug"
        elif cli_tool == 'qwen':
            return "cd {working_dir}; qwen -p  {prompt}  --approval-mode auto-edit"
        else:
            return f"cd {{working_dir}}; {cli_tool} {{prompt}}"
    
    def get_prompt_file(cli_tool: str) -> str:
        """Get prompt file path for CLI tool."""
        if cli_tool == 'codex':
            return "../../prompts/gpt-5.txt"
        elif cli_tool == 'gemini':
            return "../../prompts/gemini-2.5-pro.txt"
        elif cli_tool == 'qwen':
            return "../../prompts/qwen-3.txt"
        else:
            return "../../prompts/claude-sonnet-4.5.txt"
    
    yaml_count = 0
    output_dir.mkdir(parents=True, exist_ok=True)
    
    for (cli_tool, model, conversion), layers_apps in failed_groups.items():
        if conversion not in CONVERSION_MAP:
            continue
        
        conv_info = CONVERSION_MAP[conversion]
        command = get_command(cli_tool)
        prompt_file = get_prompt_file(cli_tool)
        
        yaml_content = f"""command: {command}
runs: 3
timeout: 300
before: {conv_info['before']}
after: {conv_info['after']}

seeds:

"""
        
        # Add seeds organized by layer
        layer_order = [
            'whole_applications',
            'business_domain',
            'dependency_injection',
            'infrastructure',
            'security',
            'integration',
            'persistence',
            'presentation'
        ]
        
        for layer in layer_order:
            if layer not in layers_apps:
                continue
            
            layer_name = layer.replace('_', ' ').title()
            yaml_content += f"# {layer_name}\n"
            
            for app in sorted(layers_apps[layer]):
                source = get_source_path(layer, app, conversion)
                output = get_output_path(cli_tool, layer, app, conversion)
                yaml_content += f"- source: {source}\n"
                yaml_content += f"  output: {output}\n"
                yaml_content += f"  exclude_files: [smoke.py, justfile, Dockerfile, .idea/]\n"
                yaml_content += "\n"
        
        # Add conversions section
        yaml_content += "conversions:\n"
        for layer in layer_order:
            if layer not in layers_apps:
                continue
            
            layer_name = layer.replace('_', ' ').title()
            yaml_content += f"# {layer_name}\n"
            
            for app in sorted(layers_apps[layer]):
                output = get_output_path(cli_tool, layer, app, conversion)
                yaml_content += f"- {output} | {prompt_file}\n"
            yaml_content += "\n"
        
        # Write YAML file
        filename = f"{cli_tool}-{model.replace('/', '-')}-{conversion}-rerun.yaml"
        filename = filename.replace(' ', '-')
        yaml_path = output_dir / filename
        
        with open(yaml_path, 'w', encoding='utf-8') as f:
            f.write(yaml_content)
        
        yaml_count += 1
        print(f"  {GREEN}✅ Generated:{RESET} {yaml_path}")
    
    print(f"\n{GREEN}✅ Generated {yaml_count} YAML file(s) for reruns{RESET}")
    return yaml_count


def generate_summary(
    conversion_results: Dict[Tuple[str, str, str, str, str], List[bool]],
    failed_paths: List[str],
    yaml_count: int
) -> str:
    """Generate a summary report of the processing."""
    total_conversions = len(conversion_results)
    total_runs = sum(len(runs) for runs in conversion_results.values())
    successful_runs = sum(sum(1 for s in runs if s) for runs in conversion_results.values())
    failed_runs = total_runs - successful_runs
    
    summary = f"""
{BOLD}📊 PROCESSING SUMMARY{RESET}
{'=' * 60}
Total conversions processed: {BOLD}{total_conversions}{RESET}
Total runs: {BOLD}{total_runs}{RESET}
  ✅ Successful: {GREEN}{successful_runs}{RESET}
  ❌ Failed: {RED}{failed_runs}{RESET}
  Success rate: {BOLD}{(successful_runs/total_runs*100):.1f}%{RESET}

Failed run directories: {BOLD}{len(failed_paths)}{RESET}
YAML files generated for reruns: {BOLD}{yaml_count}{RESET}
{'=' * 60}
"""
    return summary


def main():
    parser = argparse.ArgumentParser(
        description="Process conversion results: update MD, delete failed runs, generate rerun YAMLs"
    )
    parser.add_argument(
        "--results-json",
        help="Path to JSON results file from run_agent.py (preferred)"
    )
    parser.add_argument(
        "--results-output",
        help="Path to run_agent.py output file (fallback if JSON not available)"
    )
    parser.add_argument(
        "--results-md",
        default="whole_app_conversions.md",
        help="Path to results markdown file (default: whole_app_conversions.md)"
    )
    parser.add_argument(
        "--base-dir",
        default=".",
        help="Base directory containing conversion outputs (default: .)"
    )
    parser.add_argument(
        "--conversions-dir",
        default="agentic",
        help="Directory name containing conversions"
    )
    parser.add_argument(
        "--yaml-output-dir",
        default="rerun_yamls",
        help="Directory to write rerun YAML files (default: rerun_yamls)"
    )
    parser.add_argument(
        "--delete-failed",
        action="store_true",
        help="Delete directories for failed runs"
    )
    parser.add_argument(
        "--generate-yamls",
        action="store_true",
        help="Generate YAML files for failed conversions"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show what would be done without making changes"
    )
    args = parser.parse_args()
    
    if args.results_json:
        print(f"📖 Loading results from JSON: {args.results_json}")
        conversion_results = load_results_json(args.results_json)
    elif args.results_output:
        print(f"📖 Parsing results from output file: {args.results_output}")
        conversion_results = parse_results_from_output(args.results_output)
    else:
        print(f"{RED}❌ Must provide either --results-json or --results-output{RESET}")
        sys.exit(1)
    
    if not conversion_results:
        print(f"{YELLOW}⚠️  No conversion results found.{RESET}")
        sys.exit(0)
    
    print(f"📊 Found {len(conversion_results)} conversion result sets\n")
    
    # Update results MD
    print(f"📝 Updating {args.results_md}...")
    update_results_md(args.results_md, conversion_results, dry_run=args.dry_run)
    
    # Delete failed runs
    failed_paths = []
    if args.delete_failed:
        print(f"\n🗑️  Processing failed runs...")
        base_dir_path = Path(args.base_dir).resolve()
        deleted_count, failed_paths = delete_failed_runs(
            conversion_results, base_dir_path, args.conversions_dir, dry_run=args.dry_run
        )
    
    # Generate rerun YAMLs
    yaml_count = 0
    if args.generate_yamls:
        print(f"\n📄 Generating rerun YAML files...")
        base_dir_path = Path(args.base_dir).resolve()
        yaml_output_path = Path(args.yaml_output_dir)
        yaml_count = generate_rerun_yamls(
            conversion_results, base_dir_path, yaml_output_path, args.conversions_dir
        )
    
    # Print summary
    print(generate_summary(conversion_results, failed_paths, yaml_count))


if __name__ == "__main__":
    main()

