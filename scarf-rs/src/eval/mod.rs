use std::path::PathBuf;

use clap::{ArgAction, Args, Subcommand};


#[derive(Subcommand, Debug)]
pub enum EvalCmd {
    #[command(about="")]
    Run(EvalRunArgs),
}

#[derive(Args, Debug)]
pub struct EvalRunArgs {
    #[arg(long = "agent-dir", help = "Path (directory) to agent implementation harness.")]
    pub agent_dir: PathBuf,

    #[arg(long = "skills", help = "Path (directory) to agent skills.")]
    pub skills_dir: Option<PathBuf>,

    #[arg(long, help = "Application layer to run agent on.")]
    pub layer: Option<String>,

    #[arg(long, help = "Application to run the agent on. If layer is specified, this app must lie within that layer.")]
    pub app: Option<String>,

    #[arg(long = "from-framework", help = "The source framework for conversion.")]
    pub from_framework: String,

    #[arg(long = "to-framework", help = "The target framework for conversion.")]
    pub to_framework: String,

    #[arg(
        short,
        long = "pass-at-k",
        default_value_t = 1,
        help = "Valu of K to run for generating an Pass@K value."
    )]
    pub k: u32,

    #[arg(long, help="Output directory where the agent runs and evaluation output are stored.")]
    pub eval_out: PathBuf,

    #[arg(short, long = "num-jobs", help = "Number of parallel jobs to run.")]
    pub n: Option<usize>,

    #[arg(
        long="prepare-only",
        action = ArgAction::SetTrue,
        help = "Prepare the evaluation harness to run agents. Think of this as a dry run before actually deploying the agents."
    )]
    pub stage: bool,
}
