use std::path::Path;

use crate::eval::prepare::RunLayout;
use anyhow::Result;

pub fn dispatch_agent(
    agent_dir: &Path,
    from_framework: &String,
    to_framework: &String,
    run_layout: &RunLayout,
) -> Result<()> {
    Ok(())
}
