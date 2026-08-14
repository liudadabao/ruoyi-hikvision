# 原生库目录说明

此目录存放海康设备网络 SDK 的原生库（按平台分类），供 `NativeLibraryLoader` 在运行时加载。
原生库为海康威视版权，**不随本仓库分发**，请勿提交到公开仓库（已在 `.gitignore` 忽略）。

## 目录结构

```
sdk/version/
├── win32/        # Windows 32位  (HCNetSDK.dll, PlayCtrl.dll, HCCore.dll ...)
├── win64/        # Windows 64位
├── linux32/      # Linux 32位    (libhcnetsdk.so, libPlayCtrl.so, libHCCore.so ...)
├── linux64/      # Linux 64位
└── armlinux64/   # ARM Linux 64位
```

## 如何获取原生库

从海康开放平台下载对应平台的「设备网络 SDK」压缩包，然后执行导入脚本：

```powershell
# Windows
powershell -ExecutionPolicy Bypass -File script/download-sdk.ps1 `
    -SdkZip "C:\path\HCNetSDK..._Win64.zip" -Platform win64
```

```bash
# Linux
bash script/download-sdk.sh /path/to/HCNetSDK...linux64.zip linux64
```

## JNA 接口文件

`HCNetSDK.java` / `PlayCtrl.java`（位于模块 `sdk/` 包）同样来源于海康官方 SDK，由
`script/import-sdk-jna.py` 生成（仅改包名与导入路径）。升级 SDK 时可重新生成：

```powershell
powershell -ExecutionPolicy Bypass -File script/import-sdk-jna.ps1 `
    -SdkZip "C:\path\HCNetSDK..._Win64.zip"
```

详见仓库根目录 `LEGAL.md` 法律声明。

## 运行时加载优先级

1. 外部目录 `hikvision.sdk.native-path/{platform}`（可通过配置指定，便于升级）；
2. classpath 资源 `sdk/version/{platform}/`（随 jar 打包分发）。
