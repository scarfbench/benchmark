pub mod run;
mod prepare;

use clap::Subcommand;
use run::EvalRunArgs;

#[derive(Subcommand, Debug)]
pub enum EvalCmd {
    #[command(about = "")]
    Run(EvalRunArgs),
}

pub fn run(cmd: EvalCmd) -> anyhow::Result<i32> {
    match cmd {
        EvalCmd::Run(args) => run::run(args),
    }
}
