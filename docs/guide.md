# 海康 SDK 接入指南

本文档面向希望在本项目中**接入海康设备**、**新增业务能力**或**升级 SDK** 的开发者。

## 零、按需引入 Maven 包

不要把所有能力打进一个 fat 包。业务系统按需依赖：

| 需求 | 引入 |
|---|---|
| 设备登录（必选底座） | `ruoyi-hikvision-core`（多数特性会传递依赖） |
| 门禁开门/卡/事件 | `ruoyi-hikvision-access` |
| 实时预览 | `ruoyi-hikvision-preview` |
| 录像回放 | `ruoyi-hikvision-playback` |
| 云台 | `ruoyi-hikvision-ptz` |
| 报警 | `ruoyi-hikvision-alarm` |
| 全量 | `ruoyi-hikvision-starter` |

本仓库 `ruoyi-admin` 使用 starter。你自己的工程可以只加 access。

## 一、接入一台新设备

1. 准备设备信息：IP、端口（默认 8000）、登录账号/密码（建议使用普通用户）。
2. 在「设备管理」页面新增设备，或在代码中：

```java
HikDeviceBo bo = new HikDeviceBo();
bo.setDeviceName("园区大门");
bo.setDeviceIp("192.168.1.64");
bo.setPort(8000);
bo.setUsername("admin");
bo.setPassword("123456");
hikDeviceService.insertDevice(bo);
```

3. 登录设备并同步通道：

```java
hikDeviceService.loginDevice(deviceId);  // 内部调用 NET_DVR_Login_V40 + 通道同步
```

4. 后续所有业务通过 `deviceId` 即可，无需再关心 `userId` 会话：

```java
previewService.preview(deviceId, channelNo, streamType);
ptzService.ptzControl(deviceId, channelNo, HCNetSDK.TILT_UP, 0, 5);
alarmService.setupAlarm(deviceId);
```

## 二、新增业务能力（以「云台巡航」为例）

1. 在 `feature/ptz/` 下为 `PtzService` 增加方法：

```java
public void cruise(Long deviceId, int channel, int cruiseRoute) {
    DeviceSession session = deviceManager.requireSession(deviceId);
    boolean ok = sdkLibrary.getHcNetSDK()
        .NET_DVR_PTZCruise_Other(session.getUserId(), channel, HCNetSDK.RUN_CRUISE, cruiseRoute, 0, 0);
    if (!ok) {
        throw new ServiceException("巡航失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
    }
}
```

2. 在 `controller/` 下暴露接口，或直接在业务代码中注入 `PtzService` 使用。

> 核心原则：底层 JNA 全部封装在 `core` 与 `feature` 内，业务侧只依赖 Service。

## 三、订阅设备事件

```java
@Component
@RequiredArgsConstructor
public class DemoAlarmListener {

    private final HikAlarmRecordMapper mapper;

    @EventListener
    public void onAlarm(AlarmEvent event) {
        // 收到报警，可落库/推送/联动
    }

    @EventListener
    public void onException(DeviceExceptionEvent event) {
        // 设备离线/硬盘异常等
    }
}
```

> 报警事件默认已自动落库，并通过 RuoYi 统一消息推送（SSE/WebSocket）实时推送前端。

## 四、设备保活与自动重连

- `DeviceStatusMonitor` 按 `hikvision.device.heartbeat-interval`（默认 30 秒）对在线会话做轻量心跳探测；
- 心跳失败或收到 SDK 异常回调（网络断开等）时，自动登出、更新数据库状态为 offline，
  并按 `hikvision.device.auto-reconnect` / `reconnect-interval` 自动重连（最多 3 次）。

## 五、实时预览双路径

1. **RTSP + ZLMediaKit 拉流（推荐）**：设备 RTSP 端口可达时使用，前端直接播放 HTTP-FLV/HLS；
2. **SDK 取流 + JavaCV 转推（兜底）**：设备仅 SDK 私有协议可达时，`/hikvision/preview/startSdkPush/{deviceId}`
   通过 SDK `NET_DVR_RealPlay_V40` 取 PS 码流，JavaCV/FFmpeg 解复用后 RTSP 推给本地 ZLMediaKit 转协议。

## 六、SDK 升级

1. 从海康开放平台下载新版「设备网络 SDK」。
2. 重新执行 `script/download-sdk.ps1`（或 `.sh`）导入对应平台原生库。
3. 用新版 SDK 中的 `HCNetSDK.java` 重新生成：
   运行 `python script/import-sdk-jna.py <新版SDK.zip>`（会自动改包名并移除内嵌 PlayCtrl），
   输出到 `ruoyi-modules/ruoyi-hikvision/ruoyi-hikvision-core/src/main/java/org/dromara/hikvision/sdk/`。
4. 如 SDK 接口有变动，同步调整 `core` 与 `feature` 层引用。

## 七、部署说明
### 原生库路径

- 开发/测试：将原生库放入
  `ruoyi-modules/ruoyi-hikvision/ruoyi-hikvision-core/src/main/resources/sdk/version/{platform}/`（随 jar 打包），
  或外部目录 `hikvision.sdk.native-path/{platform}/`（默认 `./lib/hikvision/{platform}`）。
- 生产（Linux 服务器）：建议使用外部目录，便于升级，避免超大 jar：

```yaml
hikvision:
  native-path: /data/hikvision/sdk
```

### 数据库

执行 `backend/script/sql/ry_hikvision.sql`（依赖 `ry_vue.sql` 已初始化）。

### ZLMediaKit

将 `mk_api` 原生库放入 `hikvision.zlm.native-path`（默认 `./lib/zlm`），
`mk_api` 可从 [ZLMediaKit Release](https://github.com/ZLMediaKit/ZLMediaKit/releases) 获取。

### 跨平台

| 部署目标 | 原生库平台 | 说明 |
|---|---|---|
| Windows x64 | win64 | 默认开发环境 |
| Linux amd64 | linux64 | 常见生产服务器 |
| Linux arm64 | armlinux64 | 鲲鹏/飞腾等国产化服务器 |
| Windows x86 / Linux x86 | win32 / linux32 | 32 位环境（已少见） |
