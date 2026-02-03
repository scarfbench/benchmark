use clap::{Args, Parser, Subcommand};

#[derive(Parser, Debug)]
#[command(name = "scarf", version, about = "ScarfBench CLI")]
pub struct Cli {
    #[arg(
        short,
        long,
        action = clap::ArgAction::Count,
        global = true,
        help = "Increase verbosity (-v, -vv, -vvv). If RUST_LOG is set, it takes precedence."
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
}

/// Again, enums work here because we choose one of the subcommands for bench.
#[derive(Subcommand, Debug)]
pub enum BenchCmd {
    #[command(about = "List the application(s) in the benchmark.")]
    List(BenchListArgs),
    #[command(about = "Run regression tests (with `make test`) on the benchmark application(s).")]
    Test(BenchTestArgs),
}

#[derive(Args, Debug)]
pub struct BenchListArgs {
    #[arg(long, help = "Path to the root of the scarf repository.")]
    pub root: String,

    #[arg(long, help = "Application layer to list.")]
    pub layer: Option<String>,
}

#[derive(Args, Debug)]
pub struct BenchTestArgs {
    #[arg(long, help = "Path to the root of the scarf repository.")]
    pub root: String,

    #[arg(long, help = "Application layer to test.")]
    pub layer: Option<String>,

    #[arg(
        long = "dry-run",
        action = clap::ArgAction::SetTrue,
        help = "Use dry run instead of full run."
    )]
    pub dry_run: bool,
}
