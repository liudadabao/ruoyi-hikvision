package org.dromara.hikvision.core;

import com.sun.jna.Pointer;
import lombok.extern.slf4j.Slf4j;
import org.dromara.hikvision.core.event.AlarmEvent;
import org.dromara.hikvision.core.event.DeviceExceptionEvent;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * SDK 回调分发器。
 * <p>
 * 将海康 SDK 的同步回调统一转换为 Spring 事件，业务侧通过
 * {@code @EventListener} 或 {@code @TransactionalEventListener} 订阅即可，
 * 无需直接依赖 JNA 回调接口。
 *
 * @author hikvision-sdk
 */
@Slf4j
@Component
public class CallbackDispatcher {

    private final ApplicationEventPublisher publisher;

    /**
     * 全局异常回调（由 SdkLibrary 注册）
     */
    private final HCNetSDK.FExceptionCallBack exceptionCallBack;

    /**
     * 全局报警消息回调（由 AlarmService 注册）
     */
    private final HCNetSDK.FMSGCallBack_V31 alarmCallBack;

    /**
     * 报警侦听回调（旧版 FMSGCallBack，由 AlarmService 注册）
     */
    private final HCNetSDK.FMSGCallBack listenCallBack;

    public CallbackDispatcher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
        this.exceptionCallBack = (dwType, lUserID, lHandle, pUser) ->
            publisher.publishEvent(new DeviceExceptionEvent(this, dwType, lUserID));
        this.alarmCallBack = (lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser) ->
            publishAlarm(lCommand, pAlarmer);
        this.listenCallBack = (lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser) ->
            publishAlarm(lCommand, pAlarmer);
    }

    private boolean publishAlarm(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer) {
        String deviceIp = parseIp(pAlarmer);
        int userId = pAlarmer == null ? -1 : pAlarmer.lUserID;
        publisher.publishEvent(new AlarmEvent(this, lCommand, userId, deviceIp));
        return true;
    }

    /**
     * 获取异常回调实例。
     */
    public HCNetSDK.FExceptionCallBack getExceptionCallBack() {
        return exceptionCallBack;
    }

    /**
     * 获取报警回调实例。
     */
    public HCNetSDK.FMSGCallBack_V31 getAlarmCallBack() {
        return alarmCallBack;
    }

    /**
     * 获取报警侦听回调实例（旧版）。
     */
    public HCNetSDK.FMSGCallBack getListenCallBack() {
        return listenCallBack;
    }

    /**
     * 从 NET_DVR_ALARMER 中解析设备 IP。
     */
    private String parseIp(HCNetSDK.NET_DVR_ALARMER alarmer) {
        if (alarmer == null) {
            return null;
        }
        return bytesToString(alarmer.sDeviceIP, alarmer.byDeviceIPValid == 1);
    }

    /**
     * 将 SDK 字节数组转换为字符串。
     */
    public static String bytesToString(byte[] data, boolean valid) {
        if (data == null || !valid) {
            return null;
        }
        int len = 0;
        while (len < data.length && data[len] != 0) {
            len++;
        }
        Charset charset = isUtf8(data, len) ? StandardCharsets.UTF_8 : Charset.forName("GBK");
        return new String(data, 0, len, charset);
    }

    /**
     * 简单判断字节数组是否 UTF-8 编码。
     */
    private static boolean isUtf8(byte[] data, int len) {
        int i = 0;
        while (i < len) {
            int c = data[i] & 0xFF;
            if (c < 0x80) {
                i++;
            } else if ((c & 0xE0) == 0xC0) {
                if (i + 1 >= len) {
                    return false;
                }
                i += 2;
            } else if ((c & 0xF0) == 0xE0) {
                if (i + 2 >= len) {
                    return false;
                }
                i += 3;
            } else {
                return false;
            }
        }
        return true;
    }
}
