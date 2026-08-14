package org.dromara.hikvision.controller;

import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.domain.vo.PreviewInfoVo;
import org.dromara.hikvision.feature.preview.PreviewService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 实时预览接口
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@RestController
@ConditionalOnHikFeature("preview")
@RequestMapping("/hikvision/preview")
public class PreviewController {

    private final PreviewService previewService;

    /**
     * 开始预览
     */
    @SaCheckPermission("hikvision:preview:list")
    @GetMapping("/start/{deviceId}")
    public R<PreviewInfoVo> start(@PathVariable Long deviceId,
                                  @RequestParam Integer channelNo,
                                  @RequestParam(defaultValue = "0") Integer streamType) {
        return R.ok(previewService.preview(deviceId, channelNo, streamType));
    }

    /**
     * 停止预览
     */
    @SaCheckPermission("hikvision:preview:list")
    @GetMapping("/stop/{deviceId}")
    public R<Void> stop(@PathVariable Long deviceId,
                        @RequestParam Integer channelNo,
                        @RequestParam(defaultValue = "0") Integer streamType) {
        previewService.stopPreview(deviceId, channelNo, streamType);
        return R.ok();
    }

    /**
     * SDK 取流转推预览（设备 RTSP 不可直连时使用）
     */
    @SaCheckPermission("hikvision:preview:list")
    @GetMapping("/startSdkPush/{deviceId}")
    public R<Map<String, String>> startSdkPush(@PathVariable Long deviceId,
                                               @RequestParam Integer channelNo,
                                               @RequestParam(defaultValue = "0") Integer streamType) {
        return R.ok(previewService.previewBySdkPush(deviceId, channelNo, streamType));
    }

    /**
     * 停止 SDK 取流转推
     */
    @SaCheckPermission("hikvision:preview:list")
    @GetMapping("/stopSdkPush/{deviceId}")
    public R<Void> stopSdkPush(@PathVariable Long deviceId,
                               @RequestParam Integer channelNo,
                               @RequestParam(defaultValue = "0") Integer streamType) {
        previewService.stopSdkPush(deviceId, channelNo, streamType);
        return R.ok();
    }
}
