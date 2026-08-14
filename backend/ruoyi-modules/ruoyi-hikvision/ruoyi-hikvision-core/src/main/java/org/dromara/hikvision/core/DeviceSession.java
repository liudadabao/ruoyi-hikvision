package org.dromara.hikvision.core;

import lombok.Getter;
import lombok.Setter;
import org.dromara.hikvision.sdk.HCNetSDK;

import java.time.LocalDateTime;

/**
 * 设备会话，代表一个已登录的海康设备。
 *
 * @author hikvision-sdk
 */
@Getter
public class DeviceSession {

    /**
     * 设备主键（数据库 ID）
     */
    private final Long deviceId;

    /**
     * 设备 IP
     */
    private final String ip;

    /**
     * 端口
     */
    private final int port;

    /**
     * SDK 登录返回的用户 ID（-1 表示未登录）
     */
    @Setter
    private volatile int userId = -1;

    /**
     * 设备信息（登录时返回）
     */
    @Setter
    private HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo;

    /**
     * 登录时间
     */
    @Setter
    private LocalDateTime loginTime;

    /**
     * 是否在线
     */
    @Setter
    private volatile boolean online = false;

    public DeviceSession(Long deviceId, String ip, int port) {
        this.deviceId = deviceId;
        this.ip = ip;
        this.port = port;
    }

    /**
     * 是否已登录
     */
    public boolean isLoggedIn() {
        return userId >= 0 && online;
    }
}
