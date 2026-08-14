#!/usr/bin/env bash
# 从海康官方「设备网络 SDK」导入 JNA 接口文件（HCNetSDK.java / PlayCtrl.java）
# 用法: bash script/import-sdk-jna.sh /path/to/HCNetSDK....zip
set -e

if [ -z "$1" ]; then
  echo "用法: $0 <SDK.zip>"
  exit 1
fi

PYTHON="$(command -v python3 || command -v python)"
if [ -z "$PYTHON" ]; then
  echo "错误: 需要 Python 3"
  exit 1
fi

"$PYTHON" "$(dirname "$0")/import-sdk-jna.py" "$1"
