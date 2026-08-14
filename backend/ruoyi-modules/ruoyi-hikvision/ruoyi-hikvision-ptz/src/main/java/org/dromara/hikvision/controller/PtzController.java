package org.dromara.hikvision.controller;

import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.feature.ptz.PtzService;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.springframework.web.bind.annotation.*;

/**
 * 云台控制接口
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@RestController
@ConditionalOnHikFeature("ptz")
@RequestMapping("/hikvision/ptz")
public class PtzController {

    private final PtzService ptzService;

    /**
     * 云台控制
     */
    @SaCheckPermission("hikvision:preview:list")
    @PostMapping("/control/{deviceId}")
    public R<Void> control(@PathVariable Long deviceId,
                           @RequestParam Integer channelNo,
                           @RequestParam Integer command,
                           @RequestParam(defaultValue = "0") Integer stop,
                           @RequestParam(defaultValue = "5") Integer speed) {
        ptzService.ptzControl(deviceId, channelNo, command, stop, speed);
        return R.ok();
    }

    /**
     * 方向控制快捷方法
     */
    @SaCheckPermission("hikvision:preview:list")
    @PostMapping("/move/{deviceId}")
    public R<Void> move(@PathVariable Long deviceId,
                        @RequestParam Integer channelNo,
                        @RequestParam String direction,
                        @RequestParam(defaultValue = "0") Integer stop,
                        @RequestParam(defaultValue = "5") Integer speed) {
        int command = switch (direction.toLowerCase()) {
            case "up" -> HCNetSDK.TILT_UP;
            case "down" -> HCNetSDK.TILT_DOWN;
            case "left" -> HCNetSDK.PAN_LEFT;
            case "right" -> HCNetSDK.PAN_RIGHT;
            case "zoomin" -> HCNetSDK.ZOOM_IN;
            case "zoomout" -> HCNetSDK.ZOOM_OUT;
            default -> throw new IllegalArgumentException("未知方向: " + direction);
        };
        ptzService.ptzControl(deviceId, channelNo, command, stop, speed);
        return R.ok();
    }

    /**
     * 预置点操作
     */
    @SaCheckPermission("hikvision:preview:list")
    @PostMapping("/preset/{deviceId}")
    public R<Void> preset(@PathVariable Long deviceId,
                          @RequestParam Integer channelNo,
                          @RequestParam Integer command,
                          @RequestParam Integer presetIndex) {
        ptzService.preset(deviceId, channelNo, command, presetIndex);
        return R.ok();
    }
}
