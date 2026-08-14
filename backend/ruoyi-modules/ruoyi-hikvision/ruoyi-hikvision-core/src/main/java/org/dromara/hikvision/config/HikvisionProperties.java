package org.dromara.hikvision.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 海康 SDK 配置属性
 *
 * @author hikvision-sdk
 */
@Data
@ConfigurationProperties(prefix = "hikvision")
public class HikvisionProperties {

    /**
     * 是否启用海康 SDK 模块
     */
    private boolean enabled = true;

    /**
     * SDK 原生库外部目录（优先从该目录加载 dll/so，不存在则从 classpath 解压）
     */
    private String nativePath = "./lib/hikvision";

    /**
     * 是否开启 SDK 内部日志
     */
    private boolean sdkLog = false;

    /**
     * SDK 日志输出目录
     */
    private String sdkLogPath = "./logs/hikvision";

    /**
     * 设备相关配置
     */
    private Device device = new Device();

    /**
     * ZLMediaKit 相关配置
     */
    private Zlm zlm = new Zlm();

    /**
     * 特性开关（仅当对应 Maven 模块在 classpath 上时生效）。
     * 缺省全部为 true：引入哪个包就启用哪个能力，也可在配置中关闭。
     */
    private Features features = new Features();

    @Data
    public static class Device {

        /**
         * 心跳保活间隔（秒）
         */
        private int heartbeatInterval = 30;

        /**
         * 断线后是否自动重连
         */
        private boolean autoReconnect = true;

        /**
         * 重连间隔（秒）
         */
        private int reconnectInterval = 10;

        /**
         * 登录超时（毫秒）
         */
        private int loginTimeout = 10000;
    }

    @Data
    public static class Zlm {

        /**
         * 是否启用 ZLMediaKit 内嵌服务
         */
        private boolean enabled = true;

        /**
         * ZLMediaKit C API 原生库目录（mk_api / libmk_api.so）
         */
        private String nativePath = "./lib/zlm";

        /**
         * 配置 ini 文件路径（可选，不配置则使用默认配置）
         */
        private String configPath;

        /**
         * HTTP 服务端口（用于 HTTP-FLV/HLS 播放）
         */
        private int httpPort = 80;

        /**
         * RTSP 服务端口
         */
        private int rtspPort = 554;

        /**
         * 对外播放地址中的服务地址（默认取本机 IP）
         */
        private String publicHost;

        /**
         * 流媒体线程数
         */
        private int threadNum = 4;

        /**
         * 日志级别 0-4
         */
        private int logLevel = 1;
    }

    @Data
    public static class Features {
        private boolean preview = true;
        private boolean playback = true;
        private boolean ptz = true;
        private boolean alarm = true;
        private boolean audio = true;
        private boolean config = true;
        private boolean access = true;
        private boolean face = true;
        private boolean intercom = true;
        private boolean vehicle = true;
        private boolean elevator = true;
        private boolean display = true;
        private boolean media = true;
    }
}
