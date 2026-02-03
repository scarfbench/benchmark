import argparse
import csv
import re
import shutil
import socket
import subprocess
import time
import urllib.error
import urllib.request
import uuid
from concurrent.futures import ThreadPoolExecutor, as_completed
from pathlib import Path
from typing import Dict, List, Optional, Tuple


MDKey = Tuple[str, str, str, str, str]  


SAFE_DOCKERIGNORE = """target/
build/
out/
bin/
*.class
*.jar
*.war
*.ear

.mvn/
.m2/
.gradle/

.idea/
.vscode/
*.iml
*.ipr
*.iws
.classpath
.project
.settings/

.git/
.gitignore
.gitattributes

.DS_Store
Thumbs.db

.dockerignore

*.log
*.tmp
*.temp
"""


def run(cmd: List[str], cwd: Optional[Path] = None, timeout: Optional[int] = None) -> subprocess.CompletedProcess:
    return subprocess.run(cmd, cwd=cwd, capture_output=True, text=True, timeout=timeout)


def write_text(path: Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text, encoding="utf-8")


def tail(text: str, n_lines: int = 120) -> str:
    lines = (text or "").splitlines()
    return "\n".join(lines[-n_lines:])


def parse_results_md(results_file: str) -> List[Dict[str, str]]:
    try:
        lines = Path(results_file).read_text(encoding="utf-8").splitlines(True)
    except FileNotFoundError:
        print(f"Error: {results_file} not found.")
        return []

    rows: List[Dict[str, str]] = []
    for line in lines:
        s = line.strip()
        if not s.startswith("|") or s.startswith("|---"):
            continue

        parts = [p.strip() for p in line.split("|")]
        if len(parts) < 7:
            continue

        def get(i: int) -> str:
            return parts[i] if i < len(parts) else ""

        rows.append(
            {
                "cli_tool": get(1),
                "model": get(2),
                "layer": get(3),
                "conversion": get(4),
                "app": get(5),
                "orig_exists": get(6),
                "converted": get(7),
                "compiled": get(8),
                "ran": get(9),
                "smoke": get(10),
                "line": line,
            }
        )
    return rows


def count_runs(converted_str: str) -> int:
    return (converted_str or "").count("✅")


def parse_compiled_status(compiled_str: str) -> List[bool]:
    return [c == "✅" for c in (compiled_str or "") if not c.isspace()]


def get_ran_symbol(ran_str: str, run_num: int) -> Optional[str]:
    s = ran_str or ""
    emojis = [c for c in s if not c.isspace()]
    if 1 <= run_num <= len(emojis):
        return emojis[run_num - 1]
    return None


def should_reattempt_run(sym: Optional[str]) -> bool:
    return sym is None or sym in {"🔨", "⬛"}


def get_dockerfile_source(conversion: str) -> str:
    if "to-jakarta" in conversion:
        return "jakarta_Dockerfile"
    if "to-spring" in conversion:
        return "spring_Dockerfile"
    if "to-quarkus" in conversion:
        return "quarkus_Dockerfile"
    raise ValueError(f"Unknown conversion type: {conversion}")


def detect_build_system(run_dir: Path) -> str:
    if (run_dir / "pom.xml").exists():
        return "maven"
    if (run_dir / "build.gradle").exists() or (run_dir / "build.gradle.kts").exists():
        return "gradle"
    return "maven"


def detect_java_version(run_dir: Path) -> Optional[int]:
    pom = run_dir / "pom.xml"
    if pom.exists():
        try:
            content = pom.read_text(encoding="utf-8")
            m = re.search(r"<maven\.compiler\.(?:source|target)>(\d+)</maven\.compiler\.(?:source|target)>", content)
            if m:
                return int(m.group(1))
            m = re.search(r"<java\.version>(\d+)</java\.version>", content)
            if m:
                return int(m.group(1))
        except Exception:
            pass

    for fn in ("build.gradle", "build.gradle.kts"):
        gp = run_dir / fn
        if gp.exists():
            try:
                content = gp.read_text(encoding="utf-8")
                m = re.search(r"sourceCompatibility\s*=\s*JavaVersion\.VERSION_(\d+)", content)
                if m:
                    return int(m.group(1))
                m = re.search(r"java\.toolchain\.languageVersion\s*=\s*JavaLanguageVersion\.of\((\d+)\)", content)
                if m:
                    return int(m.group(1))
            except Exception:
                pass
    return None


