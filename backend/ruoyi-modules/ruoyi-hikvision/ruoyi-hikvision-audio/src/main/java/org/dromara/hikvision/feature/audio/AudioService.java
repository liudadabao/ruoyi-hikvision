package org.dromara.hikvision.feature.audio;

import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.HikErrorCode;
import org.dromara.hikvision.core.SdkLibrary;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语音对讲服务。
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("audio")
public class AudioService {

    private final SdkLibrary sdkLibrary;
    private final DeviceManager deviceManager;

    /**
     * 设备主键 -> 语音对讲句柄
     */
    private final Map<Long, Integer> talkHandles = new ConcurrentHashMap<>();

    /**
     * 开始语音对讲。
     *
     * @param channel 通道号
     * @return 语音对讲句柄
     */
    public int startVoiceTalk(Long deviceId, int channel) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        int handle = hcNetSDK.NET_DVR_StartVoiceCom_V30(session.getUserId(), channel, false, null, null);
        if (handle < 0) {
            throw new ServiceException("语音对讲开启失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
        talkHandles.put(deviceId, handle);
        return handle;
    }

    /**
     * 发送语音数据（PCM/G711 编码音频）。
     */
    public void sendVoiceData(Long deviceId, byte[] data) {
        Integer handle = talkHandles.get(deviceId);
        if (handle == null) {
            throw new ServiceException("语音对讲未开启");
        }
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        boolean ok = hcNetSDK.NET_DVR_VoiceComSendData(handle, data, data.length);
        if (!ok) {
            throw new ServiceException("语音发送失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
    }

    /**
     * 停止语音对讲。
     */
    public void stopVoiceTalk(Long deviceId) {
        Integer handle = talkHandles.remove(deviceId);
        if (handle == null) {
            return;
        }
        sdkLibrary.getHcNetSDK().NET_DVR_StopVoiceCom(handle);
    }

    /**
     * 设置对讲音量。
     */
    public void setVolume(Long deviceId, short volume) {
        Integer handle = talkHandles.get(deviceId);
        if (handle == null) {
            throw new ServiceException("语音对讲未开启");
        }
        sdkLibrary.getHcNetSDK().NET_DVR_SetVoiceComClientVolume(handle, volume);
    }
}
