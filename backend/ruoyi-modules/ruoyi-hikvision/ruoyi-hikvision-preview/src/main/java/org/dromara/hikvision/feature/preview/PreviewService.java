package org.dromara.hikvision.feature.preview;

import com.sun.jna.Pointer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.HikErrorCode;
import org.dromara.hikvision.core.SdkLibrary;
import org.dromara.hikvision.domain.HikDevice;
import org.dromara.hikvision.domain.vo.PreviewInfoVo;
import org.dromara.hikvision.mapper.HikDeviceMapper;
import org.dromara.hikvision.media.RtspUrlBuilder;
import org.dromara.hikvision.media.zlm.ZlmMediaService;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实时预览服务。
 * <p>
 * 提供两条预览路径：
 * <ol>
 *     <li>RTSP + ZLMediaKit 拉流转协议（浏览器播放首选）；</li>
 *     <li>SDK 取流（{@code NET_DVR_RealPlay_V40}），供二次开发取原始码流。</li>
 * </ol>
 *
 * @author hikvision-sdk
 */
@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("preview")
public class PreviewService {

    private final SdkLibrary sdkLibrary;
    private final DeviceManager deviceManager;
    private final ZlmMediaService zlmMediaService;
    private final HikDeviceMapper deviceMapper;
    private final SdkStreamPusher sdkStreamPusher;

    /**
     * 流 ID -> 实时预览句柄
     */
    private final Map<String, Integer> realPlayHandles = new ConcurrentHashMap<>();

    /**
     * 开始预览（RTSP + ZLMediaKit）。
     *
     * @param channelNo  通道号
     * @param streamType 码流类型（0-主码流 1-子码流）
     */
    public PreviewInfoVo preview(Long deviceId, int channelNo, int streamType) {
        HikDevice device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new ServiceException("设备不存在: " + deviceId);
        }
        String rtspUrl = streamType == 0
            ? RtspUrlBuilder.main(device.getDeviceIp(), device.getPort(), device.getUsername(), device.getPassword(), channelNo)
            : RtspUrlBuilder.sub(device.getDeviceIp(), device.getPort(), device.getUsername(), device.getPassword(), channelNo);

        String stream = deviceId + "_" + channelNo + (streamType == 0 ? "" : "_sub");
        zlmMediaService.startProxy("live", stream, rtspUrl, device.getUsername(), device.getPassword());

        PreviewInfoVo vo = new PreviewInfoVo();
        vo.setDeviceId(deviceId);
        vo.setChannelNo(channelNo);
        vo.setStream(stream);
        vo.setRtspUrl(rtspUrl);
        if (zlmMediaService.isZlmStarted()) {
            vo.setPlayUrls(zlmMediaService.buildPlayUrls("live", stream));
        }
        return vo;
    }

    /**
     * 停止预览。
     */
    public void stopPreview(Long deviceId, int channelNo, int streamType) {
        String stream = deviceId + "_" + channelNo + (streamType == 0 ? "" : "_sub");
        zlmMediaService.stopProxy("live", stream);
    }

    /**
     * SDK 取流转推预览（设备 RTSP 不可直连时的兜底方案）。
     *
     * @return 播放地址（ZLMediaKit 启用时有值）
     */
    public Map<String, String> previewBySdkPush(Long deviceId, int channelNo, int streamType) {
        String stream = deviceId + "_" + channelNo + (streamType == 0 ? "" : "_sub") + "_sdk";
        return sdkStreamPusher.start(deviceId, channelNo, streamType, stream);
    }

    /**
     * 停止 SDK 取流转推。
     */
    public void stopSdkPush(Long deviceId, int channelNo, int streamType) {
        String stream = deviceId + "_" + channelNo + (streamType == 0 ? "" : "_sub") + "_sdk";
        sdkStreamPusher.stop(stream);
    }

    /**
     * SDK 实时取流（返回预览句柄，原始码流通过回调获取）。
     *
     * @return 预览句柄
     */
    public int startRealPlay(Long deviceId, int channelNo, int streamType, HCNetSDK.FRealDataCallBack_V30 callback) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();

        HCNetSDK.NET_DVR_PREVIEWINFO previewInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = channelNo;
        previewInfo.dwStreamType = streamType;
        previewInfo.dwLinkMode = 0; // TCP
        previewInfo.bBlocked = 0;
        previewInfo.byProtoType = 0; // 私有协议
        previewInfo.write();

        HCNetSDK.FRealDataCallBack_V30 realCallback = callback != null ? callback
            : (lRealHandle, dwDataType, pBuffer, dwBufSize, pUser) -> log.debug("[hikvision] 实时码流: handle={}, type={}, size={}", lRealHandle, dwDataType, dwBufSize);

        int handle = hcNetSDK.NET_DVR_RealPlay_V40(session.getUserId(), previewInfo, realCallback, Pointer.NULL);
        if (handle < 0) {
            throw new ServiceException("实时取流失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
        realPlayHandles.put(deviceId + "_" + channelNo, handle);
        return handle;
    }

    /**
     * 停止 SDK 实时取流。
     */
    public void stopRealPlay(Long deviceId, int channelNo) {
        Integer handle = realPlayHandles.remove(deviceId + "_" + channelNo);
        if (handle != null) {
            sdkLibrary.getHcNetSDK().NET_DVR_StopRealPlay(handle);
        }
    }

    /**
     * 抓图（BMP）。
     */
    public void capturePicture(Long deviceId, int channelNo, String filePath) {
        Integer handle = realPlayHandles.get(deviceId + "_" + channelNo);
        if (handle == null) {
            throw new ServiceException("请先开始实时预览");
        }
        boolean ok = sdkLibrary.getHcNetSDK().NET_DVR_CapturePicture(handle, filePath);
        if (!ok) {
            throw new ServiceException("抓图失败: " + HikErrorCode.message(sdkLibrary.getHcNetSDK().NET_DVR_GetLastError()));
        }
    }
}
