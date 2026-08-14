#!/usr/bin/env bash
# 海康 SDK 原生库导入脚本 (Linux/macOS)
# 用法: bash script/download-sdk.sh /path/to/HCNetSDK...zip linux64
# 支持的 Platform: win32 | win64 | linux32 | linux64 | armlinux64

set -e

SDK_ZIP="$1"
PLATFORM="${2:-linux64}"

if [ -z "$SDK_ZIP" ]; then
  echo "用法: $0 <SDK.zip> [platform]"
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT="$(dirname "$SCRIPT_DIR")"
OUTPUT_DIR="$ROOT/ruoyi-modules/ruoyi-hikvision/ruoyi-hikvision-core/src/main/resources/sdk/version/$PLATFORM"

mkdir -p "$OUTPUT_DIR"

echo "提取原生库到: $OUTPUT_DIR"
unzip -j -o "$SDK_ZIP" '*.dll' '*.so' '*.so.*' -d "$OUTPUT_DIR" 2>/dev/null || true

count=$(ls -1 "$OUTPUT_DIR" | wc -l)
echo "完成: 共 $count 个文件"
echo "请确认核心库已存在: libhcnetsdk.so / libPlayCtrl.so / libHCCore.so (或 HCNetSDK.dll)"
