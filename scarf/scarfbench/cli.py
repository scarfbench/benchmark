#!/usr/bin/env python3
"""
scarfbench CLI - Unified command-line interface for ScarfBench conversion benchmark tools

This CLI provides a single entry point for all benchmark operations:
- setup: Create run directories from source applications
- run: Execute agent conversions
- process: Process conversion results (update MD, delete failed runs, generate rerun YAMLs)
- compile: Check compilation status of converted applications
- docker: Build and test Docker containers
- csv: Convert markdown results to CSV summary
"""

import argparse
import sys


def setup_command(args):
    """Handle setup subcommand."""
    from scarfbench.commands import setup
    old_argv = sys.argv[:]
    try:
        sys.argv = ['scarfbench']
        sys.argv.extend(['-c', args.config])
        if args.num_runs is not None:
            sys.argv.extend(['-n', str(args.num_runs)])
        setup.main()
    finally:
        sys.argv = old_argv


def run_command(args):
    """Handle run subcommand."""
    from scarfbench.commands import run_agent
    old_argv = sys.argv[:]
    try:
        sys.argv = ['scarfbench']
        sys.argv.extend(['-c', args.config])
        if args.num_runs is not None:
            sys.argv.extend(['-n', str(args.num_runs)])
        if args.dry_run:
            sys.argv.append('--dry-run')
        if args.results_json:
            sys.argv.extend(['--results-json', args.results_json])
        run_agent.main()
    finally:
        sys.argv = old_argv


def process_command(args):
    """Handle process subcommand."""
    from scarfbench.commands import process_results
    old_argv = sys.argv[:]
    try:
        sys.argv = ['scarfbench']
        if args.results_json:
            sys.argv.extend(['--results-json', args.results_json])
        if args.results_output:
            sys.argv.extend(['--results-output', args.results_output])
        if args.results_md:
            sys.argv.extend(['--results-md', args.results_md])
        if args.base_dir:
            sys.argv.extend(['--base-dir', args.base_dir])
        if args.conversions_dir:
            sys.argv.extend(['--conversions-dir', args.conversions_dir])
        if args.yaml_output_dir:
            sys.argv.extend(['--yaml-output-dir', args.yaml_output_dir])
        if args.delete_failed:
            sys.argv.append('--delete-failed')
        if args.generate_yamls:
            sys.argv.append('--generate-yamls')
        if args.dry_run:
            sys.argv.append('--dry-run')
        process_results.main()
    finally:
        sys.argv = old_argv


def compile_command(args):
    """Handle compile subcommand."""
    from scarfbench.commands import check_compilation
    old_argv = sys.argv[:]
    try:
        sys.argv = ['scarfbench']
        sys.argv.extend(['--conversions-dir', args.conversions_dir])
        sys.argv.extend(['--result-file', args.result_file])
        if args.results_md:
            sys.argv.extend(['--results-md', args.results_md])
        if args.only_failures:
            sys.argv.extend(['--only-failures', args.only_failures])
        if args.max_workers != 4:
            sys.argv.extend(['--max-workers', str(args.max_workers)])
        if args.timeout != 600:
            sys.argv.extend(['--timeout', str(args.timeout)])
        if args.dry_run:
            sys.argv.append('--dry-run')
        check_compilation.main()
    finally:
        sys.argv = old_argv


