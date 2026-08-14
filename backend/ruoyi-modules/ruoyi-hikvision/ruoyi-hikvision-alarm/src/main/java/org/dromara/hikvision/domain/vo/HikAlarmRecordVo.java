package org.dromara.hikvision.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.hikvision.domain.HikAlarmRecord;

import java.io.Serial;

/**
 * 海康报警记录视图对象 sys_hik_alarm_record
 *
 * @author hikvision-sdk
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AutoMapper(target = HikAlarmRecord.class)
public class HikAlarmRecordVo extends HikAlarmRecord {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 设备名称
     */
    private String deviceName;
}
