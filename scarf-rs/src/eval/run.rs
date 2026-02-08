use std::path::PathBuf;

use clap::{ArgAction, Args};
use serde::Serialize;

use crate::eval::{driver, prepare};

#[derive(Args, Debug, Serialize)]
pub struct EvalRunArgs {
    #[arg(
        long = "benchmark-dir",
        help = "Path (directory) to the benchmark.",
        value_name = "DIR"
    )]
    pub benchmark_dir: PathBuf,

    #[arg(
        long = "agent-dir",
        help = "Path (directory) to agent implementation harness.",
        value_name = "DIR"
    )]
    pub agent_dir: PathBuf,

    #[arg(
        long,
        value_name = "LAYER",
        action = ArgAction::Append,
        help = "Application layer to run agent on.",
    )]
    pub layer: Vec<String>,

    #[arg(
        long,
        value_name = "APP",
        action = ArgAction::Append,
        help = "Application to run the agent on. If layer is specified, this app must lie within that layer."
    )]
    pub app: Vec<String>,

    #[arg(
        long = "from-framework",
        help = "The source framework for conversion.",
        value_name = "FRAMEWORK"
    )]
    pub from_framework: String,

    #[arg(
        long = "to-framework",
        help = "The target framework for conversion.",
        value_name = "FRAMEWORK"
    )]
    pub to_framework: String,

    #[arg(
        short,
        long = "pass-at-k",
        default_value_t = 1,
        help = "Value of K to run for generating an Pass@K value.",
        value_name = "K"
    )]
    pub pass_at_k: u32,

    #[arg(
        long,
        help = "Output directory where the agent runs and evaluation output are stored."
    )]
    pub eval_out: PathBuf,

    #[arg(
        short,
        long = "jobs",
        default_value_t = 1,
        help = "Number of parallel jobs to run."
    )]
    pub jobs: u32,

    #[arg(
        long="prepare-only",
        action = ArgAction::SetTrue,
        help = "Prepare the evaluation harness to run agents. Think of this as a dry run before actually deploying the agents."
    )]
    pub prepare_only: bool,
}

// Create the evaluation output directory if it doesn't
pub fn run(mut args: EvalRunArgs) -> anyhow::Result<i32> {
    // Make sure from and to frameworks are not the same
    if args.from_framework.eq(&args.to_framework) {
        anyhow::bail!(
            "From and To frameworks cannot be the same: {}",
            args.from_framework
        );
    }

    // If number of jobs is less than 1, set to 1 by default
    match args.jobs {
        j if j < 1 => {
            log::warn!("Number of jobs cannot be less than 1. Setting to 1 by default.");
            args.jobs = 1;
        }
        _ => (),
    }

    log::info!(
        "Preparing evaluation harness at {}",
        args.eval_out.display()
    );
    let run_layout = prepare::prepare_harness(&args)?;

    if args.prepare_only {
        log::debug!("--prepare-only flag is set. Exiting after preparation.");
        return Ok(0);
    } else {
        driver::dispatch_agent(
            &args.agent_dir,
            &args.from_framework,
            &args.to_framework,
            &run_layout,
        );
    }

    Ok(0)
}
