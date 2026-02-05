use crate::helpers::{benchmark_dir, scarf_command};
mod helpers;

#[test]
fn bench_list_outputs_a_table() {
    let benchmark_dir = benchmark_dir();

    let output = scarf_command()
        .arg("bench")
        .arg("list")
        .arg("--root")
        .arg(benchmark_dir.to_str().unwrap())
        .output()
        .expect("Run scarf bench list --root ... ");

    assert!(
        output.status.success(),
        "stderr: {}",
        String::from_utf8_lossy(&output.stderr)
    );
}
