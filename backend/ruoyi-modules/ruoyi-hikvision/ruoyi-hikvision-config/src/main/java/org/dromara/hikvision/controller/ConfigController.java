package org.dromara.hikvision.controller;

import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.hikvision.feature.config.ConfigService;
import org.dromara.hikvision.feature.isapi.IsapiService;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.Map;

/**
 * 远程参数配置 / ISAPI 透传接口
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@RestController
@ConditionalOnHikFeature("config")
@RequestMapping("/hikvision/config")
public class ConfigController {

    private final ConfigService configService;
    private final IsapiService isapiService;

    /**
     * 获取远程参数（原始字节，Base64 返回）
     */
    @SaCheckPermission("hikvision:device:query")
    @GetMapping("/get/{deviceId}")
    public R<Map<String, Object>> get(@PathVariable Long deviceId,
                                      @RequestParam Integer command,
                                      @RequestParam(defaultValue = "-1") Integer channel,
                                      @RequestParam(defaultValue = "4096") Integer bufSize) {
        byte[] data = configService.getDvrConfig(deviceId, command, channel, bufSize);
        return R.ok(Map.of("base64", Base64.getEncoder().encodeToString(data), "length", data.length));
    }

    /**
     * 设置远程参数（原始字节，Base64）
     */
    @SaCheckPermission("hikvision:device:edit")
    @PostMapping("/set/{deviceId}")
    public R<Void> set(@PathVariable Long deviceId,
                       @RequestParam Integer command,
                       @RequestParam(defaultValue = "-1") Integer channel,
                       @RequestBody byte[] data) {
        configService.setDvrConfig(deviceId, command, channel, data);
        return R.ok();
    }

    /**
     * ISAPI 透传
     */
    @SaCheckPermission("hikvision:device:query")
    @PostMapping("/isapi/{deviceId}")
    public R<String> isapi(@PathVariable Long deviceId,
                           @RequestParam String url,
                           @RequestBody(required = false) String body) {
        return R.ok(isapiService.stdXmlConfig(deviceId, url, body, 1024 * 1024));
    }

    /**
     * ISAPI GET 透传
     */
    @SaCheckPermission("hikvision:device:query")
    @GetMapping("/isapi/{deviceId}")
    public R<String> isapiGet(@PathVariable Long deviceId, @RequestParam String url) {
        return R.ok(isapiService.get(deviceId, url));
    }
}
