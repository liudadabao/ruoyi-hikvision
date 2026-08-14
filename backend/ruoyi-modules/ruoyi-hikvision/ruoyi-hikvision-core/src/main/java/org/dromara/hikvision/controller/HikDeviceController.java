package org.dromara.hikvision.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.domain.R;
import org.dromara.common.log.annotation.Log;
import org.dromara.common.log.enums.BusinessType;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.redis.annotation.RepeatSubmit;
import org.dromara.common.web.core.BaseController;
import org.dromara.hikvision.domain.bo.HikDeviceBo;
import org.dromara.hikvision.domain.vo.HikDeviceVo;
import org.dromara.hikvision.service.IHikDeviceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 海康设备 接口
 *
 * @author hikvision-sdk
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/hikvision/device")
public class HikDeviceController extends BaseController {

    private final IHikDeviceService deviceService;

    /**
     * 分页查询设备列表
     */
    @SaCheckPermission("hikvision:device:list")
    @GetMapping("/list")
    public R<PageResult<HikDeviceVo>> list(HikDeviceBo bo, PageQuery pageQuery) {
        return R.ok(deviceService.selectPageDeviceList(bo, pageQuery));
    }

    /**
     * 查询全部设备列表
     */
    @SaCheckPermission("hikvision:device:list")
    @GetMapping("/all")
    public R<List<HikDeviceVo>> all(HikDeviceBo bo) {
        return R.ok(deviceService.selectDeviceList(bo));
    }

    /**
     * 获取设备详情
     */
    @SaCheckPermission("hikvision:device:query")
    @GetMapping("/{deviceId}")
    public R<HikDeviceVo> getInfo(@PathVariable Long deviceId) {
        return R.ok(deviceService.selectDeviceById(deviceId));
    }

    /**
     * 新增设备
     */
    @SaCheckPermission("hikvision:device:add")
    @Log(title = "海康设备", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PostMapping
    public R<Void> add(@Validated @RequestBody HikDeviceBo bo) {
        deviceService.insertDevice(bo);
        return R.ok();
    }

    /**
     * 修改设备
     */
    @SaCheckPermission("hikvision:device:edit")
    @Log(title = "海康设备", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PutMapping
    public R<Void> edit(@Validated @RequestBody HikDeviceBo bo) {
        deviceService.updateDevice(bo);
        return R.ok();
    }

    /**
     * 删除设备
     */
    @SaCheckPermission("hikvision:device:remove")
    @Log(title = "海康设备", businessType = BusinessType.DELETE)
    @DeleteMapping("/{deviceIds}")
    public R<Void> remove(@PathVariable Long[] deviceIds) {
        deviceService.deleteDeviceByIds(Arrays.asList(deviceIds));
        return R.ok();
    }

    /**
     * 登录设备
     */
    @SaCheckPermission("hikvision:device:edit")
    @Log(title = "海康设备登录", businessType = BusinessType.OTHER)
    @PostMapping("/login/{deviceId}")
    public R<Void> login(@PathVariable Long deviceId) {
        deviceService.loginDevice(deviceId);
        return R.ok();
    }

    /**
     * 登出设备
     */
    @SaCheckPermission("hikvision:device:edit")
    @Log(title = "海康设备登出", businessType = BusinessType.OTHER)
    @PostMapping("/logout/{deviceId}")
    public R<Void> logout(@PathVariable Long deviceId) {
        deviceService.logoutDevice(deviceId);
        return R.ok();
    }
}
