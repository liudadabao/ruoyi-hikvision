package org.dromara.hikvision.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.dromara.hikvision.feature.vehicle.VehicleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/hikvision/access")
@ConditionalOnHikFeature("vehicle")
public class VehicleController {

    private final VehicleService vehicleService;

    @SaCheckPermission("hikvision:device:edit")
    @PostMapping("/barrier/{deviceId}/{barrierNo}/{command}")
    public R<Void> barrier(@PathVariable Long deviceId, @PathVariable int barrierNo, @PathVariable int command) {
        vehicleService.controlBarrier(deviceId, barrierNo, command);
        return R.ok();
    }

    @SaCheckPermission("hikvision:device:query")
    @GetMapping("/vehicle/records/{deviceId}")
    public R<String> vehicleRecords(@PathVariable Long deviceId) {
        return R.ok(vehicleService.listVehicleRecords(deviceId));
    }
}
