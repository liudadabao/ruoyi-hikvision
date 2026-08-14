package org.dromara.hikvision.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.dromara.hikvision.feature.elevator.ElevatorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/hikvision/access")
@ConditionalOnHikFeature("elevator")
public class ElevatorController {

    private final ElevatorService elevatorService;

    @SaCheckPermission("hikvision:device:query")
    @GetMapping("/elevator/floors/{deviceId}")
    public R<String> floors(@PathVariable Long deviceId) {
        return R.ok(elevatorService.listFloors(deviceId));
    }
}
