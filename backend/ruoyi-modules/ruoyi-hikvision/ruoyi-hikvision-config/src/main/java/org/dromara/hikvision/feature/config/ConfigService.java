package org.dromara.hikvision.feature.config;

import com.sun.jna.Memory;
import com.sun.jna.ptr.IntByReference;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.HikErrorCode;
import org.dromara.hikvision.core.SdkLibrary;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

/**
 * 远程参数配置服务（NET_DVR_GetDVRConfig / NET_DVR_SetDVRConfig 通用封装）。
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("config")
public class ConfigService {

    private final SdkLibrary sdkLibrary;
    private final DeviceManager deviceManager;

    /**
     * 获取设备远程参数（原始字节）。
     *
     * @param command 配置命令（如 NET_DVR_GET_DEVICECFG / NET_DVR_GET_IPPARACFG）
     * @param channel 通道号（部分配置需要）
     * @param bufSize 输出缓冲区大小
     * @return 配置原始数据
     */
    public byte[] getDvrConfig(Long deviceId, int command, int channel, int bufSize) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        Memory buffer = new Memory(Math.max(bufSize, 1));
        IntByReference bytesReturned = new IntByReference();
        boolean ok = hcNetSDK.NET_DVR_GetDVRConfig(session.getUserId(), command, channel,
            buffer, bufSize, bytesReturned);
        if (!ok) {
            throw new ServiceException("获取参数失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
        int len = bytesReturned.getValue();
        if (len <= 0) {
            return new byte[0];
        }
        return buffer.getByteArray(0, len);
    }

    /**
     * 设置设备远程参数。
     *
     * @param command 配置命令
     * @param channel 通道号
     * @param data    配置数据
     */
    public void setDvrConfig(Long deviceId, int command, int channel, byte[] data) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        Memory buffer = new Memory(data.length);
        buffer.write(0, data, 0, data.length);
        boolean ok = hcNetSDK.NET_DVR_SetDVRConfig(session.getUserId(), command, channel, buffer, data.length);
        if (!ok) {
            throw new ServiceException("设置参数失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
    }
}
