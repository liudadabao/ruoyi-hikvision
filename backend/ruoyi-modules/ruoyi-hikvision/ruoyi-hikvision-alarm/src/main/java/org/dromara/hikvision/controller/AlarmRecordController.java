package org.dromara.hikvision.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.hikvision.domain.bo.HikAlarmRecordBo;
import org.dromara.hikvision.domain.vo.HikAlarmRecordVo;
import org.dromara.hikvision.service.impl.HikAlarmRecordServiceImpl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.dromara.hikvision.core.spi.ConditionalOnHikFeature;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 报警记录接口
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@RestController
@ConditionalOnHikFeature("alarm")
@RequestMapping("/hikvision/alarm/record")
public class AlarmRecordController {

    private final HikAlarmRecordServiceImpl alarmRecordService;

    /**
     * 分页查询报警记录
     */
    @SaCheckPermission("hikvision:alarm:list")
    @GetMapping("/list")
    public R<PageResult<HikAlarmRecordVo>> list(HikAlarmRecordBo bo, PageQuery pageQuery) {
        return R.ok(alarmRecordService.selectPageAlarmRecord(bo, pageQuery));
    }

    /**
     * 查询报警记录列表
     */
    @SaCheckPermission("hikvision:alarm:list")
    @GetMapping("/all")
    public R<List<HikAlarmRecordVo>> all(HikAlarmRecordBo bo) {
        return R.ok(alarmRecordService.selectAlarmRecordList(bo));
    }
}
