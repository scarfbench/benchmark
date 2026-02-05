use serde::Serialize;
use syntect::util::LinesWithEndings;

/// Pretty print and colorize json output
/// # Arguments
/// * `value` - A reference to a serializable value
/// # Returns
/// A `String` containing the pretty-printed and colorized JSON representation of the input value
pub fn json_pretty<T: Serialize>(value: &T) -> String {
    let pretty = serde_json::to_string_pretty(value);

    // Get the theme set and the syntax set
    match syntect::parsing::SyntaxSet::load_defaults_newlines().find_syntax_by_extension("json") {
        Some(syntax) => {
            let theme =
                &syntect::highlighting::ThemeSet::load_defaults().themes["Solarized (dark)"];
            let mut h = syntect::easy::HighlightLines::new(syntax, theme);
            let mut highlighted = String::new();

            let ps = syntect::parsing::SyntaxSet::load_defaults_newlines();

            for line in LinesWithEndings::from(&pretty.unwrap()) {
                let ranges: Vec<(syntect::highlighting::Style, &str)> =
                    h.highlight_line(line, &ps).unwrap();
                let escaped = syntect::util::as_24_bit_terminal_escaped(&ranges[..], false);
                highlighted.push_str(&escaped);
            }
            highlighted
        }
        None => pretty.unwrap(),
    }
}
