use crate::cli::BenchListArgs;
use anyhow::Result;
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

    for entry in WalkDir::new(&base) {
        let entry = entry?;

        if entry.file_name() == "Makefile" {
            let Some(leaf) = entry.path().parent() else {
                continue;
            };

            let rel = leaf.strip_prefix(bench_root.clone())?;
            let parts: Vec<_> = rel.iter().collect();

            if parts.len() != 3 {
                continue;
            }

            println!(
                "{}/{}/{}",
                parts[0].to_string_lossy(),
                parts[1].to_string_lossy(),
                parts[2].to_string_lossy()
            );
        }
    }
    Ok(0)
}
