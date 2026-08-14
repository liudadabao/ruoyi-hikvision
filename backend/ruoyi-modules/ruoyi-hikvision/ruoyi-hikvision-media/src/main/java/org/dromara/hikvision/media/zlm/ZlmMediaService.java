package org.dromara.hikvision.media.zlm;

import com.aizuda.zlm4j.structure.MK_PROXY_PLAYER;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hikvision.config.HikvisionProperties;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZLMediaKit 流媒体管理服务。
 * <p>
 * 将海康设备的 RTSP 流拉入 ZLMediaKit，并对外提供 HTTP-FLV / HLS / RTSP / WebRTC 播放地址，
 * 供浏览器端播放。
 *
 * @author hikvision-sdk
 */
@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("media")
public class ZlmMediaService {

    private final ZlmServer zlmServer;
    private final HikvisionProperties properties;

    /**
     * 流 ID -> 代理播放器
     */
    private final Map<String, MK_PROXY_PLAYER> proxies = new ConcurrentHashMap<>();

    /**
     * 拉取设备 RTSP 流到 ZLMediaKit。
     *
     * @param app      应用名（一般 live）
     * @param stream   流 ID（一般 deviceId_channelNo）
     * @param rtspUrl  设备 RTSP 地址
     * @param username RTSP 用户名
     * @param password RTSP 密码
     */
    public synchronized void startProxy(String app, String stream, String rtspUrl, String username, String password) {
        if (!zlmServer.isStarted()) {
            return;
        }
        String key = app + "/" + stream;
        MK_PROXY_PLAYER exist = proxies.get(key);
        if (exist != null) {
            return;
        }
        MK_PROXY_PLAYER player = zlmServer.getApi().mk_proxy_player_create("__defaultVhost__", app, stream, 1, 0);
        if (player == null) {
            log.warn("[zlm] 创建代理播放器失败: {}", key);
            return;
        }
        if (username != null && !username.isBlank()) {
            zlmServer.getApi().mk_proxy_player_set_option(player, "rtsp_user", username);
        }
        if (password != null && !password.isBlank()) {
            zlmServer.getApi().mk_proxy_player_set_option(player, "rtsp_pwd", password);
        }
        zlmServer.getApi().mk_proxy_player_play(player, rtspUrl);
        proxies.put(key, player);
        log.info("[zlm] 开始拉流: {} -> {}", rtspUrl, key);
    }

    /**
     * 停止拉流。
     */
    public void stopProxy(String app, String stream) {
        String key = app + "/" + stream;
        MK_PROXY_PLAYER player = proxies.remove(key);
        if (player != null && zlmServer.isStarted()) {
            zlmServer.getApi().mk_proxy_player_release(player);
            log.info("[zlm] 停止拉流: {}", key);
        }
    }

    /**
     * ZLMediaKit 是否可用。
     */
    public boolean isZlmStarted() {
        return zlmServer.isStarted();
    }

    /**
     * 构建播放地址。
     */
    public Map<String, String> buildPlayUrls(String app, String stream) {
        String host = resolveHost();
        int httpPort = properties.getZlm().getHttpPort();
        int rtspPort = properties.getZlm().getRtspPort();
        return Map.of(
            "flv", String.format("http://%s:%d/%s/%s.live.flv", host, httpPort, app, stream),
            "hls", String.format("http://%s:%d/%s/%s/hls.m3u8", host, httpPort, app, stream),
            "rtsp", String.format("rtsp://%s:%d/%s/%s", host, rtspPort, app, stream),
            "webrtc", String.format("http://%s:%d/index/api/webrtc?app=%s&stream=%s&type=play", host, httpPort, app, stream)
        );
    }

    private String resolveHost() {
        String publicHost = properties.getZlm().getPublicHost();
        if (publicHost != null && !publicHost.isBlank()) {
            return publicHost;
        }
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}
