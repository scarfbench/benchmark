use anyhow::Result;
use clap::Parser;

mod bench;
mod cli;
mod eval;
mod utils;

fn main() -> Result<()> {
    let cli = cli::Cli::parse();
    init_logging(cli.verbose);

    let code = match cli.command {
        cli::Commands::Bench(cmd) => bench::run(cmd)?,
        cli::Commands::Eval(cmd) => eval::run(cmd)?,
    };
    std::process::exit(code);
}

fn init_logging(verbose: u8) {
    // Use `RUST_LOG` if set; otherwise default to `warn`, and let `-v/-vv/-vvv` raise verbosity.
    let default_filter = match verbose {
        0 => "warn",
        1 => "info",
        2 => "debug",
        _ => "trace",
    };
    env_logger::Builder::from_env(env_logger::Env::default().default_filter_or(default_filter))
        .init();
}