def docker_command(args):
    """Handle docker subcommand."""
    from scarfbench.commands import check_docker
    old_argv = sys.argv[:]
    try:
        sys.argv = ['scarfbench']
        if args.results_file != 'whole_app_conversions.md':
            sys.argv.extend(['--results-file', args.results_file])
        elif args.results_file:
            sys.argv.extend(['--results-file', args.results_file])
        if args.result_file:
            sys.argv.extend(['--result-file', args.result_file])
        if args.base_dir != '.':
            sys.argv.extend(['--base-dir', args.base_dir])
        if args.conversions_dir != 'agentic':
            sys.argv.extend(['--conversions-dir', args.conversions_dir])
        if args.dry_run:
            sys.argv.append('--dry-run')
        if args.skip_existing:
            sys.argv.append('--skip-existing')
        if args.max_workers != 128:
            sys.argv.extend(['--max-workers', str(args.max_workers)])
        if args.build_timeout != 600:
            sys.argv.extend(['--build-timeout', str(args.build_timeout)])
        if args.startup_wait != 2:
            sys.argv.extend(['--startup-wait', str(args.startup_wait)])
        if args.smoke_wait != 480:
            sys.argv.extend(['--smoke-wait', str(args.smoke_wait)])
        if args.smoke_attempts != 5:
            sys.argv.extend(['--smoke-attempts', str(args.smoke_attempts)])
        if args.smoke_delay != 2.0:
            sys.argv.extend(['--smoke-delay', str(args.smoke_delay)])
        check_docker.main()
    finally:
        sys.argv = old_argv


def csv_command(args):
    """Handle csv subcommand."""
    from scarfbench.commands import md_to_csv
    md_to_csv.main(input_md=args.input, output_csv=args.output, org=args.org, link=args.link)


