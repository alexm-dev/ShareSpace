# Builds a submission package for ShareSpace, including:
# - A zip file containing:
#  - The source code (from the given git tag or HEAD)
#  - The generated Javadoc
#  - The shaded JAR file
#  - A native app-image built with jpackage

param(
    [string]$Tag
)

$ErrorActionPreference = "Stop"
$projectRoot = Split-Path -Parent $PSScriptRoot
$stage = Join-Path $projectRoot "submission\ShareSpace"
$outZip = Join-Path $projectRoot "submission\ShareSpace-submission.zip"

Write-Host "==> Cleaning previous builds"
& mvn -f "$projectRoot\pom.xml" clean -q
if ($LASTEXITCODE -ne 0) { throw "mvn clean failed" }

Write-Host "==> Compiling + packaging (creates fat JAR)"
& mvn -f "$projectRoot\pom.xml" package -q -DskipTests
if ($LASTEXITCODE -ne 0) { throw "mvn package failed" }

Write-Host "==> Generating JavaDocs"
& mvn -f "$projectRoot\pom.xml" javadoc:javadoc -q
if ($LASTEXITCODE -ne 0) { throw "mvn javadoc:javadoc failed" }

Write-Host "==> Staging submission folder"
if (Test-Path "$projectRoot\submission") { Remove-Item "$projectRoot\submission" -Recurse -Force }
New-Item -ItemType Directory -Path $stage | Out-Null

$gitRef = if ($Tag) { $Tag } else { "HEAD" }
if ($Tag) {
    if (-not (git -C $projectRoot tag --list $Tag)) { throw "Tag '$Tag' not found" }
} else {
    Write-Host "    (no -Tag given; no release tag exists yet, archiving HEAD instead)"
}

Write-Host "==> Archiving tracked source ($gitRef) to repo\"
$srcZip = Join-Path $env:TEMP "sharespace-src-$([guid]::NewGuid()).zip"
git -C $projectRoot archive --format=zip -o $srcZip $gitRef
New-Item -ItemType Directory -Path "$stage\repo" | Out-Null
Expand-Archive -Path $srcZip -DestinationPath "$stage\repo" -Force
Remove-Item $srcZip

Write-Host "==> Copying doc\"
Copy-Item -Path "$projectRoot\doc" -Destination "$stage\doc" -Recurse

Copy-Item -Path "$projectRoot\target\reports\apidocs" -Destination "$stage\javadoc" -Recurse

$shadedJar = Get-ChildItem "$projectRoot\target\*.jar" | Where-Object { $_.Name -notlike "original-*" } | Select-Object -First 1
if (-not $shadedJar) { throw "No shaded JAR found in target/" }
Copy-Item $shadedJar.FullName -Destination $stage

Write-Host "==> Building native app-image (jpackage)"
$jpackageWork = Join-Path $env:TEMP "sharespace-jpackage-$([guid]::NewGuid())"
$jpackageInput = Join-Path $jpackageWork "input"
$jpackageOutput = Join-Path $jpackageWork "output"
New-Item -ItemType Directory -Path $jpackageInput | Out-Null
Copy-Item $shadedJar.FullName -Destination $jpackageInput

& jpackage --type app-image --input $jpackageInput --main-jar $shadedJar.Name `
  --main-class app.Main --name ShareSpace --app-version 0.1.0 --dest $jpackageOutput
if ($LASTEXITCODE -ne 0) { throw "jpackage failed" }
Copy-Item -Path (Join-Path $jpackageOutput "ShareSpace") -Destination "$stage\app" -Recurse
Remove-Item $jpackageWork -Recurse -Force -ErrorAction SilentlyContinue

Write-Host "==> Creating zip"
Compress-Archive -Path "$stage\*" -DestinationPath $outZip -Force

Write-Host ""
Write-Host "Done.  Submission package at:" -ForegroundColor Green
Write-Host "  $outZip"
Write-Host "JAR included: $($shadedJar.Name) ($([math]::Round($shadedJar.Length/1MB,2)) MB)" -ForegroundColor Cyan
Write-Host "App-image included: app\ShareSpace.exe" -ForegroundColor Cyan
