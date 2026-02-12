use owo_colors::OwoColorize;
use serde::Serialize;
use syntect::util::LinesWithEndings;
/// Pretty print and colorize json output
/// # Arguments
/// * `value` - A reference to a serializable value
/// # Returns
/// A `String` containing the pretty-printed and colorized JSON representation of the input value
#[allow(unused)]
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

pub(crate) fn logo() -> String {
    format!(
        "\x1b[1m\x1b[31m{}\x1b[0m",
        r#"
 ███████╗ ██████╗ █████╗ ██████╗ ███████╗██████╗ ███████╗███╗   ██╗ ██████╗██╗  ██╗
 ██╔════╝██╔════╝██╔══██╗██╔══██╗██╔════╝██╔══██╗██╔════╝████╗  ██║██╔════╝██║  ██║
 ███████╗██║     ███████║██████╔╝█████╗  ██████╔╝█████╗  ██╔██╗ ██║██║     ███████║
 ╚════██║██║     ██╔══██║██╔══██╗██╔══╝  ██╔══██╗██╔══╝  ██║╚██╗██║██║     ██╔══██║
 ███████║╚██████╗██║  ██║██║  ██║██║     ██████╔╝███████╗██║ ╚████║╚██████╗██║  ██║
 ╚══════╝ ╚═════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝     ╚═════╝ ╚══════╝╚═╝  ╚═══╝ ╚═════╝╚═╝  ╚═╝"#
            .bold()
            .red()
    )
}

#[cfg(test)]
mod tests {
    use super::*;
    use serde_json::json;
    #[test]
    fn test_json_pretty() {
        let data = json!({
            "name": "TestApp",
            "version": "1.0",
            "features": ["feature1", "feature2", "feature3"]
        });

        let pretty_output = json_pretty(&data);
        println!("{}", pretty_output);
        let plain = String::from_utf8(strip_ansi_escapes::strip(pretty_output)).unwrap();
        assert!(plain.contains("\"name\": \"TestApp\""));
        assert!(plain.contains("\"version\": \"1.0\""));
        assert!(plain.contains("\"features\": ["));
    }
}
