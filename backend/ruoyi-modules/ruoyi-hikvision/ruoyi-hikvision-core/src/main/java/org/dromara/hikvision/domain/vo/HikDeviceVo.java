package org.dromara.hikvision.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.hikvision.domain.HikDevice;

import java.io.Serial;

/**
 * 海康设备视图对象 sys_hik_device
 *
 * @author hikvision-sdk
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HikDevice.class)
public class HikDeviceVo extends HikDevice {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 通道数量（汇总展示）
     */
    private Integer channelCount;
}
