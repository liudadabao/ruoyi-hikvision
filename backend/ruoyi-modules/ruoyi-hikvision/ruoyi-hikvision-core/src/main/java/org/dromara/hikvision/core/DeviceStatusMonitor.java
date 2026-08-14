package org.dromara.hikvision.core;

import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hikvision.config.HikvisionProperties;
import org.dromara.hikvision.core.event.DeviceExceptionEvent;
import org.dromara.hikvision.domain.HikDevice;
import org.dromara.hikvision.mapper.HikDeviceMapper;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 设备状态监控。
 * <p>
 * 定时心跳检测设备在线状态，设备异常（网络断开等）时自动重连，
 * 并将状态同步到数据库。
 *
 * @author hikvision-sdk
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class DeviceStatusMonitor {

    private final SdkLibrary sdkLibrary;
    private final DeviceManager deviceManager;
    private final HikDeviceMapper deviceMapper;
    private final HikvisionProperties properties;

    /**
     * 心跳检测（默认 30 秒一次）。
     */
    @Scheduled(fixedDelayString = "${hikvision.device.heartbeat-interval:30}000")
    public void heartbeat() {
        if (!sdkLibrary.isInitialized()) {
            return;
        }
        Collection<DeviceSession> sessions = deviceManager.getAllSessions();
        for (DeviceSession session : sessions) {
            if (!session.isLoggedIn()) {
                continue;
            }
            if (!isAlive(session.getUserId())) {
                log.warn("[hikvision] 设备心跳失败，标记离线: {} ({})", session.getDeviceId(), session.getIp());
                handleOffline(session.getDeviceId());
            }
        }
    }

    /**
     * 设备异常事件（SDK 异常回调触发）。
     */
    @EventListener
    public void onDeviceException(DeviceExceptionEvent event) {
        log.warn("[hikvision] 设备异常: type={}, userId={}", event.getExceptionType(), event.getUserId());
        // 3-网络断开 4-IP冲突 5-非法访问 等
        Long deviceId = findDeviceId(event.getUserId());
        if (deviceId != null) {
            handleOffline(deviceId);
        }
    }

    /**
     * 检测会话是否存活（取设备时间配置做轻量探测）。
     */
    private boolean isAlive(int userId) {
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        Memory buffer = new Memory(64);
        IntByReference returned = new IntByReference();
        return hcNetSDK.NET_DVR_GetDVRConfig(userId, HCNetSDK.NET_DVR_GET_TIMECFG, 0, buffer, 64, returned);
    }

    /**
     * 处理设备离线：更新状态并按需自动重连。
     */
    private void handleOffline(Long deviceId) {
        deviceManager.logout(deviceId);
        updateStatus(deviceId, "offline");
        if (properties.getDevice().isAutoReconnect()) {
            tryReconnect(deviceId, 0);
        }
    }

    /**
     * 尝试自动重连（最多 3 次，间隔取配置）。
     */
    private void tryReconnect(Long deviceId, int retry) {
        if (retry >= 3) {
            log.warn("[hikvision] 设备重连失败次数达到上限: {}", deviceId);
            return;
        }
        HikDevice device = deviceMapper.selectById(deviceId);
        if (device == null) {
            return;
        }
        try {
            Thread.sleep(properties.getDevice().getReconnectInterval() * 1000L);
            deviceManager.login(deviceId, device.getDeviceIp(), device.getPort(), device.getUsername(), device.getPassword());
            updateStatus(deviceId, "online");
            log.info("[hikvision] 设备重连成功: {}", deviceId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.warn("[hikvision] 设备重连失败({}/3): {}, {}", retry + 1, deviceId, e.getMessage());
            tryReconnect(deviceId, retry + 1);
        }
    }

    private void updateStatus(Long deviceId, String status) {
        deviceMapper.lambdaUpdate()
            .eq(HikDevice::getDeviceId, deviceId)
            .set(HikDevice::getStatus, status)
            .update();
    }

    private Long findDeviceId(int userId) {
        for (DeviceSession session : deviceManager.getAllSessions()) {
            if (session.getUserId() == userId) {
                return session.getDeviceId();
            }
        }
        return null;
    }
}
