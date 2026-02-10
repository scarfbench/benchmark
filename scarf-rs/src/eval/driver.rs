use crate::eval::types::{EvalLayout, RunMetaData};
use rayon::ThreadPoolBuilder;
use rayon::prelude::*;
use std::{
    fs::{self, File},
    io::Write,
    path::{Path, PathBuf},
    process::Command,
};

/// The main helper to dispatch calls to the user defined agent
pub fn dispatch_agent(agent_dir: &Path, eval_layout: &EvalLayout) -> anyhow::Result<()> {
    for (eval_key, eval_group) in eval_layout {
        // If the current dir eval_root/{agent_name}__{layer}__.../ doesn't contain the current agent dir thjen we skip that
        if !agent_dir
            .file_name()
            .and_then(|f| f.to_str())
            .is_some_and(|a| eval_key.agent().eq(a))
        {
            continue;
        }

        let pool = ThreadPoolBuilder::new()
            .num_threads(std::cmp::min(
                eval_group.runs().len(),
                std::thread::available_parallelism()
                    .map(|t| t.get())
                    .unwrap_or(1),
            ))
            .build()?;

        let _: anyhow::Result<Vec<()>> = pool.install(|| {
            eval_group
                .par_iter()
                .map(|eval_instance| -> anyhow::Result<()> {
                    // Read the current eval metadata
                    let mut run_metadata: RunMetaData =
                        fs::read_to_string(eval_instance.root().join("metadata.json"))
                            .map_err(anyhow::Error::from)
                            .and_then(|metadata_file| {
                                serde_json::from_str::<RunMetaData>(&metadata_file)
                                    .map_err(anyhow::Error::from)
                            })?;

                    let result = Command::new("bash")
                        .arg("-lc")
                        .arg("./run.sh")
                        .current_dir(agent_dir)
                        .env("SCARF_WORK_DIR", eval_instance.output())
                        .env("SCARF_SOURCE_FRAMEWORK", run_metadata.source_framework())
                        .env("SCARF_TARGET_FRAMEWORK", run_metadata.target_framework())
                        .stderr(
                            File::create(eval_instance.validation().join("agent.err"))?
                                .try_clone()?,
                        )
                        .stdout(
                            File::create(eval_instance.validation().join("agent.out"))?
                                .try_clone()?,
                        )
                        .output()?;

                    if result.status.success() {
                        log::debug!("Agent {} exectuion complete", eval_key.agent());
                        run_metadata.set_status(String::from("AGENT EXECUTION COMPLETE"));
                        update_eval_metadata(eval_instance.root(), &run_metadata)?;
                    } else {
                        run_metadata.set_status(String::from("AGENT EXECUTION FAILED"));
                        update_eval_metadata(eval_instance.root(), &run_metadata)?;
                    }
                    Ok(())
                })
                .collect()
        });
    }
    Ok(())
}

fn update_eval_metadata(
    eval_instance_dir: PathBuf,
    run_metadata: &RunMetaData,
) -> anyhow::Result<()> {
    match File::create(eval_instance_dir.join("metadata.json")) {
        Ok(mut f) => f.write_all(serde_json::to_string_pretty(run_metadata)?.as_bytes())?,
        Err(e) => {
            anyhow::bail!("Failed to update metadata.json {}", e);
        }
    };
    Ok(())
}
