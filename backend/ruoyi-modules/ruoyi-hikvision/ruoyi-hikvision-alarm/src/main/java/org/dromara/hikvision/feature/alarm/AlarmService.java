package org.dromara.hikvision.feature.alarm;

import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.hikvision.core.CallbackDispatcher;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.HikErrorCode;
import org.dromara.hikvision.core.SdkLibrary;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 报警订阅服务。
 *
 * @author hikvision-sdk
 */
@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("alarm")
public class AlarmService {

    private final SdkLibrary sdkLibrary;
    private final DeviceManager deviceManager;
    private final CallbackDispatcher callbackDispatcher;

    /**
     * 设备主键 -> 布防句柄
     */
    private final Map<Long, Integer> alarmHandles = new ConcurrentHashMap<>();

    private final AtomicBoolean callbackRegistered = new AtomicBoolean(false);

    /**
     * 设备布防（订阅报警）。
     */
    public void setupAlarm(Long deviceId) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();

        // 全局消息回调只注册一次
        if (callbackRegistered.compareAndSet(false, true)) {
            hcNetSDK.NET_DVR_SetDVRMessageCallBack_V50(0, callbackDispatcher.getAlarmCallBack(), Pointer.NULL);
        }

        HCNetSDK.NET_DVR_SETUPALARM_PARAM param = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
        param.dwSize = param.size();
        param.byLevel = 1;
        param.byAlarmInfoType = 0;
        param.write();

        int handle = hcNetSDK.NET_DVR_SetupAlarmChan_V41(session.getUserId(), param);
        if (handle < 0) {
            throw new ServiceException("布防失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
        alarmHandles.put(deviceId, handle);
        log.info("[hikvision] 设备 {} 布防成功, 句柄={}", deviceId, handle);
    }

    /**
     * 撤销布防。
     */
    public void closeAlarm(Long deviceId) {
        Integer handle = alarmHandles.remove(deviceId);
        if (handle == null) {
            return;
        }
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        hcNetSDK.NET_DVR_CloseAlarmChan_V30(handle);
        log.info("[hikvision] 设备 {} 撤销布防", deviceId);
    }

    /**
     * 启动报警侦听（报警主机主动上传场景）。
     */
    public void startListen(String localIp, short localPort) {
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        int result = hcNetSDK.NET_DVR_StartListen_V30(localIp, localPort, callbackDispatcher.getListenCallBack(), Pointer.NULL);
        if (result < 0) {
            throw new ServiceException("报警侦听启动失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
    }

    /**
     * 停止报警侦听。
     */
    public void stopListen() {
        sdkLibrary.getHcNetSDK().NET_DVR_StopListen();
    }
}
