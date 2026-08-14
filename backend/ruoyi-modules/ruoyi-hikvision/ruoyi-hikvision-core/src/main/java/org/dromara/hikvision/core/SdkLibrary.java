package org.dromara.hikvision.core;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hikvision.config.HikvisionProperties;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.sdk.PlayCtrl;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * SDK 生命周期管理器。
 * <p>
 * 负责 SDK 初始化（{@code NET_DVR_Init}）、全局异常回调注册与清理
 * （{@code NET_DVR_Cleanup}），并统一提供 {@link HCNetSDK} / {@link PlayCtrl} 实例。
 *
 * @author hikvision-sdk
 */
@Slf4j
@Component
public class SdkLibrary {

    private final NativeLibraryLoader nativeLibraryLoader;
    private final CallbackDispatcher callbackDispatcher;
    private final HikvisionProperties properties;

    @Getter
    private volatile HCNetSDK hcNetSDK;

    @Getter
    private volatile PlayCtrl playCtrl;

    /**
     * SDK 是否已初始化
     */
    @Getter
    private volatile boolean initialized = false;

    public SdkLibrary(NativeLibraryLoader nativeLibraryLoader,
                      CallbackDispatcher callbackDispatcher,
                      HikvisionProperties properties) {
        this.nativeLibraryLoader = nativeLibraryLoader;
        this.callbackDispatcher = callbackDispatcher;
        this.properties = properties;
    }

    /**
     * 初始化 SDK。
     */
    @PostConstruct
    public synchronized void init() {
        if (initialized) {
            return;
        }
        if (!properties.isEnabled()) {
            log.info("[hikvision] 海康 SDK 模块未启用");
            return;
        }
        try {
            this.hcNetSDK = nativeLibraryLoader.loadHCNetSDK();
            this.playCtrl = nativeLibraryLoader.loadPlayCtrl();

            String sdkPath = nativeLibraryLoader.getNativeDir().toString();
            setSdkInitCfg(hcNetSDK.NET_SDK_INIT_CFG_SDK_PATH, sdkPath);

            // 连接超时与断线重连
            hcNetSDK.NET_DVR_SetConnectTime(5000, 1);
            hcNetSDK.NET_DVR_SetReconnect(properties.getDevice().getReconnectInterval() * 1000, true);

            if (!hcNetSDK.NET_DVR_Init()) {
                throw new IllegalStateException("[hikvision] NET_DVR_Init 失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
            }

            // 注册全局异常回调
            hcNetSDK.NET_DVR_SetExceptionCallBack_V30(0, 0, callbackDispatcher.getExceptionCallBack(), null);

            // SDK 内部日志
            if (properties.isSdkLog()) {
                hcNetSDK.NET_DVR_SetLogToFile(1, properties.getSdkLogPath(), false);
            }

            initialized = true;
            log.info("[hikvision] 海康 SDK 初始化成功，平台: {}, 原生库目录: {}", nativeLibraryLoader.getPlatform().getDir(), sdkPath);
        } catch (Throwable e) {
            initialized = false;
            log.warn("[hikvision] 海康 SDK 初始化失败（缺少原生库或环境不支持），模块功能将不可用: {}", e.getMessage());
        }
    }

    /**
     * 设置 SDK 初始化配置（如组件库路径）。
     */
    private void setSdkInitCfg(int cfgType, String value) {
        HCNetSDK.NET_DVR_LOCAL_SDK_PATH path = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
        byte[] bytes = value.getBytes(Charset.forName("GBK"));
        System.arraycopy(bytes, 0, path.sPath, 0, Math.min(bytes.length, path.sPath.length - 1));
        hcNetSDK.NET_DVR_SetSDKInitCfg(cfgType, path.getPointer());
        path.read();
    }

    /**
     * 释放 SDK 资源。
     */
    @PreDestroy
    public synchronized void destroy() {
        if (!initialized) {
            return;
        }
        try {
            hcNetSDK.NET_DVR_Cleanup();
        } catch (Throwable e) {
            log.warn("[hikvision] SDK 清理异常: {}", e.getMessage());
        }
        initialized = false;
        log.info("[hikvision] 海康 SDK 已清理");
    }
}
