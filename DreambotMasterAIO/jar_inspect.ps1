Add-Type -AssemblyName "System.IO.Compression.FileSystem"
$jar = "C:\Users\pfuru\DreamBot\BotData\repository2\dreambot-client.jar"
$zip = [System.IO.Compression.ZipFile]::OpenRead($jar)
$entries = $zip.Entries | Select-Object -ExpandProperty FullName
$zip.Dispose()

Write-Host "=== GroundItem ==="
$entries | Where-Object {$_ -match "[Gg]round"}

Write-Host "=== MouseAlgorithm ==="
$entries | Where-Object {$_ -match "Algorithm" -or $_ -match "HumanMouse"}

Write-Host "=== Online/Hop ==="
$entries | Where-Object {$_ -match "online" -or $_ -match "WorldHop" -or $_ -match "Hop\.class"}

Write-Host "=== GrandExchange ==="
$entries | Where-Object {$_ -match "GrandEx" -or $_ -match "grandex"}

Write-Host "=== CombatStyle ==="
$entries | Where-Object {$_ -match "ombatStyle"}
