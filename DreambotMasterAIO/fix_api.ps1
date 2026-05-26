$enc = New-Object System.Text.UTF8Encoding $false
$src = "C:\Users\pfuru\Downloads\CyberBot\DreambotMasterAIO\src"

function Fix([string]$path, [string[]]$from, [string[]]$to) {
    $c = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8).TrimStart([char]0xFEFF)
    for ($i = 0; $i -lt $from.Length; $i++) { $c = $c.Replace($from[$i], $to[$i]) }
    [System.IO.File]::WriteAllText($path, $c, $enc)
}

# ---- Bulk fixes across ALL .java files -----------------------------------
$allJava = Get-ChildItem -Path $src -Recurse -Filter "*.java" | Select-Object -ExpandProperty FullName
$count = 0
foreach ($f in $allJava) {
    $c = [System.IO.File]::ReadAllText($f, [System.Text.Encoding]::UTF8).TrimStart([char]0xFEFF)
    $orig = $c
    # Players.localPlayer() -> Players.getLocal()
    $c = $c.Replace("Players.localPlayer()", "Players.getLocal()")
    # Bank.depositAll() no-arg -> depositAllItems()
    $c = $c.Replace("Bank.depositAll()", "Bank.depositAllItems()")
    # Bank.openClosest() -> Bank.open()
    $c = $c.Replace("Bank.openClosest()", "Bank.open()")
    # Walking.walkTo( -> Walking.walk(
    $c = $c.Replace("Walking.walkTo(", "Walking.walk(")
    # Combat API
    $c = $c.Replace("CombatStyle.ACCURATE", "CombatStyle.ATTACK")
    $c = $c.Replace("Combat.getFightMode()", "Combat.getCombatStyle()")
    $c = $c.Replace("Combat.toggleAttackStyle(", "Combat.setCombatStyle(")
    # Skills.getLevel( -> Skills.getRealLevel(  (only static call pattern)
    $c = $c.Replace("Skills.getLevel(", "Skills.getRealLevel(")
    # Mouse.scroll(in) single-arg — wrap in scroll(bool,1,()->true)
    # match "Mouse.scroll(X);" where X is a simple variable (not a multi-arg call)
    $c = [regex]::Replace($c, 'Mouse\.scroll\((\w+)\);', 'Mouse.scroll($1, 1, () -> true);')
    # GroundItems wrong package
    $c = $c.Replace("org.dreambot.api.methods.grounditems", "org.dreambot.api.methods.interactive")
    if ($c -ne $orig) {
        [System.IO.File]::WriteAllText($f, $c, $enc)
        $count++
    }
}
Write-Host "Bulk-fixed $count files."

# ---- SkillTask: remove Client.getSkills() usage --------------------------
$st = "$src\nezz\dreambot\master\skills\SkillTask.java"
Fix $st @(
    "        Skills s = Client.getSkills();`n        if (s == null) return 600;`n        int current = s.getRealLevel(module.skill());"
) @(
    "        int current = Skills.getRealLevel(module.skill());"
)
Write-Host "SkillTask fixed."

# ---- MasterAIO: remove Client.getSkills() usage --------------------------
$ma = "$src\nezz\dreambot\master\core\MasterAIO.java"
$mc = [System.IO.File]::ReadAllText($ma, [System.Text.Encoding]::UTF8).TrimStart([char]0xFEFF)
$mc = $mc.Replace(
    "Skills sk = Client.getSkills();`n            if (sk != null) for (Skill s : Skill.values()) totalXp += sk.getExperience(s);",
    "for (Skill s : Skill.values()) totalXp += Skills.getExperience(s);"
)
# Also add Skills import if missing
if ($mc -notmatch "import org.dreambot.api.methods.skills.Skills;") {
    $mc = $mc.Replace("import org.dreambot.api.methods.skills.Skill;", "import org.dreambot.api.methods.skills.Skill;`nimport org.dreambot.api.methods.skills.Skills;")
}
[System.IO.File]::WriteAllText($ma, $mc, $enc)
Write-Host "MasterAIO fixed."

Write-Host "All API fixes applied."