def ensure_safe_dockerignore(run_dir: Path) -> None:
    write_text(run_dir / ".dockerignore", SAFE_DOCKERIGNORE)


def update_java_version_in_dockerfile_text(content: str, java_version: int) -> str:
    return re.sub(r"FROM eclipse-temurin:\d+-jdk", f"FROM eclipse-temurin:{java_version}-jdk", content)


def create_dockerfile(base_dockerfile: Path, build_system: str, java_version: int) -> str:
    content = base_dockerfile.read_text(encoding="utf-8")

    if build_system == "gradle":
        content = content.replace("mvn clean package -Dmaven.test.skip=true", "./gradlew clean build -x test")
        content = content.replace("mvn liberty:run", "./gradlew libertyRun")
        content = content.replace("mvn spring-boot:run", "./gradlew bootRun")
        content = content.replace("mvn quarkus:dev", "./gradlew quarkusDev")

    if java_version and java_version != 17:
        content = update_java_version_in_dockerfile_text(content, java_version)

    return content


def get_output_dir(cli_tool: str, layer: str, app: str, conversion: str, run_num: int, base_dir: Path) -> Path:
    return base_dir / "evaluation-outputs" / cli_tool / layer / f"{app}-{conversion}" / f"run_{run_num}"


def build_docker_image(run_dir: Path, conversion: str, run_num: int, output_dir: Optional[Path], timeout: int) -> Tuple[bool, str]:
    unique_id = str(uuid.uuid4())[:8]
    image_name = f"{conversion.replace('-', '_')}_{run_num}_{int(time.time())}_{unique_id}"

    try:
        r = run(["docker", "build", "-t", image_name, "."], cwd=run_dir, timeout=timeout)
    except subprocess.TimeoutExpired:
        msg = f"DOCKER BUILD TIMED OUT ({timeout}s)\n{run_dir}"
        if output_dir:
            write_text(output_dir / "docker_build.out", msg)
        return False, msg

    if r.returncode == 0:
        return True, image_name

    msg = "\n".join(
        [
            "DOCKER BUILD FAILED",
            f"cwd: {run_dir}",
            f"cmd: docker build -t {image_name} .",
            f"exit: {r.returncode}",
            "--- stdout ---",
            r.stdout or "",
            "--- stderr ---",
            r.stderr or "",
        ]
    )
    if output_dir:
        write_text(output_dir / "docker_build.out", msg)
    return False, msg


def smoke_test_app(host_port: int, max_attempts: int, delay: float) -> Tuple[bool, str]:
    url = f"http://localhost:{host_port}"

    for attempt in range(1, max_attempts + 1):
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.settimeout(2)
            ok = sock.connect_ex(("localhost", host_port)) == 0
            sock.close()

            if not ok:
                time.sleep(delay)
                continue

            req = urllib.request.Request(url, headers={"User-Agent": "Docker-Smoke-Test/1.0", "Accept": "*/*"})
            try:
                with urllib.request.urlopen(req, timeout=5) as resp:
                    return True, f"HTTP {resp.getcode()}"
            except urllib.error.HTTPError as e:
                return True, f"HTTP {e.code}"
            except urllib.error.URLError as e:
                time.sleep(delay)
                last = str(e)
                continue
        except Exception as e:
            time.sleep(delay)
            last = str(e)
            continue

    return False, f"unreachable on port {host_port} ({max_attempts} attempts): {last if 'last' in locals() else ''}"


def cleanup_container_image(container_name: str, image_name: str) -> None:
    for cmd in (["docker", "rm", "-f", container_name], ["docker", "rmi", "-f", image_name]):
        try:
            run(cmd, timeout=8)
        except Exception:
            pass


