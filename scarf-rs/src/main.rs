use anyhow::Result;
use clap::Parser;

mod cli;
mod bench;

fn main() -> Result<()> {
    let cli = cli::Cli::parse();

    let code = match cli.command {
        cli::Commands::Bench(cmd) => bench::run(cmd)?
    };
std::process::exit(code);
}
