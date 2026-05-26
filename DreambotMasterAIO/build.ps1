# ─────────────────────────────────────────────────────────────────────────────
# build.ps1  —  Compile + deploy FLLC Master AIO to DreamBot Scripts folder
# Run from anywhere:  powershell -File ".\DreambotMasterAIO\build.ps1"
# ─────────────────────────────────────────────────────────────────────────────

$JAVAC      = "$env:USERPROFILE\Downloads\jdk-26.0.1\bin\javac.exe"
$JAR_TOOL   = "$env:USERPROFILE\Downloads\jdk-26.0.1\bin\jar.exe"
$DB_API     = "$env:USERPROFILE\DreamBot\BotData\repository2\dreambot-client.jar"
$GSON_JAR   = "$env:USERPROFILE\DreamBot\BotData\repository2\gson-2.10.1.jar"
$SRC_DIR    = "$PSScriptRoot\src"
$OUT_DIR    = "$PSScriptRoot\out\classes"
$JAR_OUT    = "$PSScriptRoot\out\FLLCMasterAIO.jar"
$DEPLOY_DIR = "$env:USERPROFILE\DreamBot\Scripts"

# ── Validate tools ─────────────────────────────────────────────────────────
foreach ($path in @($JAVAC, $JAR_TOOL, $DB_API)) {
    if (-not (Test-Path $path)) {
        Write-Host "ERROR: Not found: $path" -ForegroundColor Red
        exit 1
    }
}

# ── Clean output directory ─────────────────────────────────────────────────
if (Test-Path $OUT_DIR) { Remove-Item -Recurse -Force $OUT_DIR }
New-Item -ItemType Directory -Force $OUT_DIR | Out-Null
New-Item -ItemType Directory -Force (Split-Path $JAR_OUT) | Out-Null

# ── Collect all .java source files ────────────────────────────────────────
$javaFiles = Get-ChildItem -Path $SRC_DIR -Recurse -Filter "*.java" |
    Select-Object -ExpandProperty FullName
Write-Host "Found $($javaFiles.Count) source files." -ForegroundColor Cyan

# ── Compile (target Java 11 — matches DreamBot's embedded JRE) ────────────
Write-Host "Compiling..." -ForegroundColor Cyan
$errFile = "$env:TEMP\dreambot_build_errors.txt"
$procInfo = New-Object System.Diagnostics.ProcessStartInfo
$procInfo.FileName = $JAVAC
$listFile = "$env:TEMP\dreambot_sources.txt"
$javaFiles | Set-Content $listFile -Encoding ASCII
$procInfo.Arguments = "--release 11 -cp `"$DB_API;$GSON_JAR`" -d `"$OUT_DIR`" `"@$listFile`""
$procInfo.RedirectStandardOutput = $true
$procInfo.RedirectStandardError = $true
$procInfo.UseShellExecute = $false
$procInfo.CreateNoWindow = $true
$proc = [System.Diagnostics.Process]::Start($procInfo)
$stderr = $proc.StandardError.ReadToEnd()
$stdout = $proc.StandardOutput.ReadToEnd()
$proc.WaitForExit()
if ($stdout) { Write-Host "STDOUT: $stdout" }
$stderr | Where-Object { $_ } | ForEach-Object { Write-Host $_ }
$buildExitCode = $proc.ExitCode

if ($buildExitCode -ne 0) {
    Write-Host ""
    Write-Host "COMPILATION FAILED. Fix errors above and re-run build.ps1." -ForegroundColor Red
    exit 1
}
Write-Host "Compilation OK." -ForegroundColor Green

# ── Package into JAR (no Main-Class — DreamBot reads @ScriptManifest) ─────
Write-Host "Packaging JAR..." -ForegroundColor Cyan
& $JAR_TOOL cf $JAR_OUT -C $OUT_DIR .

if ($LASTEXITCODE -ne 0) {
    Write-Host "JAR packaging FAILED." -ForegroundColor Red
    exit 1
}
$size = [math]::Round((Get-Item $JAR_OUT).Length / 1KB, 1)
Write-Host "JAR created: $JAR_OUT  ($size KB)" -ForegroundColor Green

# ── Deploy to DreamBot Scripts folder ──────────────────────────────────────
if (-not (Test-Path $DEPLOY_DIR)) {
    New-Item -ItemType Directory -Force $DEPLOY_DIR | Out-Null
}
$dest = "$DEPLOY_DIR\FLLCMasterAIO.jar"
Copy-Item $JAR_OUT $dest -Force
Write-Host "Deployed to: $dest" -ForegroundColor Green

Write-Host ""
Write-Host "Done! In DreamBot: refresh scripts list and look for 'FLLC Master AIO'." -ForegroundColor Yellow
