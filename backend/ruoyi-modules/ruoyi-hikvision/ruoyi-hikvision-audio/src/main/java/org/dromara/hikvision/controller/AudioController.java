package org.dromara.hikvision.controller;

import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.feature.audio.AudioService;
import org.springframework.web.bind.annotation.*;

/**
 * 语音对讲接口
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@RestController
@ConditionalOnHikFeature("audio")
@RequestMapping("/hikvision/audio")
public class AudioController {

    private final AudioService audioService;

    /**
     * 开始语音对讲
     */
    @SaCheckPermission("hikvision:preview:list")
    @PostMapping("/start/{deviceId}")
    public R<Integer> start(@PathVariable Long deviceId, @RequestParam(defaultValue = "1") Integer channelNo) {
        return R.ok(audioService.startVoiceTalk(deviceId, channelNo));
    }

    /**
     * 发送语音数据
     */
    @SaCheckPermission("hikvision:preview:list")
    @PostMapping("/send/{deviceId}")
    public R<Void> send(@PathVariable Long deviceId, @RequestBody byte[] data) {
        audioService.sendVoiceData(deviceId, data);
        return R.ok();
    }

    /**
     * 停止语音对讲
     */
    @SaCheckPermission("hikvision:preview:list")
    @PostMapping("/stop/{deviceId}")
    public R<Void> stop(@PathVariable Long deviceId) {
        audioService.stopVoiceTalk(deviceId);
        return R.ok();
    }
}
