package org.dromara.hikvision.media.zlm;

import com.aizuda.zlm4j.core.ZLMApi;
import com.aizuda.zlm4j.structure.MK_CONFIG;
import com.sun.jna.Native;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hikvision.config.HikvisionProperties;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * ZLMediaKit 内嵌流媒体服务。
 * <p>
 * 通过 zlm4j 的 JNA 绑定加载 ZLMediaKit C API 库（{@code mk_api}），
 * 使流媒体能力与后端进程一体部署。原生库需放入 {@code hikvision.zlm.native-path}，
 * 可从 ZLMediaKit 官方 Release 获取。
 *
 * @author hikvision-sdk
 */
@Slf4j
@Component
@ConditionalOnHikFeature("media")
public class ZlmServer {

    private final HikvisionProperties properties;

    @Getter
    private volatile ZLMApi api;

    /**
     * 是否已启动
     */
    @Getter
    private volatile boolean started = false;

    public ZlmServer(HikvisionProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void start() {
        HikvisionProperties.Zlm zlm = properties.getZlm();
        if (!zlm.isEnabled()) {
            log.info("[zlm] ZLMediaKit 未启用");
            return;
        }
        try {
            initJnaPath(zlm.getNativePath());
            this.api = Native.load("mk_api", ZLMApi.class);

            MK_CONFIG config = new MK_CONFIG();
            config.thread_num = zlm.getThreadNum();
            config.log_level = zlm.getLogLevel();
            config.log_mask = 0;
            config.log_file_path = "./logs/zlm";
            config.log_file_days = 7;
            config.ini_is_path = 1;
            config.ini = zlm.getConfigPath();
            config.ssl_is_path = 1;
            config.ssl = null;
            config.ssl_pwd = null;
            api.mk_env_init(config);

            api.mk_http_server_start((short) zlm.getHttpPort(), 0);
            api.mk_rtsp_server_start((short) zlm.getRtspPort(), 0);
            started = true;
            log.info("[zlm] ZLMediaKit 启动成功, http={}, rtsp={}", zlm.getHttpPort(), zlm.getRtspPort());
        } catch (Throwable e) {
            started = false;
            log.warn("[zlm] ZLMediaKit 启动失败（缺少 mk_api 原生库），预览将退化为 RTSP 直连: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void stop() {
        if (api != null && started) {
            try {
                api.mk_stop_all_server();
            } catch (Throwable e) {
                log.warn("[zlm] ZLMediaKit 停止异常: {}", e.getMessage());
            }
        }
    }

    private void initJnaPath(String nativePath) {
        if (nativePath == null || nativePath.isBlank()) {
            return;
        }
        Path dir = Paths.get(nativePath).toAbsolutePath();
        if (!Files.isDirectory(dir)) {
            return;
        }
        String existing = System.getProperty("jna.library.path", "");
        String path = dir.toString();
        System.setProperty("jna.library.path", existing.isBlank() ? path : path + File.pathSeparator + existing);
    }
}
