package org.dromara.hikvision.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

/**
 * 预览结果视图对象
 *
 * @author hikvision-sdk
 */
@Data
public class PreviewInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备主键
     */
    private Long deviceId;

    /**
     * 通道号
     */
    private Integer channelNo;

    /**
     * 流 ID
     */
    private String stream;

    /**
     * 设备 RTSP 地址
     */
    private String rtspUrl;

    /**
     * 播放地址（flv/hls/rtsp/webrtc），ZLMediaKit 启用时有值
     */
    private Map<String, String> playUrls;

    /**
     * SDK 实时预览句柄（使用 SDK 取流时有值）
     */
    private Integer realPlayHandle;
}
