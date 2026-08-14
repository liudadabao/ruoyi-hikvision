package org.dromara.hikvision.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 海康报警记录表 sys_hik_alarm_record
 *
 * @author hikvision-sdk
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_hik_alarm_record")
public class HikAlarmRecord extends BaseEntity {

    /**
     * 主键
     */
    @TableId(value = "id")
    private Long id;

    /**
     * 设备主键
     */
    private Long deviceId;

    /**
     * 报警命令类型
     */
    private Integer command;

    /**
     * 报警命令名称
     */
    private String commandName;

    /**
     * 报警设备 IP
     */
    private String deviceIp;

    /**
     * 报警时间
     */
    private LocalDateTime alarmTime;

    /**
     * 备注（附加信息）
     */
    private String remark;
}
