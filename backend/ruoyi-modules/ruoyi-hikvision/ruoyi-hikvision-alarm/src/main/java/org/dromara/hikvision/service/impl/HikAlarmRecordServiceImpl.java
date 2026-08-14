package org.dromara.hikvision.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.enums.PushSourceEnum;
import org.dromara.common.core.enums.PushTypeEnum;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.common.push.helper.PushHelper;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.event.AlarmEvent;
import org.dromara.hikvision.domain.HikAlarmRecord;
import org.dromara.hikvision.domain.bo.HikAlarmRecordBo;
import org.dromara.hikvision.domain.vo.HikAlarmRecordVo;
import org.dromara.hikvision.feature.alarm.AlarmCommandName;
import org.dromara.hikvision.mapper.HikAlarmRecordMapper;
import org.dromara.system.api.domain.PushPayloadDTO;
import org.springframework.context.event.EventListener;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 海康报警记录 服务层实现。
 * <p>
 * 订阅 {@link AlarmEvent}，将 SDK 报警回调落库，并提供分页查询。
 *
 * @author hikvision-sdk
 */
@Slf4j
@RequiredArgsConstructor
@Service
@ConditionalOnHikFeature("alarm")
public class HikAlarmRecordServiceImpl {

    private final HikAlarmRecordMapper alarmRecordMapper;
    private final DeviceManager deviceManager;

    /**
     * 监听报警事件并落库 + 实时推送。
     */
    @EventListener
    public void onAlarm(AlarmEvent event) {
        try {
            HikAlarmRecord record = new HikAlarmRecord();
            record.setDeviceId(findDeviceId(event.getUserId()));
            record.setCommand(event.getCommand());
            record.setCommandName(AlarmCommandName.of(event.getCommand()));
            record.setDeviceIp(event.getDeviceIp());
            record.setAlarmTime(event.getTime());
            alarmRecordMapper.insert(record);

            // 实时推送到前端（SSE/WebSocket，依赖 common-push 配置 message.enabled）
            PushHelper.publishAll(PushPayloadDTO.of(
                PushTypeEnum.CUSTOM,
                PushSourceEnum.BACKEND,
                "海康设备报警: " + record.getCommandName(),
                record,
                "/hikvision/alarm"
            ));
        } catch (Exception e) {
            log.warn("[hikvision] 报警记录落库失败: {}", e.getMessage());
        }
    }

    /**
     * 分页查询报警记录。
     */
    public PageResult<HikAlarmRecordVo> selectPageAlarmRecord(HikAlarmRecordBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HikAlarmRecord> lqw = buildQueryWrapper(bo);
        var page = alarmRecordMapper.selectVoPage(pageQuery.build(), lqw);
        return PageResult.build(page.getRecords(), page.getTotal());
    }

    /**
     * 查询报警记录列表。
     */
    public List<HikAlarmRecordVo> selectAlarmRecordList(HikAlarmRecordBo bo) {
        return alarmRecordMapper.selectVoList(buildQueryWrapper(bo));
    }

    private LambdaQueryWrapper<HikAlarmRecord> buildQueryWrapper(HikAlarmRecordBo bo) {
        Map<String, Object> params = bo.getParams();
        return QueryBuilder.lambda(HikAlarmRecord.class)
            .eqIfPresent(HikAlarmRecord::getDeviceId, bo.getDeviceId())
            .eqIfPresent(HikAlarmRecord::getCommand, bo.getCommand())
            .betweenParams(HikAlarmRecord::getAlarmTime, params, "beginTime", "endTime")
            .orderByDesc(HikAlarmRecord::getAlarmTime)
            .build();
    }

    /**
     * 根据 userId 查找设备主键。
     */
    private Long findDeviceId(int userId) {
        for (DeviceSession session : deviceManager.getAllSessions()) {
            if (session.getUserId() == userId) {
                return session.getDeviceId();
            }
        }
        return null;
    }
}
