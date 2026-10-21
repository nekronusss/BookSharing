$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$installRoot = Join-Path $projectRoot '.local/ollama'
$binary = Join-Path $installRoot 'bin/ollama.exe'
if (-not (Test-Path -LiteralPath $binary)) { throw 'Run scripts/install-ollama.ps1 first' }
$env:OLLAMA_MODELS = Join-Path $installRoot 'models'
$env:OLLAMA_HOST = '127.0.0.1:11434'
$env:OLLAMA_NUM_PARALLEL = '1'
$env:OLLAMA_MAX_LOADED_MODELS = '1'
# Windows PowerShell 5.1 wraps native stderr logs as errors when redirected.
# Ollama writes normal startup messages there; its exit code remains authoritative.
$ErrorActionPreference = 'Continue'
& $binary serve
exit $LASTEXITCODE
