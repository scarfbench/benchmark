mod driver;
mod prepare;
mod types;

pub(crate) mod run;

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
