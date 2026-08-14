package org.dromara.hikvision.feature.playback;

import com.sun.jna.ptr.IntByReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.HikErrorCode;
import org.dromara.hikvision.core.SdkLibrary;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 录像回放与下载服务。
 *
 * @author hikvision-sdk
 */
@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("playback")
public class PlaybackService {

    private final SdkLibrary sdkLibrary;
    private final DeviceManager deviceManager;

    /**
     * 按时间回放。
     *
     * @param channel    通道号
     * @param start      开始时间
     * @param end        结束时间
     * @param streamType 码流类型
     * @return 回放句柄
     */
    public int playbackByTime(Long deviceId, int channel, LocalDateTime start, LocalDateTime end, int streamType) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();

        HCNetSDK.NET_DVR_STREAM_INFO streamInfo = new HCNetSDK.NET_DVR_STREAM_INFO();
        streamInfo.dwSize = streamInfo.size();
        streamInfo.dwChannel = channel;
        streamInfo.write();

        HCNetSDK.NET_DVR_VOD_PARA vodPara = new HCNetSDK.NET_DVR_VOD_PARA();
        vodPara.dwSize = vodPara.size();
        vodPara.struIDInfo = streamInfo;
        vodPara.struBeginTime = toTime(start);
        vodPara.struEndTime = toTime(end);
        vodPara.byStreamType = (byte) streamType;
        vodPara.byDrawFrame = 0;
        vodPara.byDownload = 0;
        vodPara.write();

        int handle = hcNetSDK.NET_DVR_PlayBackByTime_V40(session.getUserId(), vodPara);
        if (handle < 0) {
            throw new ServiceException("回放失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
        return handle;
    }

    /**
     * 回放控制。
     *
     * @param handle      回放句柄
     * @param controlCode 控制码（NET_DVR_PLAYSTART/STOP/PAUSE/RESTART/FAST/SLOW/NORMAL）
     * @param inValue     参数（快放/慢放速度等）
     */
    public void playbackControl(int handle, int controlCode, int inValue) {
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();
        IntByReference out = new IntByReference();
        boolean ok = hcNetSDK.NET_DVR_PlayBackControl(handle, controlCode, inValue, out);
        if (!ok) {
            throw new ServiceException("回放控制失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
    }

    /**
     * 停止回放。
     */
    public void stopPlayback(int handle) {
        sdkLibrary.getHcNetSDK().NET_DVR_StopPlayBack(handle);
    }

    /**
     * 设置回放原始码流回调。
     */
    public void setPlayDataCallBack(int handle, HCNetSDK.FPlayDataCallBack callback) {
        boolean ok = sdkLibrary.getHcNetSDK().NET_DVR_SetPlayDataCallBack(handle, callback, 0);
        if (!ok) {
            throw new ServiceException("设置回放码流回调失败");
        }
    }

    /**
     * 按时间下载录像文件到本地。
     *
     * @return 下载句柄
     */
    public int downloadByTime(Long deviceId, int channel, LocalDateTime start, LocalDateTime end, int streamType, String savePath) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();

        HCNetSDK.NET_DVR_PLAYCOND playCond = new HCNetSDK.NET_DVR_PLAYCOND();
        playCond.dwChannel = channel;
        playCond.struStartTime = toTime(start);
        playCond.struStopTime = toTime(end);
        playCond.byStreamType = (byte) streamType;
        playCond.write();

        int handle = hcNetSDK.NET_DVR_GetFileByTime_V40(session.getUserId(), savePath, playCond);
        if (handle < 0) {
            throw new ServiceException("录像下载失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
        }
        return handle;
    }

    private HCNetSDK.NET_DVR_TIME toTime(LocalDateTime time) {
        HCNetSDK.NET_DVR_TIME t = new HCNetSDK.NET_DVR_TIME();
        t.dwYear = time.getYear();
        t.dwMonth = time.getMonthValue();
        t.dwDay = time.getDayOfMonth();
        t.dwHour = time.getHour();
        t.dwMinute = time.getMinute();
        t.dwSecond = time.getSecond();
        t.write();
        return t;
    }
}
