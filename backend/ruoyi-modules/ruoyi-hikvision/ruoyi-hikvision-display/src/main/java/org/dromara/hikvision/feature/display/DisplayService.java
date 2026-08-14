package org.dromara.hikvision.feature.display;

import lombok.RequiredArgsConstructor;
import org.dromara.hikvision.feature.isapi.IsapiService;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

/**
 * LED / LCD 显示控制服务。
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("display")
public class DisplayService {

    private final IsapiService isapiService;

    /**
     * 发送 LED 显示内容。
     *
     * @param screenNo 屏号
     * @param content  显示内容
     */
    public String sendLedText(Long deviceId, int screenNo, String content) {
        String url = "/ISAPI/LED/Display/Set";
        String body = "{\"screenNo\":" + screenNo + ",\"content\":\"" + content + "\"}";
        return isapiService.stdXmlConfig(deviceId, url, body, 1024);
    }

    /**
     * 查询 LED 显示配置。
     */
    public String listLedConfig(Long deviceId) {
        return isapiService.get(deviceId, "/ISAPI/LED/Display?format=json");
    }
}
