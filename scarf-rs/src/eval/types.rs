use std::{collections::HashMap, path::PathBuf};

use rayon::iter::{IntoParallelIterator, IntoParallelRefIterator};
use serde::{Deserialize, Serialize};

/// This holds the eval datastructure
///
/// # Fields:
/// - `root`: The base directory where this instance of the run's data is stored.
/// - `input`: The subdirectory inside root where a copy of the source application is made.
/// - `output`: The subdirectory inside root where the agent works to transform the app into the target framework.
/// - `validation`: The subdirectory where we hold final validation, smoke test logs, etc.
#[derive(Serialize)]
pub(super) struct EvalInstance {
    root: PathBuf,
    input: PathBuf,
    output: PathBuf,
    validation: PathBuf,
}
impl EvalInstance {
    pub(super) fn new(
        root: impl Into<PathBuf>,
        input: impl Into<PathBuf>,
        output: impl Into<PathBuf>,
        validation: impl Into<PathBuf>,
    ) -> Self {
        Self {
            root: root.into(),
            input: input.into(),
            output: output.into(),
            validation: validation.into(),
        }
    }
    pub(super) fn root(&self) -> PathBuf {
        self.root.to_path_buf()
    }
    pub(super) fn output(&self) -> PathBuf {
        self.output.to_path_buf()
    }
    pub(super) fn validation(&self) -> PathBuf {
        self.validation.to_path_buf()
    }
}

/// A container to hold all the eval instance off k runs of an agent
///
/// # Fields:
/// - root: The outer directory where all the runs are stored. Typically, its structured as follows:
///   agent__layer__app__source_framework__target_framework/...
/// - runs: A vector of EvalInstance corresponding to each run of the evaluation (the length of this
///   vector match the value of k from the pass at k value)
#[derive(Serialize)]
pub(super) struct EvalGroup {
    root: PathBuf,
    runs: Vec<EvalInstance>, // Each item captures a pass@k repeat
}
impl EvalGroup {
    pub(super) fn new(root: impl Into<PathBuf>, runs: impl Into<Vec<EvalInstance>>) -> Self {
        Self {
            root: root.into(),
            runs: runs.into(),
        }
    }
    pub(super) fn runs(&self) -> &[EvalInstance] {
        &self.runs
    }
    pub(super) fn par_runs(&self) -> rayon::slice::Iter<'_, EvalInstance> {
        self.runs().par_iter()
    }
}
impl<'a> IntoIterator for &'a EvalGroup {
    type Item = &'a EvalInstance;
    type IntoIter = std::slice::Iter<'a, EvalInstance>;
    fn into_iter(self) -> Self::IntoIter {
        self.runs().iter()
    }
}
// Parallel iterator so I can call eval_group.par_iter()
impl<'a> IntoParallelIterator for &'a EvalGroup
where
    EvalInstance: Sync,
{
    type Item = &'a EvalInstance;
    type Iter = rayon::slice::Iter<'a, EvalInstance>;

    fn into_par_iter(self) -> Self::Iter {
        self.par_runs()
    }
}

/// Key to map each evaluation
///
/// # Fields:
/// - agent: Name of the agent (derived from the name of the agent dir)
/// - layer: Application layer
/// - app: Name of the application
/// - source_framework: The orgin framework to begin conversion
/// - target_framework: The destination framework to end conversion
#[derive(Serialize, Eq, PartialEq, Hash, Clone)]
pub(super) struct EvalKey {
    agent: String,
    layer: String,
    app: String,
    source_framework: String,
    target_framework: String,
}
impl EvalKey {
    pub(super) fn new(
        agent: impl Into<String>,
        layer: impl Into<String>,
        app: impl Into<String>,
        source_framework: impl Into<String>,
        target_framework: impl Into<String>,
    ) -> Self {
        Self {
            agent: agent.into(),
            layer: layer.into(),
            app: app.into(),
            source_framework: source_framework.into(),
            target_framework: target_framework.into(),
        }
    }
    pub(super) fn agent(&self) -> String {
        self.agent.to_string()
    }
    pub(super) fn layer(&self) -> String {
        self.layer.to_string()
    }
    pub(super) fn app(&self) -> String {
        self.app.to_string()
    }
    pub(super) fn source_framework(&self) -> String {
        self.source_framework.to_string()
    }
    pub(super) fn target_framework(&self) -> String {
        self.target_framework.to_string()
    }

