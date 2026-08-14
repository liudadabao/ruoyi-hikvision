package org.dromara.hikvision.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.dromara.hikvision.feature.access.AccessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门禁接口（仅引入 ruoyi-hikvision-access 时生效）
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/hikvision/access")
@ConditionalOnHikFeature("access")
public class AccessController {

    private final AccessService accessService;

    @SaCheckPermission("hikvision:device:edit")
    @PostMapping("/door/{deviceId}/{doorNo}/{command}")
    public R<Void> controlDoor(@PathVariable Long deviceId, @PathVariable int doorNo, @PathVariable int command) {
        accessService.controlDoor(deviceId, doorNo, command);
        return R.ok();
    }

    @SaCheckPermission("hikvision:device:query")
    @GetMapping("/cards/{deviceId}")
    public R<String> cards(@PathVariable Long deviceId) {
        return R.ok(accessService.listCards(deviceId, null));
    }

    @SaCheckPermission("hikvision:device:query")
    @GetMapping("/events/{deviceId}")
    public R<String> acsEvents(@PathVariable Long deviceId) {
        return R.ok(accessService.listAcsEvents(deviceId));
    }

    @SaCheckPermission("hikvision:device:query")
    @GetMapping("/doors/{deviceId}")
    public R<String> doors(@PathVariable Long deviceId) {
        return R.ok(accessService.listDoors(deviceId));
    }
}
