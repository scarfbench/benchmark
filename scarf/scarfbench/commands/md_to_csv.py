#!/usr/bin/env python3
"""
md_to_csv.py - Convert markdown results table to CSV summary with updates

- Reads a Markdown table with columns:
  cli-tool | model | layer | conversion | app | orig-exists | converted | compiled automatic | ran | smoke
- Aggregates across all rows, grouping by (model, layer, conversion).
- For each (model, conversion) group, computes percent pass for:
    converted  -> translate
    compiled automatic -> compile
    ran -> run (counts 🟢 as success, 🔨🚫⏭️ as failure)
- Percent pass = (# of ✅ or 🟢) / (# of ✅ or 🟢 + # of ❌ or 🔨🚫⏭️) * 100.
- Updates existing CSV file, preserving org, date, status, link for existing rows
- Writes CSV with header:
  solution,org,date,status,link,layer,from,to,compile,run,translate
"""

import argparse
import csv
import sys
from datetime import datetime
from collections import defaultdict
from pathlib import Path


def count_marks(s: str):
    """Count success and failure marks in a status string."""
    if not s:
        return 0, 0
    passes = s.count('✅')
    fails = s.count('❌')
    total = passes + fails
    return passes, total


def count_ran_marks(s: str):
    """Count success and failure marks in the 'ran' column.
    Success: 🟢
    Failure: 🔨, 🚫, ⏭️, ❌
    """
    if not s:
        return 0, 0
    passes = s.count('🟢')
    # Count failures: 🔨 (build failed), 🚫 (smoke failed), ⏭️ (skipped), ❌ (generic failure)
    fails = s.count('🔨') + s.count('🚫') + s.count('⏭️') + s.count('❌')
    total = passes + fails
    return passes, total


def parse_md_table(md_text: str):
    lines = [ln.strip() for ln in md_text.strip().splitlines() if ln.strip()]
    header_idx = None
    for i, ln in enumerate(lines):
        if ln.startswith('|') and '|' in ln.strip('|'):
            header_idx = i
            break
    if header_idx is None:
        raise ValueError('No markdown table header found.')

    headers = [h.strip() for h in lines[header_idx].strip('|').split('|')]

    content_rows = []
    for ln in lines[header_idx+1:]:
        if not ln.startswith('|'):
            break
        if set(ln.replace('|','').strip()) <= set('-'):
            continue
        parts = [p.strip() for p in ln.strip('|').split('|')]
        if len(parts) < len(headers):
            parts += [''] * (len(headers) - len(parts))
        elif len(parts) > len(headers):
            parts = parts[:len(headers)]
        content_rows.append(dict(zip(headers, parts)))

    return headers, content_rows


def aggregate(rows):
    """Aggregate statistics from markdown rows.
    Groups by (model, layer, conversion) to support multiple layers.
    """
    agg = defaultdict(lambda: {
        'translate_pass':0,'translate_total':0,
        'compile_pass':0,'compile_total':0,
        'run_pass':0,'run_total':0,
        'layer': ''
    })

    for r in rows:
        layer = r.get('layer', '')
        if not layer:
            continue
        key = (r.get('model'), layer, r.get('conversion'))
        p,t = count_marks(r.get('converted',''))
        agg[key]['translate_pass'] += p
        agg[key]['translate_total'] += t
        p,t = count_marks(r.get('compiled automatic',''))
        agg[key]['compile_pass'] += p
        agg[key]['compile_total'] += t
        # Use special counting for 'ran' column which uses 🟢, 🔨, 🚫, ⏭️
        p,t = count_ran_marks(r.get('ran',''))
        agg[key]['run_pass'] += p
        agg[key]['run_total'] += t
        agg[key]['layer'] = layer
    return agg


def load_existing_csv(csv_path: Path) -> dict:
    """Load existing CSV and return a dict keyed by (solution, layer, from, to)."""
    existing = {}
    if csv_path.exists():
        try:
            with open(csv_path, 'r', encoding='utf-8') as f:
                reader = csv.DictReader(f)
                for row in reader:
                    key = (row['solution'], row.get('layer', 'whole app'), row['from'], row['to'])
                    existing[key] = row
        except Exception as e:
            print(f"Warning: Could not read existing CSV: {e}")
    return existing


