package org.dromara.hikvision.controller;

import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.feature.playback.PlaybackService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 录像回放与下载接口
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@RestController
@ConditionalOnHikFeature("playback")
@RequestMapping("/hikvision/playback")
public class PlaybackController {

    private final PlaybackService playbackService;

    /**
     * 按时间回放
     */
    @SaCheckPermission("hikvision:playback:list")
    @PostMapping("/play/{deviceId}")
    public R<Integer> play(@PathVariable Long deviceId,
                           @RequestParam Integer channelNo,
                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                           @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                           @RequestParam(defaultValue = "0") Integer streamType) {
        return R.ok(playbackService.playbackByTime(deviceId, channelNo, startTime, endTime, streamType));
    }

    /**
     * 回放控制
     */
    @SaCheckPermission("hikvision:playback:list")
    @PostMapping("/control/{handle}")
    public R<Void> control(@PathVariable Integer handle,
                           @RequestParam Integer controlCode,
                           @RequestParam(defaultValue = "0") Integer inValue) {
        playbackService.playbackControl(handle, controlCode, inValue);
        return R.ok();
    }

    /**
     * 停止回放
     */
    @SaCheckPermission("hikvision:playback:list")
    @PostMapping("/stop/{handle}")
    public R<Void> stop(@PathVariable Integer handle) {
        playbackService.stopPlayback(handle);
        return R.ok();
    }

    /**
     * 按时间下载录像
     */
    @SaCheckPermission("hikvision:playback:list")
    @PostMapping("/download/{deviceId}")
    public R<Integer> download(@PathVariable Long deviceId,
                               @RequestParam Integer channelNo,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                               @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
                               @RequestParam(defaultValue = "0") Integer streamType,
                               @RequestParam String savePath) {
        return R.ok(playbackService.downloadByTime(deviceId, channelNo, startTime, endTime, streamType, savePath));
    }
}
