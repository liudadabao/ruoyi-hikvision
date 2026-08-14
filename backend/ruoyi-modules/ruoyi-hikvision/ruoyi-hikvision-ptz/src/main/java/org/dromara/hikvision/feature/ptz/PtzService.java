package org.dromara.hikvision.feature.ptz;

import lombok.RequiredArgsConstructor;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.HikErrorCode;
import org.dromara.hikvision.core.SdkLibrary;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

/**
 * 云台控制服务。
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("ptz")
public class PtzService {

    private final SdkLibrary sdkLibrary;
    private final DeviceManager deviceManager;

    /**
     * 云台控制。
     *
     * @param deviceId 设备主键
     * @param channel  通道号
     * @param command  控制命令（HCNetSDK.PAN_LEFT/PAN_RIGHT/TILT_UP/TILT_DOWN/ZOOM_IN/ZOOM_OUT ...）
     * @param stop     0-开始动作 1-停止动作
     * @param speed    速度（1-7）
     */
    public void ptzControl(Long deviceId, int channel, int command, int stop, int speed) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        boolean ok = hcNetSDK.NET_DVR_PTZControlWithSpeed_Other(session.getUserId(), channel, command, stop, speed);
        check(ok, hcNetSDK);
    }

    /**
     * 预置点操作。
     *
     * @param presetIndex 预置点号
     * @param command     命令（SET_PRESET/CLE_PRESET/GOTO_PRESET）
     */
    public void preset(Long deviceId, int channel, int command, int presetIndex) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        boolean ok = hcNetSDK.NET_DVR_PTZPreset_Other(session.getUserId(), channel, command, presetIndex);
        check(ok, hcNetSDK);
    }

    /**
     * 设置预置点。
     */
    public void setPreset(Long deviceId, int channel, int presetIndex) {
        preset(deviceId, channel, HCNetSDK.SET_PRESET, presetIndex);
    }

    /**
     * 清除预置点。
     */
    public void clearPreset(Long deviceId, int channel, int presetIndex) {
        preset(deviceId, channel, HCNetSDK.CLE_PRESET, presetIndex);
    }

    /**
     * 转到预置点。
     */
    public void gotoPreset(Long deviceId, int channel, int presetIndex) {
        preset(deviceId, channel, HCNetSDK.GOTO_PRESET, presetIndex);
    }

    private void check(boolean ok, HCNetSDK hcNetSDK) {
        if (!ok) {
            throw new org.dromara.common.core.exception.ServiceException(
                "云台操作失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
    }
}