def build_rows(agg, org: str, link: str, existing_csv: dict):
    """Build output rows, merging with existing CSV data."""
    out_rows = []
    for (model, layer, conversion), stats in agg.items():
        frm, to = conversion.split('-to-') if conversion and '-to-' in conversion else ('', '')
        compile_pct = (stats['compile_pass']/stats['compile_total']*100.0) if stats['compile_total'] else 0.0
        run_pct = (stats['run_pass']/stats['run_total']*100.0) if stats['run_total'] else 0.0
        translate_pct = "-"
        
        layer_display = 'whole app' if layer == 'whole_applications' else layer.replace('_', ' ')
        
        key = (model, layer_display, frm, to)
        if key in existing_csv:
            existing_row = existing_csv[key]
            existing_org = existing_row.get('org', '').strip()
            existing_link = existing_row.get('link', '').strip()
            out_rows.append({
                'solution': model,
                'org': org if org else existing_org,  
                'date': existing_row.get('date', datetime.now().strftime('%m/%d/%y')),
                'status': existing_row.get('status', 'Computed'),
                'link': link if link else existing_link,  
                'layer': existing_row.get('layer', layer_display),
                'from': frm,
                'to': to,
                'compile': round(compile_pct, 1),
                'run': round(run_pct, 1),
                'translate': translate_pct
            })
        else:
            out_rows.append({
                'solution': model,
                'org': org,
                'date': datetime.now().strftime('%m/%d/%y'),
                'status': 'Computed',
                'link': link,
                'layer': layer_display,
                'from': frm,
                'to': to,
                'compile': round(compile_pct, 1),
                'run': round(run_pct, 1),
                'translate': translate_pct
            })
    

    updated_keys = {(row['solution'], row.get('layer', 'whole app'), row['from'], row['to']) for row in out_rows}
    for key, existing_row in existing_csv.items():
        if key not in updated_keys:
            out_rows.append(existing_row)
    
    return out_rows


def main(input_md: str = None, output_csv: str = None, org: str = None, link: str = None):
    """Main function to convert markdown to CSV.
    
    Args:
        input_md: Path to input markdown file
        output_csv: Path to output CSV file
        org: Organization name (required)
        link: Website/link URL (optional)
    """
    if input_md is None or output_csv is None:
        parser = argparse.ArgumentParser(
            description='Convert markdown results table to CSV summary with updates'
        )
        parser.add_argument('input_md', help='Input markdown file (e.g., whole_app_conversions.md)')
        parser.add_argument('output_csv', help='Output CSV file (e.g., whole_app_conversions.csv)')
        parser.add_argument('--org', required=True,
                           help='Organization name (e.g., "Your Company")')
        parser.add_argument('--link', default='',
                           help='Website/link URL (optional, e.g., "https://yourcompany.com")')
        args = parser.parse_args()
        input_md = args.input_md
        output_csv = args.output_csv
        org = args.org
        link = args.link or ''
    
    if not org:
        print('Error: --org argument is required')
        sys.exit(1)
    
    if link is None:
        link = ''
    
    in_path = Path(input_md)
    out_path = Path(output_csv)
    
    if not in_path.exists():
        print(f'Error: Input file not found: {in_path}')
        sys.exit(1)

    with open(in_path, 'r', encoding='utf-8') as f:
        md_text = f.read()

    headers, rows = parse_md_table(md_text)
    agg = aggregate(rows)
    
    existing_csv = load_existing_csv(out_path)
    out_rows = build_rows(agg, org, link, existing_csv)
    
    out_rows.sort(key=lambda x: (x['solution'], x['from'], x['to']))

    fieldnames = ['solution','org','date','status','link','layer','from','to','compile','run','translate']
    out_path.parent.mkdir(parents=True, exist_ok=True)
    with open(out_path, 'w', newline='', encoding='utf-8') as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        for row in out_rows:
            writer.writerow(row)

    updated_keys = {(r['solution'], r.get('layer', 'whole app'), r['from'], r['to']) for r in out_rows}
    existing_keys = set(existing_csv.keys())
    
    updated_count = len(updated_keys & existing_keys) 
    new_count = len(updated_keys - existing_keys) 
    preserved_count = len(existing_keys - updated_keys)  
    
    print(f'✅ Wrote {len(out_rows)} rows to {out_path}')
    if updated_count > 0:
        print(f'   📝 Updated: {updated_count} rows')
    if new_count > 0:
        print(f'   ➕ New: {new_count} rows')
    if preserved_count > 0:
        print(f'   📋 Preserved: {preserved_count} existing rows')

if __name__ == '__main__':
    main()
