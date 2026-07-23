[CmdletBinding()]
param(
    [string]$HostName = '127.0.0.1',
    [ValidateRange(1, 65535)][int]$Port = 3306,
    [ValidatePattern('^[A-Za-z0-9_]+$')][string]$Database = 'takeout',
    [ValidatePattern('^[A-Za-z0-9_.-]+$')][string]$Username = 'takeout',
    [string]$OutputDirectory = (Join-Path $PSScriptRoot '..\backups\mysql')
)

$ErrorActionPreference = 'Stop'
$dumpCommand = Get-Command mysqldump -ErrorAction Stop
$resolvedOutputDirectory = [System.IO.Path]::GetFullPath($OutputDirectory)
[System.IO.Directory]::CreateDirectory($resolvedOutputDirectory) | Out-Null

$timestamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$backupPath = Join-Path $resolvedOutputDirectory "$Database-$timestamp.sql"
$hashPath = "$backupPath.sha256"
$originalPassword = $env:MYSQL_PWD
$temporaryPassword = $null

try {
    if ([string]::IsNullOrWhiteSpace($env:MYSQL_PWD)) {
        $securePassword = Read-Host 'MySQL password' -AsSecureString
        $passwordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
        try {
            $temporaryPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($passwordPointer)
        } finally {
            [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($passwordPointer)
        }
        $env:MYSQL_PWD = $temporaryPassword
    }

    $arguments = @(
        "--host=$HostName",
        "--port=$Port",
        "--user=$Username",
        '--single-transaction',
        '--routines',
        '--triggers',
        '--events',
        '--set-gtid-purged=OFF',
        '--default-character-set=utf8mb4',
        $Database
    )
    $process = Start-Process -FilePath $dumpCommand.Source -ArgumentList $arguments -NoNewWindow -Wait -PassThru -RedirectStandardOutput $backupPath
    if ($process.ExitCode -ne 0) {
        throw "mysqldump failed with exit code $($process.ExitCode)"
    }
    if ((Get-Item -LiteralPath $backupPath).Length -eq 0) {
        throw 'mysqldump created an empty backup file'
    }

    $hash = (Get-FileHash -LiteralPath $backupPath -Algorithm SHA256).Hash.ToLowerInvariant()
    Set-Content -LiteralPath $hashPath -Value "$hash  $([System.IO.Path]::GetFileName($backupPath))" -Encoding ASCII
    Write-Output "Backup: $backupPath"
    Write-Output "SHA256: $hash"
} catch {
    Remove-Item -LiteralPath $backupPath, $hashPath -Force -ErrorAction SilentlyContinue
    throw
} finally {
    if ($null -eq $originalPassword) {
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    } else {
        $env:MYSQL_PWD = $originalPassword
    }
    $temporaryPassword = $null
}
