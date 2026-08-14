package org.dromara.hikvision.core.spi;

import java.util.Map;

/**
 * 通道名称解析 SPI。
 * <p>
 * 由 {@code ruoyi-hikvision-config} 等可选模块实现；core 不强制依赖 ISAPI。
 */
public interface ChannelNameResolver {

    /**
     * 解析设备通道号到名称的映射。
     *
     * @param deviceId 设备主键
     * @return 通道号 -> 名称，失败返回空 Map
     */
    Map<Integer, String> resolve(Long deviceId);
}
