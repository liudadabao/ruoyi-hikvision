# RuoYi-Hikvision · 基于 RuoYi-Vue-Plus 的海康威视 SDK 对接平台

[![GitHub stars](https://img.shields.io/github/stars/liudadabao/ruoyi-hikvision)](https://github.com/liudadabao/ruoyi-hikvision/stargazers)
[![build](https://github.com/liudadabao/ruoyi-hikvision/actions/workflows/build.yml/badge.svg)](https://github.com/liudadabao/ruoyi-hikvision/actions/workflows/build.yml)
[![license](https://img.shields.io/badge/license-MIT-green.svg)](./LICENSE)

> GitHub 仓库：<https://github.com/liudadabao/ruoyi-hikvision>

基于 [RuoYi-Vue-Plus](https://gitee.com/dromara/RuoYi-Vue-Plus)（Spring Boot 3 / JDK 21）与
[plus-ui](https://gitee.com/dromara/plus-ui)（Vue 3）构建的海康威视**设备网络 SDK** 统一对接模块，
将海康 SDK 全部核心能力封装为**可复用、易扩展**的服务层，并集成 ZLMediaKit / JavaCV 提供流媒体能力，
让后续接入海康设备与业务功能变得简单快捷。

## ✨ 特性

- **跨平台原生库加载**：支持 Win32 / Win64 / Linux32 / Linux64 / ArmLinux64，运行时自动识别与加载。
- **设备统一管理**：设备/通道落库（含 ISAPI 通道实名同步）、登录会话池、定时心跳、断线自动重连、在线状态。
- **全业务域封装**：实时预览（双路径）、录像回放/下载、云台控制、报警订阅（实时推送+落库）、语音对讲、远程参数配置、ISAPI 透传、门禁、人脸、可视对讲、车辆道闸、梯控、LED/LCD 显示。
- **回调事件化**：SDK 同步回调统一转换为 Spring 事件，业务侧 `@EventListener` 即可订阅。
- **流媒体一体部署**：内置 ZLMediaKit（zlm4j JNA 绑定），设备 RTSP 拉流转 HTTP-FLV / HLS / WebRTC / RTSP 供浏览器播放；支持 SDK 取流 + JavaCV 转推兜底路径。
- **视频处理**：JavaCV / FFmpeg 提供录像转存、视频截图等能力。
- **Docker 一体化部署**：MySQL / Redis / 后端 / 前端 docker compose 一键部署。

## 🧰 技术栈

| 层 | 技术 |
|---|---|
| 后端 | RuoYi-Vue-Plus 6.0 · Spring Boot 3 · JDK 21 · MyBatis-Plus · Sa-Token |
| 前端 | plus-ui 6.X · Vue 3 · Vite · Element Plus |
| 设备 SDK | 海康 HCNetSDK（JNA 封装）· JNA 5.x |
| 流媒体 | ZLMediaKit + [zlm4j](https://gitee.com/aizuda/zlm4j)（C API 内嵌） |
| 视频处理 | JavaCV / FFmpeg |

## 📁 目录结构

```
├── backend/                       # RuoYi-Vue-Plus 后端
│   ├── ruoyi-modules/ruoyi-hikvision/
│   │   └── src/main/java/org/dromara/hikvision/
│   │       ├── sdk/               # JNA 原生接口层（HCNetSDK.java / PlayCtrl.java）
│   │       ├── core/              # 核心封装：设备管理/心跳重连/回调分发/错误码
│   │       ├── feature/           # 各业务域 Service（预览/回放/云台/报警/对讲/门禁/人脸...）
│   │       ├── media/             # ZLMediaKit 集成 + JavaCV 工具
│   │       ├── controller/        # REST API
│   │       ├── domain/            # 实体/VO/BO
│   │       └── config/            # 自动装配
│   └── script/                    # SDK 原生库导入脚本 / SQL
├── frontend/                      # plus-ui 前端（设备/预览/回放/报警/门禁调试）
├── docker/                        # docker compose 一体化部署
└── docs/                          # 接入指南
```

## 🔌 Maven 插拔

每个能力一个包，引入即用：

```xml
<!-- 只要门禁 -->
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-hikvision-access</artifactId>
</dependency>

<!-- 只要预览 -->
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-hikvision-preview</artifactId>
</dependency>

<!-- 全部能力 -->
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-hikvision-starter</artifactId>
</dependency>
```

也可用配置关闭已引入的能力：`hikvision.features.access=false`。详见 [模块说明](backend/ruoyi-modules/ruoyi-hikvision/README.md)。

## 🚀 快速开始

### 1. 准备海康 SDK（原生库 + JNA 接口）

海康 SDK 原生库与 JNA 接口文件均为海康威视版权，**不随仓库分发**。请从
[海康开放平台](https://open.hikvision.com/) 下载对应平台的「设备网络 SDK」后执行：

```powershell
# a) 导入原生库（dll/so，按平台选 win64/linux64/armlinux64...）
powershell -ExecutionPolicy Bypass -File backend/script/download-sdk.ps1 `
    -SdkZip "C:\path\HCNetSDK..._Win64.zip" -Platform win64

# b) 再生成 JNA 接口文件 HCNetSDK.java / PlayCtrl.java（仓库内已有，可选，用于升级 SDK 时重建）
powershell -ExecutionPolicy Bypass -File backend/script/import-sdk-jna.ps1 `
    -SdkZip "C:\path\HCNetSDK..._Win64.zip"
```

> `HCNetSDK.java` / `PlayCtrl.java` 由 `import-sdk-jna` 脚本从官方 SDK 生成（仅改包名与导入路径），
> 文件头已注明版权归属。详见 [LEGAL.md](./LEGAL.md)。

### 2. 启动后端

```bash
cd backend
# 初始化数据库：执行 script/sql/ry_vue.sql 与 script/sql/ry_hikvision.sql
mvn spring-boot:run -pl ruoyi-admin
```

默认配置下，海康模块通过 `META-INF/spring/...AutoConfiguration.imports` 自动装配，
相关配置见 `application.yml` 中的 `hikvision.*` 前缀（原生库目录、心跳、ZLMediaKit 端口等）。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

### 4. 使用

1. 「海康SDK → 设备管理」新增设备（IP/端口/账号/密码）→ 点击「登录」；
2. 登录成功后自动同步设备通道信息；
3. 「实时预览」选择设备+通道开始播放（需 ZLMediaKit 原生库 `mk_api`，见下文）；
4. 「报警记录」查看设备报警事件。

## 📺 ZLMediaKit 内嵌说明

本项目通过 zlm4j（ZLMediaKit C API 的 JNA 绑定）将 ZLMediaKit **内嵌进后端进程**，
实现流媒体与后端一体部署。需将 ZLMediaKit 官方 Release 的 `mk_api` 原生库放入
`hikvision.zlm.native-path`（默认 `./lib/zlm`）目录。

> 若未提供 `mk_api` 原生库，模块仍可正常使用设备管理、云台、报警、门禁等能力，
> 仅实时预览退化为返回设备 RTSP 直连地址。

## 🧩 已实现能力清单

| 分类 | 能力 | 实现方式 |
|---|---|---|
| 设备接入 | 登录/登出/会话池/通道同步/在线状态/心跳/自动重连 | `NET_DVR_Login_V40` 等 |
| 实时预览 | RTSP+ZLMediaKit 拉流转协议 / SDK 取流+JavaCV 转推 | `NET_DVR_RealPlay_V40` |
| 录像回放 | 按时间回放/控制/下载 | `NET_DVR_PlayBackByTime_V40` |
| 云台控制 | 方向/变倍/预置点 | `NET_DVR_PTZControlWithSpeed_Other` |
| 报警订阅 | 布防/撤防/报警侦听 + 落库 + 实时推送 | `NET_DVR_SetupAlarmChan_V41` |
| 语音对讲 | 开始/发送/停止/音量 | `NET_DVR_StartVoiceCom_V30` |
| 参数配置 | 远程参数读写（通用） | `NET_DVR_Get/SetDVRConfig` |
| ISAPI | XML 透传（通用，附前端调试工具） | `NET_DVR_STDXMLConfig` |
| 门禁 | 门控制/卡/事件 | `NET_DVR_RemoteControl` + ISAPI |
| 人脸/车辆/梯控/屏显/对讲 | 查询与控制 | ISAPI 透传 |

## 📚 文档

- [接入指南](docs/guide.md)：如何新增设备/新增业务能力、回调订阅、SDK 升级说明。
- [API 接口](backend/ruoyi-modules/ruoyi-hikvision/README.md)：模块接口清单。

## ⚠️ 法律声明（务必阅读 [LEGAL.md](./LEGAL.md)）

- 本项目**与海康威视无任何隶属关系**，非海康官方项目。
- 本项目自身代码基于 [MIT](./LICENSE) 协议开源（仅覆盖本项目自有代码）。
- 海康威视 SDK 及其原生库、`HCNetSDK.java` / `PlayCtrl.java` 等版权归海康威视所有；
  使用者须从官方渠道获取 SDK 并自行遵守其授权协议，**不要将海康文件作为自有代码分发**。
- 接入真实设备时，请同时遵守当地安防、个人信息与监控录像相关法律法规。

## 🙏 致谢

- [RuoYi-Vue-Plus](https://gitee.com/dromara/RuoYi-Vue-Plus)
- [plus-ui](https://gitee.com/dromara/plus-ui)
- [ZLMediaKit](https://github.com/ZLMediaKit/ZLMediaKit)
- [zlm4j](https://gitee.com/aizuda/zlm4j)
- [JavaCV](https://github.com/bytedeco/javacv)
