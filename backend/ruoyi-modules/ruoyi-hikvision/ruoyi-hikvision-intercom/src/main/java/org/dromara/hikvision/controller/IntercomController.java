package org.dromara.hikvision.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.dromara.hikvision.feature.intercom.IntercomService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/hikvision/access")
@ConditionalOnHikFeature("intercom")
public class IntercomController {

    private final IntercomService intercomService;

    @SaCheckPermission("hikvision:device:edit")
    @PostMapping("/intercom/unlock/{deviceId}/{doorNo}")
    public R<Void> unlock(@PathVariable Long deviceId, @PathVariable int doorNo) {
        intercomService.unlock(deviceId, doorNo);
        return R.ok();
    }
}
