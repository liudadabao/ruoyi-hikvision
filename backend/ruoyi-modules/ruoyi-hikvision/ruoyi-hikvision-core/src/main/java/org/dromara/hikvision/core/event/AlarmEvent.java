package org.dromara.hikvision.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 设备报警事件（移动侦测、遮挡、IO、门禁、越界等），由 {@code FMSGCallBack_V31} 回调触发。
 *
 * @author hikvision-sdk
 */
@Getter
public class AlarmEvent extends ApplicationEvent {

    /**
     * 报警命令类型（对应 NET_DVR_ 开头的常量，如 COMM_ALARM=0x1100）
     */
    private final int command;

    /**
     * 用户 ID（对应登录会话）
     */
    private final int userId;

    /**
     * 报警设备 IP（部分命令有效）
     */
    private final String deviceIp;

    /**
     * 事件时间
     */
    private final LocalDateTime time;

    public AlarmEvent(Object source, int command, int userId, String deviceIp) {
        super(source);
        this.command = command;
        this.userId = userId;
        this.deviceIp = deviceIp;
        this.time = LocalDateTime.now();
    }
}
