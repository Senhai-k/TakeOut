[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$BackupPath,
    [Parameter(Mandatory)][ValidatePattern('^[A-Za-z0-9_]+$')][string]$Database,
    [string]$HostName = '127.0.0.1',
    [ValidateRange(1, 65535)][int]$Port = 3306,
    [ValidatePattern('^[A-Za-z0-9_.-]+$')][string]$Username = 'takeout',
    [switch]$ConfirmRestore
)

$ErrorActionPreference = 'Stop'
if (-not $ConfirmRestore) {
    throw 'Restore refused. Re-run with -ConfirmRestore after verifying the target database and backup file.'
}

$mysqlCommand = Get-Command mysql -ErrorAction Stop
$resolvedBackupPath = (Resolve-Path -LiteralPath $BackupPath -ErrorAction Stop).Path
if ([System.IO.Path]::GetExtension($resolvedBackupPath) -ne '.sql') {
    throw 'BackupPath must reference a .sql file'
}
if ((Get-Item -LiteralPath $resolvedBackupPath).Length -eq 0) {
    throw 'Backup file is empty'
}

$hashPath = "$resolvedBackupPath.sha256"
if (Test-Path -LiteralPath $hashPath) {
    $expectedHash = ((Get-Content -LiteralPath $hashPath -Raw).Trim() -split '\s+')[0]
    $actualHash = (Get-FileHash -LiteralPath $resolvedBackupPath -Algorithm SHA256).Hash
    if (-not $actualHash.Equals($expectedHash, [StringComparison]::OrdinalIgnoreCase)) {
        throw 'Backup SHA256 verification failed'
    }
} else {
    throw "Missing checksum file: $hashPath"
}

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

    $arguments = @("--host=$HostName", "--port=$Port", "--user=$Username", '--default-character-set=utf8mb4', $Database)
    $process = Start-Process -FilePath $mysqlCommand.Source -ArgumentList $arguments -NoNewWindow -Wait -PassThru -RedirectStandardInput $resolvedBackupPath
    if ($process.ExitCode -ne 0) {
        throw "mysql restore failed with exit code $($process.ExitCode)"
    }
    Write-Output "Restore completed: $resolvedBackupPath -> $Database"
} finally {
    if ($null -eq $originalPassword) {
        Remove-Item Env:MYSQL_PWD -ErrorAction SilentlyContinue
    } else {
        $env:MYSQL_PWD = $originalPassword
    }
    $temporaryPassword = $null
}