    pub(super) fn repr(&self) -> String {
        format!(
            "{}__{}__{}__{}__{}",
            &self.agent, &self.layer, &self.app, &self.source_framework, &self.target_framework
        )
    }
}

/// Here we maintain the outer layout to handle the runs
///
/// # Fields:
/// - evals: Map of evaluations where the key marks a distinct combination of agent/layer/app/from/to
#[derive(Serialize)]
pub(super) struct EvalLayout {
    evals: HashMap<EvalKey, EvalGroup>,
}
impl EvalLayout {
    pub(super) fn new(evals: HashMap<EvalKey, EvalGroup>) -> Self {
        Self { evals }
    }
}
// Lets implement an iterator over EvalLayout so we can do for (key, value) in &eval_layout
// This is for ergonomics and my pedagogical interests, technically I can just use eval_layout.iter()
//
// We associate a lifetime 'a to this iterator and tie all the operations to that lifetime.
//
// In order to have an iterator, we need 2 types to be declared
// - Item which tells the compiler what are the (lifetime tagged) key and value types that come out at each step
// - IntoIter which tells the compiler which concrete iterator type is being used when we do the yield
impl<'a> IntoIterator for &'a EvalLayout {
    // Each item produced by this iterator is a pair of values of type EvalKey and EvalGroup both
    // with the lifetime 'a
    type Item = (&'a EvalKey, &'a EvalGroup);
    // We'll use the hash map as the template for the iterator. If we had, say, a vector we'd have used that as the template.
    // Basically, the compiler needs you to tell it exactly which concrete iterator type we are returing when into_iter is called.
    // In other words, "whenever someone calls for ... in &EvalLayout, they will get a hashmap's iterator over the borrowed keys
    // and values.
    type IntoIter = std::collections::hash_map::Iter<'a, EvalKey, EvalGroup>;
    // We'll delegate any into_iter calls to our regular iterator.
    fn into_iter(self) -> Self::IntoIter {
        self.evals.iter()
    }
}
impl<'x> IntoParallelIterator for &'x EvalLayout
where
    EvalKey: Sync,
    EvalGroup: Sync,
{
    type Item = (&'x EvalKey, &'x EvalGroup);
    type Iter = rayon::collections::hash_map::Iter<'x, EvalKey, EvalGroup>;
    fn into_par_iter(self) -> Self::Iter {
        self.evals.par_iter()
    }
}

/// This is to hold the run metadata for saving in the evals folder later
#[derive(Serialize, Deserialize)]
pub(super) struct RunMetaData {
    agent: String,
    layer: String,
    app: String,
    status: String,
    repeat: u32,
    source_framework: String,
    target_framework: String,
}
impl RunMetaData {
    pub(super) fn new(
        agent: impl Into<String>,
        layer: impl Into<String>,
        app: impl Into<String>,
        status: impl Into<String>,
        repeat: impl Into<u32>,
        source_framework: impl Into<String>,
        target_framework: impl Into<String>,
    ) -> Self {
        Self {
            agent: agent.into(),
            layer: layer.into(),
            app: app.into(),
            status: status.into(),
            repeat: repeat.into(),
            source_framework: source_framework.into(),
            target_framework: target_framework.into(),
        }
    }
    pub(super) fn source_framework(&self) -> String {
        self.source_framework.to_string()
    }
    pub(super) fn target_framework(&self) -> String {
        self.target_framework.to_string()
    }
    pub(super) fn set_status(&mut self, status: String) -> &mut Self {
        self.status = status;
        self
    }
}
