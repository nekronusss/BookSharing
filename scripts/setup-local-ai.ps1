param([string]$PostgresBin = 'C:/Program Files/PostgreSQL/18/bin')
$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path $PSScriptRoot -Parent
$localRoot = Join-Path $projectRoot '.local'
$dbRoot = Join-Path $localRoot 'postgres'
$envFile = Join-Path $projectRoot '.env'
if ((Test-Path -LiteralPath $envFile) -or (Test-Path -LiteralPath $dbRoot)) {
    throw 'Local configuration already exists. Use it rather than overwriting the database or .env.'
}
if (-not (Test-Path -LiteralPath (Join-Path $PostgresBin 'initdb.exe'))) { throw 'PostgreSQL binaries not found' }
New-Item -ItemType Directory -Force -Path $localRoot | Out-Null
function New-LocalSecret {
    $bytes = New-Object byte[] 48
    $generator = [System.Security.Cryptography.RandomNumberGenerator]::Create()
    try { $generator.GetBytes($bytes); return [Convert]::ToBase64String($bytes) }
    finally { $generator.Dispose() }
}
$dbPassword = New-LocalSecret
$jwtSecret = New-LocalSecret
$passwordFile = Join-Path $localRoot 'postgres-init-password'
[IO.File]::WriteAllText($passwordFile, $dbPassword, (New-Object Text.UTF8Encoding($false)))
try {
    & (Join-Path $PostgresBin 'initdb.exe') -D $dbRoot -U booksharing_local --auth=scram-sha-256 --encoding=UTF8 --locale=C --pwfile=$passwordFile
    if ($LASTEXITCODE -ne 0) { throw 'Local PostgreSQL initialization failed' }
} finally { Remove-Item -LiteralPath $passwordFile -ErrorAction SilentlyContinue }
& (Join-Path $PostgresBin 'pg_ctl.exe') -D $dbRoot -l (Join-Path $localRoot 'postgres.log') -o '-h 127.0.0.1 -p 55440' -w start
if ($LASTEXITCODE -ne 0) { throw 'Local PostgreSQL startup failed' }
$previousPassword = $env:PGPASSWORD
try {
    $env:PGPASSWORD = $dbPassword
    & (Join-Path $PostgresBin 'createdb.exe') -h 127.0.0.1 -p 55440 -U booksharing_local booksharing_ai_local
    if ($LASTEXITCODE -ne 0) { throw 'Local database creation failed' }
} finally { $env:PGPASSWORD = $previousPassword }
$config = @(
    'DB_URL=jdbc:postgresql://127.0.0.1:55440/booksharing_ai_local',
    'DB_USERNAME=booksharing_local',
    "DB_PASSWORD=$dbPassword",
    "JWT_SECRET=$jwtSecret",
    'DDL_AUTO=update',
    'SERVER_ADDRESS=127.0.0.1',
    'CORS_ALLOWED_ORIGINS=http://localhost:4200,http://127.0.0.1:4200',
    'AI_ENABLED=true',
    'AI_PROVIDER=ollama',
    'AI_BASE_URL=http://127.0.0.1:11434',
    'AI_API_KEY=',
    'AI_CHAT_MODEL=qwen2.5:7b',
    'AI_EMBEDDING_MODEL=bge-m3',
    'AI_TIMEOUT_SECONDS=120'
) -join [Environment]::NewLine
[IO.File]::WriteAllText($envFile, $config + [Environment]::NewLine, (New-Object Text.UTF8Encoding($false)))
Write-Output 'Local PostgreSQL is ready on 127.0.0.1:55440. Backend configuration saved to .env without printing secrets.'
