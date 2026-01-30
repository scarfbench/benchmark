use crate::cli::BenchTestArgs;
use anyhow::Result;

pub fn run(args: BenchTestArgs) -> Result<i32> {
    println!("bench test: layer={:?}", args.layer);
    Ok(0)
}
