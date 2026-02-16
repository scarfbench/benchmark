use crate::helpers::{benchmark_dir, scarf_command};
mod helpers;

/*
 * +--------------------------------------+
 * | Tests for scarf bench list commands  |
 * +--------------------------------------+
 */
#[test]
fn bench_list_outputs_a_table() {
    let benchmark_dir = benchmark_dir();

    let output = scarf_command()
        .arg("bench")
        .arg("list")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .output()
        .expect("Run scarf bench list --benchmark-dir ... ");

    assert!(
        output.status.success(),
        "stderr: {}",
        String::from_utf8_lossy(&output.stderr)
    );
}

#[test]
fn bench_list_outputs_a_table_with_a_specific_layer() {
    let benchmark_dir = benchmark_dir();
    let (layer, _, __) = helpers::find_first_app(&benchmark_dir);
    let output = scarf_command()
        .arg("bench")
        .arg("list")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--layer")
        .arg(layer.as_str())
        .output()
        .expect("Run scarf bench list --benchmark-dir ... --layer ... ");

    assert!(
        output.status.success(),
        "stderr: {}",
        String::from_utf8_lossy(&output.stderr)
    );
    assert!(
        String::from_utf8_lossy(&output.stdout).contains(layer.as_str()),
        "Output did not contain the specified layer"
    );
}

#[test]
fn bench_list_bails_when_a_layer_does_not_exist() {
    let benchmark_dir = benchmark_dir();

    let output = scarf_command()
        .arg("bench")
        .arg("list")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--layer")
        .arg("this_layer_does_not_exist")
        .output()
        .expect("Run scarf bench list --benchmark-dir ... --layer ... ");

    assert!(
        !output.status.success(),
        "stderr: {}",
        String::from_utf8_lossy(&output.stderr)
    );

    assert!(
        String::from_utf8_lossy(&output.stderr).contains("this_layer_does_not_exist"),
        "Error message did not include the non-existent layer name"
    );
}

/*
 * +--------------------------------------+
 * | Tests for scarf bench test commands  |
 * +--------------------------------------+
 */
#[test]
fn bench_test_as_a_dryrun_on_a_specfic_layer() {
    let benchmark_dir = benchmark_dir();
    let (layer, app, framework) = helpers::find_first_app(&benchmark_dir);
    let output = scarf_command()
        .arg("bench")
        .arg("test")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--layer")
        .arg(layer.as_str())
        .arg("--dry-run") // <--- Not actually running make test (see src/bench/test.rs:L15)
        .output()
        .expect("Run scarf bench test --benchmark-dir ... --layer ... ");

    // The command must run without failures
    assert!(
        output.status.success(),
        "stderr: {}",
        String::from_utf8_lossy(&output.stderr)
    );

    let stdout = String::from_utf8_lossy(&output.stdout);

    // There must a report that's generated a table
    assert!(
        stdout.contains("Application Path")
            && stdout.contains("Result")
            && stdout.contains(
                format!("{}/{}/{}", layer.as_str(), app.as_str(), framework.as_str()).as_str()
            )
            && stdout.contains("Success")
    );
}

#[test]
fn bench_test_on_an_absent_layer() {
    let benchmark_dir = benchmark_dir();

    let output = scarf_command()
        .arg("bench")
        .arg("test")
        .arg("--benchmark-dir")
        .arg(benchmark_dir.to_str().unwrap())
        .arg("--layer")
        .arg("this_layer_does_not_exist")
        .output()
        .expect("Run scarf bench test --benchmark-dir ... --layer ... ");

    // The command must panic with failures
    assert!(
        !output.status.success(),
        "stderr: {}",
        String::from_utf8_lossy(&output.stderr)
    );

    assert!(
        String::from_utf8_lossy(&output.stderr).contains("this_layer_does_not_exist"),
        "Error message did not include the non-existent layer name"
    );
}