def run_docker_container(
    image_name: str,
    output_dir: Optional[Path],
    startup_wait: int,
    smoke_wait: int,
    smoke_attempts: int,
    smoke_delay: float,
) -> Tuple[bool, str]:
    container_name = f"{image_name}_container"

    r = run(["docker", "run", "-d", "-p", "0:8080", "--name", container_name, image_name], timeout=20)
    if r.returncode != 0:
        msg = "\n".join(["DOCKER RUN FAILED", r.stdout or "", r.stderr or ""])
        if output_dir:
            write_text(output_dir / "docker_run.out", msg)
        cleanup_container_image(container_name, image_name)
        return False, msg

    time.sleep(startup_wait)

    status = run(["docker", "ps", "-a", "--filter", f"name={container_name}", "--format", "{{.Status}}"], timeout=10)
    if "Up" not in (status.stdout or ""):
        logs = run(["docker", "logs", container_name], timeout=20)
        msg = "\n".join(
            [
                "CONTAINER NOT RUNNING",
                f"status: {status.stdout.strip()}",
                "--- logs ---",
                (logs.stdout or "") + ("\n" + logs.stderr if logs.stderr else ""),
            ]
        )
        if output_dir:
            write_text(output_dir / "docker_run.out", msg)
        cleanup_container_image(container_name, image_name)
        return False, msg

    port = run(["docker", "port", container_name, "8080"], timeout=10)
    m = re.search(r":(\d+)", port.stdout or "")
    if not m:
        msg = f"Could not determine host port\n{port.stdout}\n{port.stderr}"
        if output_dir:
            write_text(output_dir / "docker_run.out", msg)
        cleanup_container_image(container_name, image_name)
        return False, msg

    host_port = int(m.group(1))
    if smoke_wait:
        time.sleep(smoke_wait)

    ok, smoke_msg = smoke_test_app(host_port, max_attempts=smoke_attempts, delay=smoke_delay)
    logs = run(["docker", "logs", container_name], timeout=30)

    if output_dir:
        write_text(
            output_dir / "smoke.out",
            "\n".join(
                [
                    "SMOKE TEST PASSED" if ok else "SMOKE TEST FAILED",
                    f"url: http://localhost:{host_port}",
                    f"result: {smoke_msg}",
                    "--- logs ---",
                    (logs.stdout or "") + ("\n" + logs.stderr if logs.stderr else ""),
                ]
            ),
        )

    cleanup_container_image(container_name, image_name)
    return (ok, "" if ok else smoke_msg)


def process_run(
    run_dir: Path,
    conversion: str,
    run_num: int,
    base_dir: Path,
    dockerfile_source: str,
    compiled_successfully: bool,
    cli_tool: str,
    layer: str,
    app: str,
    build_timeout: int,
    startup_wait: int,
    smoke_wait: int,
    smoke_attempts: int,
    smoke_delay: float,
) -> Tuple[bool, str]:
    if not run_dir.exists():
        return False, f"run directory does not exist: {run_dir}"
    
    if not re.match(r'^run_\d+$', run_dir.name):
        return False, f"path is not at run_X level: {run_dir}"

    ensure_safe_dockerignore(run_dir)

    build_system = detect_build_system(run_dir)
    if build_system == "maven" and not (run_dir / "pom.xml").exists():
        return False, f"pom.xml not found in {run_dir}"
    if build_system == "gradle" and not ((run_dir / "build.gradle").exists() or (run_dir / "build.gradle.kts").exists()):
        return False, f"build.gradle(.kts) not found in {run_dir}"

    detected_java = detect_java_version(run_dir) or 17
    java_candidates = [detected_java]
    if compiled_successfully and detected_java != 21:
        java_candidates.append(21)

    dockerfile_source_path = base_dir / dockerfile_source
    if not dockerfile_source_path.exists():
        return False, f"Dockerfile source not found: {dockerfile_source_path}"

    out_dir = get_output_dir(cli_tool, layer, app, conversion, run_num, base_dir)

    last_build_err = ""
    image_name = ""
    for jv in java_candidates:
        df_text = create_dockerfile(dockerfile_source_path, build_system, jv)
        write_text(run_dir / "Dockerfile", df_text)
        ok, res = build_docker_image(run_dir, conversion, run_num, out_dir, timeout=build_timeout)
        if ok:
            image_name = res
            break
        last_build_err = res

    if not image_name:
        return False, tail(last_build_err)

    ok, err = run_docker_container(
        image_name=image_name,
        output_dir=out_dir,
        startup_wait=startup_wait,
        smoke_wait=smoke_wait,
        smoke_attempts=smoke_attempts,
        smoke_delay=smoke_delay,
    )
    return (ok, "" if ok else err)


