package org.dromara.hikvision.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.dromara.hikvision.feature.display.DisplayService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/hikvision/access")
@ConditionalOnHikFeature("display")
public class DisplayController {

    private final DisplayService displayService;

    @SaCheckPermission("hikvision:device:edit")
    @PostMapping("/led/{deviceId}")
    public R<String> led(@PathVariable Long deviceId, @RequestParam int screenNo, @RequestParam String content) {
        return R.ok(displayService.sendLedText(deviceId, screenNo, content));
    }
}
