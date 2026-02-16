use std::{
    ffi::{OsStr, OsString},
    fs,
    iter::once,
};

use anyhow::Error;
use serde_json::Value;
use walkdir::WalkDir;

use crate::helpers::{TestEnv, benchmark_dir, find_first_app, scarf_command};
mod helpers;

/*
 * +------------------------------------------+
 * | Tests for scarf bench eval run commands  |
 * +------------------------------------------+
 */
#[test]
fn eval_run_prepare_only() {
    let benchmark_dir = benchmark_dir();
    let (layer, app, _) = find_first_app(&benchmark_dir);
    let test_env = TestEnv::new();
    let output = scarf_command()
        .arg("eval")
        .arg("run")
        .arg("--prepare-only")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--agent-dir")
        .arg(test_env.agent_dir().to_str().unwrap())
        .arg("--layer")
        .arg(layer.as_str())
        .arg("--app")
        .arg(app.as_str())
        .arg("--source-framework=spring")
        .arg("--target-framework=quarkus")
        .arg("--eval-out")
        .arg(test_env.eval_out().to_str().unwrap())
        .output()
        .expect("Run scarf eval run --benchmark-dir ... --prepare-only ...");

    assert!(
        output.status.success(),
        "stderr: {}",
        String::from_utf8_lossy(&output.stderr)
    );
}

#[test]
fn eval_run_prepare_only_with_several_layers_at_once() {
    let benchmark_dir = benchmark_dir();
    let test_env = TestEnv::new();
    let output = scarf_command()
        .arg("eval")
        .arg("run")
        .arg("--prepare-only")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--agent-dir")
        .arg(test_env.agent_dir().to_str().unwrap())
        .arg("--layer")
        .arg("business_domain")
        .arg("--layer")
        .arg("persistence")
        .arg("--layer")
        .arg("infrastructure")
        .arg("--source-framework=spring")
        .arg("--target-framework=quarkus")
        .arg("--eval-out")
        .arg(test_env.eval_out().to_str().unwrap())
        .output()
        .expect("Run scarf eval run --benchmark-dir ... --prepare-only ...");

    assert!(
        output.status.success(),
        "stderr: {}",
        String::from_utf8_lossy(&output.stderr)
    );
}

#[test]
fn eval_run_with_jobs_less_than_one() {
    let benchmark_dir = benchmark_dir();
    let (layer, app, _) = find_first_app(&benchmark_dir);
    let test_env = TestEnv::new();
    let output = scarf_command()
        .arg("eval")
        .arg("run")
        .arg("--prepare-only")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--agent-dir")
        .arg(test_env.agent_dir().to_str().unwrap())
        .arg("--layer")
        .arg(layer.as_str())
        .arg("--app")
        .arg(app.as_str())
        .arg("--jobs")
        .arg("0")
        .arg("--source-framework=spring")
        .arg("--target-framework=quarkus")
        .arg("--eval-out")
        .arg(test_env.eval_out().to_str().unwrap())
        .output()
        .expect("Run scarf eval run --benchmark-dir ... --prepare-only ...");

    assert!(
        output.status.success(),
        "stderr: {}",
        String::from_utf8_lossy(&output.stderr)
    );
}

#[test]
fn eval_run_with_pass_at_k_should_correctly_create_the_folder_structure_with_k_runs() {
    let benchmark_dir = benchmark_dir();
    let (layer, app, _) = find_first_app(&benchmark_dir);
    let test_env = TestEnv::new();
    let _ = scarf_command()
        .arg("eval")
        .arg("run")
        .arg("--prepare-only")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--agent-dir")
        .arg(test_env.agent_dir().to_str().unwrap())
        .arg("--layer")
        .arg(layer.as_str())
        .arg("--app")
        .arg(app.as_str())
        .arg("--pass-at-k")
        .arg("3")
        .arg("--source-framework=spring")
        .arg("--target-framework=quarkus")
        .arg("--eval-out")
        .arg(test_env.eval_out().to_str().unwrap())
        .output()
        .expect("Run scarf eval run --benchmark-dir ... --prepare-only ...");

    // Ensure the eval_out contains a directory and it's named agent_name__layer__app__spring__quarkus
    assert!(
        WalkDir::new(test_env.eval_out())
            .max_depth(1)
            .min_depth(1)
            .into_iter()
            .filter_map(Result::ok)
            .filter(|f| f.file_type().is_dir())
            .map(|f| f.file_name().to_owned())
            .eq(once(OsString::from(format!(
                "{}__{}__{}__spring__quarkus",
                test_env
                    .agent_dir()
                    .file_name()
                    .and_then(OsStr::to_str)
                    .expect("Not a valid UTF-8 file name for the agent dir"),
                layer.as_str(),
                app.as_str()
            )))),
        "The created folder name doesn't match"
    );

    // THe directory must contain 3 folders all starting with run_
    assert_eq!(
        WalkDir::new(test_env.eval_out())
            .max_depth(2)
            .min_depth(2)
            .into_iter()
            .filter_map(Result::ok)
            .filter(|file| file.file_type().is_dir())
            .count(),
        3,
        "We need 3 folders (one for each run)."
    );

    assert!(
        WalkDir::new(test_env.eval_out())
            .max_depth(2)
            .min_depth(2)
            .into_iter()
            .filter_map(Result::ok)
            .filter(|file| file.file_type().is_dir())
            .all(|file| file.file_name().to_string_lossy().contains("run_")),
        "Pass at k folder prefix doesn't contain `run_`."
    );
}

