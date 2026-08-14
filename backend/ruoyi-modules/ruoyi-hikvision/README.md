# ruoyi-hikvision 插拔式模块

海康威视设备网络 SDK 按能力拆成独立 Maven 包。**引入哪个包，就启用哪个功能**；未引入的包不会进 classpath，对应接口与 Bean 不会装配。

## 怎么用

### 只要门禁

```xml
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-hikvision-core</artifactId>
</dependency>
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-hikvision-access</artifactId>
</dependency>
```

`access` 会传递依赖 `config`（ISAPI）和 `core`（设备登录）。

### 只要预览

```xml
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-hikvision-preview</artifactId>
</dependency>
```

会带上 `core` + `media`（ZLMediaKit / JavaCV）。

### 全量

```xml
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>ruoyi-hikvision-starter</artifactId>
</dependency>
```

本仓库的 `ruoyi-admin` 默认使用 starter。

## 模块一览

| Maven 坐标 | 能力 | 依赖 |
|---|---|---|
| `ruoyi-hikvision-core` | 设备登录/会话/心跳重连/JNA | 必选 |
| `ruoyi-hikvision-media` | ZLMediaKit + JavaCV | core |
| `ruoyi-hikvision-preview` | 实时预览 | core + media |
| `ruoyi-hikvision-playback` | 录像回放/下载 | core |
| `ruoyi-hikvision-ptz` | 云台 | core |
| `ruoyi-hikvision-alarm` | 报警布防 + 落库推送 | core |
| `ruoyi-hikvision-audio` | 语音对讲 | core |
| `ruoyi-hikvision-config` | 远程参数 / ISAPI | core |
| `ruoyi-hikvision-access` | **门禁** | core + config |
| `ruoyi-hikvision-face` | 人脸库 | core + config |
| `ruoyi-hikvision-intercom` | 可视对讲 | core + audio + access |
| `ruoyi-hikvision-vehicle` | 道闸/车辆 | core + access |
| `ruoyi-hikvision-elevator` | 梯控 | core + config |
| `ruoyi-hikvision-display` | LED/LCD | core + config |
| `ruoyi-hikvision-starter` | 全部 | 上述全部 |

## 配置开关

即使包已引入，也可用配置关闭：

```yaml
hikvision:
  enabled: true
  features:
    access: true
    preview: false   # 关闭预览（即使引入了 preview 包）
    face: true
```

缺省均为 `true`。总开关 `hikvision.enabled=false` 会关闭全部特性。

## 扩展一个新能力

1. 新建模块 `ruoyi-hikvision-xxx`，依赖 `ruoyi-hikvision-core`；
2. Service / Controller 上加 `@ConditionalOnHikFeature("xxx")`；
3. 在 `HikvisionProperties.Features` 增加对应字段；
4. 按需加入 starter。

通道名称等可选能力通过 SPI（如 `ChannelNameResolver`）注入，core 不强制依赖 ISAPI。

## JNA 接口与版权

- `ruoyi-hikvision-core` 的 `sdk` 包内 `HCNetSDK.java` / `PlayCtrl.java` **来源于海康官方 SDK**，
  由 `script/import-sdk-jna.py` 生成（仅改包名与导入路径），文件头已注明版权归属。
- 原生库（dll/so）不随仓库与 Maven 包分发，使用者通过 `script/download-sdk.*` 自行导入。
- 本项目与海康威视无隶属关系。发布/使用前请阅读根目录 `LEGAL.md` 法律声明。
