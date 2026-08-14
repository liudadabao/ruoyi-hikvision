package org.dromara.hikvision.core.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 设备异常事件（网络断开、硬盘异常等），由 {@code NET_DVR_SetExceptionCallBack_V30} 回调触发。
 *
 * @author hikvision-sdk
 */
@Getter
public class DeviceExceptionEvent extends ApplicationEvent {

    /**
     * 异常类型：1=硬盘满 2=硬盘出错 3=网络断开 4=IP冲突 5=非法访问 6=视频信号异常 ...
     */
    private final int exceptionType;

    /**
     * 用户 ID（对应登录会话）
     */
    private final int userId;

    /**
     * 事件时间
     */
    private final LocalDateTime time;

    public DeviceExceptionEvent(Object source, int exceptionType, int userId) {
        super(source);
        this.exceptionType = exceptionType;
        this.userId = userId;
        this.time = LocalDateTime.now();
    }
}
