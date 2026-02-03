pub mod list;
pub mod test;

use crate::cli::BenchCmd;
use anyhow::Result;

pub fn run(cmd: BenchCmd) -> Result<i32> {
    match cmd {
        BenchCmd::List(args) => list::run(args),
        BenchCmd::Test(args) => test::run(args),
    }
}
