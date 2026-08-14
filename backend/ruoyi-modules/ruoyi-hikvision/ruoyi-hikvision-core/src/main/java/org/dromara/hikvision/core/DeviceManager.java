package org.dromara.hikvision.core;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备管理器。
 * <p>
 * 负责设备登录/登出与在线会话管理（线程安全），登录成功后的 {@link DeviceSession}
 * 可供预览、回放、云台、对讲、门禁等业务直接复用 {@code userId}。
 *
 * @author hikvision-sdk
 */
@Slf4j
@Component
public class DeviceManager {

    private final SdkLibrary sdkLibrary;
    private final Map<Long, DeviceSession> sessions = new ConcurrentHashMap<>();

    public DeviceManager(SdkLibrary sdkLibrary) {
        this.sdkLibrary = sdkLibrary;
    }

    /**
     * 登录设备。
     *
     * @param deviceId 设备主键
     * @param ip       设备 IP
     * @param port     端口
     * @param username 用户名
     * @param password 密码
     * @return 设备会话
     */
    public DeviceSession login(Long deviceId, String ip, int port, String username, String password) {
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        if (hcNetSDK == null || !sdkLibrary.isInitialized()) {
            throw new ServiceException("海康 SDK 未初始化，请先配置原生库");
        }

        // 已登录则先登出，避免重复登录
        DeviceSession exist = sessions.get(deviceId);
        if (exist != null && exist.isLoggedIn()) {
            logout(deviceId);
        }

        HCNetSDK.NET_DVR_USER_LOGIN_INFO loginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();
        fillBytes(loginInfo.sDeviceAddress, ip);
        loginInfo.wPort = (short) port;
        fillBytes(loginInfo.sUserName, username);
        fillBytes(loginInfo.sPassword, password);
        loginInfo.bUseAsynLogin = false;
        loginInfo.byLoginMode = 0; // 0-Private 私有协议登录
        loginInfo.write();

        HCNetSDK.NET_DVR_DEVICEINFO_V40 deviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        int userId = hcNetSDK.NET_DVR_Login_V40(loginInfo, deviceInfo);
        if (userId < 0) {
            int errCode = hcNetSDK.NET_DVR_GetLastError();
            throw new ServiceException("设备登录失败: " + HikErrorCode.message(errCode));
        }
        deviceInfo.read();

        DeviceSession session = new DeviceSession(deviceId, ip, port);
        session.setUserId(userId);
        session.setDeviceInfo(deviceInfo);
        session.setLoginTime(java.time.LocalDateTime.now());
        session.setOnline(true);
        sessions.put(deviceId, session);
        log.info("[hikvision] 设备登录成功: {} ({}) userId={}", deviceId, ip, userId);
        return session;
    }

    /**
     * 登出设备。
     */
    public void logout(Long deviceId) {
        DeviceSession session = sessions.remove(deviceId);
        if (session == null) {
            return;
        }
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        if (hcNetSDK != null && session.getUserId() >= 0) {
            hcNetSDK.NET_DVR_Logout_V30(session.getUserId());
        }
        session.setOnline(false);
        session.setUserId(-1);
        log.info("[hikvision] 设备已登出: {} ({})", deviceId, session.getIp());
    }

    /**
     * 获取设备会话。
     */
    public DeviceSession getSession(Long deviceId) {
        return sessions.get(deviceId);
    }

    /**
     * 获取已登录的会话，未登录抛出异常。
     */
    public DeviceSession requireSession(Long deviceId) {
        DeviceSession session = sessions.get(deviceId);
        if (session == null || !session.isLoggedIn()) {
            throw new ServiceException("设备未登录或不在线: " + deviceId);
        }
        return session;
    }

    /**
     * 判断设备是否在线。
     */
    public boolean isOnline(Long deviceId) {
        DeviceSession session = sessions.get(deviceId);
        return session != null && session.isOnline();
    }

    /**
     * 获取所有会话。
     */
    public Collection<DeviceSession> getAllSessions() {
        return sessions.values();
    }

    /**
     * 关闭所有设备会话（应用停止时调用）。
     */
    public void logoutAll() {
        sessions.keySet().forEach(this::logout);
    }

    /**
     * 将字符串写入 SDK 定长字节数组（GBK 编码，空余补 0）。
     */
    private void fillBytes(byte[] dest, String value) {
        if (ObjectUtil.isEmpty(value)) {
            return;
        }
        byte[] bytes = value.getBytes(Charset.forName("GBK"));
        System.arraycopy(bytes, 0, dest, 0, Math.min(bytes.length, dest.length - 1));
    }

    /**
     * 读取 SDK 定长字节数组为字符串。
     */
    public static String readString(byte[] data) {
        if (data == null) {
            return null;
        }
        int len = 0;
        while (len < data.length && data[len] != 0) {
            len++;
        }
        return new String(data, 0, len, StandardCharsets.UTF_8);
    }
}
