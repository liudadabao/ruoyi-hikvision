# 从海康官方「设备网络 SDK」导入 JNA 接口文件（HCNetSDK.java / PlayCtrl.java）
# 用法:
#   powershell -ExecutionPolicy Bypass -File script/import-sdk-jna.ps1 `
#       -SdkZip "C:\path\HCNetSDK..._Win64.zip"
# 需要: Python 3 已安装并在 PATH 中。
param(
    [Parameter(Mandatory = $true)][string]$SdkZip
)
$ErrorActionPreference = "Stop"
$py = Get-Command python -ErrorAction SilentlyContinue
if (-not $py) {
    throw "需要 Python 3: 请先安装 python 并加入 PATH"
}
$script = Join-Path $PSScriptRoot "import-sdk-jna.py"
& python $script $SdkZip
if ($LASTEXITCODE -ne 0) {
    throw "导入失败，请检查 SDK 压缩包内容"
}
