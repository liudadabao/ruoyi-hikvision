# 海康 SDK 原生库导入脚本 (Windows PowerShell)
# 从海康官方设备网络 SDK 压缩包中提取原生库，放置到模块 resources 下，
# 使运行时 NativeLibraryLoader 可加载 (src/main/resources/sdk/version/{platform}/)。
#
# 用法:
#   powershell -ExecutionPolicy Bypass -File script/download-sdk.ps1 `
#       -SdkZip "C:\path\HCNetSDKV6.1.11.30_build20260805_linux64.zip" -Platform linux64
#
# 支持的 Platform: win32 | win64 | linux32 | linux64 | armlinux64
#
# 注意: 海康 SDK 原生库为海康威视版权所有，请勿将其提交到公开仓库。
#      本脚本输出的 sdk/version 目录已在 .gitignore 中忽略。

param(
    [Parameter(Mandatory = $true)][string]$SdkZip,
    [Parameter(Mandatory = $true)][ValidateSet("win32", "win64", "linux32", "linux64", "armlinux64")]
    [string]$Platform
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$outputDir = Join-Path $root "ruoyi-modules\ruoyi-hikvision\ruoyi-hikvision-core\src\main\resources\sdk\version\$Platform"

if (-not (Test-Path -LiteralPath $SdkZip)) {
    throw "SDK 压缩包不存在: $SdkZip"
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead((Resolve-Path -LiteralPath $SdkZip).Path)

New-Item -ItemType Directory -Force -Path $outputDir | Out-Null

$count = 0
foreach ($entry in $zip.Entries) {
    if ($entry.FullName -match '\.(dll|so)(\.[0-9]+)*$') {
        $name = $entry.FullName -replace '.*/', ''
        if ($name -match '\.(dll|so)$' -or $name -match '\.so\.') {
            $dest = Join-Path $outputDir $name
            [System.IO.Compression.ZipFileExtensions]::ExtractToFile($entry, $dest, $true)
            Write-Host "  提取: $name"
            $count++
        }
    }
}
$zip.Dispose()

Write-Host ""
Write-Host "完成: 共提取 $count 个原生库文件到 $outputDir"
Write-Host "请确认以下核心库已存在:"
Write-Host "  Windows: HCNetSDK.dll / PlayCtrl.dll / HCCore.dll"
Write-Host "  Linux  : libhcnetsdk.so / libPlayCtrl.so / libHCCore.so"
