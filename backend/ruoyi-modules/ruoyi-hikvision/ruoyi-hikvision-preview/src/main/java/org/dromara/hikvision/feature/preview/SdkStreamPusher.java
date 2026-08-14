package org.dromara.hikvision.feature.preview;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.dromara.hikvision.config.HikvisionProperties;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.HikErrorCode;
import org.dromara.hikvision.core.SdkLibrary;
import org.dromara.hikvision.media.zlm.ZlmMediaService;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SDK 取流 → ZLMediaKit 推流服务（高级预览路径）。
 * <p>
 * 当设备 RTSP 端口不可直连（如仅 SDK 私有协议可达）时，使用 SDK
 * {@code NET_DVR_RealPlay_V40} 获取 PS 码流，经 JavaCV/FFmpeg 解复用后
 * 以 RTSP 推送给本地 ZLMediaKit，再转 HTTP-FLV/HLS 供浏览器播放。
 * <p>
 * 说明：此路径为兜底方案，常规场景优先使用 RTSP + ZLM 拉流代理
 * （见 {@link ZlmMediaService}）。
 *
 * @author hikvision-sdk
 */
@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("preview")
public class SdkStreamPusher {

    private final SdkLibrary sdkLibrary;
    private final DeviceManager deviceManager;
    private final HikvisionProperties properties;
    private final ZlmMediaService zlmMediaService;

    /**
     * 流 ID -> 推流上下文
     */
    private final Map<String, PushContext> contexts = new ConcurrentHashMap<>();

    /**
     * 开始 SDK 取流并推送到 ZLMediaKit。
     *
     * @param deviceId   设备主键
     * @param channelNo  通道号
     * @param streamType 码流类型
     * @param stream     流 ID（如 deviceId_channelNo）
     * @return 播放地址
     */
    public Map<String, String> start(Long deviceId, int channelNo, int streamType, String stream) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK hcNetSDK = sdkLibrary.getHcNetSDK();

        File psFile = new File(System.getProperty("java.io.tmpdir"), "hikvision-push-" + stream + ".ps");
        try {
            BufferedOutputStream psOut = new BufferedOutputStream(new FileOutputStream(psFile, false));

            HCNetSDK.NET_DVR_PREVIEWINFO previewInfo = new HCNetSDK.NET_DVR_PREVIEWINFO();
            previewInfo.lChannel = channelNo;
            previewInfo.dwStreamType = streamType;
            previewInfo.dwLinkMode = 0;
            previewInfo.bBlocked = 0;
            previewInfo.write();

            PushContext ctx = new PushContext();
            ctx.running = true;
            ctx.psFile = psFile;
            ctx.psOut = psOut;

            // SDK 取流回调：PS 码流写入临时文件
            int handle = hcNetSDK.NET_DVR_RealPlay_V40(session.getUserId(), previewInfo,
                (lRealHandle, dwDataType, pBuffer, dwBufSize, pUser) -> {
                    if (!ctx.running) {
                        return;
                    }
                    try {
                        byte[] data = pBuffer.getByteArray(0, dwBufSize);
                        synchronized (ctx.psOut) {
                            ctx.psOut.write(data);
                        }
                    } catch (Exception e) {
                        log.debug("[hikvision] PS 码流写入失败: {}", e.getMessage());
                    }
                }, null);
            if (handle < 0) {
                throw new org.dromara.common.core.exception.ServiceException(
                    "SDK 取流失败: " + HikErrorCode.message(hcNetSDK.NET_DVR_GetLastError()));
            }
            ctx.realPlayHandle = handle;
            contexts.put(stream, ctx);

            // 启动推流线程
            String rtspPushUrl = String.format("rtsp://127.0.0.1:%d/live/%s",
                properties.getZlm().getRtspPort(), stream);
            ctx.thread = new Thread(() -> pushLoop(ctx, psFile, rtspPushUrl), "hikvision-push-" + stream);
            ctx.thread.setDaemon(true);
            ctx.thread.start();

            return zlmMediaService.isZlmStarted() ? zlmMediaService.buildPlayUrls("live", stream) : Map.of();
        } catch (Exception e) {
            throw new org.dromara.common.core.exception.ServiceException("SDK 推流启动失败: " + e.getMessage());
        }
    }

    /**
     * 停止推流。
     */
    public void stop(String stream) {
        PushContext ctx = contexts.remove(stream);
        if (ctx == null) {
            return;
        }
        ctx.running = false;
        if (ctx.realPlayHandle >= 0) {
            sdkLibrary.getHcNetSDK().NET_DVR_StopRealPlay(ctx.realPlayHandle);
        }
        try {
            synchronized (ctx.psOut) {
                ctx.psOut.close();
            }
        } catch (Exception ignored) {
        }
        if (ctx.thread != null) {
            try {
                ctx.thread.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        ctx.psFile.delete();
    }

    /**
     * 推流循环：读取增长的 PS 文件并推送 RTSP。
     * 文件到达 EOF 后重建抓取器，等待新数据继续。
     */
    private void pushLoop(PushContext ctx, File psFile, String rtspPushUrl) {
        while (ctx.running) {
            try {
                if (psFile.length() < 1024) {
                    Thread.sleep(200);
                    continue;
                }
                try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(psFile)) {
                    grabber.setFormat("mpeg");
                    grabber.start();
                    try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(rtspPushUrl,
                        grabber.getImageWidth() > 0 ? grabber.getImageWidth() : 1920,
                        grabber.getImageHeight() > 0 ? grabber.getImageHeight() : 1080,
                        grabber.getAudioChannels())) {
                        recorder.setFormat("rtsp");
                        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
                        recorder.setFrameRate(grabber.getFrameRate() > 0 ? grabber.getFrameRate() : 25);
                        recorder.setOption("rtsp_transport", "tcp");
                        recorder.start();

                        Frame frame;
                        while (ctx.running && (frame = grabber.grabFrame()) != null) {
                            recorder.record(frame);
                        }
                        recorder.stop();
                    }
                    grabber.stop();
                }
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                log.warn("[hikvision] 推流循环异常: {}", e.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.info("[hikvision] 推流线程结束: {}", psFile.getName());
    }

    private static class PushContext {
        volatile boolean running;
        int realPlayHandle = -1;
        File psFile;
        BufferedOutputStream psOut;
        Thread thread;
    }
}
