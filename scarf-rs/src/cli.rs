use crate::bench::BenchCmd;
use crate::eval::EvalCmd;
use crate::utils::logo;
use clap::{Parser, Subcommand};
use owo_colors::OwoColorize;

#[derive(Parser, Debug)]
#[command(
    name = "scarf",
    version,
    before_help=logo(),
    about=format!("{}: {}", "ScarfBench CLI".bold().bright_red(), "The command line helper tool for scarf bench".bright_red()),
)]
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
