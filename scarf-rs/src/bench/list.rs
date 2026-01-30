use crate::cli::BenchListArgs;
use anyhow::Result;
use comfy_table::Table;
use log;
use std::path::PathBuf;
use walkdir::WalkDir;

pub fn run(args: BenchListArgs) -> Result<i32> {
    let repo_root = PathBuf::from(args.root.as_str());
    log::info!("Repository root: {}", repo_root.display());
    let bench_root = repo_root.join("benchmark");
    log::info!("Benchmark root: {}", bench_root.display());
    let base = match &args.layer {
        Some(layer) => bench_root.join(layer),
        None => bench_root.clone(),
    };

    let mut rows: Vec<[String; 4]> = Vec::new();
    let header = [
        "Layer".to_string(),
        "Application".to_string(),
        "Framework".to_string(),
        "Path".to_string(),
    ];
    for entry in WalkDir::new(&base) {
        let entry = entry?;

        if entry.file_name() == "Makefile" {
            let Some(leaf) = entry.path().parent() else {
                continue;
            };

            let rel = leaf.strip_prefix(bench_root.clone())?;
            let parts: Vec<String> = rel
                .iter()
                .map(|p| p.to_string_lossy().into_owned())
                .collect();

            if parts.len() != 3 {
                continue;
            }

            rows.push([
                parts[0].clone(),
                parts[1].clone(),
                parts[2].clone(),
                leaf.to_string_lossy().into_owned(),
            ]);
        }
    }
    println!("{}", tabulate(&header, &rows).to_string());
    Ok(0)
}

fn tabulate(header: &[String; 4], rows: &[[String; 4]]) -> Table {
    let mut table = Table::new();
    // Set header of the able
    table.set_header(header.to_vec());
    for row in rows {
        table.add_row(row.to_vec());
    }
    return table;
}
