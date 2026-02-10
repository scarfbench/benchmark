use std::{
    fs,
    path::Path,
    process::{Command, Stdio},
};

use crate::eval::types::{EvalLayout, RunMetaData};
use anyhow::{Error, Result};
/// The main helper to dispatch calls to the user defined agent
pub fn dispatch_agent(agent_dir: &Path, eval_layout: &EvalLayout) -> Result<()> {
    for (eval_key, eval_group) in eval_layout {
        // If the current dir eval_root/{agent_name}__{layer}__.../ doesn't contain the current agent dir thjen we skip that
        if !agent_dir
            .file_name()
            .and_then(|f| f.to_str())
            .is_some_and(|a| eval_key.agent().eq(a))
        {
            continue;
        }
        // TODO: The following loop ought to be parallelized...
        for eval_instance in eval_group {
            // Read the current eval metadata
            let run_metadata: RunMetaData =
                fs::read_to_string(eval_instance.root().join("metadata.json"))
                    .map_err(Error::from)
                    .and_then(|metadata_file| {
                        serde_json::from_str::<RunMetaData>(&metadata_file).map_err(Error::from)
                    })?;

            let _ = Command::new("bash")
                .arg("-lc")
                .arg("./run.sh")
                .current_dir(agent_dir)
                .env("SCARF_WORK_DIR", eval_instance.output())
                .env("SCARF_FROM_FRAMEWORK", run_metadata.from_framework())
                .env("SCARF_TO_FRAMEWORK", run_metadata.to_framework())
                .stderr(Stdio::piped())
                .stdout(Stdio::piped())
                .status()?;
        }
    }
    Ok(())
}
