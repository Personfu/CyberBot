$JAVAC   = "C:\Users\pfuru\Downloads\jdk-26.0.1\bin\javac.exe"
$DB_API  = "C:\Users\pfuru\DreamBot\BotData\repository2\dreambot-client.jar"
$SRC     = "C:\Users\pfuru\Downloads\CyberBot\DreambotMasterAIO\src"
$OUT     = "C:\Users\pfuru\Downloads\CyberBot\DreambotMasterAIO\out\classes"
$OUTFILE = "C:\temp\javac_verbose.txt"

Remove-Item $OUT -Recurse -Force -ErrorAction SilentlyContinue
New-Item $OUT -ItemType Directory -Force | Out-Null

$files = (Get-ChildItem $SRC -Recurse -Filter "*.java").FullName

$listFile = "C:\temp\javac_sources.txt"
$files | Set-Content $listFile -Encoding ASCII

# Run javac, capturing all output to file using the JVM's -J flag for encoding
$procInfo = New-Object System.Diagnostics.ProcessStartInfo
$procInfo.FileName = $JAVAC
$procInfo.Arguments = "--release 11 -cp `"$DB_API`" -d `"$OUT`" -Xlint:all -J-Dfile.encoding=UTF-8 `"@$listFile`""
$procInfo.RedirectStandardOutput = $true
$procInfo.RedirectStandardError = $true
$procInfo.UseShellExecute = $false
$procInfo.CreateNoWindow = $true

$proc = [System.Diagnostics.Process]::Start($procInfo)
$stdout = $proc.StandardOutput.ReadToEnd()
$stderr = $proc.StandardError.ReadToEnd()
$proc.WaitForExit()

"=== STDOUT ===" | Out-File $OUTFILE
$stdout | Out-File $OUTFILE -Append
"=== STDERR ===" | Out-File $OUTFILE -Append
$stderr | Out-File $OUTFILE -Append
"=== EXIT: $($proc.ExitCode) ===" | Out-File $OUTFILE -Append

Write-Host "Exit: $($proc.ExitCode)"
Write-Host "--- STDERR ---"
$stderr
Write-Host "--- STDOUT ---"
$stdout
