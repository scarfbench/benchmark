mod prepare;
pub mod run;
mod driver;

use clap::Subcommand;
use run::EvalRunArgs;

#[derive(Subcommand, Debug)]
pub enum EvalCmd {
    #[command(about = "Evaluate an agent on Scarfbench")]
    Run(EvalRunArgs),
}

pub fn run(cmd: EvalCmd) -> anyhow::Result<i32> {
    match cmd {
        EvalCmd::Run(args) => run::run(args),
    }
}
