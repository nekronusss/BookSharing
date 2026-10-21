param([string]$EnvFile = '.env', [switch]$Packaged)
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
if (-not [System.IO.Path]::IsPathRooted($EnvFile)) {
    $EnvFile = Join-Path $projectRoot $EnvFile
}
if (Test-Path -LiteralPath $EnvFile) {
    foreach ($line in Get-Content -LiteralPath $EnvFile -Encoding UTF8) {
        if ($line.Trim() -eq '' -or $line.TrimStart().StartsWith('#')) { continue }
        if ($line -notmatch '^([A-Z][A-Z0-9_]*)=(.*)$') { throw 'Invalid environment file entry' }
        $name = $Matches[1]
        $value = $Matches[2].Trim()
        if ($name -notmatch '^(AI_|DB_|JWT_|CORS_|DDL_AUTO$|SERVER_(PORT|ADDRESS)$|SPRING_)') {
            throw "Unsupported environment variable: $name"
        }
        # Values are data, never PowerShell expressions. Never print secrets.
        if ($value.Length -ge 2 -and (
            ($value.StartsWith('"') -and $value.EndsWith('"')) -or
            ($value.StartsWith("'") -and $value.EndsWith("'")))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        [Environment]::SetEnvironmentVariable($name, $value, 'Process')
    }
}
Push-Location $projectRoot
try {
    if ($Packaged) {
        $java = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
        & $java -jar (Join-Path $projectRoot 'target/BookSharing-0.0.1-SNAPSHOT.jar')
    } else { & ./mvnw.cmd spring-boot:run }
    exit $LASTEXITCODE
}
finally { Pop-Location }
