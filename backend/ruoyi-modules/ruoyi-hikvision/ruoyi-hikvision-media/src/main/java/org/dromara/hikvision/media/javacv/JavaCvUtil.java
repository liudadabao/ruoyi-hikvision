package org.dromara.hikvision.media.javacv;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * JavaCV / FFmpeg 工具类。
 * <p>
 * 提供录像转存、视频截图等能力，作为流媒体能力的补充。
 * 依赖 {@code org.bytedeco:ffmpeg-platform} 原生库。
 *
 * @author hikvision-sdk
 */
@Slf4j
public class JavaCvUtil {

    private JavaCvUtil() {
    }

    /**
     * 将视频流（RTSP/RTMP/文件）录制为 MP4。
     *
     * @param inputUrl  输入地址
     * @param outputPath 输出文件
     * @param maxSeconds 最大录制时长（秒）
     */
    public static void recordToMp4(String inputUrl, String outputPath, int maxSeconds) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputUrl);
        try {
            grabber.start();
            try (FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputPath, grabber.getImageWidth(), grabber.getImageHeight(), grabber.getAudioChannels())) {
                recorder.setFormat("mp4");
                recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
                recorder.setFrameRate(grabber.getFrameRate());
                recorder.start();

                long start = System.currentTimeMillis();
                Frame frame;
                while ((frame = grabber.grabFrame()) != null) {
                    recorder.record(frame);
                    if (System.currentTimeMillis() - start > maxSeconds * 1000L) {
                        break;
                    }
                }
                recorder.stop();
            }
            grabber.stop();
        } catch (Exception e) {
            throw new IllegalStateException("录像转存失败: " + e.getMessage(), e);
        } finally {
            try {
                grabber.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 从视频流/文件截取一帧图片。
     *
     * @param inputUrl   输入地址
     * @param outputPath 输出图片路径
     */
    public static void extractThumbnail(String inputUrl, String outputPath) {
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(inputUrl);
        try {
            grabber.start();
            Frame frame = grabber.grabImage();
            if (frame == null) {
                throw new IllegalStateException("未抓取到视频帧");
            }
            try (Java2DFrameConverter converter = new Java2DFrameConverter()) {
                BufferedImage image = converter.convert(frame);
                ImageIO.write(image, "jpg", new File(outputPath));
            }
            grabber.stop();
        } catch (Exception e) {
            throw new IllegalStateException("视频截图失败: " + e.getMessage(), e);
        } finally {
            try {
                grabber.close();
            } catch (Exception ignored) {
            }
        }
    }
}
