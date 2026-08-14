package org.dromara.hikvision.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.dromara.hikvision.feature.face.FaceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/hikvision/access")
@ConditionalOnHikFeature("face")
public class FaceController {

    private final FaceService faceService;

    @SaCheckPermission("hikvision:device:query")
    @GetMapping("/face/libs/{deviceId}")
    public R<String> faceLibs(@PathVariable Long deviceId) {
        return R.ok(faceService.listFaceLibraries(deviceId));
    }

    @SaCheckPermission("hikvision:device:query")
    @GetMapping("/face/records/{deviceId}")
    public R<String> faceRecords(@PathVariable Long deviceId) {
        return R.ok(faceService.listFaceRecords(deviceId));
    }
}
