package org.dromara.hikvision.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * 海康设备表 sys_hik_device
 *
 * @author hikvision-sdk
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_hik_device")
public class HikDevice extends BaseEntity {

    /**
     * 设备主键
     */
    @TableId(value = "device_id")
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备 IP 地址
     */
    private String deviceIp;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录密码
     */
    private String password;

    /**
     * 设备类型（1=DVR 2=ATM DVR 3=DVS 7=IPC 8=NVR ...）
     */
    private Integer deviceType;

    /**
     * 厂家（默认 hikvision）
     */
    private String manufacturer;

    /**
     * 设备序列号
     */
    private String serialNumber;

    /**
     * 模拟通道数
     */
    private Integer channelNum;

    /**
     * 数字通道数（IP 通道）
     */
    private Integer ipChannelNum;

    /**
     * 在线状态（online/offline）
     */
    private String status;

    /**
     * 备注
     */
    private String remark;
}