def main():
    """Main CLI entry point."""
    parser = argparse.ArgumentParser(
        prog='scarfbench',
        description='ScarfBench conversion benchmark tools - unified CLI',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Setup run directories
  scarfbench setup -c config.yaml

  # Run conversions
  scarfbench run -c config.yaml --results-json results.json

  # Process results
  scarfbench process --results-json results.json --delete-failed --generate-yamls

  # Check compilation
  scarfbench compile --conversions-dir agentic --result-file results_compile.csv

  # Test Docker containers
  scarfbench docker --results-file whole_app_conversions.md

  # Generate CSV summary
  scarfbench csv whole_app_conversions.md summary.csv --org "Your Company"
        """
    )
    
    subparsers = parser.add_subparsers(dest='command', help='Available commands', metavar='COMMAND')
    
    # Setup command
    setup_parser = subparsers.add_parser('setup', help='Create run directories from source applications')
    setup_parser.add_argument('-c', '--config', default='config.yaml',
                              help='Path to YAML configuration file (default: config.yaml)')
    setup_parser.add_argument('-n', '--num-runs', type=int, default=None,
                              help='Number of runs to create for each conversion (overrides config file)')
    setup_parser.set_defaults(func=setup_command)
    
    # Run command
    run_parser = subparsers.add_parser('run', help='Execute agent conversions')
    run_parser.add_argument('-c', '--config', default='config.yaml',
                            help='Path to YAML configuration file (default: config.yaml)')
    run_parser.add_argument('-n', '--num-runs', type=int, default=None,
                            help='Number of runs to execute (overrides config file)')
    run_parser.add_argument('--dry-run', action='store_true',
                            help='Show what would be executed without running commands')
    run_parser.add_argument('--results-json', type=str, default=None,
                            help='Path to write JSON results file (optional)')
    run_parser.set_defaults(func=run_command)
    
    # Process command
    process_parser = subparsers.add_parser('process', 
                                           help='Process conversion results: update MD, delete failed runs, generate rerun YAMLs')
    process_parser.add_argument('--results-json',
                                help='Path to JSON results file from run_agent.py (preferred)')
    process_parser.add_argument('--results-output',
                                help='Path to run_agent.py output file (fallback if JSON not available)')
    process_parser.add_argument('--results-md', default='whole_app_conversions.md',
                                help='Path to results markdown file (default: whole_app_conversions.md)')
    process_parser.add_argument('--base-dir', default='.',
                                help='Base directory containing conversion outputs (default: .)')
    process_parser.add_argument('--conversions-dir', default='agentic',
                                help='Directory name containing conversions (e.g., "agentic" or "agentic2", default: "agentic")')
    process_parser.add_argument('--yaml-output-dir', default='rerun_yamls',
                                help='Directory to write rerun YAML files (default: rerun_yamls)')
    process_parser.add_argument('--delete-failed', action='store_true',
                                help='Delete directories for failed runs')
    process_parser.add_argument('--generate-yamls', action='store_true',
                                help='Generate YAML files for failed conversions')
    process_parser.add_argument('--dry-run', action='store_true',
                                help='Show what would be done without making changes')
    process_parser.set_defaults(func=process_command)
    
    # Compile command
    compile_parser = subparsers.add_parser('compile', help='Check compilation status for all converted applications')
    compile_parser.add_argument('--conversions-dir', required=True,
                                help='Directory containing all conversion outputs to check (e.g., agentic)')
    compile_parser.add_argument('--result-file', required=True,
                                help='Path to output CSV file (e.g., results_compile.csv)')
    compile_parser.add_argument('--results-md',
                                help='Path to results markdown file to update (e.g., whole_app_conversions.md)')
    compile_parser.add_argument('--only-failures',
                                help='Path to markdown file to read failures from. Only rerun projects that show ❌ in the compiled column.')
    compile_parser.add_argument('--max-workers', type=int, default=4,
                                help='Number of parallel compilation jobs (default: 4)')
    compile_parser.add_argument('--timeout', type=int, default=600,
                                help='Timeout per compilation in seconds (default: 600)')
    compile_parser.add_argument('--dry-run', action='store_true',
                                help='Show what would be done without making changes')
    compile_parser.set_defaults(func=compile_command)
    
    # Docker command
    docker_parser = subparsers.add_parser('docker', help='Build Docker containers and verify applications run')
    docker_parser.add_argument('--results-file', default='whole_app_conversions.md',
                               help='Path to results markdown file to read/update (default: whole_app_conversions.md)')
    docker_parser.add_argument('--result-file',
                               help='Path to output CSV file for Docker results (e.g., results_docker.csv)')
    docker_parser.add_argument('--base-dir', default='.',
                              help='Base directory (default: .)')
    docker_parser.add_argument('--conversions-dir', default='agentic',
                              help='Directory containing conversions (default: agentic)')
    docker_parser.add_argument('--dry-run', action='store_true',
                               help='Show what would be done without making changes')
    docker_parser.add_argument('--skip-existing', action='store_true',
                               help='Skip runs that already have success status')
    docker_parser.add_argument('--max-workers', type=int, default=128,
                               help='Maximum parallel workers (default: 128)')
    docker_parser.add_argument('--build-timeout', type=int, default=600,
                               help='Timeout for Docker builds in seconds (default: 600)')
    docker_parser.add_argument('--startup-wait', type=int, default=2,
                               help='Seconds to wait after container starts (default: 2)')
    docker_parser.add_argument('--smoke-wait', type=int, default=480,
                               help='Seconds to wait before smoke testing (default: 480)')
    docker_parser.add_argument('--smoke-attempts', type=int, default=5,
                               help='Number of smoke test attempts (default: 5)')
    docker_parser.add_argument('--smoke-delay', type=float, default=2.0,
                               help='Delay between smoke test attempts in seconds (default: 2.0)')
    docker_parser.set_defaults(func=docker_command)
    
    # CSV command
    csv_parser = subparsers.add_parser('csv', help='Convert markdown results table to CSV summary')
    csv_parser.add_argument('input', help='Input markdown file (e.g., whole_app_conversions.md)')
    csv_parser.add_argument('output', help='Output CSV file (e.g., whole_app_conversions.csv)')
    csv_parser.add_argument('--org', required=True,
                           help='Organization name (e.g., "Your Company")')
    csv_parser.add_argument('--link', default='',
                           help='Website/link URL (optional, e.g., "https://yourcompany.com")')
    csv_parser.set_defaults(func=csv_command)
    
    args = parser.parse_args()
    
    if not args.command:
        parser.print_help()
        sys.exit(1)
    
    # Call the appropriate command function
    args.func(args)


if __name__ == '__main__':
    main()

