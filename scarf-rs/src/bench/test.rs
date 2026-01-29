use anyhow::Result;
use crate::cli::BenchTestArgs;

pub fn run(args: BenchTestArgs) -> Result<i32> {
    println!("bench test: layer={:?}", args.layer);
    return Ok(0);
}
