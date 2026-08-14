package org.dromara.hikvision.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 海康设备通道表 sys_hik_channel
 *
 * @author hikvision-sdk
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_hik_channel")
public class HikChannel extends BaseEntity {

    /**
     * 通道主键
     */
    @TableId(value = "channel_id")
    private Long channelId;

    /**
     * 设备主键
     */
    private Long deviceId;

    /**
     * 通道号
     */
    private Integer channelNo;

    /**
     * 通道名称
     */
    private String channelName;

    /**
     * 通道类型（1=模拟 2=数字 3=零通道）
     */
    private Integer channelType;

    /**
     * 是否在线
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
