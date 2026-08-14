package org.dromara.hikvision.controller;

import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.feature.alarm.AlarmService;
import org.springframework.web.bind.annotation.*;

/**
 * 报警订阅接口
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@RestController
@ConditionalOnHikFeature("alarm")
@RequestMapping("/hikvision/alarm")
public class AlarmController {

    private final AlarmService alarmService;

    /**
     * 设备布防
     */
    @SaCheckPermission("hikvision:alarm:list")
    @PostMapping("/setup/{deviceId}")
    public R<Void> setup(@PathVariable Long deviceId) {
        alarmService.setupAlarm(deviceId);
        return R.ok();
    }

    /**
     * 撤销布防
     */
    @SaCheckPermission("hikvision:alarm:list")
    @PostMapping("/close/{deviceId}")
    public R<Void> close(@PathVariable Long deviceId) {
        alarmService.closeAlarm(deviceId);
        return R.ok();
    }
}
