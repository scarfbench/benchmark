use anyhow::{Context, Result};
use clap::Args;
use rayon::prelude::*;
use std::{path::PathBuf, process::Command, sync::mpsc};
use walkdir::WalkDir;

#[derive(Args, Debug)]
pub struct BenchTestArgs {
    #[arg(long, help = "Path to the root of the scarf benchmark.")]
    pub root: String,

    #[arg(long, help = "Application layer to test.")]
    pub layer: Option<String>,

    #[arg(
        long = "dry-run",
        action = clap::ArgAction::SetTrue,
        help = "Use dry run instead of full run."
    )]
    pub dry_run: bool,
}

/// Create a container to hold command run result
struct RunResult {
    dir: PathBuf,
    ok: bool,
    stdout: String,
    stderr: String,
}

/// Discover all the directories that contain the makefiles in the
/// benchmark folder
fn find_app_dirs(base_dir: &PathBuf) -> Result<Vec<PathBuf>> {
    // Initialize rows vector
    let mut rows: Vec<PathBuf> = Vec::new();

    // Iterate through the files in the base dir and find the directories that contain Makefiles
    for entry in WalkDir::new(base_dir) {
        let entry = entry?;
        if entry.file_type().is_file() && entry.file_name() == "Makefile" {
            let leaf = entry
                .path()
                .parent()
                .context("Makefile had no parent directory")
                .unwrap()
                .to_path_buf();
            rows.push(leaf);
        }
    }
    Ok(rows)
}

/// Run the make -n test command on the makefile in the provided directory
fn run_makefile(path: &PathBuf, dry_run: bool) -> Result<RunResult> {
    // Check to see if there is a makefile in the provided directory.
    if !path.join("Makefile").exists() {
        return Err(anyhow::anyhow!(
            "No Makefile found in the provided directory: {}",
            path.display()
        ));
    }

    let mut cmd: Command = Command::new("make");

    if dry_run {
        cmd.arg("-n");
    }

    cmd.arg("test");

    let output = cmd
        .current_dir(path)
        .output()
        .with_context(|| format!("Failed to execute 'make [-n] test' in {}", path.display()))?;

    Ok(RunResult {
        dir: path.to_path_buf(),
        ok: output.status.success(),
        stdout: String::from_utf8_lossy(&output.stdout).into_owned(),
        stderr: String::from_utf8_lossy(&output.stderr).into_owned(),
    })
}

/// The test subcommand that runs make test on all the applications to ensure they work as expected
pub fn run(args: BenchTestArgs) -> Result<i32> {
    log::info!("Running tests to ensure functionality of benchmark applications...");

    // Obtain the benchmark root from the repository root
    let bench_root = std::fs::canonicalize(PathBuf::from(args.root.as_str()))
        .context(format!(
            "Failed to canonicalize the benchmark root path: {}",
            args.root
        ))
        .unwrap();
    assert!(
        bench_root.exists(),
        "The benchmark folder {} does not exist?",
        bench_root.display()
    );
    log::info!("Benchmark directory: {}", bench_root.display());

    let base = match &args.layer {
        Some(layer) => bench_root.join(layer),
        None => bench_root.clone(),
    };

    log::info!("Base directory: {}", base.display());

    let app_dirs = find_app_dirs(&base).expect("Failed to find application directories");

    // Let's obtain a multi-provider (tx) single channel (rx) to collect results
    let (tx, rx) = mpsc::channel::<(PathBuf, anyhow::Result<RunResult>)>();
    //                              ^^^^^^^  ^^^^^^^^^^^^^^^^^^^^^^^^^
    //                                 ▲                  ▲
    //                                 │                  │
    //                                Tx type     Rx Result (Result of makefile run)

    // Each item in the apps_dir will be sent to the following closure such that the closure gets
    // a tx (a transmitter of its own to the common channel) and the reference to the dir to do its work
    app_dirs.par_iter().for_each_with(tx, |tx, dir| {
        // Each worker does its job (i.e., run the makefile and return the result as RunResult)
        log::info!("Running makefile test in directory: {}", dir.display());
        let result = run_makefile(dir, args.dry_run);
        let status = result
            .as_ref()
            .map_or("Error", |r| if r.ok { "Success" } else { "Failure" });
        log::info!(
            "Completed makefile test in directory: {}. Status: {}",
            dir.display(),
            status
        );
        // Now, clone into an owned directory (using to_path_buf) that each of the worker is
        // using and send that back to the receiver along with the ownership of the result.
        let _ = tx.send((dir.to_path_buf(), result));
    });

    let mut results: Vec<[String; 2]> = Vec::new();
    //              Only iterate as many times as we have directories
    //                                       │
    //                                       ▼
    //                             |```````````````````|
    // for (dir, res) in rx.iter().take(app_dirs.len()) {
    for (dir, res) in rx.iter() {
        match res {
            Ok(res) if res.ok => {
                results.push([dir.to_string_lossy().into_owned(), "Success".to_string()]);
                log::info!(
                    "Makefile test in {} succeeded. Output:\n{}",
                    res.dir.display(),
                    res.stdout
                );
            }
            Ok(res) => {
                results.push([dir.to_string_lossy().into_owned(), "Failure".to_string()]);
                log::warn!(
                    "Makefile test in {} failed. Stderr:\n{}",
                    res.dir.display(),
                    res.stderr
                );
            }
            Err(e) => {
                results.push([dir.to_string_lossy().into_owned(), "Error".to_string()]);
                log::error!(
                    "Makefile test in {} encountered an error: {}",
                    dir.display(),
                    e
                );
            }
        }
    }

    let header: [String; 2] = ["Application Path".to_string(), "Result".to_string()];
    let mut table: comfy_table::Table = comfy_table::Table::new();

    // Tabulate the final results
    table.load_preset(comfy_table::presets::UTF8_FULL_CONDENSED);
    table.set_header(header);
    for rows in results {
        table.add_row(rows.to_vec());
    }
    println!("{}", table);
    Ok(0)
}

// =====[ Unit Tests ]=====
#[cfg(test)]
mod tests {
    use super::*;
    use std::fs;

    fn _touch_makefile(dir: &PathBuf) -> Result<()> {
        fs::create_dir_all(dir)?;
        let makefile_path = dir.join("Makefile");
        fs::write(makefile_path, "test:\n\techo Hello World\n")?;
        Ok(())
    }

    #[test]
    pub fn test_find_app_dirs() {
        let bench_root = tempfile::tempdir()
            .unwrap()
            .path()
            .join("benchmark/layer/app/framework");

        // Create a makefile in the temporary directory
        _touch_makefile(&bench_root).expect("Failed to create Makefile in benchmark root");

        let makefiles = find_app_dirs(&bench_root).expect("Failed to find application directories");

        assert_eq!(makefiles.len(), 1);
        assert_eq!(makefiles[0], bench_root);
    }

    #[test]
    pub fn test_make_dry_run() {
        let tempfile = tempfile::tempdir().unwrap();
        let app_dir = tempfile.path().join("layer/app/framwork");

        // Create a dummy makefile
        _touch_makefile(&app_dir).expect("Failed to create Makefile in app directory");

        // Run the makefile in dry-run mode
        let result = run_makefile(&app_dir, false).expect("Failed to run the makefile");

        // Validate the RunResult captures the makefile directory correctly
        assert_eq!(result.dir, app_dir);

        // The output must be okay
        assert!(result.ok);

        // The stdout should contain the echo command
        assert!(result.stdout.contains("echo Hello World"));
    }
}
