use anyhow::Result;
use clap::Args;
use comfy_table::Table;
use log;
use std::path::PathBuf;
use walkdir::WalkDir;

#[derive(Args, Debug)]
pub struct BenchListArgs {
    #[arg(long, help = "Path to the root of the scarf benchmark.")]
    pub benchmark_dir: String,

    #[arg(long, help = "Application layer to list.")]
    pub layer: Option<String>,
}

/// A simple list subcommand that lists all the benchmark applications as a table.
pub fn run(args: BenchListArgs) -> Result<i32> {
    // Get parse repository root
    let bench_root = PathBuf::from(args.benchmark_dir.as_str());
    assert!(
        bench_root.exists(),
        "This provided repository root {} doesn't exist?",
        bench_root.display()
    );
    log::debug!("Benchmark root: {}", bench_root.display());

    let base = match &args.layer {
        Some(layer) => bench_root.join(layer),
        None => bench_root.clone(),
    };

    if base.exists() {
        log::debug!("Base directory: {}", base.display());
    } else {
        anyhow::bail!(
            "The specified layer {} does not exist under base directory {}?",
            args.layer.as_deref().unwrap_or(""),
            base.display()
        );
    }

    let header = gen_header();
    match gen_rows(&base, &bench_root) {
        Ok(rows) => {
            // rows is fine, so let's use it!
            println!("{}", tabulate(&header, &rows));
        }
        Err(e) => {
            // Something wrong with the results, so log exception.
            log::error!("{e}");
        }
    };
    Ok(0)
}

/// Generate a header for the table
fn gen_header() -> [String; 4] {
    [
        "Layer".to_string(),
        "Application".to_string(),
        "Framework".to_string(),
        "Path".to_string(),
    ]
}

/// Generate the table rows
fn gen_rows(base: &PathBuf, bench_root: &PathBuf) -> Result<Vec<[String; 4]>, anyhow::Error> {
    let mut rows: Vec<[String; 4]> = Vec::new();

    for entry in WalkDir::new(base) {
        let entry = entry?;

        if entry.file_name() == "Makefile" {
            let Some(leaf) = entry.path().parent() else {
                continue;
            };
            // Find the relative path to the layer directory
            let rel = leaf.strip_prefix(bench_root)?;
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
    Ok(rows)
}

fn tabulate(header: &[String; 4], rows: &[[String; 4]]) -> Table {
    let mut table = Table::new();
    table.load_preset(comfy_table::presets::UTF8_FULL_CONDENSED);
    // Set header of the able
    table.set_header(header.to_vec());
    for row in rows {
        table.add_row(row.to_vec());
    }
    table
}

/// =====[ UNIT TESTS ]=====
#[cfg(test)]
mod tests {
    use super::*;
    use std::fs;
    use std::path::Path;

    fn _touch_makefile(dir: &Path) -> Result<()> {
        fs::create_dir_all(dir)?;
        fs::write(dir.join("Makefile"), "all:\n\techo Ok\n")?;
        Ok(())
    }

    /// Test to make sure the header is correct.
    #[test]
    fn test_gen_header() {
        let header = gen_header();
        assert_eq!(
            header,
            [
                "Layer".to_string(),
                "Application".to_string(),
                "Framework".to_string(),
                "Path".to_string(),
            ]
        );
    }

    /// Test generate rows for the table
    #[test]
    fn test_gen_rows() -> Result<()> {
        let tmpdir = tempfile::tempdir().unwrap();

        let repo_root = tmpdir.path();
        let bench_root = repo_root.join("benchmark");

        // Let's create a Makefile in the tempdir/benchmark/layer
        match _touch_makefile(&bench_root.join("layer/app/framework")) {
            Ok(()) => {
                log::info!("Created Makefile in layer/app/framework");
            }
            Err(e) => {
                log::error!("Failed to create Makefile: {}", e);
            }
        }

        // Let's also create another Makefile but now in layer/app.
        // This should not render in the table
        match _touch_makefile(&bench_root.join("layer/app")) {
            Ok(()) => {
                log::info!("Created Makefile in layer/app");
            }
            Err(e) => {
                log::error!("Failed to create Makefile: {}", e);
            }
        }

        let mut rows =
            gen_rows(&bench_root.join("layer"), &bench_root).expect("gen_rows failed: {}");

        // Turns our walkdir can mangle ordering, so we have to manually order them
        rows.sort();
        assert_eq!(rows.len(), 1);
        assert_eq!(rows[0][0], "layer");
        assert_eq!(rows[0][1], "app");
        assert_eq!(rows[0][2], "framework");
        assert_eq!(
            rows[0][3],
            bench_root
                .join("layer/app/framework")
                .to_string_lossy()
                .into_owned()
        );
        Ok(())
    }

    /// Test that when I give a specific layer, I correctly ignore the other layers
    #[test]
    fn test_specify_benchroot() -> Result<()> {
        let tmpdir = tempfile::tempdir().unwrap();
        let repo_root = tmpdir.path();
        let bench_root = repo_root.join("benchmark");
        let base = bench_root.join("layer1");

        _touch_makefile(&bench_root.join("layer1/app1/framework"))?;
        _touch_makefile(&bench_root.join("layer2/app2/framework"))?;

        let mut rows = gen_rows(&base, &bench_root).expect("gen_rows failed: {}");
        rows.sort(); // Sort to preserve the row order

        assert_eq!(rows.len(), 1);
        assert_eq!(rows[0][0], "layer1");
        assert_eq!(rows[0][1], "app1");
        assert_eq!(rows[0][2], "framework");
        assert_eq!(
            rows[0][3],
            bench_root
                .join("layer1/app1/framework")
                .to_string_lossy()
                .into_owned()
        );

        Ok(())
    }
}
