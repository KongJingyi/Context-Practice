# 将 Gerrit 三仓库同步到 GitHub monorepo
# 用法：在 PowerShell 中执行 .\sync-from-gerrit.ps1

$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$exclude = @('.git', 'target', 'node_modules', 'dist', 'dist-admin', '.idea', 'data')

function Sync-Repo {
    param([string]$Src, [string]$Dest)
    New-Item -ItemType Directory -Path $Dest -Force | Out-Null
    robocopy $Src $Dest /E /XD $exclude /NFL /NDL /NJH /NJS /nc /ns /np | Out-Null
    if ($LASTEXITCODE -ge 8) { throw "robocopy failed: $Src -> $Dest (exit $LASTEXITCODE)" }
}

Sync-Repo "$Root\..\ContextPractice-backend" "$Root\backend"
Sync-Repo "$Root\..\ContextPractice-frontend" "$Root\frontend"
Sync-Repo "$Root\..\ContextPractice-coach" "$Root\coach"

Copy-Item "$Root\backend\docs\项目运行说明书.md" "$Root\项目运行说明书.md" -Force

# GitHub 备份勿提交本地数据库密码
$devYml = "$Root\backend\src\main\resources\application-dev.yml"
(Get-Content $devYml -Raw) `
    -replace 'username: RF1', 'username: ${MYSQL_USERNAME:root}' `
    -replace 'password: 123456', 'password: ${MYSQL_PASSWORD:}' |
    Set-Content $devYml -NoNewline

Write-Host "Synced backend / frontend / coach -> Context-Practice"
Write-Host "Next: cd Context-Practice; git status; git add .; git commit; git push origin main"
