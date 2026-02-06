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
        .arg("--from-framework=spring")
        .arg("--to-framework=quarkus")
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
        .arg("--from-framework=spring")
        .arg("--to-framework=quarkus")
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
        .arg("--from-framework=spring")
        .arg("--to-framework=quarkus")
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
