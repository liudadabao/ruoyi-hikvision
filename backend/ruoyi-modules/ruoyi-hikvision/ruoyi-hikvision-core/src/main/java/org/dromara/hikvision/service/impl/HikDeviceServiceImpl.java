package org.dromara.hikvision.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.PageResult;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.query.QueryBuilder;
import org.dromara.hikvision.core.DeviceManager;
import org.dromara.hikvision.core.DeviceSession;
import org.dromara.hikvision.core.spi.ChannelNameResolver;
import org.dromara.hikvision.domain.HikChannel;
import org.dromara.hikvision.domain.HikDevice;
import org.dromara.hikvision.domain.bo.HikDeviceBo;
import org.dromara.hikvision.domain.vo.HikDeviceVo;
import org.dromara.hikvision.mapper.HikChannelMapper;
import org.dromara.hikvision.mapper.HikDeviceMapper;
import org.dromara.hikvision.service.IHikDeviceService;
import org.dromara.hikvision.sdk.HCNetSDK;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 海康设备 服务层实现
 *
 * @author hikvision-sdk
 */
@RequiredArgsConstructor
@Service
public class HikDeviceServiceImpl implements IHikDeviceService {

    private final HikDeviceMapper deviceMapper;
    private final HikChannelMapper channelMapper;
    private final DeviceManager deviceManager;
    private final ObjectProvider<ChannelNameResolver> channelNameResolver;

    @Override
    public PageResult<HikDeviceVo> selectPageDeviceList(HikDeviceBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HikDevice> lqw = buildQueryWrapper(bo);
        Page<HikDeviceVo> page = deviceMapper.selectVoPage(pageQuery.build(), lqw);
        List<HikDeviceVo> records = page.getRecords();
        records.forEach(this::fillRuntimeInfo);
        return PageResult.build(records, page.getTotal());
    }

    @Override
    public List<HikDeviceVo> selectDeviceList(HikDeviceBo bo) {
        return deviceMapper.selectVoList(buildQueryWrapper(bo));
    }

    @Override
    public HikDeviceVo selectDeviceById(Long deviceId) {
        HikDeviceVo vo = deviceMapper.selectVoById(deviceId);
        if (vo != null) {
            fillRuntimeInfo(vo);
        }
        return vo;
    }

    @Override
    public void insertDevice(HikDeviceBo bo) {
        HikDevice device = MapstructUtils.convert(bo, HikDevice.class);
        if (ObjectUtil.isEmpty(device.getManufacturer())) {
            device.setManufacturer("hikvision");
        }
        device.setStatus("offline");
        deviceMapper.insert(device);
    }

    @Override
    public void updateDevice(HikDeviceBo bo) {
        HikDevice device = MapstructUtils.convert(bo, HikDevice.class);
        deviceMapper.updateById(device);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeviceByIds(Collection<Long> deviceIds) {
        deviceIds.forEach(deviceManager::logout);
        channelMapper.lambda().in(HikChannel::getDeviceId, deviceIds).delete();
        deviceMapper.deleteByIds(deviceIds);
    }

    @Override
    public DeviceSession loginDevice(Long deviceId) {
        HikDevice device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new ServiceException("设备不存在: " + deviceId);
        }
        DeviceSession session = deviceManager.login(deviceId, device.getDeviceIp(),
            device.getPort(), device.getUsername(), device.getPassword());

        // 更新设备状态与信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 info = session.getDeviceInfo();
        HCNetSDK.NET_DVR_DEVICEINFO_V30 v30 = info.struDeviceV30;
        device.setStatus("online");
        device.setSerialNumber(DeviceManager.readString(v30.sSerialNumber));
        device.setDeviceType((int) v30.byDVRType);
        device.setChannelNum((int) v30.byChanNum);
        device.setIpChannelNum((v30.byHighDChanNum & 0xFF) * 256 + (v30.byIPChanNum & 0xFF));
        deviceMapper.updateById(device);

        // 同步通道
        syncChannels(deviceId);
        return session;
    }

    @Override
    public void logoutDevice(Long deviceId) {
        deviceManager.logout(deviceId);
        deviceMapper.lambdaUpdate()
            .eq(HikDevice::getDeviceId, deviceId)
            .set(HikDevice::getStatus, "offline")
            .update();
    }

    @Override
    public boolean isOnline(Long deviceId) {
        return deviceManager.isOnline(deviceId);
    }

    @Override
    public void syncChannels(Long deviceId) {
        DeviceSession session = deviceManager.requireSession(deviceId);
        HCNetSDK.NET_DVR_DEVICEINFO_V30 v30 = session.getDeviceInfo().struDeviceV30;

        // 通过 ISAPI 获取真实通道名称（部分设备不支持，失败则使用默认名）
        Map<Integer, String> channelNames = fetchChannelNames(deviceId);

        // 删除旧通道
        channelMapper.lambda().eq(HikChannel::getDeviceId, deviceId).delete();

        // 模拟通道
        int startChan = v30.byStartChan;
        int analogNum = v30.byChanNum;
        for (int i = 0; i < analogNum; i++) {
            int no = startChan + i;
            saveChannel(deviceId, no, 1, channelNames.getOrDefault(no, "模拟通道" + no));
        }

        // 数字通道
        int startDChan = v30.byStartDChan;
        int digitalNum = (v30.byHighDChanNum & 0xFF) * 256 + (v30.byIPChanNum & 0xFF);
        for (int i = 0; i < digitalNum; i++) {
            int no = startDChan + i;
            saveChannel(deviceId, no, 2, channelNames.getOrDefault(no, "数字通道" + no));
        }

        // 零通道
        if (v30.byZeroChanNum > 0) {
            saveChannel(deviceId, 0, 3, "零通道");
        }
    }

    /**
     * 通过可选 SPI 获取通道名称；未引入 config 模块时返回空映射。
     */
    private Map<Integer, String> fetchChannelNames(Long deviceId) {
        ChannelNameResolver resolver = channelNameResolver.getIfAvailable();
        if (resolver == null) {
            return Collections.emptyMap();
        }
        try {
            Map<Integer, String> names = resolver.resolve(deviceId);
            return names == null ? Collections.emptyMap() : names;
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private void saveChannel(Long deviceId, int channelNo, int type, String name) {
        HikChannel channel = new HikChannel();
        channel.setDeviceId(deviceId);
        channel.setChannelNo(channelNo);
        channel.setChannelType(type);
        channel.setChannelName(name);
        channel.setStatus("online");
        channelMapper.insert(channel);
    }

    /**
     * 填充运行时信息（在线状态、通道数）。
     */
    private void fillRuntimeInfo(HikDeviceVo vo) {
        if (vo == null) {
            return;
        }
        vo.setStatus(deviceManager.isOnline(vo.getDeviceId()) ? "online" : "offline");
        Long count = channelMapper.lambda().eq(HikChannel::getDeviceId, vo.getDeviceId()).count();
        vo.setChannelCount(count == null ? 0 : count.intValue());
    }

    private LambdaQueryWrapper<HikDevice> buildQueryWrapper(HikDeviceBo bo) {
        Map<String, Object> params = bo.getParams();
        return QueryBuilder.lambda(HikDevice.class)
            .likeIfText(HikDevice::getDeviceName, bo.getDeviceName())
            .eqIfText(HikDevice::getDeviceIp, bo.getDeviceIp())
            .eqIfPresent(HikDevice::getDeviceType, bo.getDeviceType())
            .betweenParams(HikDevice::getCreateTime, params, "beginTime", "endTime")
            .orderByAsc(HikDevice::getDeviceId)
            .build();
    }
}
