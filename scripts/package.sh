#!/usr/bin/env bash
# Build a self-contained native app for Linux/macOS -> dist/ShareSpace/bin/ShareSpace

set -euo pipefail
cd "$(dirname "$0")/.."

mvn -B clean package -DskipTests

jar=$(ls target/ShareSpace-*.jar | grep -v 'original-' | head -n1)
stage=target/jpackage-input
rm -rf "$stage"; mkdir -p "$stage"
cp "$jar" "$stage/"

rm -rf dist
jpackage --type app-image --input "$stage" --main-jar "$(basename "$jar")" \
  --main-class app.Main --name ShareSpace --app-version 0.1.0 --dest dist

echo "Done -> dist/ShareSpace (run: dist/ShareSpace/bin/ShareSpace)"
