package org.dromara.hikvision.feature.isapi;

import lombok.RequiredArgsConstructor;
import org.dromara.hikvision.core.spi.ChannelNameResolver;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通过 ISAPI 解析通道名称。仅在引入 config 模块时生效。
 */
@RequiredArgsConstructor
@Component
@ConditionalOnHikFeature("config")
public class IsapiChannelNameResolver implements ChannelNameResolver {

    private static final Pattern PATTERN = Pattern.compile("<id>(\\d+)</id>\\s*<name>([^<]*)</name>");

    private final IsapiService isapiService;

    @Override
    public Map<Integer, String> resolve(Long deviceId) {
        Map<Integer, String> names = new HashMap<>();
        try {
            String xml = isapiService.get(deviceId, "/ISAPI/System/Video/inputs/channels");
            if (xml == null || xml.isBlank()) {
                return names;
            }
            Matcher matcher = PATTERN.matcher(xml);
            while (matcher.find()) {
                names.put(Integer.valueOf(matcher.group(1)), matcher.group(2));
            }
        } catch (Exception ignored) {
            // 设备不支持 ISAPI 时忽略
        }
        return names;
    }
}
