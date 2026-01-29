use std::path::PathBuf;
use walkdir::WalkDir;

use anyhow::Result;
use crate::cli::BenchListArgs;

pub fn run(args: BenchListArgs) -> Result<i32> {

    let repo_root = PathBuf::from(args.root.as_str());
    let bench_root = repo_root.join("benchmark");
    let base = match &args.layer {
        Some(layer) => bench_root.join(layer),
        None => bench_root.clone(),
    };

    for entry in WalkDir::new(&base) {
        let entry = entry?;

        if entry.file_name() == "Makefile" {
            let Some(leaf) = entry.path().parent() else {continue;};

            let rel = leaf.strip_prefix(bench_root.clone())?;
            let parts: Vec<_> = rel.iter().collect();

            if parts.len() != 3 {
                continue;
            }

            println!("{}/{}/{}",
                parts[0].to_string_lossy().to_string(),
                parts[1].to_string_lossy().to_string(),
                parts[2].to_string_lossy().to_string()
            );
        }
    }
    return Ok(0);
}
