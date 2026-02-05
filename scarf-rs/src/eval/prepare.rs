use std::io::Write;
use std::{
    collections::HashMap,
    fs::{self, File, create_dir_all},
    path::{Path, PathBuf},
};

use serde::Serialize;
use walkdir::WalkDir;

use crate::{eval::run::EvalRunArgs, utils};

/*
 * Some helper types
 */
/// Here we maintain the outer layout to handle the runs
#[derive(Serialize)]
struct RunLayout {
    root: PathBuf,
    evals: HashMap<String, EvalLayout>,
}

/// This holds the eval datastructure
#[derive(Serialize)]
struct EvalLayout {
    root: PathBuf,
    input: PathBuf,
    output: PathBuf,
    validation: PathBuf,
}

/// This is to hold the run metadata for saving in the evals folder later
#[derive(Serialize)]
struct RunMetaData {
    eval_id: String,
    agent: String,
    layer: String,
    app: String,
    from_framework: String,
    to_framework: String,
    status: String,
}

/// The public facing prepare harness that sets up the evaluation environment
pub fn prepare_harness(args: &EvalRunArgs) -> anyhow::Result<()> {
    let eval_out_dir = &args.eval_out;
    let run_layout = RunLayout {
        root: eval_out_dir.to_path_buf(),
        evals: initialize_evals(args)?,
    };
    log::info!(
        "Evaluation harness prepared\n{}",
        utils::json_pretty(&run_layout)
    );
    Ok(())
}

/// Populate the evals data structure
fn initialize_evals(args: &EvalRunArgs) -> anyhow::Result<HashMap<String, EvalLayout>> {
    let mut evals: HashMap<String, EvalLayout> = HashMap::new();

    // We'll assume for now that the agent name is the directory name where the agent is (I can change this later if needed)
    let agent_name = format!("{}", args.agent_dir.file_name().unwrap().to_string_lossy());
    log::debug!("Using agent name: {}", agent_name);

    // Iterate over all the selected layers and pick the apps chosen by the user
    // if not all apps will be chosen.
    let apps: Vec<_> = (if !args.layer.is_empty() {
        args.layer.clone()
    } else {
        WalkDir::new(&args.benchmark_dir)
            .min_depth(1)
            .max_depth(1)
            .into_iter()
            .filter_map(|e| e.ok())
            .filter(|e| e.file_type().is_dir())
            .map(|e| e.file_name().to_string_lossy().into_owned())
            .collect()
    })
    .iter()
    .flat_map(|layer| {
        WalkDir::new(args.benchmark_dir.join(layer))
            .min_depth(1)
            .max_depth(1)
            .into_iter()
            .filter_map(|e| e.ok())
            .filter(|e| e.file_type().is_dir())
            .filter(|e| {
                if args.app.is_empty() {
                    true
                } else {
                    e.file_name()
                        .to_str()
                        .map(|n| args.app.iter().any(|a| a == n))
                        .unwrap_or(false)
                }
            })
            .map(|e| e.path().to_path_buf())
    })
    .collect();

    // If the user gave --app(s) and they weren't any of the layer(s) the user provided then
    if apps.is_empty() {
        anyhow::bail!(
            "The app(s) provided with the --app flag were not found for the specified --layer(s)."
        );
    }

    for app_path in apps.iter() {
        log::debug!(
            "Preparing eval for application at path: {}",
            app_path.display()
        );

        let eval_instance_key = app_path
            .file_name()
            .and_then(|n| n.to_str())
            .and_then(|app| {
                app_path
                    .parent()
                    .and_then(|p| p.file_name())
                    .and_then(|layer| layer.to_str())
                    .map(|layer| {
                        format!(
                            "{}__{}__{}__{}__{}",
                            agent_name, layer, app, args.from_framework, args.to_framework
                        )
                    })
            })
            .unwrap();

        // Create a directory in the --eval-out directory
        let eval_instance_dir = args.eval_out.join(&eval_instance_key);

        // Create the outer eval directory
        match create_dir_all(&eval_instance_dir) {
            Ok(_) => {
                log::debug!(
                    "Created eval instance directory: {}",
                    eval_instance_dir.display()
                );
            }
            Err(e) => {
                anyhow::bail!(
                    "Failed to create eval instance directory {}: {}",
                    eval_instance_dir.display(),
                    e
                );
            }
        }
        match create_eval_metadata(&eval_instance_dir, &eval_instance_key) {
            Ok(_) => {
                log::debug!(
                    "Created eval metadata file in: {}",
                    eval_instance_dir.display()
                );
            }
            Err(e) => {
                anyhow::bail!(
                    "Failed to create eval metadata file in {}: {}",
                    eval_instance_dir.display(),
                    e
                );
            }
        }

        // Create the input, output, and validation directories
        let eval_input_dir: PathBuf = eval_instance_dir.join("input");
        match create_dir_all(&eval_input_dir) {
            Ok(_) => {
                log::debug!(
                    "Created input directory: {} and seeded it with the source framework",
                    eval_instance_dir.join("input").display()
                );
            }
            Err(e) => {
                anyhow::bail!(
                    "Failed to create input directory {}: {}",
                    eval_instance_dir.join("input").display(),
                    e
                );
            }
        }
        // Copy the app files into the input directory
        copy_app_dir(app_path, &args.from_framework, &eval_input_dir)?;

        let eval_output_dir: PathBuf = eval_instance_dir.join("output");
        match create_dir_all(eval_instance_dir.join("output")) {
            Ok(_) => {
                log::debug!(
                    "Created output directory: {} and seeded it with the source framework",
                    eval_instance_dir.join("output").display()
                );
            }
            Err(e) => {
                anyhow::bail!(
                    "Failed to create output directory {}: {}",
                    eval_instance_dir.join("output").display(),
                    e
                );
            }
        }
        copy_app_dir(app_path, &args.from_framework, &eval_output_dir)?;

        let eval_validation_dir: PathBuf = eval_instance_dir.join("validation");
        match create_dir_all(eval_instance_dir.join("validation")) {
            Ok(_) => {
                log::debug!(
                    "Created validation directory: {}",
                    eval_validation_dir.display()
                );
            }
            Err(e) => {
                anyhow::bail!(
                    "Failed to create validation directory {}: {}",
                    eval_validation_dir.display(),
                    e
                );
            }
        }

        // Update evals directory structure.
        evals.insert(
            eval_instance_key.clone(),
            EvalLayout {
                root: eval_instance_dir.clone(),
                input: eval_input_dir.clone(),
                output: eval_output_dir.clone(),
                validation: eval_validation_dir.clone(),
            },
        );
    }
    Ok(evals)
}

