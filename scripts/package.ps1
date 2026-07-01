#!/usr/bin/env pwsh
# Build a self-contained native app for Windows -> dist/ShareSpace/ShareSpace.exe

$ErrorActionPreference = "Stop"
Set-Location (Split-Path $PSScriptRoot -Parent)

mvn -B clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "maven build failed" }

$jar = Get-ChildItem target -Filter "ShareSpace-*.jar" |
       Where-Object { $_.Name -notlike "original-*" } | Select-Object -First 1
$stage = "target/jpackage-input"
Remove-Item -Recurse -Force $stage -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force $stage | Out-Null
Copy-Item $jar.FullName "$stage/"

Remove-Item -Recurse -Force dist -ErrorAction SilentlyContinue
jpackage --type app-image --input $stage --main-jar $jar.Name `
  --main-class app.Main --name ShareSpace --app-version 0.1.0 --dest dist
if ($LASTEXITCODE -ne 0) { throw "jpackage failed" }

Write-Host "`nDone -> dist/ShareSpace/ShareSpace.exe"
