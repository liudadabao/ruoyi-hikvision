package org.dromara.hikvision.service;

import org.dromara.common.core.domain.PageResult;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.domain.bo.HikDeviceBo;
import org.dromara.hikvision.domain.vo.HikDeviceVo;

import java.util.Collection;
import java.util.List;

/**
 * 海康设备 服务接口
 *
 * @author hikvision-sdk
 */
public interface IHikDeviceService {

    /**
     * 分页查询设备列表
     */
    PageResult<HikDeviceVo> selectPageDeviceList(HikDeviceBo bo, PageQuery pageQuery);

    /**
     * 查询设备列表
     */
    List<HikDeviceVo> selectDeviceList(HikDeviceBo bo);

    /**
     * 查询设备详情
     */
    HikDeviceVo selectDeviceById(Long deviceId);

    /**
     * 新增设备
     */
    void insertDevice(HikDeviceBo bo);

    /**
     * 修改设备
     */
    void updateDevice(HikDeviceBo bo);

    /**
     * 批量删除设备
     */
    void deleteDeviceByIds(Collection<Long> deviceIds);

    /**
     * 登录设备（建立会话）
     */
    DeviceSession loginDevice(Long deviceId);

    /**
     * 登出设备
     */
    void logoutDevice(Long deviceId);

    /**
     * 获取设备在线状态
     */
    boolean isOnline(Long deviceId);

    /**
     * 同步设备通道信息
     */
    void syncChannels(Long deviceId);
}