fn create_eval_metadata(eval_instance_dir: &Path, eval_id: &str) -> anyhow::Result<()> {
    let metadata: RunMetaData = {
        let [agent, layer, app, from_framework, to_framework]: [&str; 5] = eval_id
            .split("__")
            .take(5)
            .collect::<Vec<_>>()
            .try_into()
            .expect("Failed to parse eval instance directory name");
        RunMetaData {
            eval_id: eval_id.to_owned(),
            layer: layer.to_string(),
            agent: agent.to_string(),
            app: app.to_string(),
            from_framework: from_framework.to_string(),
            to_framework: to_framework.to_string(),
            status: "PREPARED".to_string(),
        }
    };
    // Generate a JSON String (that's prettified)
    let json = serde_json::to_string_pretty(&metadata)?;

    let mut file = File::create(eval_instance_dir.join("metadata.json"))?;
    file.write_all(json.as_bytes())?;
    Ok(())
}

fn copy_app_dir(apps: &Path, from_framework: &String, dest: &Path) -> anyhow::Result<()> {
    for entry in apps
        .join(from_framework)
        .read_dir()
        .expect("Failed to read {from_framework} directory")
    {
        let entry = entry.expect("Failed to read file in app directory");
        log::trace!("Processing entry: {}", entry.path().display());
        let path = entry.path();
        let file_name = path
            .file_name()
            .expect("Failed to get file name")
            .to_owned();

        if matches!(
            &file_name,
            n if n == std::ffi::OsStr::new("smoke.py")
              || n == std::ffi::OsStr::new("smoke")
              || n == std::ffi::OsStr::new("Makefile")
              || n == std::ffi::OsStr::new(".dockerignore")
              || n == std::ffi::OsStr::new("Dockerfile")
        ) {
            log::trace!("Skipping file {}", file_name.to_string_lossy());
            continue;
        }

        log::trace!("Copying file {}", file_name.to_string_lossy());
        let dest_path = dest.join(&file_name);
        let meta = fs::metadata(&path)?;
        if meta.is_dir() {
            copy_dir_recursive(&path, &dest_path)?;
        } else if meta.is_file() {
            fs::copy(&path, &dest_path)?;
        }
    }
    Ok(())
}

fn copy_dir_recursive(src: &Path, dest: &Path) -> anyhow::Result<()> {
    create_dir_all(dest)?;
    for entry in src.read_dir()? {
        let entry = entry?;
        let path = entry.path();
        let dest_path = dest.join(entry.file_name());
        let meta = fs::metadata(&path)?;

        if meta.is_dir() {
            copy_dir_recursive(&path, &dest_path)?;
        } else if meta.is_file() {
            fs::copy(&path, &dest_path)?;
        }
    }
    Ok(())
}
