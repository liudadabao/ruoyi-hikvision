package org.dromara.hikvision.domain.bo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 海康报警记录业务对象 sys_hik_alarm_record
 *
 * @author hikvision-sdk
 */
@Data
public class HikAlarmRecordBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备主键
     */
    private Long deviceId;

    /**
     * 报警命令类型
     */
    private Integer command;

    /**
     * 请求参数
     */
    private Map<String, Object> params = new HashMap<>();
}
