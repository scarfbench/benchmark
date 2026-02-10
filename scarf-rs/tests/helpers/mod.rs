use assert_cmd::cargo;
use std::path::PathBuf;
use std::process::Command;
use tempfile::TempDir;
use walkdir::WalkDir;

/// Create a Environment that can be shared containing test fixtures
#[allow(unused)]
pub struct TestEnv {
    _tmp: TempDir,
    eval_out: PathBuf,
    agent_dir: PathBuf,
}

/// Implement TestEnv
#[allow(unused)]
impl TestEnv {
    /// Initialize a new TestEnv with a temporary directory and temporary folder underneath
    pub fn new() -> Self {
        // Use the agent directory from
        let agent_dir: PathBuf = PathBuf::from(env!("CARGO_MANIFEST_DIR"))
            .join("tests")
            .join("fixtures")
            .join("test_agent");

        // Create a temporary eval out directory
        let tmp = tempfile::tempdir().expect("Failed to create temp dir for test env");
        let eval_out = tmp.path().join("test_eval_out");
        std::fs::create_dir_all(&eval_out).expect("Failed to create eval_out dir");

        Self {
            _tmp: tmp,
            eval_out,
            agent_dir,
        }
    }

    /// Getter for eval_out
    pub fn eval_out(&self) -> &PathBuf {
        &self.eval_out
    }

    /// Get agent dir
    pub fn agent_dir(&self) -> &PathBuf {
        &self.agent_dir
    }
}

/// Provide a instance of the scarf binary so the other tests can call the cli
#[allow(unused)]
pub fn scarf_command() -> Command {
    Command::new(cargo::cargo_bin!("scarf"))
}

/// Benchmark directory
/// +----------------------------------------------------------------------------------------------+
/// |  Note: For now, I am assuming the build is happening in scarfbench/scarf/ folder             |
/// |  and the benchmark is the sibling folder at scarfbench/benchmark                             |
/// |                                                                                              |
/// |  This is expected to change when we have scarf bench pull [--version] command in the future. |
/// +----------------------------------------------------------------------------------------------+
#[allow(unused)]
pub fn benchmark_dir() -> PathBuf {
    let benchmark_dir = PathBuf::from(env!("CARGO_MANIFEST_DIR"))
        .join("..")
        .join("benchmark")
        .canonicalize()
        .expect("Failed to canonicalize benchmark dir");
    benchmark_dir
}

/// A helper to get a random (I am going with first) application in the benchmark
#[allow(unused)]
pub fn find_first_app(bench_dir: &PathBuf) -> (String, String, String) {
    let (layer, app, framework) = WalkDir::new(bench_dir)
        .min_depth(3)
        .max_depth(3)
        .into_iter()
        .filter_map(|e| e.ok())
        .filter(|e| e.file_type().is_dir())
        .find_map(|entry| {
            let rel = entry
                .path()
                .strip_prefix(bench_dir)
                .expect("Failed to get relative path");
            let mut it = rel.iter().map(|p| p.to_string_lossy().into_owned());
            match (it.next(), it.next(), it.next()) {
                // The moment all three of the layer, app, and framework are present, return them
                // as individual components.
                //              Then select them      <-------   if all of these are present
                //                      |                                      |
                //  |```````````````````````````````````````|   |````````````````````````````|
                (Some(layer), Some(app), Some(framework)) => Some((layer, app, framework)),
                _ => None,
            }
        })
        .expect("No application found in benchmark");

    (layer, app, framework)
}
