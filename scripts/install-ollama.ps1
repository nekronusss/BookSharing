$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$installRoot = Join-Path $projectRoot '.local/ollama'
$archive = Join-Path $installRoot 'ollama-windows-amd64.zip'
$binaryRoot = Join-Path $installRoot 'bin'
$version = 'v0.40.6'
$expectedHash = 'e359637575302f288d76c0007c408a4be9c7376d59c4f28684d63b232a44b427'
New-Item -ItemType Directory -Force -Path $installRoot | Out-Null
if (-not (Test-Path -LiteralPath $archive)) {
    & curl.exe --fail --location --retry 3 --output $archive "https://github.com/ollama/ollama/releases/download/$version/ollama-windows-amd64.zip"
    if ($LASTEXITCODE -ne 0) { throw 'Ollama download failed' }
}
if ((Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant() -ne $expectedHash) {
    throw 'Ollama archive checksum mismatch; do not execute this archive'
}
New-Item -ItemType Directory -Force -Path $binaryRoot | Out-Null
& tar.exe -xf $archive -C $binaryRoot
if ($LASTEXITCODE -ne 0) { throw 'Ollama extraction failed' }
Write-Output "Ollama $version installed in $binaryRoot"
Write-Output 'Start it with scripts/start-ollama.ps1, then pull bge-m3 and qwen2.5:7b.'
