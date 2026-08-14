#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
从海康威视官方「设备网络 SDK」压缩包导入 JNA 接口文件 (HCNetSDK.java / PlayCtrl.java)。

用法:
  python import-sdk-jna.py <SDK.zip> [输出目录]

说明:
  HCNetSDK.java / PlayCtrl.java 来源于海康威视官方设备网络 SDK，版权归海康威视所有。
  本脚本仅做包名与导入路径适配（package 改为 org.dromara.hikvision.sdk，
  com.sun.jna.examples.win32.W32API 改为本项目 shim），生成文件用于本项目编译。
  请勿将生成文件作为自有代码二次分发；请遵守海康威视 SDK 授权协议。
"""
import os
import sys
import zipfile

SDK_PACKAGE = "org.dromara.hikvision.sdk"
SHIM_IMPORT = "import org.dromara.hikvision.sdk.win32.W32API;"

HEADER = (
    "// 本文件由 script/import-sdk-jna.py 从海康威视官方「设备网络 SDK」生成。\n"
    "// 源码: 海康威视设备网络 SDK（HCNetSDK.java / PlayCtrl.java），版权归海康威视所有。\n"
    "// 本项目仅做包名与导入路径适配，用于 JNA 调用 HCNetSDK / PlayCtrl 动态库。\n"
    "// 请勿将该文件作为自有代码二次分发；请遵守海康威视 SDK 授权协议。\n"
    "\n"
)


def find_entry_text(zf, suffix, must_contain):
    """在 zip 中查找文件名以 suffix 结尾且内容包含 must_contain 的第一个条目。"""
    for name in zf.namelist():
        if not name.endswith(suffix):
            continue
        try:
            data = zf.read(name)
        except Exception:
            continue
        text = data.decode("utf-8", errors="replace")
        if must_contain in text:
            return text
    return None


def strip_embedded_playctrl(text):
    """移除 HCNetSDK.java 末尾内嵌的顶层 interface PlayCtrl（与独立 PlayCtrl.java 重复）。"""
    lines = text.split("\n")
    out = []
    i = 0
    skip_depth = 0
    while i < len(lines):
        line = lines[i]
        if skip_depth > 0:
            skip_depth += line.count("{") - line.count("}")
            i += 1
            continue
        if line.strip() == "interface PlayCtrl extends Library {":
            skip_depth = line.count("{") - line.count("}")
            i += 1
            continue
        out.append(line)
        i += 1
    return "\n".join(out)


def convert_hcnetsdk(text):
    text = text.replace("package com.NetSDKDemo;", "package %s;" % SDK_PACKAGE)
    text = text.replace(
        "import com.sun.jna.examples.win32.W32API;", SHIM_IMPORT
    )
    text = text.replace(
        "import com.sun.jna.examples.win32.W32API.HWND;",
        "import org.dromara.hikvision.sdk.win32.W32API.HWND;",
    )
    text = text.replace(
        "com.sun.jna.examples.win32.GDI32.RECT",
        "org.dromara.hikvision.sdk.win32.W32API.RECT",
    )
    text = strip_embedded_playctrl(text)
    return text


def convert_playctrl(text):
    text = text.replace("package NetSDKDemo;", "package %s;" % SDK_PACKAGE)
    text = text.replace(
        "import com.sun.jna.examples.win32.W32API;", SHIM_IMPORT
    )
    return text


def main():
    if len(sys.argv) < 2:
        print("用法: python import-sdk-jna.py <SDK.zip> [输出目录]")
        sys.exit(1)
    sdk_zip = sys.argv[1]
    if len(sys.argv) > 2:
        outdir = os.path.abspath(sys.argv[2])
    else:
        outdir = os.path.abspath(
            os.path.join(
                os.path.dirname(os.path.abspath(__file__)),
                "..",
                "ruoyi-modules/ruoyi-hikvision/ruoyi-hikvision-core/"
                "src/main/java/org/dromara/hikvision/sdk",
            )
        )
    if not os.path.isfile(sdk_zip):
        print("错误: SDK 压缩包不存在: %s" % sdk_zip)
        sys.exit(1)
    os.makedirs(outdir, exist_ok=True)

    with zipfile.ZipFile(sdk_zip) as zf:
        hc = find_entry_text(zf, "HCNetSDK.java", "package com.NetSDKDemo;")
        pc = find_entry_text(zf, "PlayCtrl.java", "package NetSDKDemo;")
    if hc is None:
        print("错误: 未在 SDK 包中找到 com.NetSDKDemo.HCNetSDK.java")
        sys.exit(1)
    if pc is None:
        print("错误: 未在 SDK 包中找到 NetSDKDemo.PlayCtrl.java")
        sys.exit(1)

    for fname, text, conv in (
        ("HCNetSDK.java", hc, convert_hcnetsdk),
        ("PlayCtrl.java", pc, convert_playctrl),
    ):
        out = os.path.join(outdir, fname)
        with open(out, "w", encoding="utf-8", newline="\n") as f:
            f.write(HEADER + conv(text))
        print("生成: %s" % out)
    print("完成。请勿将以上文件作为自有代码分发。")


if __name__ == "__main__":
    main()