def update_results_file(results_file: Path, run_results: Dict[MDKey, List[str]]) -> None:
    content = results_file.read_text(encoding="utf-8")
    # Split by newlines
    lines = content.splitlines()
    out: List[str] = []

    for line in lines:
        s = line.strip()
        # Handle header and separator lines
        if not s.startswith("|") or s.startswith("|---"):
            out.append(s)
            continue

        # Split by | to get table cells
        parts = [p.strip() for p in line.split("|")]
        if len(parts) < 7:
            out.append(s)
            continue

        cli_tool = parts[1]
        model = parts[2]
        layer = parts[3]
        conversion = parts[4]
        app = parts[5]
        key: MDKey = (cli_tool, model, layer, conversion, app)

        if key in run_results:
            while len(parts) <= 9:
                parts.append("")
            existing = parts[9] or ""
            new_syms = run_results[key]
            merged: List[str] = []
            for i, ns in enumerate(new_syms):
                if ns:
                    merged.append(ns)
                elif i < len(existing):
                    merged.append(existing[i])
                else:
                    merged.append("")
            parts[9] = "".join(merged)

        while len(parts) <= 10:
            parts.append("")
        
        out.append("|".join(parts))

    results_file.write_text("\n".join(out) + "\n", encoding="utf-8")


def main() -> None:
    p = argparse.ArgumentParser()
    p.add_argument("--results-file", default="whole_app_conversions.md",
                    help="Path to results markdown file to read/update (default: whole_app_conversions.md)")
    p.add_argument("--result-file",
                    help="Path to output CSV file for Docker results (e.g., results_docker.csv)")
    p.add_argument("--base-dir", default=".")
    p.add_argument("--conversions-dir", default="agentic")
    p.add_argument("--dry-run", action="store_true")
    p.add_argument("--skip-existing", action="store_true")
    p.add_argument("--max-workers", type=int, default=128)

    p.add_argument("--build-timeout", type=int, default=600)
    p.add_argument("--startup-wait", type=int, default=2)
    p.add_argument("--smoke-wait", type=int, default=480)
    p.add_argument("--smoke-attempts", type=int, default=5)
    p.add_argument("--smoke-delay", type=float, default=2.0)
    args = p.parse_args()

    base_dir = Path(args.base_dir).resolve()
    conversions_dir = Path(args.conversions_dir).resolve()
    results_file = Path(args.results_file)
    result_csv_file = Path(args.result_file) if args.result_file else None

    rows = parse_results_md(str(results_file))
    print(f"📖 Parsed {len(rows)} rows from {results_file}")

    tasks: List[Tuple[MDKey, int, Path, str, str, bool]] = []
    run_results: Dict[MDKey, List[str]] = {}

    for row in rows:
        cli_tool = row["cli_tool"]
        model = row["model"]
        layer = row["layer"]
        conversion = row["conversion"]
        app = row["app"]

        converted = row["converted"]
        ran = row["ran"]
        num_runs = count_runs(converted)
        if num_runs <= 0:
            continue

        compiled = parse_compiled_status(row["compiled"])
        key: MDKey = (cli_tool, model, layer, conversion, app)
        run_results[key] = [""] * num_runs

        app_dir = conversions_dir / cli_tool / layer / f"{app}-{conversion}"
        if not app_dir.exists():
            print(f"Missing: {app_dir}")
            continue

        dockerfile_source = get_dockerfile_source(conversion)

        for run_num in range(1, num_runs + 1):
            compiled_ok = run_num <= len(compiled) and compiled[run_num - 1]
            if not compiled_ok:
                sym = get_ran_symbol(ran, run_num) or "⏭️"
                run_results[key][run_num - 1] = sym
                continue

            sym = get_ran_symbol(ran, run_num)
            if args.skip_existing and sym == "🟢":
                run_results[key][run_num - 1] = sym
                continue

            if not should_reattempt_run(sym):
                run_results[key][run_num - 1] = sym or "⏭️"
                continue

            if args.dry_run:
                continue

            run_dir = app_dir / f"run_{run_num}"
            if not re.match(r'^run_\d+$', run_dir.name):
                print(f"⚠️  Skipping non-run_X level directory: {run_dir}")
                continue
            compiled_successfully = compiled_ok
            tasks.append((key, run_num, run_dir, conversion, dockerfile_source, compiled_successfully))

    if args.dry_run:
        print(f"🔍 [DRY RUN] Would process {len(tasks)} runs and update {results_file}")
        return

    def do_task(t: Tuple[MDKey, int, Path, str, str, bool]) -> Tuple[MDKey, int, str, str]:
        key, run_num, run_dir, conversion, dockerfile_source, compiled_successfully = t
        cli_tool, _, layer, _, app = key
        try:
            ok, err = process_run(
                run_dir=run_dir,
                conversion=conversion,
                run_num=run_num,
                base_dir=base_dir,
                dockerfile_source=dockerfile_source,
                compiled_successfully=compiled_successfully,
                cli_tool=cli_tool,
                layer=layer,
                app=app,
                build_timeout=args.build_timeout,
                startup_wait=args.startup_wait,
                smoke_wait=args.smoke_wait,
                smoke_attempts=args.smoke_attempts,
                smoke_delay=args.smoke_delay,
            )
            if ok:
                return key, run_num, "🟢", ""
            
            err_lower = (err or "").lower()
            error_type = ""
            
            if "dockerfile source not found" in err_lower:
                error_type = "no dockerfile found"
            elif "run directory does not exist" in err_lower:
                error_type = "run directory not found"
            elif "pom.xml not found" in err_lower or ("build.gradle" in err_lower and "not found" in err_lower):
                error_type = "build file not found"
            elif "docker build timed out" in err_lower or "docker build failed" in err_lower or "build failed" in err_lower:
                error_type = "docker build error"
            elif "docker run failed" in err_lower or "container not running" in err_lower:
                error_type = "docker run error"
            elif "unreachable" in err_lower or "smoke test failed" in err_lower:
                error_type = "docker ping error"
            elif "docker build" in err_lower or "mvn" in err_lower or "gradle" in err_lower:
                error_type = "docker build error"
            else:
                first_line = (err or "").split("\n")[0].strip()
                if first_line:
                    error_type = first_line[:100]  
                else:
                    error_type = "docker error"
            
            e = err_lower
            if "docker build" in e or "mvn" in e or "gradle" in e or "build failed" in e or "build timed out" in e:
                return key, run_num, "🔨", error_type
            return key, run_num, "🚫", error_type
        except Exception as ex:
            return key, run_num, "🔨", f"exception: {str(ex)}"

    csv_results = []
    
    if tasks:
        print(f"🐳 Processing {len(tasks)} Docker runs (max-workers={args.max_workers})")
        with ThreadPoolExecutor(max_workers=args.max_workers) as ex:
            futs = [ex.submit(do_task, t) for t in tasks]
            done = 0
            for f in as_completed(futs):
                done += 1
                key, run_num, sym, error_msg = f.result()
                run_results[key][run_num - 1] = sym
                
                if result_csv_file:
                    cli_tool, model, layer, conversion, app = key
                    app_dir = conversions_dir / cli_tool / layer / f"{app}-{conversion}"
                    run_dir = app_dir / f"run_{run_num}"
                    csv_results.append([
                        str(run_dir),
                        sym,
                        error_msg 
                    ])
                
                print(
                    f"[{done}/{len(tasks)}] {key[0]}/{key[2]}/{key[4]}-{key[3]} run_{run_num} = {sym}",
                    flush=True,
                )

    update_results_file(results_file, run_results)
    
    if result_csv_file and csv_results:
        result_csv_file.parent.mkdir(parents=True, exist_ok=True)
        with open(result_csv_file, mode="w", newline="") as file:
            writer = csv.writer(file)
            writer.writerow(["Path", "Status", "Error"])
            writer.writerows(csv_results)
        print(f"\n📄 Docker results saved to: {result_csv_file}")
    print("✅ Done.")


if __name__ == "__main__":
    main()

