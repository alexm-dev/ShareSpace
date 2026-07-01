#!/usr/bin/env bash
# Builds a submission package for ShareSpace, including:
# - A zip file containing:
#  - The source code (from the given git tag or HEAD)
#  - The generated Javadoc
#  - The shaded JAR file
#  - A native app-image built with jpackage

set -euo pipefail

project_root="$(cd "$(dirname "$0")/.." && pwd)"
stage="$project_root/submission/ShareSpace"
out_zip="$project_root/submission/ShareSpace-submission.zip"
git_ref="${1:-HEAD}"

echo "==> Cleaning previous builds"
mvn -f "$project_root/pom.xml" clean -q

echo "==> Compiling + packaging (creates fat JAR)"
mvn -f "$project_root/pom.xml" package -q -DskipTests

echo "==> Generating JavaDocs"
mvn -f "$project_root/pom.xml" javadoc:javadoc -q

echo "==> Staging submission folder"
rm -rf "$project_root/submission"
mkdir -p "$stage"

if [[ $# -ge 1 ]]; then
    if ! git -C "$project_root" tag --list "$git_ref" | grep -q .; then
        echo "Error: tag '$git_ref' not found" >&2
        exit 1
    fi
else
    echo "    (no tag given; no release tag exists yet, archiving HEAD instead)"
fi

echo "==> Archiving tracked source ($git_ref) to repo/"
mkdir -p "$stage/repo"
git -C "$project_root" archive --format=tar "$git_ref" | tar -x -C "$stage/repo"

echo "==> Copying doc/"
cp -r "$project_root/doc" "$stage/doc"

cp -r "$project_root/target/reports/apidocs" "$stage/javadoc"

shaded_jar="$(find "$project_root/target" -maxdepth 1 -name '*.jar' ! -name 'original-*' | head -n 1)"
if [[ -z "$shaded_jar" ]]; then
    echo "Error: no shaded JAR found in target/" >&2
    exit 1
fi
cp "$shaded_jar" "$stage/"

echo "==> Building native app-image (jpackage)"
jpackage_work="$(mktemp -d)"
trap 'rm -rf "$jpackage_work"' EXIT
jpackage_input="$jpackage_work/input"
jpackage_output="$jpackage_work/output"
mkdir -p "$jpackage_input"
cp "$shaded_jar" "$jpackage_input/"

jpackage --type app-image --input "$jpackage_input" --main-jar "$(basename "$shaded_jar")" \
  --main-class app.Main --name ShareSpace --app-version 0.1.0 --dest "$jpackage_output"
cp -r "$jpackage_output/ShareSpace" "$stage/app"

echo "==> Creating zip"
( cd "$stage/.." && zip -qr "$out_zip" "$(basename "$stage")"/* )

jar_size_mb=$(du -m "$shaded_jar" | cut -f1)
echo ""
echo "Done.  Submission package at:"
echo "  $out_zip"
echo "JAR included: $(basename "$shaded_jar") (${jar_size_mb} MB)"
echo "App-image included: app/bin/ShareSpace"