#[test]
fn eval_run_must_error_out_when_from_target_framework_are_same() {
    let benchmark_dir = benchmark_dir();
    let (layer, app, _) = find_first_app(&benchmark_dir);
    let test_env = TestEnv::new();
    let output = scarf_command()
        .arg("eval")
        .arg("run")
        .arg("--prepare-only")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--agent-dir")
        .arg(test_env.agent_dir().to_str().unwrap())
        .arg("--layer")
        .arg(layer.as_str())
        .arg("--app")
        .arg(app.as_str())
        .arg("--jobs")
        .arg("0")
        .arg("--source-framework=spring")
        .arg("--target-framework=spring")
        .arg("--eval-out")
        .arg(test_env.eval_out().to_str().unwrap())
        .output()
        .expect("Run scarf eval run --benchmark-dir ... --prepare-only ...");

    assert!(
        !output.status.success(),
        "THe command should have errored out when from and to frameworks are the same."
    );

    assert!(
        String::from_utf8_lossy(&output.stderr)
            .contains("From and To frameworks cannot be the same"),
        "Must error out with the message 'From and To frameworks cannot be the same' when from and to frameworks are the same. Actual stderr: {}",
        String::from_utf8_lossy(&output.stderr),
    );
}

#[test]
fn eval_run_must_run_test_agent_k_times_and_each_time_succeed() {
    let benchmark_dir = benchmark_dir();
    let (layer, app, _) = find_first_app(&benchmark_dir);
    let test_env = TestEnv::new();
    let output = scarf_command()
        .arg("eval")
        .arg("run")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--agent-dir")
        .arg(test_env.agent_dir().to_str().unwrap())
        .arg("--layer")
        .arg(layer.as_str())
        .arg("--app")
        .arg(app.as_str())
        .arg("--pass-at-k")
        .arg("3")
        .arg("--source-framework=spring")
        .arg("--target-framework=quarkus")
        .arg("--eval-out")
        .arg(test_env.eval_out().to_str().unwrap())
        .output()
        .expect("Run scarf eval run --benchmark-dir ... --prepare-only ...");

    assert!(
        output.status.success(),
        "The command should have succeeded."
    );

    assert!(
        WalkDir::new(test_env.eval_out().to_path_buf())
            .min_depth(3)
            .max_depth(3)
            .into_iter()
            .filter_map(Result::ok)
            .filter(|f| !f.file_type().is_dir() && f.file_name().eq("metadata.json"))
            .all(|f| {
                let metadata: Value = match fs::read_to_string(f.path())
                    .map_err(Error::from)
                    .and_then(|file| serde_json::from_str(&file).map_err(Error::from))
                {
                    Ok(m) => {
                        let _ = match fs::read_to_string(
                            f.path()
                                .to_path_buf()
                                .parent()
                                .unwrap()
                                .join("validation")
                                .join("agent.out"),
                        ) {
                            Ok(f) => {
                                if !f.contains("[INFO] Agent successfully ran") {
                                    return false;
                                }
                            }
                            Err(e) => {
                                eprintln!("Failed with error: {}", e);
                                return false;
                            }
                        };
                        m
                    }
                    Err(e) => {
                        eprintln!("Failed with error: {}", e);
                        return false;
                    }
                };
                metadata["status"]
                    .to_string()
                    .contains("AGENT EXECUTION COMPLETE")
            })
    );
}

#[test]
fn eval_run_must_run_test_agent_k_times_and_record_agent_failures() {
    let benchmark_dir = benchmark_dir();
    let (layer, app, _) = find_first_app(&benchmark_dir);
    let test_env = TestEnv::new();
    scarf_command()
        .arg("eval")
        .arg("run")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--agent-dir")
        .arg(test_env.agent_dir().to_str().unwrap())
        .arg("--layer")
        .arg(layer.as_str())
        .arg("--app")
        .arg(app.as_str())
        .arg("--pass-at-k")
        .arg("3")
        .arg("--source-framework=spring")
        .arg("--target-framework=quarkus")
        .arg("--eval-out")
        .arg(test_env.eval_out().to_str().unwrap())
        .env("FAIL", "true")
        .output()
        .expect("Run scarf eval run --benchmark-dir ... --prepare-only ...");

    assert!(
        WalkDir::new(test_env.eval_out().to_path_buf())
            .min_depth(3)
            .max_depth(3)
            .into_iter()
            .filter_map(Result::ok)
            .filter(|f| !f.file_type().is_dir() && f.file_name().eq("metadata.json"))
            .all(|f| {
                let metadata: Value = match fs::read_to_string(f.path())
                    .map_err(Error::from)
                    .and_then(|file| serde_json::from_str(&file).map_err(Error::from))
                {
                    Ok(m) => {
                        let _ = match fs::read_to_string(
                            f.path()
                                .to_path_buf()
                                .parent()
                                .unwrap()
                                .join("validation")
                                .join("agent.err"),
                        ) {
                            Ok(f) => {
                                if !f.contains("[ERROR]") {
                                    return false;
                                }
                            }
                            Err(e) => {
                                eprintln!("Failed with error: {}", e);
                                return false;
                            }
                        };
                        m
                    }
                    Err(e) => {
                        eprintln!("Failed with error: {}", e);
                        return false;
                    }
                };
                metadata["status"]
                    .to_string()
                    .contains("AGENT EXECUTION FAILED")
            })
    );
}
