package org.dromara.hikvision.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.hikvision.domain.HikChannel;

import java.io.Serial;

/**
 * 海康通道视图对象 sys_hik_channel
 *
 * @author hikvision-sdk
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HikChannel.class)
public class HikChannelVo extends HikChannel {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 所属设备名称
     */
    private String deviceName;
}
