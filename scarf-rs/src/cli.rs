use crate::bench::BenchCmd;
use crate::eval::EvalCmd;
use clap::{Parser, Subcommand};

#[derive(Parser, Debug)]
#[command(name = "scarf", version, about = "ScarfBench CLI")]
pub struct Cli {
    #[arg(
        short,
        long,
        action = clap::ArgAction::Count,
        global = true,
        help = "Increase verbosity (-v, -vv, -vvv)."
    )]
    pub verbose: u8,

    #[command(subcommand)]
    pub command: Commands,
}

/// I'll use an enum here to capture all the commands.
/// Enums are great here because in Rust they represent "exactly one variant" at a time
#[derive(Subcommand, Debug)]
pub enum Commands {
    #[command(
        subcommand,
        about = "A series of subcommands to run on the benchmark applications."
    )]
    Bench(BenchCmd),

    #[command(subcommand, about = "Subcommands to run evaluation over the benchmark")]
    Eval(EvalCmd),
}
